package com.portafolio.costing.engine;

import com.portafolio.costing.exception.InsufficientBalanceException;
import com.portafolio.model.entities.DetalleCosteoEntity;
import com.portafolio.model.entities.KardexEntity;
import com.portafolio.model.entities.TipoMovimientoEntity;
import com.portafolio.model.entities.TransaccionEntity;
import com.portafolio.persistence.repositorio.TipoMovimientoRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Queue;

/**
 * Handler especializado para procesar transacciones de EGRESO.
 * Implementa el algoritmo FIFO consumiendo de la cola de ingresos disponibles.
 */
@Slf4j
@RequiredArgsConstructor
public class EgresoHandler {

    private static final BigDecimal TOLERANCIA_AJUSTE = new BigDecimal("0.5");
    
    private final EntityManager entityManager;
    private final TipoMovimientoRepository tipoMovimientoRepository;
    private final KardexFactory kardexFactory;

    /**
     * Record que encapsula el resultado del procesamiento de un egreso.
     */
    public record EgresoResult(BigDecimal nuevoSaldoCantidad, BigDecimal nuevoSaldoValor) {}

    /**
     * Procesa una transacción de egreso usando el algoritmo FIFO.
     *
     * @param transaccion La transacción de egreso a procesar
     * @param ingresosQueue Cola FIFO de ingresos disponibles
     * @param saldoCantidadActual Saldo de cantidad antes del egreso
     * @param saldoValorActual Saldo de valor antes del egreso
     * @param claveAgrupacion Clave del grupo de costeo
     * @return Nuevos saldos después del egreso
     * @throws InsufficientBalanceException Si no hay suficiente inventario
     */
    public EgresoResult handle(
            TransaccionEntity transaccion,
            Queue<IngresoDisponible> ingresosQueue,
            BigDecimal saldoCantidadActual,
            BigDecimal saldoValorActual,
            String claveAgrupacion) throws InsufficientBalanceException {

        BigDecimal cantidadEgreso = transaccion.getCantidad();

        log.debug("Procesando egreso - Tx ID: {}, Cantidad: {}, Saldo disponible: {}",
                transaccion.getId(), cantidadEgreso, saldoCantidadActual);

        // 1. VERIFICAR SI HAY SALDO SUFICIENTE
        if (saldoCantidadActual.compareTo(cantidadEgreso) < 0) {
            BigDecimal diferencia = cantidadEgreso.subtract(saldoCantidadActual);
            
            // Aplicar tolerancia si la diferencia es pequeña
            if (diferencia.abs().compareTo(TOLERANCIA_AJUSTE) <= 0) {
                log.warn("Saldo casi suficiente para Tx ID: {}. Creando ajuste automático por tolerancia de {}",
                        transaccion.getId(), diferencia);
                
                // Crear ajuste automático
                KardexEntity kardexAjuste = crearAjusteAutomatico(
                        transaccion, diferencia, saldoValorActual, saldoCantidadActual, claveAgrupacion);
                
                // Actualizar saldos con el ajuste
                saldoCantidadActual = saldoCantidadActual.add(kardexAjuste.getCantidad());
                saldoValorActual = saldoValorActual.add(kardexAjuste.getCostoTotal());
                
                // Añadir el ajuste a la cola FIFO
                ingresosQueue.add(new IngresoDisponible(kardexAjuste));
            } else {
                // Diferencia muy grande, lanzar excepción
                throw new InsufficientBalanceException(
                        claveAgrupacion,
                        cantidadEgreso,
                        saldoCantidadActual
                );
            }
        }

        // 2. ALGORITMO FIFO - Consumir de la cola de ingresos
        BigDecimal cantidadPendiente = cantidadEgreso;
        BigDecimal costoTotalCalculado = BigDecimal.ZERO;

        while (cantidadPendiente.compareTo(BigDecimal.ZERO) > 0) {
            // Obtener el primer lote disponible (FIFO)
            IngresoDisponible loteIngreso = ingresosQueue.peek();
            
            if (loteIngreso == null) {
                throw new InsufficientBalanceException(
                        claveAgrupacion,
                        cantidadEgreso,
                        saldoCantidadActual.subtract(cantidadPendiente)
                );
            }

            // Calcular cuánto se puede consumir de este lote
            BigDecimal cantidadAConsumir = cantidadPendiente.min(loteIngreso.getCantidadDisponible());
            
            // Consumir del lote
            BigDecimal cantidadConsumida = loteIngreso.consumir(cantidadAConsumir);
            BigDecimal costoParcial = loteIngreso.getCostoTotal(cantidadConsumida);
            
            costoTotalCalculado = costoTotalCalculado.add(costoParcial);
            cantidadPendiente = cantidadPendiente.subtract(cantidadConsumida);

            // Actualizar el kardex del ingreso con la nueva cantidad disponible
            KardexEntity kardexIngreso = loteIngreso.getKardexIngreso();
            kardexIngreso.setCantidadDisponible(loteIngreso.getCantidadDisponible());
            entityManager.merge(kardexIngreso);

            // Crear registro de Kardex para este consumo parcial
            BigDecimal saldoCantidadParcial = saldoCantidadActual.subtract(cantidadEgreso).add(cantidadPendiente);
            BigDecimal saldoValorParcial = saldoValorActual.subtract(costoTotalCalculado);
            
            KardexEntity kardexEgreso = kardexFactory.createFromEgreso(
                    transaccion,
                    cantidadConsumida,
                    costoParcial,
                    saldoCantidadParcial,
                    saldoValorParcial,
                    claveAgrupacion
            );
            entityManager.persist(kardexEgreso);

            // Crear detalle de costeo para trazabilidad
            crearDetalleCosteo(
                    kardexIngreso.getTransaccion(),
                    transaccion,
                    cantidadConsumida,
                    costoParcial,
                    claveAgrupacion
            );

            log.debug("Consumido del lote - Cantidad: {}, Costo: {}, Restante en lote: {}",
                    cantidadConsumida, costoParcial, loteIngreso.getCantidadDisponible());

            // Si el lote se consumió completamente, removerlo de la cola
            if (!loteIngreso.tieneDisponible()) {
                ingresosQueue.poll();
                log.debug("Lote completamente consumido y removido de la cola FIFO");
            }
        }

        // Calcular nuevos saldos finales
        BigDecimal nuevoSaldoCantidad = saldoCantidadActual.subtract(cantidadEgreso);
        BigDecimal nuevoSaldoValor = saldoValorActual.subtract(costoTotalCalculado);

        log.info("Egreso procesado exitosamente - Tx ID: {}, Cantidad: {}, Costo FIFO: {}, Nuevo saldo: qty={}, val={}",
                transaccion.getId(), cantidadEgreso, costoTotalCalculado, nuevoSaldoCantidad, nuevoSaldoValor);

        return new EgresoResult(nuevoSaldoCantidad, nuevoSaldoValor);
    }

    /**
     * Crea un registro de detalle de costeo para trazabilidad.
     * Vincula un ingreso con un egreso específico.
     */
    private void crearDetalleCosteo(
            TransaccionEntity ingreso,
            TransaccionEntity egreso,
            BigDecimal cantidadUsada,
            BigDecimal costoParcial,
            String claveAgrupacion) {

        DetalleCosteoEntity detalle = DetalleCosteoEntity.builder()
                .ingreso(ingreso)
                .egreso(egreso)
                .cantidadUsada(cantidadUsada)
                .costoParcial(costoParcial)
                .claveAgrupacion(claveAgrupacion)
                .build();

        entityManager.persist(detalle);
    }

    /**
     * Crea un ajuste automático por tolerancia cuando falta una cantidad pequeña.
     * Esto evita errores por redondeos o diferencias mínimas.
     */
    private KardexEntity crearAjusteAutomatico(
            TransaccionEntity transaccionOriginal,
            BigDecimal cantidadAjuste,
            BigDecimal saldoValor,
            BigDecimal saldoCantidad,
            String claveAgrupacion) {

        // Calcular valores del ajuste
        BigDecimal costoUnitarioAjuste = transaccionOriginal.getPrecio() != null
                ? transaccionOriginal.getPrecio()
                : BigDecimal.ZERO;
        BigDecimal montoAjuste = cantidadAjuste.multiply(costoUnitarioAjuste);

        // Buscar el tipo de movimiento para ajustes automáticos
        TipoMovimientoEntity tipoMovimientoAjuste = tipoMovimientoRepository
                .findByTipoMovimiento("AJUSTE INGRESO")
                .orElseThrow(() -> new IllegalStateException(
                        "El tipo de movimiento 'AJUSTE INGRESO' no está configurado en la base de datos"));

        // Crear transacción de ajuste
        TransaccionEntity ajusteTx = TransaccionEntity.builder()
                .empresa(transaccionOriginal.getEmpresa())
                .cuenta(transaccionOriginal.getCuenta())
                .custodio(transaccionOriginal.getCustodio())
                .instrumento(transaccionOriginal.getInstrumento())
                .fechaTransaccion(transaccionOriginal.getFechaTransaccion())
                .tipoMovimiento(tipoMovimientoAjuste)
                .cantidad(cantidadAjuste)
                .precio(costoUnitarioAjuste)
                .montoTotal(montoAjuste)
                .glosa("Ajuste automático por tolerancia para Tx ID: " + transaccionOriginal.getId())
                .costeado(true)
                .paraRevision(false)
                .build();

        entityManager.persist(ajusteTx);

        // Calcular nuevos saldos con el ajuste
        BigDecimal nuevoSaldoCantidad = saldoCantidad.add(cantidadAjuste);
        BigDecimal nuevoSaldoValor = saldoValor.add(montoAjuste);

        // Crear registro de Kardex para el ajuste
        KardexEntity kardexAjuste = kardexFactory.createFromIngreso(
                ajusteTx,
                nuevoSaldoCantidad,
                nuevoSaldoValor,
                claveAgrupacion
        );
        
        entityManager.persist(kardexAjuste);

        log.info("Ajuste automático creado - Cantidad: {}, Monto: {}", cantidadAjuste, montoAjuste);

        return kardexAjuste;
    }
}