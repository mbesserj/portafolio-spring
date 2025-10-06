package com.portafolio.costing.service;

import com.portafolio.model.dto.TransaccionDto;
import com.portafolio.model.entities.TransaccionEntity;
import com.portafolio.model.enums.TipoEnumsCosteo;
import com.portafolio.persistence.repositorio.TransaccionRepository;
import com.portafolio.mapper.TransaccionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio Spring para gestión de transacciones en el contexto de costeo.
 * Maneja operaciones de consulta, marcado y gestión de estado de transacciones.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransaccionManagementService {

    private final EntityManager entityManager;
    private final TransaccionRepository transaccionRepository;
    private final TransaccionMapper transaccionMapper;

    // ===== CONSULTAS DE TRANSACCIONES =====

    /**
     * Obtiene las transacciones pendientes de costeo para una empresa.
     */
    @Transactional(readOnly = true)
    public List<TransaccionDto> obtenerTransaccionesPendientes(Long empresaId) {
        log.debug("Obteniendo transacciones pendientes para empresa: {}", empresaId);
        
        List<TransaccionEntity> transaccionesPendientes = entityManager.createQuery("""
            SELECT t FROM TransaccionEntity t
            JOIN FETCH t.empresa
            JOIN FETCH t.custodio
            JOIN FETCH t.instrumento
            JOIN FETCH t.tipoMovimiento tm
            JOIN FETCH tm.movimientoContable mc
            WHERE t.empresa.id = :empresaId
              AND mc.tipoContable <> :noCostear
              AND t.costeado = false
              AND t.paraRevision = false
              AND t.ignorarEnCosteo = false
            ORDER BY t.fechaTransaccion ASC,
                     CASE WHEN tm.esSaldoInicial = true THEN 0 ELSE 1 END,
                     CASE WHEN mc.tipoContable = 'INGRESO' THEN 2 ELSE 3 END,
                     t.id ASC
            """, TransaccionEntity.class)
                .setParameter("empresaId", empresaId)
                .setParameter("noCostear", TipoEnumsCosteo.NO_COSTEAR)
                .getResultList();
        
        return transaccionMapper.toDtoList(transaccionesPendientes);
    }

    /**
     * Obtiene las transacciones marcadas para revisión.
     */
    @Transactional(readOnly = true)
    public List<TransaccionDto> obtenerTransaccionesParaRevision(Long empresaId) {
        log.debug("Obteniendo transacciones para revisión en empresa: {}", empresaId);
        
        List<TransaccionEntity> transaccionesRevision = transaccionRepository
                .findByEmpresaIdAndParaRevisionTrueOrderByFechaTransaccionAsc(empresaId);
        
        return transaccionMapper.toDtoList(transaccionesRevision);
    }

    /**
     * Obtiene transacciones por grupo con paginación.
     */
    @Transactional(readOnly = true)
    public Page<TransaccionDto> obtenerTransaccionesPorGrupo(
            Long empresaId, Long custodioId, Long instrumentoId, String cuenta,
            int page, int size, boolean soloCosteadas) {
        
        log.debug("Obteniendo transacciones paginadas para grupo: {}-{}-{}-{}", 
                empresaId, custodioId, instrumentoId, cuenta);
        
        Pageable pageable = PageRequest.of(page, size, 
                Sort.by("fechaTransaccion").ascending().and(Sort.by("id").ascending()));
        
        Page<TransaccionEntity> transaccionesPage;
        
        if (soloCosteadas) {
            transaccionesPage = transaccionRepository
                    .findByEmpresaIdAndCustodioIdAndInstrumentoIdAndCuentaAndCosteadoTrue(
                            empresaId, custodioId, instrumentoId, cuenta, pageable);
        } else {
            transaccionesPage = transaccionRepository
                    .findByEmpresaIdAndCustodioIdAndInstrumentoIdAndCuenta(
                            empresaId, custodioId, instrumentoId, cuenta, pageable);
        }
        
        return transaccionesPage.map(transaccionMapper::toDto);
    }

    /**
     * Busca transacciones por criterios múltiples.
     */
    @Transactional(readOnly = true)
    public List<TransaccionDto> buscarTransacciones(
            Long empresaId,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            String instrumentoNemo,
            String cuenta,
            Boolean costeado,
            Boolean paraRevision) {
        
        log.debug("Buscando transacciones con criterios: empresa={}, fechas={}-{}, instrumento={}, cuenta={}", 
                empresaId, fechaInicio, fechaFin, instrumentoNemo, cuenta);
        
        StringBuilder jpql = new StringBuilder("""
            SELECT t FROM TransaccionEntity t
            JOIN FETCH t.empresa
            JOIN FETCH t.custodio
            JOIN FETCH t.instrumento i
            JOIN FETCH t.tipoMovimiento tm
            WHERE t.empresa.id = :empresaId
            """);
        
        if (fechaInicio != null) {
            jpql.append(" AND t.fechaTransaccion >= :fechaInicio");
        }
        if (fechaFin != null) {
            jpql.append(" AND t.fechaTransaccion <= :fechaFin");
        }
        if (instrumentoNemo != null && !instrumentoNemo.trim().isEmpty()) {
            jpql.append(" AND i.instrumentoNemo = :instrumentoNemo");
        }
        if (cuenta != null && !cuenta.trim().isEmpty()) {
            jpql.append(" AND t.cuenta = :cuenta");
        }
        if (costeado != null) {
            jpql.append(" AND t.costeado = :costeado");
        }
        if (paraRevision != null) {
            jpql.append(" AND t.paraRevision = :paraRevision");
        }
        
        jpql.append(" ORDER BY t.fechaTransaccion DESC, t.id DESC");
        
        var query = entityManager.createQuery(jpql.toString(), TransaccionEntity.class)
                .setParameter("empresaId", empresaId)
                .setMaxResults(500); // Limitar resultados
        
        if (fechaInicio != null) {
            query.setParameter("fechaInicio", fechaInicio);
        }
        if (fechaFin != null) {
            query.setParameter("fechaFin", fechaFin);
        }
        if (instrumentoNemo != null && !instrumentoNemo.trim().isEmpty()) {
            query.setParameter("instrumentoNemo", instrumentoNemo.trim());
        }
        if (cuenta != null && !cuenta.trim().isEmpty()) {
            query.setParameter("cuenta", cuenta.trim());
        }
        if (costeado != null) {
            query.setParameter("costeado", costeado);
        }
        if (paraRevision != null) {
            query.setParameter("paraRevision", paraRevision);
        }
        
        List<TransaccionEntity> transacciones = query.getResultList();
        return transaccionMapper.toDtoList(transacciones);
    }

    // ===== GESTIÓN DE ESTADO =====

    /**
     * Marca transacciones específicas para revisión manual.
     */
    @Transactional
    public int marcarParaRevision(List<Long> transaccionIds, String motivo) {
        log.info("Marcando {} transacciones para revisión. Motivo: {}", transaccionIds.size(), motivo);
        
        if (transaccionIds.isEmpty()) {
            return 0;
        }
        
        String motivoCompleto = motivo != null && !motivo.trim().isEmpty()
                ? "Marcada para revisión: " + motivo.trim()
                : "Marcada para revisión manual";
        
        int transaccionesActualizadas = entityManager.createQuery("""
            UPDATE TransaccionEntity t 
            SET t.paraRevision = true, 
                t.costeado = false,
                t.glosa = CASE 
                    WHEN t.glosa IS NULL THEN :motivo
                    ELSE CONCAT(t.glosa, ' | ', :motivo)
                END
            WHERE t.id IN :ids
            """)
                .setParameter("ids", transaccionIds)
                .setParameter("motivo", motivoCompleto)
                .executeUpdate();
        
        log.info("Marcadas {} transacciones para revisión", transaccionesActualizadas);
        return transaccionesActualizadas;
    }

    /**
     * Desmarca transacciones de revisión (las vuelve a dejar pendientes).
     */
    @Transactional
    public int desmarcarDeRevision(List<Long> transaccionIds) {
        log.info("Desmarcando {} transacciones de revisión", transaccionIds.size());
        
        if (transaccionIds.isEmpty()) {
            return 0;
        }
        
        int transaccionesActualizadas = entityManager.createQuery("""
            UPDATE TransaccionEntity t 
            SET t.paraRevision = false, 
                t.costeado = false
            WHERE t.id IN :ids
            """)
                .setParameter("ids", transaccionIds)
                .executeUpdate();
        
        log.info("Desmarcadas {} transacciones de revisión", transaccionesActualizadas);
        return transaccionesActualizadas;
    }

    /**
     * Ignora transacciones específicas del proceso de costeo.
     */
    @Transactional
    public int ignorarEnCosteo(List<Long> transaccionIds, String motivo) {
        log.info("Ignorando {} transacciones en costeo. Motivo: {}", transaccionIds.size(), motivo);
        
        if (transaccionIds.isEmpty()) {
            return 0;
        }
        
        String motivoCompleto = motivo != null && !motivo.trim().isEmpty()
                ? "Ignorada en costeo: " + motivo.trim()
                : "Ignorada en costeo";
        
        int transaccionesActualizadas = entityManager.createQuery("""
            UPDATE TransaccionEntity t 
            SET t.ignorarEnCosteo = true,
                t.paraRevision = false,
                t.costeado = false,
                t.glosa = CASE 
                    WHEN t.glosa IS NULL THEN :motivo
                    ELSE CONCAT(t.glosa, ' | ', :motivo)
                END
            WHERE t.id IN :ids
            """)
                .setParameter("ids", transaccionIds)
                .setParameter("motivo", motivoCompleto)
                .executeUpdate();
        
        log.info("Ignoradas {} transacciones en costeo", transaccionesActualizadas);
        return transaccionesActualizadas;
    }

    /**
     * Restaura transacciones ignoradas para que vuelvan al proceso de costeo.
     */
    @Transactional
    public int restaurarEnCosteo(List<Long> transaccionIds) {
        log.info("Restaurando {} transacciones al proceso de costeo", transaccionIds.size());
        
        if (transaccionIds.isEmpty()) {
            return 0;
        }
        
        int transaccionesActualizadas = entityManager.createQuery("""
            UPDATE TransaccionEntity t 
            SET t.ignorarEnCosteo = false,
                t.paraRevision = false,
                t.costeado = false
            WHERE t.id IN :ids
            """)
                .setParameter("ids", transaccionIds)
                .executeUpdate();
        
        log.info("Restauradas {} transacciones al proceso de costeo", transaccionesActualizadas);
        return transaccionesActualizadas;
    }

    // ===== CONSULTAS ESPECIALIZADAS =====

    /**
     * Obtiene estadísticas de transacciones por estado.
     */
    @Transactional(readOnly = true)
    public Map<String, Long> obtenerEstadisticasTransacciones(Long empresaId) {
        log.debug("Obteniendo estadísticas de transacciones para empresa: {}", empresaId);
        
        List<Object[]> resultados = entityManager.createQuery("""
            SELECT 
                CASE 
                    WHEN t.ignorarEnCosteo = true THEN 'IGNORADAS'
                    WHEN t.paraRevision = true THEN 'PARA_REVISION'
                    WHEN t.costeado = true THEN 'COSTEADAS'
                    WHEN tm.movimientoContable.tipoContable = :noCostear THEN 'NO_COSTEAR'
                    ELSE 'PENDIENTES'
                END as estado,
                COUNT(t.id)
            FROM TransaccionEntity t
            JOIN t.tipoMovimiento tm
            WHERE t.empresa.id = :empresaId
            GROUP BY CASE 
                WHEN t.ignorarEnCosteo = true THEN 'IGNORADAS'
                WHEN t.paraRevision = true THEN 'PARA_REVISION'
                WHEN t.costeado = true THEN 'COSTEADAS'
                WHEN tm.movimientoContable.tipoContable = :noCostear THEN 'NO_COSTEAR'
                ELSE 'PENDIENTES'
            END
            """, Object[].class)
                .setParameter("empresaId", empresaId)
                .setParameter("noCostear", TipoEnumsCosteo.NO_COSTEAR)
                .getResultList();
        
        return resultados.stream()
                .collect(java.util.stream.Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]
                ));
    }

    /**
     * Obtiene las transacciones más recientes para un grupo.
     */
    @Transactional(readOnly = true)
    public List<TransaccionDto> obtenerUltimasTransacciones(
            Long empresaId, Long custodioId, Long instrumentoId, String cuenta, int limite) {
        
        log.debug("Obteniendo últimas {} transacciones para grupo: {}-{}-{}-{}", 
                limite, empresaId, custodioId, instrumentoId, cuenta);
        
        List<TransaccionEntity> ultimasTransacciones = entityManager.createQuery("""
            SELECT t FROM TransaccionEntity t
            JOIN FETCH t.empresa
            JOIN FETCH t.custodio
            JOIN FETCH t.instrumento
            JOIN FETCH t.tipoMovimiento
            WHERE t.empresa.id = :empresaId
              AND t.custodio.id = :custodioId
              AND t.instrumento.id = :instrumentoId
              AND t.cuenta = :cuenta
            ORDER BY t.fechaTransaccion DESC, t.id DESC
            """, TransaccionEntity.class)
                .setParameter("empresaId", empresaId)
                .setParameter("custodioId", custodioId)
                .setParameter("instrumentoId", instrumentoId)
                .setParameter("cuenta", cuenta)
                .setMaxResults(limite)
                .getResultList();
        
        return transaccionMapper.toDtoList(ultimasTransacciones);
    }

    /**
     * Verifica si una transacción específica existe y está disponible para costeo.
     */
    @Transactional(readOnly = true)
    public Optional<TransaccionDto> verificarTransaccion(Long transaccionId) {
        log.debug("Verificando transacción ID: {}", transaccionId);
        
        Optional<TransaccionEntity> transaccionOpt = transaccionRepository
                .findByIdWithAllRelations(transaccionId);
        
        return transaccionOpt.map(transaccionMapper::toDto);
    }

    /**
     * Obtiene el resumen de un grupo de costeo (totales y estadísticas).
     */
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerResumenGrupo(
            Long empresaId, Long custodioId, Long instrumentoId, String cuenta) {
        
        log.debug("Obteniendo resumen del grupo: {}-{}-{}-{}", 
                empresaId, custodioId, instrumentoId, cuenta);
        
        Object[] resultado = entityManager.createQuery("""
            SELECT 
                COUNT(t.id) as total,
                SUM(CASE WHEN t.costeado = true THEN 1 ELSE 0 END) as costeadas,
                SUM(CASE WHEN t.paraRevision = true THEN 1 ELSE 0 END) as paraRevision,
                SUM(CASE WHEN t.ignorarEnCosteo = true THEN 1 ELSE 0 END) as ignoradas,
                MIN(t.fechaTransaccion) as fechaMinima,
                MAX(t.fechaTransaccion) as fechaMaxima
            FROM TransaccionEntity t
            WHERE t.empresa.id = :empresaId
              AND t.custodio.id = :custodioId
              AND t.instrumento.id = :instrumentoId
              AND t.cuenta = :cuenta
            """, Object[].class)
                .setParameter("empresaId", empresaId)
                .setParameter("custodioId", custodioId)
                .setParameter("instrumentoId", instrumentoId)
                .setParameter("cuenta", cuenta)
                .getSingleResult();
        
        return Map.of(
                "totalTransacciones", resultado[0],
                "transaccionesCosteadas", resultado[1],
                "transaccionesParaRevision", resultado[2],
                "transaccionesIgnoradas", resultado[3],
                "fechaPrimeraTransaccion", resultado[4],
                "fechaUltimaTransaccion", resultado[5],
                "transaccionesPendientes", ((Long) resultado[0]) - ((Long) resultado[1]) - ((Long) resultado[2]) - ((Long) resultado[3])
        );
    }
}