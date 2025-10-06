package com.portafolio.costing.engine;

import com.portafolio.model.entities.KardexEntity;
import com.portafolio.model.entities.TransaccionEntity;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Queue;
import org.springframework.stereotype.Component;

/**
 * Handler especializado para procesar transacciones de INGRESO.
 * Crea registros de Kardex y añade lotes a la cola FIFO.
 */
@RequiredArgsConstructor
@Component
@Slf4j
public class IngresoHandler {

    private final EntityManager entityManager;
    private final KardexFactory kardexFactory;

    /**
     * Record que encapsula el resultado del procesamiento de un ingreso.
     */
    public record IngresoResult(BigDecimal nuevoSaldoCantidad, BigDecimal nuevoSaldoValor) {}

    /**
     * Procesa una transacción de ingreso.
     *
     * @param transaccion La transacción de ingreso a procesar
     * @param ingresosQueue Cola FIFO de ingresos disponibles
     * @param saldoCantidadActual Saldo de cantidad antes del ingreso
     * @param saldoValorActual Saldo de valor antes del ingreso
     * @param claveAgrupacion Clave del grupo de costeo
     * @return Nuevos saldos después del ingreso
     */
    public IngresoResult handle(
            TransaccionEntity transaccion,
            Queue<IngresoDisponible> ingresosQueue,
            BigDecimal saldoCantidadActual,
            BigDecimal saldoValorActual,
            String claveAgrupacion) {

        validarEntrada(transaccion, ingresosQueue, saldoCantidadActual, saldoValorActual, claveAgrupacion);

        // Calcular nuevos saldos
        BigDecimal cantidadIngreso = transaccion.getCantidad();
        BigDecimal precioIngreso = transaccion.getPrecio() != null 
                ? transaccion.getPrecio() 
                : BigDecimal.ZERO;
        
        BigDecimal valorIngreso = calcularValorIngreso(transaccion);
        
        BigDecimal nuevoSaldoCantidad = saldoCantidadActual.add(cantidadIngreso);
        BigDecimal nuevoSaldoValor = saldoValorActual.add(valorIngreso);

        log.debug("Procesando ingreso - Tx ID: {}, Cantidad: {}, Precio: {}, Valor: {}",
                transaccion.getId(), cantidadIngreso, precioIngreso, valorIngreso);

        try {
            // Crear registro de Kardex para el ingreso
            KardexEntity kardex = kardexFactory.createFromIngreso(
                    transaccion,
                    nuevoSaldoCantidad,
                    nuevoSaldoValor,
                    claveAgrupacion
            );
            
            entityManager.persist(kardex);
            
            // Añadir el lote a la cola FIFO
            ingresosQueue.add(new IngresoDisponible(kardex));

            log.info("Ingreso procesado exitosamente - Tx ID: {}, Nuevo saldo: qty={}, val={}",
                    transaccion.getId(), nuevoSaldoCantidad, nuevoSaldoValor);

            return new IngresoResult(nuevoSaldoCantidad, nuevoSaldoValor);

        } catch (Exception e) {
            log.error("Error al procesar ingreso - Tx ID: {}", transaccion.getId(), e);
            throw new RuntimeException("Error en el procesamiento del ingreso: " + e.getMessage(), e);
        }
    }

    /**
     * Calcula el valor total del ingreso (monto + comisión + gastos + IVA).
     */
    private BigDecimal calcularValorIngreso(TransaccionEntity tx) {
        BigDecimal cantidad = tx.getCantidad() != null ? tx.getCantidad() : BigDecimal.ZERO;
        BigDecimal precio = tx.getPrecio() != null ? tx.getPrecio() : BigDecimal.ZERO;
        BigDecimal comision = tx.getComision() != null ? tx.getComision() : BigDecimal.ZERO;
        BigDecimal gastos = tx.getGastos() != null ? tx.getGastos() : BigDecimal.ZERO;
        BigDecimal iva = tx.getIva() != null ? tx.getIva() : BigDecimal.ZERO;

        // Valor base del ingreso
        BigDecimal valorBase = cantidad.multiply(precio);
        
        // Sumar costos adicionales
        return valorBase.add(comision).add(gastos).add(iva);
    }

    /**
     * Valida los parámetros de entrada.
     */
    private void validarEntrada(
            TransaccionEntity tx,
            Queue<IngresoDisponible> queue,
            BigDecimal currentQty,
            BigDecimal currentVal,
            String clave) {

        if (tx == null) {
            throw new IllegalArgumentException("La transacción no puede ser nula");
        }
        if (queue == null) {
            throw new IllegalArgumentException("La cola de ingresos no puede ser nula");
        }
        if (currentQty == null || currentQty.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("La cantidad actual debe ser mayor o igual a cero");
        }
        if (currentVal == null) {
            throw new IllegalArgumentException("El valor actual no puede ser nulo");
        }
        if (clave == null || clave.trim().isEmpty()) {
            throw new IllegalArgumentException("La clave de agrupación no puede ser nula o vacía");
        }
        if (tx.getCantidad() == null || tx.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad del ingreso debe ser mayor a cero");
        }
        if (tx.getPrecio() == null || tx.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio del ingreso no puede ser negativo");
        }
    }
}