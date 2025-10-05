package com.costing.service;

import com.costing.api.CostingApi;
import com.costing.engine.FifoCostingEngine;
import com.model.dto.AjustePropuestoDto;
import com.model.dto.CostingGroupDto;
import com.model.dto.KardexDto;
import com.model.entities.KardexEntity;
import com.model.entities.SaldoKardexEntity;
import com.model.enums.TipoAjuste;
import com.model.enums.TipoEnumsCosteo;
import com.persistence.repositorio.KardexRepository;
import com.persistence.repositorio.SaldoKardexRepository;
import com.persistence.repositorio.TransaccionRepository;
import com.service.mapper.KardexMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Implementación Spring del servicio de costeo FIFO.
 * Coordina todas las operaciones de costeo usando los componentes del motor.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CostingServiceImpl implements CostingApi {

    // Dependencias Spring
    private final FifoCostingEngine fifoCostingEngine;
    private final EntityManager entityManager;
    private final TransaccionRepository transaccionRepository;
    private final KardexRepository kardexRepository;
    private final SaldoKardexRepository saldoKardexRepository;
    private final KardexMapper kardexMapper;

    /**
     * Procesa el costeo para todas las transacciones pendientes hasta una fecha.
     */
    @Override
    @Transactional
    public int procesarCosteo(LocalDate fechaCorte) {
        log.info("=== Iniciando proceso de costeo hasta fecha: {} ===", fechaCorte);
        
        try {
            // El motor ya maneja toda la lógica de costeo
            int gruposProcesados = fifoCostingEngine.procesarCosteo();
            
            log.info("=== Proceso de costeo completado: {} grupos procesados ===", gruposProcesados);
            return gruposProcesados;
            
        } catch (Exception e) {
            log.error("Error en el proceso de costeo: {}", e.getMessage(), e);
            throw new RuntimeException("Falló el proceso de costeo: " + e.getMessage(), e);
        }
    }

    /**
     * Procesa el costeo para un grupo específico.
     */
    @Override
    @Transactional
    public void procesarGrupo(CostingGroupDto grupo, LocalDate fechaCorte) {
        log.info("Procesando grupo específico: {}", grupo.getClaveAgrupacion());
        
        try {
            // 1. Resetear el grupo antes de recostearlo
            resetearGrupo(
                grupo.getEmpresaId(),
                grupo.getCustodioId(), 
                grupo.getInstrumentoId(),
                grupo.getCuenta(),
                fechaCorte
            );
            
            // 2. Procesar el costeo completo (incluirá este grupo)
            fifoCostingEngine.procesarCosteo();
            
            log.info("Grupo procesado exitosamente: {}", grupo.getClaveAgrupacion());
            
        } catch (Exception e) {
            log.error("Error procesando grupo {}: {}", grupo.getClaveAgrupacion(), e.getMessage(), e);
            throw new RuntimeException("Falló el procesamiento del grupo: " + e.getMessage(), e);
        }
    }

    /**
     * Calcula ajustes pendientes para un grupo.
     */
    @Override
    @Transactional(readOnly = true)
    public List<AjustePropuestoDto> calcularAjustes(
            Long empresaId, 
            Long custodioId, 
            Long instrumentoId,
            String cuenta,
            TipoAjuste tipoAjuste) {
        
        log.debug("Calculando ajustes para grupo: {}-{}-{}-{}, tipo: {}", 
                empresaId, custodioId, instrumentoId, cuenta, tipoAjuste);
        
        // TODO: Implementar lógica de cálculo de ajustes
        // Esto debería analizar las transacciones para revisión y proponer ajustes
        
        return List.of(); // Placeholder
    }

    /**
     * Obtiene el kardex para un grupo específico.
     */
    @Override
    @Transactional(readOnly = true)
    public List<KardexDto> obtenerKardex(
            Long empresaId, 
            Long custodioId, 
            Long instrumentoId,
            String cuenta) {
        
        log.debug("Obteniendo kardex para grupo: {}-{}-{}-{}", 
                empresaId, custodioId, instrumentoId, cuenta);
        
        List<KardexEntity> kardexEntities = kardexRepository
                .findByEmpresaIdAndCustodioIdAndInstrumentoIdAndCuentaOrderByFechaTransaccionAscIdAsc(
                        empresaId, custodioId, instrumentoId, cuenta);
        
        return kardexMapper.toDtoList(kardexEntities);
    }

    /**
     * Obtiene el saldo actual de un grupo.
     */
    @Override
    @Transactional(readOnly = true)
    public BigDecimal obtenerSaldoActual(
            Long empresaId, 
            Long custodioId, 
            Long instrumentoId,
            String cuenta) {
        
        log.debug("Obteniendo saldo actual para grupo: {}-{}-{}-{}", 
                empresaId, custodioId, instrumentoId, cuenta);
        
        Optional<SaldoKardexEntity> saldoOpt = saldoKardexRepository
                .findByEmpresaIdAndCustodioIdAndInstrumentoIdAndCuenta(
                        empresaId, custodioId, instrumentoId, cuenta);
        
        return saldoOpt.map(SaldoKardexEntity::getSaldoCantidad)
                .orElse(BigDecimal.ZERO);
    }

    /**
     * Reinicia el costeo para una fecha específica.
     */
    @Override
    @Transactional
    public void reiniciarCosteo(LocalDate fechaDesde) {
        log.warn("=== REINICIANDO COSTEO DESDE FECHA: {} ===", fechaDesde);
        
        try {
            // 1. Eliminar todos los registros de kardex desde la fecha
            int kardexEliminados = entityManager.createQuery("""
                DELETE FROM KardexEntity k 
                WHERE k.fechaTransaccion >= :fechaDesde
                """)
                .setParameter("fechaDesde", fechaDesde)
                .executeUpdate();
            
            // 2. Eliminar detalles de costeo
            int detallesEliminados = entityManager.createQuery("""
                DELETE FROM DetalleCosteoEntity d 
                WHERE d.egreso.fechaTransaccion >= :fechaDesde
                """)
                .setParameter("fechaDesde", fechaDesde)
                .executeUpdate();
            
            // 3. Resetear flags de costeo
            int transaccionesReset = entityManager.createQuery("""
                UPDATE TransaccionEntity t 
                SET t.costeado = false, t.paraRevision = false
                WHERE t.fechaTransaccion >= :fechaDesde
                  AND t.tipoMovimiento.movimientoContable.tipoContable <> :noCostear
                """)
                .setParameter("fechaDesde", fechaDesde)
                .setParameter("noCostear", TipoEnumsCosteo.NO_COSTEAR)
                .executeUpdate();
            
            // 4. Actualizar saldos kardex
            actualizarSaldosKardexDespuesDeReset(fechaDesde);
            
            log.warn("RESET COMPLETADO - Kardex eliminados: {}, Detalles eliminados: {}, Transacciones reset: {}", 
                    kardexEliminados, detallesEliminados, transaccionesReset);
            
        } catch (Exception e) {
            log.error("Error durante el reset de costeo: {}", e.getMessage(), e);
            throw new RuntimeException("Falló el reset de costeo: " + e.getMessage(), e);
        }
    }

    /**
     * Reinicia el costeo para un grupo específico.
     */
    @Override
    @Transactional
    public void reiniciarGrupo(
            Long empresaId, 
            Long custodioId, 
            Long instrumentoId,
            String cuenta,
            LocalDate fechaDesde) {
        
        log.warn("=== REINICIANDO GRUPO {}-{}-{}-{} DESDE FECHA: {} ===", 
                empresaId, custodioId, instrumentoId, cuenta, fechaDesde);
        
        try {
            resetearGrupo(empresaId, custodioId, instrumentoId, cuenta, fechaDesde);
            log.warn("RESET DE GRUPO COMPLETADO");
            
        } catch (Exception e) {
            log.error("Error durante el reset del grupo: {}", e.getMessage(), e);
            throw new RuntimeException("Falló el reset del grupo: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene todos los grupos de costeo existentes.
     */
    @Transactional(readOnly = true)
    public List<CostingGroupDto> obtenerGruposCosteo() {
        log.debug("Obteniendo todos los grupos de costeo");
        
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CostingGroupDto> cq = cb.createQuery(CostingGroupDto.class);
        
        Root<KardexEntity> k = cq.from(KardexEntity.class);
        
        cq.select(cb.construct(
                CostingGroupDto.class,
                k.get("empresa").get("id"),
                k.get("custodio").get("id"),
                k.get("instrumento").get("id"),
                k.get("cuenta"),
                k.get("claveAgrupacion"),
                k.get("empresa").get("razonSocial"),
                k.get("custodio").get("nombre"),
                k.get("instrumento").get("instrumentoNemo"),
                cb.min(k.get("fechaTransaccion")),
                cb.max(k.get("fechaTransaccion")),
                cb.count(k.get("id"))
        ));
        
        cq.groupBy(
                k.get("empresa").get("id"),
                k.get("custodio").get("id"),
                k.get("instrumento").get("id"),
                k.get("cuenta"),
                k.get("claveAgrupacion"),
                k.get("empresa").get("razonSocial"),
                k.get("custodio").get("nombre"),
                k.get("instrumento").get("instrumentoNemo")
        );
        
        cq.orderBy(
                cb.asc(k.get("empresa").get("razonSocial")),
                cb.asc(k.get("instrumento").get("instrumentoNemo")),
                cb.asc(k.get("cuenta"))
        );
        
        return entityManager.createQuery(cq).getResultList();
    }

    // ===== MÉTODOS PRIVADOS =====

    /**
     * Resetea un grupo específico eliminando su historial de costeo.
     */
    private void resetearGrupo(Long empresaId, Long custodioId, Long instrumentoId, 
                              String cuenta, LocalDate fechaDesde) {
        
        // 1. Eliminar kardex del grupo
        int kardexEliminados = entityManager.createQuery("""
            DELETE FROM KardexEntity k 
            WHERE k.empresa.id = :empresaId
              AND k.custodio.id = :custodioId
              AND k.instrumento.id = :instrumentoId
              AND k.cuenta = :cuenta
              AND k.fechaTransaccion >= :fechaDesde
            """)
            .setParameter("empresaId", empresaId)
            .setParameter("custodioId", custodioId)
            .setParameter("instrumentoId", instrumentoId)
            .setParameter("cuenta", cuenta)
            .setParameter("fechaDesde", fechaDesde)
            .executeUpdate();
        
        // 2. Eliminar detalles de costeo
        int detallesEliminados = entityManager.createQuery("""
            DELETE FROM DetalleCosteoEntity d 
            WHERE d.egreso.empresa.id = :empresaId
              AND d.egreso.custodio.id = :custodioId
              AND d.egreso.instrumento.id = :instrumentoId
              AND d.egreso.cuenta = :cuenta
              AND d.egreso.fechaTransaccion >= :fechaDesde
            """)
            .setParameter("empresaId", empresaId)
            .setParameter("custodioId", custodioId)
            .setParameter("instrumentoId", instrumentoId)
            .setParameter("cuenta", cuenta)
            .setParameter("fechaDesde", fechaDesde)
            .executeUpdate();
        
        // 3. Resetear flags de transacciones
        int transaccionesReset = entityManager.createQuery("""
            UPDATE TransaccionEntity t 
            SET t.costeado = false, t.paraRevision = false
            WHERE t.empresa.id = :empresaId
              AND t.custodio.id = :custodioId
              AND t.instrumento.id = :instrumentoId
              AND t.cuenta = :cuenta
              AND t.fechaTransaccion >= :fechaDesde
              AND t.tipoMovimiento.movimientoContable.tipoContable <> :noCostear
            """)
            .setParameter("empresaId", empresaId)
            .setParameter("custodioId", custodioId)
            .setParameter("instrumentoId", instrumentoId)
            .setParameter("cuenta", cuenta)
            .setParameter("fechaDesde", fechaDesde)
            .setParameter("noCostear", TipoEnumsCosteo.NO_COSTEAR)
            .executeUpdate();
        
        // 4. Actualizar saldo consolidado del grupo
        actualizarSaldoKardexGrupo(empresaId, custodioId, instrumentoId, cuenta);
        
        log.info("Grupo reseteado - Kardex: {}, Detalles: {}, Transacciones: {}", 
                kardexEliminados, detallesEliminados, transaccionesReset);
    }

    /**
     * Actualiza los saldos consolidados después de un reset.
     */
    private void actualizarSaldosKardexDespuesDeReset(LocalDate fechaDesde) {
        // Obtener todos los grupos afectados
        List<SaldoKardexEntity> saldosAfectados = entityManager.createQuery("""
            SELECT DISTINCT s FROM SaldoKardexEntity s
            WHERE s.fechaUltimaActualizacion >= :fechaDesde
            """, SaldoKardexEntity.class)
            .setParameter("fechaDesde", fechaDesde)
            .getResultList();
        
        // Recalcular cada saldo
        for (SaldoKardexEntity saldo : saldosAfectados) {
            actualizarSaldoKardexGrupo(
                saldo.getEmpresa().getId(),
                saldo.getCustodio().getId(),
                saldo.getInstrumento().getId(),
                saldo.getCuenta()
            );
        }
    }

    /**
     * Actualiza el saldo consolidado de un grupo basado en su último kardex.
     */
    private void actualizarSaldoKardexGrupo(Long empresaId, Long custodioId, 
                                           Long instrumentoId, String cuenta) {
        
        // Buscar el último registro de kardex
        Optional<KardexEntity> ultimoKardexOpt = kardexRepository
                .findByEmpresaIdAndCustodioIdAndInstrumentoIdAndCuenta(
                        empresaId, custodioId, instrumentoId, cuenta)
                .stream()
                .max((k1, k2) -> {
                    int fechaComp = k1.getFechaTransaccion().compareTo(k2.getFechaTransaccion());
                    return fechaComp != 0 ? fechaComp : k1.getId().compareTo(k2.getId());
                });
        
        // Buscar o crear el saldo consolidado
        SaldoKardexEntity saldo = saldoKardexRepository
                .findByEmpresaIdAndCustodioIdAndInstrumentoIdAndCuenta(
                        empresaId, custodioId, instrumentoId, cuenta)
                .orElseGet(() -> {
                    // Crear nuevo saldo si no existe
                    return SaldoKardexEntity.builder()
                            .empresa(entityManager.getReference(com.model.entities.EmpresaEntity.class, empresaId))
                            .custodio(entityManager.getReference(com.model.entities.CustodioEntity.class, custodioId))
                            .instrumento(entityManager.getReference(com.model.entities.InstrumentoEntity.class, instrumentoId))
                            .cuenta(cuenta)
                            .saldoCantidad(BigDecimal.ZERO)
                            .costoTotal(BigDecimal.ZERO)
                            .costoPromedio(BigDecimal.ZERO)
                            .fechaUltimaActualizacion(LocalDate.now())
                            .build();
                });
        
        // Actualizar con los valores del último kardex
        if (ultimoKardexOpt.isPresent()) {
            KardexEntity ultimoKardex = ultimoKardexOpt.get();
            saldo.setSaldoCantidad(ultimoKardex.getSaldoCantidad());
            saldo.setCostoTotal(ultimoKardex.getSaldoValor());
            saldo.recalcularCostoPromedio();
            saldo.setFechaUltimaActualizacion(ultimoKardex.getFechaTransaccion());
        } else {
            // No hay kardex, resetear a cero
            saldo.setSaldoCantidad(BigDecimal.ZERO);
            saldo.setCostoTotal(BigDecimal.ZERO);
            saldo.setCostoPromedio(BigDecimal.ZERO);
            saldo.setFechaUltimaActualizacion(LocalDate.now());
        }
        
        // Persistir o actualizar
        if (saldo.getId() == null) {
            entityManager.persist(saldo);
        } else {
            entityManager.merge(saldo);
        }
    }
}