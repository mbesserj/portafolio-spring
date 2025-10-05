package com.portafolio.costing.service;

import com.portafolio.model.dto.KardexDto;
import com.portafolio.model.dto.SaldoKardexDto;
import com.portafolio.model.entities.KardexEntity;
import com.portafolio.model.entities.SaldoKardexEntity;
import com.portafolio.model.entities.SaldosDiariosEntity;
import com.portafolio.model.enums.TipoEnumsCosteo;
import com.portafolio.persistence.repositorio.KardexRepository;
import com.portafolio.persistence.repositorio.SaldoKardexRepository;
import com.portafolio.ui.mapper.KardexMapper;
import com.portafolio.ui.mapper.SaldoKardexMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio Spring para consultas del kardex y saldos.
 * Proporciona métodos de solo lectura para reportes y análisis.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KardexQueryService {

    private final EntityManager entityManager;
    private final KardexRepository kardexRepository;
    private final SaldoKardexRepository saldoKardexRepository;
    private final KardexMapper kardexMapper;
    private final SaldoKardexMapper saldoKardexMapper;

    // ===== CONSULTAS DE KARDEX =====

    /**
     * Obtiene el kardex completo para un grupo específico.
     */
    public List<KardexDto> obtenerKardexCompleto(
            Long empresaId, Long custodioId, Long instrumentoId, String cuenta) {
        
        log.debug("Obteniendo kardex completo para grupo: {}-{}-{}-{}", 
                empresaId, custodioId, instrumentoId, cuenta);
        
        List<KardexEntity> kardexEntities = kardexRepository
                .findByEmpresaIdAndCustodioIdAndInstrumentoIdAndCuentaOrderByFechaTransaccionAscIdAsc(
                        empresaId, custodioId, instrumentoId, cuenta);
        
        return kardexMapper.toDtoList(kardexEntities);
    }

    /**
     * Obtiene el kardex para un grupo en un rango de fechas.
     */
    public List<KardexDto> obtenerKardexPorFechas(
            Long empresaId, Long custodioId, Long instrumentoId, String cuenta,
            LocalDate fechaInicio, LocalDate fechaFin) {
        
        log.debug("Obteniendo kardex por fechas para grupo: {}-{}-{}-{}, desde {} hasta {}", 
                empresaId, custodioId, instrumentoId, cuenta, fechaInicio, fechaFin);
        
        List<KardexEntity> kardexEntities = entityManager.createQuery("""
            SELECT k FROM KardexEntity k
            WHERE k.empresa.id = :empresaId
              AND k.custodio.id = :custodioId
              AND k.instrumento.id = :instrumentoId
              AND k.cuenta = :cuenta
              AND k.fechaTransaccion BETWEEN :fechaInicio AND :fechaFin
            ORDER BY k.fechaTransaccion ASC, k.id ASC
            """, KardexEntity.class)
                .setParameter("empresaId", empresaId)
                .setParameter("custodioId", custodioId)
                .setParameter("instrumentoId", instrumentoId)
                .setParameter("cuenta", cuenta)
                .setParameter("fechaInicio", fechaInicio)
                .setParameter("fechaFin", fechaFin)
                .getResultList();
        
        return kardexMapper.toDtoList(kardexEntities);
    }

    /**
     * Obtiene el kardex paginado para un grupo.
     */
    public Page<KardexDto> obtenerKardexPaginado(
            Long empresaId, Long custodioId, Long instrumentoId, String cuenta,
            int page, int size) {
        
        log.debug("Obteniendo kardex paginado para grupo: {}-{}-{}-{}, página: {}, tamaño: {}", 
                empresaId, custodioId, instrumentoId, cuenta, page, size);
        
        Pageable pageable = PageRequest.of(page, size, 
                Sort.by("fechaTransaccion").ascending().and(Sort.by("id").ascending()));
        
        Page<KardexEntity> kardexPage = kardexRepository
                .findByEmpresaIdAndCustodioIdAndInstrumentoIdAndCuenta(
                        empresaId, custodioId, instrumentoId, cuenta, pageable);
        
        return kardexPage.map(kardexMapper::toDto);
    }

    /**
     * Obtiene solo los ingresos disponibles para consumo FIFO en un grupo.
     */
    public List<KardexDto> obtenerIngresosDisponibles(
            Long empresaId, Long custodioId, Long instrumentoId, String cuenta) {
        
        log.debug("Obteniendo ingresos disponibles para grupo: {}-{}-{}-{}", 
                empresaId, custodioId, instrumentoId, cuenta);
        
        List<KardexEntity> ingresosDisponibles = entityManager.createQuery("""
            SELECT k FROM KardexEntity k
            WHERE k.empresa.id = :empresaId
              AND k.custodio.id = :custodioId
              AND k.instrumento.id = :instrumentoId
              AND k.cuenta = :cuenta
              AND k.tipoContable = :tipoIngreso
              AND k.cantidadDisponible > 0
            ORDER BY k.fechaTransaccion ASC, k.id ASC
            """, KardexEntity.class)
                .setParameter("empresaId", empresaId)
                .setParameter("custodioId", custodioId)
                .setParameter("instrumentoId", instrumentoId)
                .setParameter("cuenta", cuenta)
                .setParameter("tipoIngreso", TipoEnumsCosteo.INGRESO)
                .getResultList();
        
        return kardexMapper.toDtoList(ingresosDisponibles);
    }

    // ===== CONSULTAS DE SALDOS =====

    /**
     * Obtiene el saldo actual consolidado para un grupo.
     */
    public Optional<SaldoKardexDto> obtenerSaldoActual(
            Long empresaId, Long custodioId, Long instrumentoId, String cuenta) {
        
        log.debug("Obteniendo saldo actual para grupo: {}-{}-{}-{}", 
                empresaId, custodioId, instrumentoId, cuenta);
        
        Optional<SaldoKardexEntity> saldoOpt = saldoKardexRepository
                .findByEmpresaIdAndCustodioIdAndInstrumentoIdAndCuenta(
                        empresaId, custodioId, instrumentoId, cuenta);
        
        return saldoOpt.map(saldoKardexMapper::toDto);
    }

    /**
     * Obtiene todos los saldos consolidados por empresa.
     */
    public List<SaldoKardexDto> obtenerSaldosPorEmpresa(Long empresaId) {
        log.debug("Obteniendo saldos por empresa: {}", empresaId);
        
        List<SaldoKardexEntity> saldos = entityManager.createQuery("""
            SELECT s FROM SaldoKardexEntity s
            WHERE s.empresa.id = :empresaId
              AND s.saldoCantidad > 0
            ORDER BY s.instrumento.instrumentoNemo ASC, s.cuenta ASC
            """, SaldoKardexEntity.class)
                .setParameter("empresaId", empresaId)
                .getResultList();
        
        return saldoKardexMapper.toDtoList(saldos);
    }

    /**
     * Obtiene saldos diarios para un grupo en un rango de fechas.
     */
    public List<Map<String, Object>> obtenerSaldosDiarios(
            Long empresaId, Long custodioId, Long instrumentoId, String cuenta,
            LocalDate fechaInicio, LocalDate fechaFin) {
        
        log.debug("Obteniendo saldos diarios para grupo: {}-{}-{}-{}, desde {} hasta {}", 
                empresaId, custodioId, instrumentoId, cuenta, fechaInicio, fechaFin);
        
        List<SaldosDiariosEntity> saldosDiarios = entityManager.createQuery("""
            SELECT s FROM SaldosDiariosEntity s
            WHERE s.empresa.id = :empresaId
              AND s.custodio.id = :custodioId
              AND s.instrumento.id = :instrumentoId
              AND s.cuenta = :cuenta
              AND s.fecha BETWEEN :fechaInicio AND :fechaFin
            ORDER BY s.fecha ASC
            """, SaldosDiariosEntity.class)
                .setParameter("empresaId", empresaId)
                .setParameter("custodioId", custodioId)
                .setParameter("instrumentoId", instrumentoId)
                .setParameter("cuenta", cuenta)
                .setParameter("fechaInicio", fechaInicio)
                .setParameter("fechaFin", fechaFin)
                .getResultList();
        
        return saldosDiarios.stream()
                .map(this::convertirSaldoDiarioAMap)
                .collect(Collectors.toList());
    }

    // ===== CONSULTAS ANALÍTICAS =====

    /**
     * Obtiene resumen de movimientos por tipo para un grupo.
     */
    public Map<TipoEnumsCosteo, Map<String, Object>> obtenerResumenMovimientos(
            Long empresaId, Long custodioId, Long instrumentoId, String cuenta,
            LocalDate fechaInicio, LocalDate fechaFin) {
        
        log.debug("Obteniendo resumen de movimientos para grupo: {}-{}-{}-{}", 
                empresaId, custodioId, instrumentoId, cuenta);
        
        List<Object[]> resultados = entityManager.createQuery("""
            SELECT k.tipoContable, 
                   COUNT(k.id), 
                   SUM(k.cantidad), 
                   SUM(k.costoTotal),
                   AVG(k.costoUnitario)
            FROM KardexEntity k
            WHERE k.empresa.id = :empresaId
              AND k.custodio.id = :custodioId
              AND k.instrumento.id = :instrumentoId
              AND k.cuenta = :cuenta
              AND k.fechaTransaccion BETWEEN :fechaInicio AND :fechaFin
            GROUP BY k.tipoContable
            """, Object[].class)
                .setParameter("empresaId", empresaId)
                .setParameter("custodioId", custodioId)
                .setParameter("instrumentoId", instrumentoId)
                .setParameter("cuenta", cuenta)
                .setParameter("fechaInicio", fechaInicio)
                .setParameter("fechaFin", fechaFin)
                .getResultList();
        
        return resultados.stream()
                .collect(Collectors.toMap(
                        row -> (TipoEnumsCosteo) row[0],
                        row -> Map.of(
                                "totalMovimientos", row[1],
                                "cantidadTotal", row[2],
                                "valorTotal", row[3],
                                "costoUnitarioPromedio", row[4]
                        )
                ));
    }

    /**
     * Obtiene el inventario valorizado actual por empresa.
     */
    public List<Map<String, Object>> obtenerInventarioValorado(Long empresaId) {
        log.debug("Obteniendo inventario valorizado para empresa: {}", empresaId);
        
        List<Object[]> resultados = entityManager.createQuery("""
            SELECT s.instrumento.instrumentoNemo,
                   s.instrumento.nombre,
                   s.cuenta,
                   s.custodio.nombre,
                   s.saldoCantidad,
                   s.costoTotal,
                   s.costoPromedio,
                   s.fechaUltimaActualizacion
            FROM SaldoKardexEntity s
            WHERE s.empresa.id = :empresaId
              AND s.saldoCantidad > 0
            ORDER BY s.instrumento.instrumentoNemo ASC, s.cuenta ASC
            """, Object[].class)
                .setParameter("empresaId", empresaId)
                .getResultList();
        
        return resultados.stream()
                .map(row -> Map.of(
                        "instrumentoNemo", row[0],
                        "instrumentoNombre", row[1],
                        "cuenta", row[2],
                        "custodioNombre", row[3],
                        "saldoCantidad", row[4],
                        "costoTotal", row[5],
                        "costoPromedio", row[6],
                        "fechaUltimaActualizacion", row[7]
                ))
                .collect(Collectors.toList());
    }

    /**
     * Verifica la consistencia del kardex para un grupo.
     */
    public Map<String, Object> verificarConsistenciaKardex(
            Long empresaId, Long custodioId, Long instrumentoId, String cuenta) {
        
        log.debug("Verificando consistencia de kardex para grupo: {}-{}-{}-{}", 
                empresaId, custodioId, instrumentoId, cuenta);
        
        // Obtener el último saldo del kardex
        Optional<KardexEntity> ultimoKardexOpt = kardexRepository
                .findByEmpresaIdAndCustodioIdAndInstrumentoIdAndCuenta(
                        empresaId, custodioId, instrumentoId, cuenta)
                .stream()
                .max((k1, k2) -> {
                    int fechaComp = k1.getFechaTransaccion().compareTo(k2.getFechaTransaccion());
                    return fechaComp != 0 ? fechaComp : k1.getId().compareTo(k2.getId());
                });
        
        // Obtener el saldo consolidado
        Optional<SaldoKardexEntity> saldoConsolidadoOpt = saldoKardexRepository
                .findByEmpresaIdAndCustodioIdAndInstrumentoIdAndCuenta(
                        empresaId, custodioId, instrumentoId, cuenta);
        
        // Verificar consistencia
        boolean esConsistente = true;
        String mensaje = "Kardex consistente";
        
        if (ultimoKardexOpt.isPresent() && saldoConsolidadoOpt.isPresent()) {
            KardexEntity ultimoKardex = ultimoKardexOpt.get();
            SaldoKardexEntity saldoConsolidado = saldoConsolidadoOpt.get();
            
            boolean cantidadConsistente = ultimoKardex.getSaldoCantidad()
                    .compareTo(saldoConsolidado.getSaldoCantidad()) == 0;
            
            boolean valorConsistente = ultimoKardex.getSaldoValor()
                    .compareTo(saldoConsolidado.getCostoTotal()) == 0;
            
            if (!cantidadConsistente || !valorConsistente) {
                esConsistente = false;
                mensaje = String.format(
                        "Inconsistencia detectada - Kardex: qty=%s, val=%s | Consolidado: qty=%s, val=%s",
                        ultimoKardex.getSaldoCantidad(),
                        ultimoKardex.getSaldoValor(),
                        saldoConsolidado.getSaldoCantidad(),
                        saldoConsolidado.getCostoTotal()
                );
            }
        } else if (ultimoKardexOpt.isEmpty() && saldoConsolidadoOpt.isPresent()) {
            esConsistente = false;
            mensaje = "Existe saldo consolidado pero no hay kardex";
        } else if (ultimoKardexOpt.isPresent() && saldoConsolidadoOpt.isEmpty()) {
            esConsistente = false;
            mensaje = "Existe kardex pero no hay saldo consolidado";
        }
        
        return Map.of(
                "consistente", esConsistente,
                "mensaje", mensaje,
                "ultimoKardex", ultimoKardexOpt.map(kardexMapper::toDto).orElse(null),
                "saldoConsolidado", saldoConsolidadoOpt.map(saldoKardexMapper::toDto).orElse(null)
        );
    }

    /**
     * Obtiene estadísticas de costeo para un período.
     */
    public Map<String, Object> obtenerEstadisticasCosteo(
            Long empresaId, LocalDate fechaInicio, LocalDate fechaFin) {
        
        log.debug("Obteniendo estadísticas de costeo para empresa {} desde {} hasta {}", 
                empresaId, fechaInicio, fechaFin);
        
        // Total de movimientos
        Long totalMovimientos = entityManager.createQuery("""
            SELECT COUNT(k.id) FROM KardexEntity k
            WHERE k.empresa.id = :empresaId
              AND k.fechaTransaccion BETWEEN :fechaInicio AND :fechaFin
            """, Long.class)
                .setParameter("empresaId", empresaId)
                .setParameter("fechaInicio", fechaInicio)
                .setParameter("fechaFin", fechaFin)
                .getSingleResult();
        
        // Movimientos por tipo
        List<Object[]> movimientosPorTipo = entityManager.createQuery("""
            SELECT k.tipoContable, COUNT(k.id)
            FROM KardexEntity k
            WHERE k.empresa.id = :empresaId
              AND k.fechaTransaccion BETWEEN :fechaInicio AND :fechaFin
            GROUP BY k.tipoContable
            """, Object[].class)
                .setParameter("empresaId", empresaId)
                .setParameter("fechaInicio", fechaInicio)
                .setParameter("fechaFin", fechaFin)
                .getResultList();
        
        // Transacciones pendientes
        Long transaccionesPendientes = entityManager.createQuery("""
            SELECT COUNT(t.id) FROM TransaccionEntity t
            WHERE t.empresa.id = :empresaId
              AND t.costeado = false
              AND t.tipoMovimiento.movimientoContable.tipoContable <> :noCostear
            """, Long.class)
                .setParameter("empresaId", empresaId)
                .setParameter("noCostear", TipoEnumsCosteo.NO_COSTEAR)
                .getSingleResult();
        
        // Transacciones para revisión
        Long transaccionesRevision = entityManager.createQuery("""
            SELECT COUNT(t.id) FROM TransaccionEntity t
            WHERE t.empresa.id = :empresaId
              AND t.paraRevision = true
            """, Long.class)
                .setParameter("empresaId", empresaId)
                .getSingleResult();
        
        // Valor total del inventario
        BigDecimal valorInventario = entityManager.createQuery("""
            SELECT COALESCE(SUM(s.costoTotal), 0) FROM SaldoKardexEntity s
            WHERE s.empresa.id = :empresaId
              AND s.saldoCantidad > 0
            """, BigDecimal.class)
                .setParameter("empresaId", empresaId)
                .getSingleResult();
        
        Map<TipoEnumsCosteo, Long> movimientosPorTipoMap = movimientosPorTipo.stream()
                .collect(Collectors.toMap(
                        row -> (TipoEnumsCosteo) row[0],
                        row -> (Long) row[1]
                ));
        
        return Map.of(
                "totalMovimientos", totalMovimientos,
                "movimientosPorTipo", movimientosPorTipoMap,
                "transaccionesPendientes", transaccionesPendientes,
                "transaccionesParaRevision", transaccionesRevision,
                "valorTotalInventario", valorInventario,
                "fechaInicio", fechaInicio,
                "fechaFin", fechaFin
        );
    }

    /**
     * Busca transacciones que requieren atención (para revisión o sin costear).
     */
    public List<Map<String, Object>> buscarTransaccionesProblematicas(Long empresaId) {
        log.debug("Buscando transacciones problemáticas para empresa: {}", empresaId);
        
        List<Object[]> resultados = entityManager.createQuery("""
            SELECT t.id,
                   t.fechaTransaccion,
                   t.folio,
                   i.instrumentoNemo,
                   t.cuenta,
                   c.nombre,
                   t.cantidad,
                   t.precio,
                   tm.tipoMovimiento,
                   t.costeado,
                   t.paraRevision,
                   t.glosa
            FROM TransaccionEntity t
            JOIN t.instrumento i
            JOIN t.custodio c
            JOIN t.tipoMovimiento tm
            WHERE t.empresa.id = :empresaId
              AND (t.paraRevision = true OR 
                   (t.costeado = false AND tm.movimientoContable.tipoContable <> :noCostear))
            ORDER BY t.fechaTransaccion DESC, t.id DESC
            """, Object[].class)
                .setParameter("empresaId", empresaId)
                .setParameter("noCostear", TipoEnumsCosteo.NO_COSTEAR)
                .setMaxResults(100) // Limitar a las 100 más recientes
                .getResultList();
        
        return resultados.stream()
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("transaccionId", row[0]);
                    map.put("fecha", row[1]);
                    map.put("folio", row[2] != null ? row[2] : "");
                    map.put("instrumentoNemo", row[3]);
                    map.put("cuenta", row[4]);
                    map.put("custodioNombre", row[5]);
                    map.put("cantidad", row[6]);
                    map.put("precio", row[7]);
                    map.put("tipoMovimiento", row[8]);
                    map.put("costeado", row[9]);
                    map.put("paraRevision", row[10]);
                    map.put("glosa", row[11] != null ? row[11] : "");
                    map.put("estado", (Boolean) row[10] ? "Para Revisión" : "Pendiente de Costeo");
                    return map;
                })
                .collect(Collectors.toList());
    }

    // ===== MÉTODOS PRIVADOS =====

    /**
     * Convierte una entidad de saldo diario a un mapa para el API.
     */
    private Map<String, Object> convertirSaldoDiarioAMap(SaldosDiariosEntity saldo) {
        return Map.of(
                "fecha", saldo.getFecha(),
                "saldoCantidad", saldo.getSaldoCantidad(),
                "saldoValor", saldo.getSaldoValor(),
                "costoPromedio", saldo.getSaldoCantidad().compareTo(BigDecimal.ZERO) > 0
                        ? saldo.getSaldoValor().divide(saldo.getSaldoCantidad(), 6, java.math.RoundingMode.HALF_UP)
                        : BigDecimal.ZERO
        );
    }
}