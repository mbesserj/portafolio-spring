package com.costing.service;

import com.model.enums.TipoEnumsCosteo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio Spring para generación de reportes de costeo.
 * Proporciona reportes analíticos, de inventario y de rendimiento del sistema.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CostingReportsService {

    private final EntityManager entityManager;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ===== REPORTES DE INVENTARIO =====

    /**
     * Genera reporte de inventario valorizado por empresa.
     */
    public Map<String, Object> generarReporteInventarioValorado(Long empresaId, LocalDate fechaCorte) {
        log.info("Generando reporte de inventario valorizado para empresa {} al {}", empresaId, fechaCorte);
        
        List<Object[]> inventario = entityManager.createQuery("""
            SELECT 
                i.instrumentoNemo,
                i.nombre as instrumentoNombre,
                c.nombre as custodioNombre,
                s.cuenta,
                s.saldoCantidad,
                s.costoTotal,
                s.costoPromedio,
                s.fechaUltimaActualizacion
            FROM SaldoKardexEntity s
            JOIN s.instrumento i
            JOIN s.custodio c
            WHERE s.empresa.id = :empresaId
              AND s.saldoCantidad > 0
              AND s.fechaUltimaActualizacion <= :fechaCorte
            ORDER BY i.instrumentoNemo ASC, s.cuenta ASC
            """, Object[].class)
                .setParameter("empresaId", empresaId)
                .setParameter("fechaCorte", fechaCorte)
                .getResultList();
        
        BigDecimal valorTotal = inventario.stream()
                .map(row -> (BigDecimal) row[5])
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        List<Map<String, Object>> items = inventario.stream()
                .map(row -> Map.of(
                        "instrumentoNemo", row[0],
                        "instrumentoNombre", row[1],
                        "custodioNombre", row[2],
                        "cuenta", row[3],
                        "cantidad", row[4],
                        "costoTotal", row[5],
                        "costoUnitario", row[6],
                        "fechaActualizacion", row[7],
                        "participacion", calcularPorcentaje((BigDecimal) row[5], valorTotal)
                ))
                .collect(Collectors.toList());
        
        return Map.of(
                "fechaCorte", fechaCorte,
                "fechaGeneracion", LocalDate.now(),
                "empresaId", empresaId,
                "valorTotalInventario", valorTotal,
                "totalItems", items.size(),
                "items", items,
                "resumen", generarResumenInventario(items)
        );
    }

    /**
     * Genera reporte de movimientos por período.
     */
    public Map<String, Object> generarReporteMovimientos(
            Long empresaId, LocalDate fechaInicio, LocalDate fechaFin, 
            String instrumentoNemo, String cuenta) {
        
        log.info("Generando reporte de movimientos para empresa {} del {} al {}", 
                empresaId, fechaInicio, fechaFin);
        
        StringBuilder jpql = new StringBuilder("""
            SELECT 
                k.fechaTransaccion,
                i.instrumentoNemo,
                k.cuenta,
                c.nombre as custodioNombre,
                k.tipoContable,
                k.cantidad,
                k.costoUnitario,
                k.costoTotal,
                k.saldoCantidad,
                k.saldoValor,
                t.folio,
                tm.tipoMovimiento
            FROM KardexEntity k
            JOIN k.instrumento i
            JOIN k.custodio c
            JOIN k.transaccion t
            JOIN t.tipoMovimiento tm
            WHERE k.empresa.id = :empresaId
              AND k.fechaTransaccion BETWEEN :fechaInicio AND :fechaFin
            """);
        
        if (instrumentoNemo != null && !instrumentoNemo.trim().isEmpty()) {
            jpql.append(" AND i.instrumentoNemo = :instrumentoNemo");
        }
        if (cuenta != null && !cuenta.trim().isEmpty()) {
            jpql.append(" AND k.cuenta = :cuenta");
        }
        
        jpql.append(" ORDER BY k.fechaTransaccion ASC, k.id ASC");
        
        var query = entityManager.createQuery(jpql.toString(), Object[].class)
                .setParameter("empresaId", empresaId)
                .setParameter("fechaInicio", fechaInicio)
                .setParameter("fechaFin", fechaFin);
        
        if (instrumentoNemo != null && !instrumentoNemo.trim().isEmpty()) {
            query.setParameter("instrumentoNemo", instrumentoNemo);
        }
        if (cuenta != null && !cuenta.trim().isEmpty()) {
            query.setParameter("cuenta", cuenta);
        }
        
        List<Object[]> movimientos = query.getResultList();
        
        List<Map<String, Object>> items = movimientos.stream()
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("fecha", ((LocalDate) row[0]).format(DATE_FORMATTER));
                    map.put("instrumentoNemo", row[1]);
                    map.put("cuenta", row[2]);
                    map.put("custodio", row[3]);
                    map.put("tipo", row[4]);
                    map.put("cantidad", row[5]);
                    map.put("costoUnitario", row[6]);
                    map.put("costoTotal", row[7]);
                    map.put("saldoCantidad", row[8]);
                    map.put("saldoValor", row[9]);
                    map.put("folio", row[10] != null ? row[10] : "");
                    map.put("tipoMovimiento", row[11]);
                    return map;
                })
                .collect(Collectors.toList());
        
        Map<TipoEnumsCosteo, Map<String, Object>> resumenPorTipo = generarResumenPorTipo(movimientos);
        
        return Map.of(
                "fechaInicio", fechaInicio,
                "fechaFin", fechaFin,
                "fechaGeneracion", LocalDate.now(),
                "empresaId", empresaId,
                "filtros", Map.of(
                        "instrumentoNemo", instrumentoNemo != null ? instrumentoNemo : "TODOS",
                        "cuenta", cuenta != null ? cuenta : "TODAS"
                ),
                "totalMovimientos", items.size(),
                "movimientos", items,
                "resumenPorTipo", resumenPorTipo
        );
    }

    /**
     * Genera reporte de utilidades FIFO por ventas.
     */
    public Map<String, Object> generarReporteUtilidades(
            Long empresaId, LocalDate fechaInicio, LocalDate fechaFin) {
        
        log.info("Generando reporte de utilidades para empresa {} del {} al {}", 
                empresaId, fechaInicio, fechaFin);
        
        // Obtener los egresos (ventas) con sus detalles de costeo
        List<Object[]> egresos = entityManager.createQuery("""
            SELECT 
                ke.fechaTransaccion,
                i.instrumentoNemo,
                ke.cuenta,
                c.nombre as custodioNombre,
                ke.cantidad,
                te.precio as precioVenta,
                te.montoTotal as montoVenta,
                ke.costoTotal as costoFIFO,
                te.folio
            FROM KardexEntity ke
            JOIN ke.instrumento i
            JOIN ke.custodio c
            JOIN ke.transaccion te
            WHERE ke.empresa.id = :empresaId
              AND ke.tipoContable = :tipoEgreso
              AND ke.fechaTransaccion BETWEEN :fechaInicio AND :fechaFin
              AND te.precio IS NOT NULL
            ORDER BY ke.fechaTransaccion ASC
            """, Object[].class)
                .setParameter("empresaId", empresaId)
                .setParameter("tipoEgreso", TipoEnumsCosteo.EGRESO)
                .setParameter("fechaInicio", fechaInicio)
                .setParameter("fechaFin", fechaFin)
                .getResultList();
        
        BigDecimal totalVentas = BigDecimal.ZERO;
        BigDecimal totalCostos = BigDecimal.ZERO;
        
        List<Map<String, Object>> ventasDetalle = new ArrayList<>();
        
        for (Object[] row : egresos) {
            BigDecimal montoVenta = (BigDecimal) row[6];
            BigDecimal costoFIFO = (BigDecimal) row[7];
            BigDecimal utilidad = montoVenta.subtract(costoFIFO);
            BigDecimal margen = montoVenta.compareTo(BigDecimal.ZERO) > 0 
                    ? utilidad.divide(montoVenta, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                    : BigDecimal.ZERO;
            
            totalVentas = totalVentas.add(montoVenta);
            totalCostos = totalCostos.add(costoFIFO);
            
            ventasDetalle.add(Map.of(
                    "fecha", ((LocalDate) row[0]).format(DATE_FORMATTER),
                    "instrumentoNemo", row[1],
                    "cuenta", row[2],
                    "custodio", row[3],
                    "cantidad", row[4],
                    "precioVenta", row[5],
                    "montoVenta", montoVenta,
                    "costoFIFO", costoFIFO,
                    "utilidad", utilidad,
                    "margenPorcentaje", margen
            ));
        }
        
        BigDecimal utilidadTotal = totalVentas.subtract(totalCostos);
        BigDecimal margenTotal = totalVentas.compareTo(BigDecimal.ZERO) > 0 
                ? utilidadTotal.divide(totalVentas, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;
        
        return Map.of(
                "fechaInicio", fechaInicio,
                "fechaFin", fechaFin,
                "fechaGeneracion", LocalDate.now(),
                "empresaId", empresaId,
                "resumen", Map.of(
                        "totalVentas", totalVentas,
                        "totalCostos", totalCostos,
                        "utilidadTotal", utilidadTotal,
                        "margenTotal", margenTotal,
                        "cantidadOperaciones", ventasDetalle.size()
                ),
                "ventasDetalle", ventasDetalle
        );
    }

    // ===== REPORTES DE RENDIMIENTO =====

    /**
     * Genera reporte de rendimiento del sistema de costeo.
     */
    public Map<String, Object> generarReporteRendimiento(Long empresaId) {
        log.info("Generando reporte de rendimiento para empresa: {}", empresaId);
        
        // Estadísticas generales
        Object[] estadisticas = entityManager.createQuery("""
            SELECT 
                COUNT(t.id) as totalTransacciones,
                SUM(CASE WHEN t.costeado = true THEN 1 ELSE 0 END) as transaccionesCosteadas,
                SUM(CASE WHEN t.paraRevision = true THEN 1 ELSE 0 END) as transaccionesRevision,
                SUM(CASE WHEN t.ignorarEnCosteo = true THEN 1 ELSE 0 END) as transaccionesIgnoradas,
                COUNT(DISTINCT CONCAT(t.empresa.id, '|', t.cuenta, '|', t.custodio.id, '|', t.instrumento.id)) as gruposActivos
            FROM TransaccionEntity t
            WHERE t.empresa.id = :empresaId
            """, Object[].class)
                .setParameter("empresaId", empresaId)
                .getSingleResult();
        
        // Rendimiento por instrumento
        List<Object[]> rendimientoPorInstrumento = entityManager.createQuery("""
            SELECT 
                i.instrumentoNemo,
                COUNT(t.id) as totalTx,
                SUM(CASE WHEN t.costeado = true THEN 1 ELSE 0 END) as costeadas,
                SUM(CASE WHEN t.paraRevision = true THEN 1 ELSE 0 END) as paraRevision
            FROM TransaccionEntity t
            JOIN t.instrumento i
            WHERE t.empresa.id = :empresaId
            GROUP BY i.instrumentoNemo
            ORDER BY COUNT(t.id) DESC
            """, Object[].class)
                .setParameter("empresaId", empresaId)
                .setMaxResults(20)
                .getResultList();
        
        // Actividad por mes (últimos 12 meses)
        LocalDate hace12Meses = LocalDate.now().minusMonths(12);
        List<Object[]> actividadMensual = entityManager.createQuery("""
            SELECT 
                FUNCTION('YEAR', k.fechaTransaccion) as ano,
                FUNCTION('MONTH', k.fechaTransaccion) as mes,
                COUNT(k.id) as movimientos,
                COUNT(DISTINCT k.claveAgrupacion) as gruposProcesados
            FROM KardexEntity k
            WHERE k.empresa.id = :empresaId
              AND k.fechaTransaccion >= :fechaDesde
            GROUP BY FUNCTION('YEAR', k.fechaTransaccion), FUNCTION('MONTH', k.fechaTransaccion)
            ORDER BY ano ASC, mes ASC
            """, Object[].class)
                .setParameter("empresaId", empresaId)
                .setParameter("fechaDesde", hace12Meses)
                .getResultList();
        
        Long totalTransacciones = (Long) estadisticas[0];
        Long transaccionesCosteadas = (Long) estadisticas[1];
        Long transaccionesRevision = (Long) estadisticas[2];
        Long transaccionesIgnoradas = (Long) estadisticas[3];
        Long gruposActivos = (Long) estadisticas[4];
        
        BigDecimal porcentajeCosteado = totalTransacciones > 0 
                ? new BigDecimal(transaccionesCosteadas).divide(new BigDecimal(totalTransacciones), 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;
        
        BigDecimal porcentajeRevision = totalTransacciones > 0 
                ? new BigDecimal(transaccionesRevision).divide(new BigDecimal(totalTransacciones), 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;
        
        return Map.of(
                "fechaGeneracion", LocalDate.now(),
                "empresaId", empresaId,
                "estadisticasGenerales", Map.of(
                        "totalTransacciones", totalTransacciones,
                        "transaccionesCosteadas", transaccionesCosteadas,
                        "transaccionesRevision", transaccionesRevision,
                        "transaccionesIgnoradas", transaccionesIgnoradas,
                        "transaccionesPendientes", totalTransacciones - transaccionesCosteadas - transaccionesRevision - transaccionesIgnoradas,
                        "gruposActivos", gruposActivos,
                        "porcentajeCosteado", porcentajeCosteado,
                        "porcentajeRevision", porcentajeRevision
                ),
                "rendimientoPorInstrumento", rendimientoPorInstrumento.stream()
                        .map(row -> Map.of(
                                "instrumentoNemo", row[0],
                                "totalTransacciones", row[1],
                                "transaccionesCosteadas", row[2],
                                "transaccionesRevision", row[3],
                                "porcentajeCosteado", calcularPorcentaje((Long) row[2], (Long) row[1])
                        ))
                        .collect(Collectors.toList()),
                "actividadMensual", actividadMensual.stream()
                        .map(row -> Map.of(
                                "periodo", String.format("%d-%02d", (Integer) row[0], (Integer) row[1]),
                                "movimientos", row[2],
                                "gruposProcesados", row[3]
                        ))
                        .collect(Collectors.toList())
        );
    }

    /**
     * Genera reporte de transacciones problemáticas.
     */
    public Map<String, Object> generarReporteTransaccionesProblematicas(Long empresaId) {
        log.info("Generando reporte de transacciones problemáticas para empresa: {}", empresaId);
        
        // Transacciones para revisión
        List<Object[]> transaccionesRevision = entityManager.createQuery("""
            SELECT 
                t.id,
                t.fechaTransaccion,
                i.instrumentoNemo,
                t.cuenta,
                c.nombre as custodioNombre,
                t.cantidad,
                t.precio,
                tm.tipoMovimiento,
                t.glosa,
                t.folio
            FROM TransaccionEntity t
            JOIN t.instrumento i
            JOIN t.custodio c
            JOIN t.tipoMovimiento tm
            WHERE t.empresa.id = :empresaId
              AND t.paraRevision = true
            ORDER BY t.fechaTransaccion DESC
            """, Object[].class)
                .setParameter("empresaId", empresaId)
                .setMaxResults(100)
                .getResultList();
        
        // Transacciones pendientes por mucho tiempo
        LocalDate fechaLimite = LocalDate.now().minusDays(30);
        List<Object[]> transaccionesPendientesAntiguas = entityManager.createQuery("""
            SELECT 
                t.id,
                t.fechaTransaccion,
                i.instrumentoNemo,
                t.cuenta,
                c.nombre as custodioNombre,
                t.cantidad,
                t.precio,
                tm.tipoMovimiento
            FROM TransaccionEntity t
            JOIN t.instrumento i
            JOIN t.custodio c
            JOIN t.tipoMovimiento tm
            WHERE t.empresa.id = :empresaId
              AND t.costeado = false
              AND t.paraRevision = false
              AND t.ignorarEnCosteo = false
              AND t.fechaTransaccion <= :fechaLimite
              AND tm.movimientoContable.tipoContable <> :noCostear
            ORDER BY t.fechaTransaccion ASC
            """, Object[].class)
                .setParameter("empresaId", empresaId)
                .setParameter("fechaLimite", fechaLimite)
                .setParameter("noCostear", TipoEnumsCosteo.NO_COSTEAR)
                .setMaxResults(50)
                .getResultList();
        
        return Map.of(
                "fechaGeneracion", LocalDate.now(),
                "empresaId", empresaId,
                "transaccionesParaRevision", transaccionesRevision.stream()
                        .map(row -> {
                            Map<String, Object> map = new HashMap<>();
                            map.put("transaccionId", row[0]);
                            map.put("fecha", ((LocalDate) row[1]).format(DATE_FORMATTER));
                            map.put("instrumentoNemo", row[2]);
                            map.put("cuenta", row[3]);
                            map.put("custodio", row[4]);
                            map.put("cantidad", row[5]);
                            map.put("precio", row[6]);
                            map.put("tipoMovimiento", row[7]);
                            map.put("glosa", row[8] != null ? row[8] : "");
                            map.put("folio", row[9] != null ? row[9] : "");
                            return map;
                        })
                        .collect(Collectors.toList()),
                "transaccionesPendientesAntiguas", transaccionesPendientesAntiguas.stream()
                        .map(row -> {
                            Map<String, Object> map = new HashMap<>();
                            map.put("transaccionId", row[0]);
                            map.put("fecha", ((LocalDate) row[1]).format(DATE_FORMATTER));
                            map.put("instrumentoNemo", row[2]);
                            map.put("cuenta", row[3]);
                            map.put("custodio", row[4]);
                            map.put("cantidad", row[5]);
                            map.put("precio", row[6]);
                            map.put("tipoMovimiento", row[7]);
                            map.put("diasPendiente", java.time.temporal.ChronoUnit.DAYS.between((LocalDate) row[1], LocalDate.now()));
                            return map;
                        })
                        .collect(Collectors.toList()),
                "resumen", Map.of(
                        "totalParaRevision", transaccionesRevision.size(),
                        "totalPendientesAntiguas", transaccionesPendientesAntiguas.size()
                )
        );
    }

    // ===== MÉTODOS AUXILIARES =====

    private BigDecimal calcularPorcentaje(BigDecimal parte, BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return parte.divide(total, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
    }

    private BigDecimal calcularPorcentaje(Long parte, Long total) {
        return calcularPorcentaje(new BigDecimal(parte), new BigDecimal(total));
    }

    private Map<String, Object> generarResumenInventario(List<Map<String, Object>> items) {
        int totalItems = items.size();
        BigDecimal valorTotal = items.stream()
                .map(item -> (BigDecimal) item.get("costoTotal"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Map<String, Long> itemsPorInstrumento = items.stream()
                .collect(Collectors.groupingBy(
                        item -> (String) item.get("instrumentoNemo"),
                        Collectors.counting()
                ));
        
        return Map.of(
                "totalItems", totalItems,
                "valorTotal", valorTotal,
                "instrumentosDistintos", itemsPorInstrumento.size(),
                "itemsPorInstrumento", itemsPorInstrumento
        );
    }

    private Map<TipoEnumsCosteo, Map<String, Object>> generarResumenPorTipo(List<Object[]> movimientos) {
        Map<TipoEnumsCosteo, List<Object[]>> movimientosPorTipo = movimientos.stream()
                .collect(Collectors.groupingBy(row -> (TipoEnumsCosteo) row[4]));
        
        return movimientosPorTipo.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            List<Object[]> movs = entry.getValue();
                            BigDecimal cantidadTotal = movs.stream()
                                    .map(row -> (BigDecimal) row[5])
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                            BigDecimal valorTotal = movs.stream()
                                    .map(row -> (BigDecimal) row[7])
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                            
                            return Map.of(
                                    "cantidad", movs.size(),
                                    "cantidadTotal", cantidadTotal,
                                    "valorTotal", valorTotal
                            );
                        }
                ));
    }
}