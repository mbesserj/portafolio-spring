package com.costing.service;

import com.model.dto.AjustePropuestoDto;
import com.model.entities.*;
import com.model.enums.TipoAjuste;
import com.persistence.repositorio.KardexRepository;
import com.persistence.repositorio.SaldoKardexRepository;
import com.persistence.repositorio.TipoMovimientoRepository;
import com.persistence.repositorio.TransaccionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

/**
 * Servicio Spring para gestión de ajustes de costeo.
 * Maneja la creación, cálculo y eliminación de ajustes manuales y automáticos.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AjustesService {

    private final EntityManager entityManager;
    private final TransaccionRepository transaccionRepository;
    private final KardexRepository kardexRepository;
    private final SaldoKardexRepository saldoKardexRepository;
    private final TipoMovimientoRepository tipoMovimientoRepository;

    /**
     * Propone un ajuste manual para una transacción específica.
     */
    @Transactional(readOnly = true)
    public AjustePropuestoDto proponerAjusteManual(Long transaccionId, TipoAjuste tipoAjuste) {
        log.debug("Proponiendo ajuste {} para transacción ID: {}", tipoAjuste, transaccionId);
        
        TransaccionEntity transaccion = transaccionRepository.findById(transaccionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró la transacción con ID: " + transaccionId));
        
        // Obtener el último saldo antes de esta transacción
        Optional<KardexEntity> ultimoKardexOpt = obtenerUltimoKardexAntesDe(transaccion);
        
        BigDecimal saldoCantidadAnterior = ultimoKardexOpt
                .map(KardexEntity::getSaldoCantidad)
                .orElse(BigDecimal.ZERO);
        
        BigDecimal saldoValorAnterior = ultimoKardexOpt
                .map(KardexEntity::getSaldoValor)
                .orElse(BigDecimal.ZERO);
        
        // Calcular la propuesta de ajuste según el tipo
        AjustePropuestoDto propuesta = calcularPropuestaAjuste(
                transaccion, tipoAjuste, saldoCantidadAnterior, saldoValorAnterior);
        
        log.info("Ajuste propuesto para Tx ID {}: Cantidad={}, Precio={}", 
                transaccionId, propuesta.getCantidad(), propuesta.getPrecio());
        
        return propuesta;
    }

    /**
     * Crea un ajuste manual en el sistema.
     */
    @Transactional
    public TransaccionEntity crearAjusteManual(
            Long transaccionReferenciaId,
            TipoAjuste tipoAjuste,
            BigDecimal cantidad,
            BigDecimal precio,
            String observaciones) {
        
        log.info("Creando ajuste manual {} para transacción ID: {}", tipoAjuste, transaccionReferenciaId);
        
        TransaccionEntity transaccionReferencia = transaccionRepository.findById(transaccionReferenciaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró la transacción de referencia con ID: " + transaccionReferenciaId));
        
        // Buscar el tipo de movimiento para el ajuste
        String nombreTipoMovimiento = tipoAjuste == TipoAjuste.INGRESO 
                ? "AJUSTE INGRESO" 
                : "AJUSTE EGRESO";
        
        TipoMovimientoEntity tipoMovimiento = tipoMovimientoRepository
                .findByTipoMovimiento(nombreTipoMovimiento)
                .orElseThrow(() -> new IllegalStateException(
                        "El tipo de movimiento '" + nombreTipoMovimiento + "' no está configurado"));
        
        // Crear la transacción de ajuste
        TransaccionEntity ajuste = TransaccionEntity.builder()
                .empresa(transaccionReferencia.getEmpresa())
                .cuenta(transaccionReferencia.getCuenta())
                .custodio(transaccionReferencia.getCustodio())
                .instrumento(transaccionReferencia.getInstrumento())
                .fechaTransaccion(transaccionReferencia.getFechaTransaccion())
                .tipoMovimiento(tipoMovimiento)
                .cantidad(cantidad.abs()) // Siempre positivo, el tipo define ingreso/egreso
                .precio(precio)
                .montoTotal(cantidad.abs().multiply(precio))
                .glosa(construirGlosaAjuste(transaccionReferenciaId, observaciones))
                .costeado(false) // Se costeará en el próximo proceso
                .paraRevision(false)
                .ignorarEnCosteo(false)
                .build();
        
        TransaccionEntity ajusteGuardado = transaccionRepository.save(ajuste);
        
        // Marcar la transacción original como no para revisión si estaba marcada
        if (transaccionReferencia.getParaRevision()) {
            transaccionReferencia.setParaRevision(false);
            transaccionRepository.save(transaccionReferencia);
            log.info("Transacción original ID {} desmarcada de revisión", transaccionReferenciaId);
        }
        
        log.info("Ajuste manual creado exitosamente - ID: {}, Tipo: {}, Cantidad: {}, Precio: {}", 
                ajusteGuardado.getId(), tipoAjuste, cantidad, precio);
        
        return ajusteGuardado;
    }

    /**
     * Elimina un ajuste manual y resetea el costeo si es necesario.
     */
    @Transactional
    public void eliminarAjusteManual(Long ajusteId) {
        log.warn("Eliminando ajuste manual ID: {}", ajusteId);
        
        TransaccionEntity ajuste = transaccionRepository.findById(ajusteId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró el ajuste con ID: " + ajusteId));
        
        // Verificar que sea realmente un ajuste
        String tipoMovimiento = ajuste.getTipoMovimiento().getTipoMovimiento();
        if (!esAjusteEliminable(tipoMovimiento)) {
            throw new IllegalArgumentException(
                    "La transacción seleccionada no es un ajuste eliminable: " + tipoMovimiento);
        }
        
        // Eliminar registros relacionados
        eliminarRegistrosRelacionados(ajusteId);
        
        // Si es un ajuste crítico (saldo inicial, cuadratura), resetear todo el grupo
        if (esAjusteCritico(tipoMovimiento)) {
            resetearGrupoPorAjusteCritico(ajuste);
        }
        
        // Eliminar la transacción de ajuste
        transaccionRepository.delete(ajuste);
        
        log.warn("Ajuste eliminado exitosamente - ID: {}", ajusteId);
    }

    /**
     * Lista los ajustes pendientes de aplicar para un grupo.
     */
    @Transactional(readOnly = true)
    public List<AjustePropuestoDto> listarAjustesPendientes(
            Long empresaId, Long custodioId, Long instrumentoId, String cuenta) {
        
        log.debug("Listando ajustes pendientes para grupo: {}-{}-{}-{}", 
                empresaId, custodioId, instrumentoId, cuenta);
        
        // Buscar transacciones marcadas para revisión en el grupo
        List<TransaccionEntity> transaccionesParaRevision = transaccionRepository
                .findByEmpresaIdAndCustodioIdAndInstrumentoIdAndCuentaAndParaRevisionTrueOrderByFechaTransaccionAsc(
                        empresaId, custodioId, instrumentoId, cuenta);
        
        return transaccionesParaRevision.stream()
                .map(tx -> {
                    // Para cada transacción problemática, proponer ambos tipos de ajuste
                    AjustePropuestoDto ajusteIngreso = proponerAjusteManual(tx.getId(), TipoAjuste.INGRESO);
                    ajusteIngreso.setTipoAjuste(TipoAjuste.INGRESO);
                    ajusteIngreso.setTransaccionReferenciaId(tx.getId());
                    return ajusteIngreso;
                })
                .toList();
    }

    // ===== MÉTODOS PRIVADOS =====

    /**
     * Obtiene el último registro de kardex antes de una transacción.
     */
    private Optional<KardexEntity> obtenerUltimoKardexAntesDe(TransaccionEntity transaccion) {
        return kardexRepository
                .findByEmpresaIdAndCustodioIdAndInstrumentoIdAndCuenta(
                        transaccion.getEmpresa().getId(),
                        transaccion.getCustodio().getId(),
                        transaccion.getInstrumento().getId(),
                        transaccion.getCuenta())
                .stream()
                .filter(k -> k.getFechaTransaccion().isBefore(transaccion.getFechaTransaccion()) ||
                           (k.getFechaTransaccion().equals(transaccion.getFechaTransaccion()) && 
                            k.getId() < transaccion.getId()))
                .max((k1, k2) -> {
                    int fechaComp = k1.getFechaTransaccion().compareTo(k2.getFechaTransaccion());
                    return fechaComp != 0 ? fechaComp : k1.getId().compareTo(k2.getId());
                });
    }

    /**
     * Calcula la propuesta de ajuste según el tipo.
     */
    private AjustePropuestoDto calcularPropuestaAjuste(
            TransaccionEntity transaccion,
            TipoAjuste tipoAjuste,
            BigDecimal saldoCantidadAnterior,
            BigDecimal saldoValorAnterior) {
        
        BigDecimal cantidadAjuste;
        BigDecimal precioAjuste;
        
        if (tipoAjuste == TipoAjuste.INGRESO) {
            // Para ajuste de ingreso: agregar lo que falta para poder hacer el egreso
            cantidadAjuste = transaccion.getCantidad().abs().subtract(saldoCantidadAnterior);
            
            // Precio promedio histórico o precio de la transacción
            precioAjuste = calcularPrecioPromedioODefault(
                    saldoCantidadAnterior, saldoValorAnterior, transaccion.getPrecio());
            
        } else {
            // Para ajuste de egreso: la cantidad exacta de la transacción problemática
            cantidadAjuste = transaccion.getCantidad().abs();
            precioAjuste = transaccion.getPrecio() != null 
                    ? transaccion.getPrecio() 
                    : BigDecimal.ZERO;
        }
        
        return AjustePropuestoDto.builder()
                .fecha(transaccion.getFechaTransaccion())
                .tipoMovimiento("AJUSTE " + tipoAjuste.name())
                .cantidad(cantidadAjuste.abs())
                .precio(precioAjuste)
                .monto(cantidadAjuste.abs().multiply(precioAjuste))
                .observaciones("Propuesta de ajuste para Tx ID: " + transaccion.getId())
                .saldoAnteriorCantidad(saldoCantidadAnterior)
                .saldoAnteriorFecha(transaccion.getFechaTransaccion().minusDays(1))
                .tipoAjuste(tipoAjuste)
                .transaccionReferenciaId(transaccion.getId())
                .build();
    }

    /**
     * Calcula el precio promedio basado en saldos históricos o usa un default.
     */
    private BigDecimal calcularPrecioPromedioODefault(
            BigDecimal saldoCantidad, BigDecimal saldoValor, BigDecimal precioDefault) {
        
        if (saldoCantidad != null && saldoCantidad.compareTo(BigDecimal.ZERO) > 0 && 
            saldoValor != null) {
            return saldoValor.divide(saldoCantidad, 6, RoundingMode.HALF_UP);
        }
        
        return precioDefault != null ? precioDefault : BigDecimal.ZERO;
    }

    /**
     * Construye la glosa para un ajuste.
     */
    private String construirGlosaAjuste(Long transaccionReferenciaId, String observaciones) {
        StringBuilder glosa = new StringBuilder("Ajuste manual para Tx ID: ");
        glosa.append(transaccionReferenciaId);
        
        if (observaciones != null && !observaciones.trim().isEmpty()) {
            glosa.append(" - ").append(observaciones.trim());
        }
        
        return glosa.toString();
    }

    /**
     * Verifica si un tipo de movimiento es un ajuste eliminable.
     */
    private boolean esAjusteEliminable(String tipoMovimiento) {
        return tipoMovimiento.contains("AJUSTE") || 
               tipoMovimiento.contains("SALDO INICIAL") ||
               tipoMovimiento.contains("CUADRATURA");
    }

    /**
     * Verifica si un ajuste es crítico (requiere reset del grupo completo).
     */
    private boolean esAjusteCritico(String tipoMovimiento) {
        return tipoMovimiento.contains("SALDO INICIAL") || 
               tipoMovimiento.contains("CUADRATURA");
    }

    /**
     * Elimina todos los registros relacionados con un ajuste.
     */
    private void eliminarRegistrosRelacionados(Long ajusteId) {
        // Eliminar detalles de costeo
        int detallesEliminados = entityManager.createQuery("""
            DELETE FROM DetalleCosteoEntity d 
            WHERE d.ingreso.id = :ajusteId OR d.egreso.id = :ajusteId
            """)
            .setParameter("ajusteId", ajusteId)
            .executeUpdate();
        
        // Eliminar registros de kardex
        int kardexEliminados = entityManager.createQuery("""
            DELETE FROM KardexEntity k 
            WHERE k.transaccion.id = :ajusteId
            """)
            .setParameter("ajusteId", ajusteId)
            .executeUpdate();
        
        log.debug("Registros relacionados eliminados - Detalles: {}, Kardex: {}", 
                detallesEliminados, kardexEliminados);
    }

    /**
     * Resetea todo el costeo de un grupo cuando se elimina un ajuste crítico.
     */
    private void resetearGrupoPorAjusteCritico(TransaccionEntity ajuste) {
        log.warn("Reseteando costeo completo del grupo por eliminación de ajuste crítico");
        
        // Resetear flags de costeo para todo el grupo
        int transaccionesReset = entityManager.createQuery("""
            UPDATE TransaccionEntity t 
            SET t.costeado = false, t.paraRevision = false
            WHERE t.empresa = :empresa 
              AND t.custodio = :custodio 
              AND t.instrumento = :instrumento 
              AND t.cuenta = :cuenta
            """)
            .setParameter("empresa", ajuste.getEmpresa())
            .setParameter("custodio", ajuste.getCustodio())
            .setParameter("instrumento", ajuste.getInstrumento())
            .setParameter("cuenta", ajuste.getCuenta())
            .executeUpdate();
        
        log.warn("Transacciones reseteadas para recosteo: {}", transaccionesReset);
    }
}