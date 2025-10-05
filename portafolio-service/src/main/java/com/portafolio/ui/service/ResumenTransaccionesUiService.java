package com.portafolio.ui.service;

import com.portafolio.persistence.repositorio.TransaccionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de UI para consultas de transacciones.
 * Proporciona listados, filtros y búsquedas para la interfaz.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResumenTransaccionesUiService {

    private final EntityManager entityManager;
    private final TransaccionRepository transaccionRepository;

    /**
     * Obtiene transacciones paginadas con filtros opcionales.
     */
    public Page<TransaccionUiDto> obtenerTransacciones(
            Long empresaId,
            FiltrosTransaccionDto filtros,
            Pageable pageable) {

        log.debug("Obteniendo transacciones para empresa {} con filtros: {}", empresaId, filtros);

        // Construir query dinámicamente
        StringBuilder jpql = new StringBuilder("""
            SELECT 
                t.id,
                t.fecha,
                t.fechaLiquidacion,
                i.instrumentoNemo,
                i.instrumentoNombre,
                c.nombre as custodioNombre,
                t.cuenta,
                tm.tipoMovimiento,
                t.cantidad,
                t.precio,
                t.monto,
                t.costeado,
                t.paraRevision
            FROM TransaccionEntity t
            JOIN t.instrumento i
            JOIN t.custodio c
            JOIN t.tipoMovimiento tm
            WHERE t.empresa.id = :empresaId
            """);

        // Aplicar filtros
        if (filtros.getFechaDesde() != null) {
            jpql.append(" AND t.fecha >= :fechaDesde");
        }
        if (filtros.getFechaHasta() != null) {
            jpql.append(" AND t.fecha <= :fechaHasta");
        }
        if (filtros.getInstrumentoNemo() != null) {
            jpql.append(" AND i.instrumentoNemo = :instrumentoNemo");
        }
        if (filtros.getCustodioId() != null) {
            jpql.append(" AND c.id = :custodioId");
        }
        if (filtros.getCuenta() != null) {
            jpql.append(" AND t.cuenta = :cuenta");
        }
        if (filtros.getTipoMovimiento() != null) {
            jpql.append(" AND tm.operacion = :tipoMovimiento");
        }
        if (filtros.getSoloNoCosteadas() != null && filtros.getSoloNoCosteadas()) {
            jpql.append(" AND t.costeado = false");
        }
        if (filtros.getSoloParaRevision() != null && filtros.getSoloParaRevision()) {
            jpql.append(" AND t.paraRevision = true");
        }

        jpql.append(" ORDER BY t.fecha DESC, t.id DESC");

        // Query para contar total
        String countJpql = jpql.toString()
                .replace("SELECT t.id, t.fecha, t.fechaLiquidacion, i.instrumentoNemo, i.nombre, c.nombre, t.cuenta, tm.operacion, t.cantidad, t.precio, t.monto, t.costeado, t.paraRevision",
                        "SELECT COUNT(t)");

        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);
        setQueryParameters(countQuery, empresaId, filtros);
        Long total = countQuery.getSingleResult();

        // Query de datos
        TypedQuery<Object[]> dataQuery = entityManager.createQuery(jpql.toString(), Object[].class);
        setQueryParameters(dataQuery, empresaId, filtros);
        dataQuery.setFirstResult((int) pageable.getOffset());
        dataQuery.setMaxResults(pageable.getPageSize());

        List<TransaccionUiDto> contenido = dataQuery.getResultList().stream()
                .map(this::mapearTransaccion)
                .collect(Collectors.toList());

        return new PageImpl<>(contenido, pageable, total);
    }

    /**
     * Obtiene transacciones por rango de fechas (sin paginación).
     */
    public List<TransaccionUiDto> obtenerTransaccionesPorPeriodo(
            Long empresaId,
            LocalDate fechaDesde,
            LocalDate fechaHasta) {

        log.debug("Obteniendo transacciones para empresa {} entre {} y {}", 
                empresaId, fechaDesde, fechaHasta);

        List<Object[]> resultados = entityManager.createQuery("""
            SELECT 
                t.id,
                t.fecha,
                t.fechaLiquidacion,
                i.instrumentoNemo,
                i.nombre as instrumentoNombre,
                c.nombre as custodioNombre,
                t.cuenta,
                tm.operacion as tipoMovimiento,
                t.cantidad,
                t.precio,
                t.monto,
                t.costeado,
                t.paraRevision
            FROM TransaccionEntity t
            JOIN t.instrumento i
            JOIN t.custodio c
            JOIN t.tipoMovimiento tm
            WHERE t.empresa.id = :empresaId
              AND t.fecha BETWEEN :fechaDesde AND :fechaHasta
            ORDER BY t.fecha DESC, t.id DESC
            """, Object[].class)
                .setParameter("empresaId", empresaId)
                .setParameter("fechaDesde", fechaDesde)
                .setParameter("fechaHasta", fechaHasta)
                .getResultList();

        return resultados.stream()
                .map(this::mapearTransaccion)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene transacciones no costeadas para revisión.
     */
    public List<TransaccionUiDto> obtenerTransaccionesNoCosteadas(Long empresaId) {
        log.debug("Obteniendo transacciones no costeadas para empresa {}", empresaId);

        List<Object[]> resultados = entityManager.createQuery("""
            SELECT 
                t.id,
                t.fecha,
                t.fechaLiquidacion,
                i.instrumentoNemo,
                i.nombre as instrumentoNombre,
                c.nombre as custodioNombre,
                t.cuenta,
                tm.operacion as tipoMovimiento,
                t.cantidad,
                t.precio,
                t.monto,
                t.costeado,
                t.paraRevision
            FROM TransaccionEntity t
            JOIN t.instrumento i
            JOIN t.custodio c
            JOIN t.tipoMovimiento tm
            WHERE t.empresa.id = :empresaId
              AND t.costeado = false
            ORDER BY t.fecha ASC, t.id ASC
            """, Object[].class)
                .setParameter("empresaId", empresaId)
                .getResultList();

        return resultados.stream()
                .map(this::mapearTransaccion)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene transacciones marcadas para revisión.
     */
    public List<TransaccionUiDto> obtenerTransaccionesParaRevision(Long empresaId) {
        log.debug("Obteniendo transacciones para revisión de empresa {}", empresaId);

        List<Object[]> resultados = entityManager.createQuery("""
            SELECT 
                t.id,
                t.fecha,
                t.fechaLiquidacion,
                i.instrumentoNemo,
                i.nombre as instrumentoNombre,
                c.nombre as custodioNombre,
                t.cuenta,
                tm.operacion as tipoMovimiento,
                t.cantidad,
                t.precio,
                t.monto,
                t.costeado,
                t.paraRevision
            FROM TransaccionEntity t
            JOIN t.instrumento i
            JOIN t.custodio c
            JOIN t.tipoMovimiento tm
            WHERE t.empresa.id = :empresaId
              AND t.paraRevision = true
            ORDER BY t.fecha ASC, t.id ASC
            """, Object[].class)
                .setParameter("empresaId", empresaId)
                .getResultList();

        return resultados.stream()
                .map(this::mapearTransaccion)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene estadísticas de transacciones por período.
     */
    public EstadisticasTransaccionesDto obtenerEstadisticas(
            Long empresaId,
            LocalDate fechaDesde,
            LocalDate fechaHasta) {

        log.debug("Obteniendo estadísticas de transacciones para empresa {} entre {} y {}", 
                empresaId, fechaDesde, fechaHasta);

        Object[] resultado = entityManager.createQuery("""
            SELECT 
                COUNT(t),
                COUNT(CASE WHEN tm.operacion IN ('COMPRA', 'APORTE') THEN 1 END),
                COUNT(CASE WHEN tm.operacion IN ('VENTA', 'RETIRO') THEN 1 END),
                COUNT(CASE WHEN t.costeado = true THEN 1 END),
                COUNT(CASE WHEN t.costeado = false THEN 1 END),
                COUNT(CASE WHEN t.paraRevision = true THEN 1 END),
                COALESCE(SUM(CASE WHEN tm.operacion IN ('COMPRA', 'APORTE') THEN t.monto ELSE 0 END), 0),
                COALESCE(SUM(CASE WHEN tm.operacion IN ('VENTA', 'RETIRO') THEN t.monto ELSE 0 END), 0)
            FROM TransaccionEntity t
            JOIN t.tipoMovimiento tm
            WHERE t.empresa.id = :empresaId
              AND t.fecha BETWEEN :fechaDesde AND :fechaHasta
            """, Object[].class)
                .setParameter("empresaId", empresaId)
                .setParameter("fechaDesde", fechaDesde)
                .setParameter("fechaHasta", fechaHasta)
                .getSingleResult();

        return EstadisticasTransaccionesDto.builder()
                .totalTransacciones(((Long) resultado[0]).intValue())
                .totalComprasAportes(((Long) resultado[1]).intValue())
                .totalVentasRetiros(((Long) resultado[2]).intValue())
                .totalCosteadas(((Long) resultado[3]).intValue())
                .totalNoCosteadas(((Long) resultado[4]).intValue())
                .totalParaRevision(((Long) resultado[5]).intValue())
                .montoTotalCompras((BigDecimal) resultado[6])
                .montoTotalVentas((BigDecimal) resultado[7])
                .build();
    }

    /**
     * Obtiene resumen de transacciones por instrumento.
     */
    public List<ResumenPorInstrumentoDto> obtenerResumenPorInstrumento(
            Long empresaId,
            LocalDate fechaDesde,
            LocalDate fechaHasta) {

        log.debug("Obteniendo resumen de transacciones por instrumento");

        List<Object[]> resultados = entityManager.createQuery("""
            SELECT 
                i.instrumentoNemo,
                i.nombre as instrumentoNombre,
                COUNT(t),
                COUNT(CASE WHEN tm.operacion IN ('COMPRA', 'APORTE') THEN 1 END),
                COUNT(CASE WHEN tm.operacion IN ('VENTA', 'RETIRO') THEN 1 END),
                COALESCE(SUM(CASE WHEN tm.operacion IN ('COMPRA', 'APORTE') THEN t.cantidad ELSE 0 END), 0),
                COALESCE(SUM(CASE WHEN tm.operacion IN ('VENTA', 'RETIRO') THEN t.cantidad ELSE 0 END), 0),
                COALESCE(SUM(CASE WHEN tm.operacion IN ('COMPRA', 'APORTE') THEN t.monto ELSE 0 END), 0)
            FROM TransaccionEntity t
            JOIN t.instrumento i
            JOIN t.tipoMovimiento tm
            WHERE t.empresa.id = :empresaId
              AND t.fecha BETWEEN :fechaDesde AND :fechaHasta
            GROUP BY i.instrumentoNemo, i.nombre
            ORDER BY COUNT(t) DESC
            """, Object[].class)
                .setParameter("empresaId", empresaId)
                .setParameter("fechaDesde", fechaDesde)
                .setParameter("fechaHasta", fechaHasta)
                .getResultList();

        return resultados.stream()
                .map(row -> ResumenPorInstrumentoDto.builder()
                        .instrumentoNemo((String) row[0])
                        .instrumentoNombre((String) row[1])
                        .totalTransacciones(((Long) row[2]).intValue())
                        .totalCompras(((Long) row[3]).intValue())
                        .totalVentas(((Long) row[4]).intValue())
                        .cantidadComprada((BigDecimal) row[5])
                        .cantidadVendida((BigDecimal) row[6])
                        .montoInvertido((BigDecimal) row[7])
                        .build())
                .collect(Collectors.toList());
    }

    // ===== MÉTODOS PRIVADOS =====

    /**
     * Establece los parámetros en la query según los filtros.
     */
    private void setQueryParameters(TypedQuery<?> query, Long empresaId, FiltrosTransaccionDto filtros) {
        query.setParameter("empresaId", empresaId);

        if (filtros.getFechaDesde() != null) {
            query.setParameter("fechaDesde", filtros.getFechaDesde());
        }
        if (filtros.getFechaHasta() != null) {
            query.setParameter("fechaHasta", filtros.getFechaHasta());
        }
        if (filtros.getInstrumentoNemo() != null) {
            query.setParameter("instrumentoNemo", filtros.getInstrumentoNemo());
        }
        if (filtros.getCustodioId() != null) {
            query.setParameter("custodioId", filtros.getCustodioId());
        }
        if (filtros.getCuenta() != null) {
            query.setParameter("cuenta", filtros.getCuenta());
        }
        if (filtros.getTipoMovimiento() != null) {
            query.setParameter("tipoMovimiento", filtros.getTipoMovimiento());
        }
    }

    /**
     * Mapea una fila de resultado a DTO de transacción.
     */
    private TransaccionUiDto mapearTransaccion(Object[] row) {
        return TransaccionUiDto.builder()
                .id((Long) row[0])
                .fecha((LocalDate) row[1])
                .fechaLiquidacion((LocalDate) row[2])
                .instrumentoNemo((String) row[3])
                .instrumentoNombre((String) row[4])
                .custodioNombre((String) row[5])
                .cuenta((String) row[6])
                .tipoMovimiento((String) row[7])
                .cantidad((BigDecimal) row[8])
                .precio((BigDecimal) row[9])
                .monto((BigDecimal) row[10])
                .costeado((Boolean) row[11])
                .paraRevision((Boolean) row[12])
                .build();
    }

    // ===== DTOs INTERNOS =====

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TransaccionUiDto {
        private Long id;
        private LocalDate fecha;
        private LocalDate fechaLiquidacion;
        private String instrumentoNemo;
        private String instrumentoNombre;
        private String custodioNombre;
        private String cuenta;
        private String tipoMovimiento;
        private BigDecimal cantidad;
        private BigDecimal precio;
        private BigDecimal monto;
        private Boolean costeado;
        private Boolean paraRevision;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class FiltrosTransaccionDto {
        private LocalDate fechaDesde;
        private LocalDate fechaHasta;
        private String instrumentoNemo;
        private Long custodioId;
        private String cuenta;
        private String tipoMovimiento;
        private Boolean soloNoCosteadas;
        private Boolean soloParaRevision;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class EstadisticasTransaccionesDto {
        private Integer totalTransacciones;
        private Integer totalComprasAportes;
        private Integer totalVentasRetiros;
        private Integer totalCosteadas;
        private Integer totalNoCosteadas;
        private Integer totalParaRevision;
        private BigDecimal montoTotalCompras;
        private BigDecimal montoTotalVentas;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ResumenPorInstrumentoDto {
        private String instrumentoNemo;
        private String instrumentoNombre;
        private Integer totalTransacciones;
        private Integer totalCompras;
        private Integer totalVentas;
        private BigDecimal cantidadComprada;
        private BigDecimal cantidadVendida;
        private BigDecimal montoInvertido;
    }
}