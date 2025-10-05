package com.portafolio.ui.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de UI para generación de reportes.
 * Proporciona datos para exportación y visualización de reportes complejos.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportesUiService {

    private final EntityManager entityManager;

    // ===== REPORTE DE MOVIMIENTOS =====

    /**
     * Genera reporte de movimientos detallado por período.
     * Incluye todas las transacciones con su información completa.
     */
    public List<ReporteMovimientoDto> generarReporteMovimientos(
            Long empresaId,
            LocalDate fechaDesde,
            LocalDate fechaHasta) {

        log.debug("Generando reporte de movimientos para empresa {} entre {} y {}", 
                empresaId, fechaDesde, fechaHasta);

        List<Object[]> resultados = entityManager.createQuery("""
            SELECT 
                t.fecha,
                t.fechaLiquidacion,
                e.nombre,
                c.nombre,
                t.cuenta,
                i.instrumentoNemo,
                i.instrumentoNombre,
                p.nombre,
                tm.tipoMovimiento,
                mc.tipoContable,
                t.cantidad,
                t.precio,
                t.monto,
                t.costeado,
                t.paraRevision,
                t.numeroOperacion
            FROM TransaccionEntity t
            JOIN t.empresa e
            JOIN t.custodio c
            JOIN t.instrumento i
            JOIN i.producto p
            JOIN t.tipoMovimiento tm
            WHERE t.empresa.id = :empresaId
              AND t.fecha BETWEEN :fechaDesde AND :fechaHasta
            ORDER BY t.fecha ASC, t.id ASC
            """, Object[].class)
                .setParameter("empresaId", empresaId)
                .setParameter("fechaDesde", fechaDesde)
                .setParameter("fechaHasta", fechaHasta)
                .getResultList();

        return resultados.stream()
                .map(this::mapearReporteMovimiento)
                .collect(Collectors.toList());
    }

    // ===== REPORTE DE KARDEX =====

    /**
     * Genera reporte de kardex con historial completo de movimientos y saldos.
     */
    public List<ReporteKardexDto> generarReporteKardex(
            Long empresaId,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            String instrumentoNemo) {

        log.debug("Generando reporte kardex para instrumento {} entre {} y {}", 
                instrumentoNemo, fechaDesde, fechaHasta);

        List<Object[]> resultados = entityManager.createQuery("""
            SELECT 
                k.fecha,
                i.instrumentoNemo,
                i.nombre,
                c.nombre,
                k.cuenta,
                k.tipoMovimiento,
                k.cantidadEntrada,
                k.costoEntrada,
                k.cantidadSalida,
                k.costoSalida,
                k.saldoCantidad,
                k.costoTotal,
                k.costoPromedio
            FROM SaldoKardexHistoricoEntity k
            JOIN k.instrumento i
            JOIN k.custodio c
            WHERE k.empresa.id = :empresaId
              AND k.fecha BETWEEN :fechaDesde AND :fechaHasta
              AND (:instrumentoNemo IS NULL OR i.instrumentoNemo = :instrumentoNemo)
            ORDER BY k.fecha ASC, k.id ASC
            """, Object[].class)
                .setParameter("empresaId", empresaId)
                .setParameter("fechaDesde", fechaDesde)
                .setParameter("fechaHasta", fechaHasta)
                .setParameter("instrumentoNemo", instrumentoNemo)
                .getResultList();

        return resultados.stream()
                .map(this::mapearReporteKardex)
                .collect(Collectors.toList());
    }

    // ===== REPORTE DE UTILIDADES REALIZADAS =====

    /**
     * Genera reporte de utilidades/pérdidas realizadas por ventas.
     */
    public List<ReporteUtilidadRealizadaDto> generarReporteUtilidadesRealizadas(
            Long empresaId,
            LocalDate fechaDesde,
            LocalDate fechaHasta) {

        log.debug("Generando reporte de utilidades realizadas para empresa {} entre {} y {}", 
                empresaId, fechaDesde, fechaHasta);

        List<Object[]> resultados = entityManager.createQuery("""
            SELECT 
                r.fecha,
                i.instrumentoNemo,
                i.nombre,
                c.nombre,
                r.cuenta,
                r.cantidad,
                r.costoPromedio,
                r.precioVenta,
                r.costoTotal,
                r.valorVenta,
                r.utilidad,
                r.porcentajeUtilidad
            FROM ResultadoEntity r
            JOIN r.instrumento i
            JOIN r.custodio c
            WHERE r.empresa.id = :empresaId
              AND r.fecha BETWEEN :fechaDesde AND :fechaHasta
            ORDER BY r.fecha ASC
            """, Object[].class)
                .setParameter("empresaId", empresaId)
                .setParameter("fechaDesde", fechaDesde)
                .setParameter("fechaHasta", fechaHasta)
                .getResultList();

        return resultados.stream()
                .map(this::mapearReporteUtilidad)
                .collect(Collectors.toList());
    }

    // ===== REPORTE DE EVOLUCIÓN DE PORTAFOLIO =====

    /**
     * Genera reporte de evolución del portafolio día a día.
     */
    public List<ReporteEvolucionDto> generarReporteEvolucion(
            Long empresaId,
            LocalDate fechaDesde,
            LocalDate fechaHasta) {

        log.debug("Generando reporte de evolución para empresa {} entre {} y {}", 
                empresaId, fechaDesde, fechaHasta);

        List<Object[]> resultados = entityManager.createQuery("""
            SELECT 
                sd.fecha,
                COUNT(DISTINCT sd.instrumento.id),
                COUNT(DISTINCT sd.cuenta),
                COALESCE(SUM(sd.saldoCantidad), 0),
                COALESCE(SUM(sd.saldoValor), 0)
            FROM SaldosDiariosEntity sd
            WHERE sd.empresa.id = :empresaId
              AND sd.fecha BETWEEN :fechaDesde AND :fechaHasta
              AND sd.saldoCantidad > 0
            GROUP BY sd.fecha
            ORDER BY sd.fecha ASC
            """, Object[].class)
                .setParameter("empresaId", empresaId)
                .setParameter("fechaDesde", fechaDesde)
                .setParameter("fechaHasta", fechaHasta)
                .getResultList();

        return resultados.stream()
                .map(this::mapearReporteEvolucion)
                .collect(Collectors.toList());
    }

    // ===== REPORTE DE CONCENTRACIÓN =====

    /**
     * Genera reporte de concentración del portafolio.
     * Muestra el % de concentración por instrumento.
     */
    public List<ReporteConcentracionDto> generarReporteConcentracion(Long empresaId) {
        log.debug("Generando reporte de concentración para empresa {}", empresaId);

        // Primero obtenemos el valor total
        BigDecimal valorTotal = entityManager.createQuery("""
            SELECT COALESCE(SUM(s.costoTotal), 0)
            FROM SaldoKardexEntity s
            WHERE s.empresa.id = :empresaId
              AND s.saldoCantidad > 0
            """, BigDecimal.class)
                .setParameter("empresaId", empresaId)
                .getSingleResult();

        if (valorTotal.compareTo(BigDecimal.ZERO) == 0) {
            return List.of();
        }

        List<Object[]> resultados = entityManager.createQuery("""
            SELECT 
                i.instrumentoNemo,
                i.nombre,
                p.nombre,
                COALESCE(SUM(s.saldoCantidad), 0),
                COALESCE(SUM(s.costoTotal), 0),
                COALESCE(AVG(s.costoPromedio), 0)
            FROM SaldoKardexEntity s
            JOIN s.instrumento i
            JOIN i.producto p
            WHERE s.empresa.id = :empresaId
              AND s.saldoCantidad > 0
            GROUP BY i.instrumentoNemo, i.nombre, p.nombre
            ORDER BY SUM(s.costoTotal) DESC
            """, Object[].class)
                .setParameter("empresaId", empresaId)
                .getResultList();

        return resultados.stream()
                .map(row -> mapearReporteConcentracion(row, valorTotal))
                .collect(Collectors.toList());
    }

    // ===== REPORTE DE RENTABILIDAD POR INSTRUMENTO =====

    /**
     * Genera reporte de rentabilidad por instrumento.
     * Compara costo vs valor de mercado actual.
     */
    public List<ReporteRentabilidadDto> generarReporteRentabilidad(
            Long empresaId, 
            LocalDate fechaValorizacion) {

        log.debug("Generando reporte de rentabilidad para empresa {} a fecha {}", 
                empresaId, fechaValorizacion);

        List<Object[]> saldos = entityManager.createQuery("""
            SELECT 
                i.id,
                i.instrumentoNemo,
                i.nombre,
                COALESCE(SUM(s.saldoCantidad), 0),
                COALESCE(SUM(s.costoTotal), 0),
                COALESCE(AVG(s.costoPromedio), 0)
            FROM SaldoKardexEntity s
            JOIN s.instrumento i
            WHERE s.empresa.id = :empresaId
              AND s.saldoCantidad > 0
            GROUP BY i.id, i.instrumentoNemo, i.nombre
            """, Object[].class)
                .setParameter("empresaId", empresaId)
                .getResultList();

        // Obtener precios de mercado
        List<Long> instrumentosIds = saldos.stream()
                .map(row -> (Long) row[0])
                .collect(Collectors.toList());

        if (instrumentosIds.isEmpty()) {
            return List.of();
        }

        List<Object[]> precios = entityManager.createQuery("""
            SELECT 
                p.instrumento.id,
                p.precio
            FROM PrecioEntity p
            WHERE p.instrumento.id IN :instrumentosIds
              AND p.fecha = (
                  SELECT MAX(p2.fecha)
                  FROM PrecioEntity p2
                  WHERE p2.instrumento.id = p.instrumento.id
                    AND p2.fecha <= :fechaValorizacion
              )
            """, Object[].class)
                .setParameter("instrumentosIds", instrumentosIds)
                .setParameter("fechaValorizacion", fechaValorizacion)
                .getResultList();

        var precioMap = precios.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (BigDecimal) row[1]
                ));

        return saldos.stream()
                .map(row -> mapearReporteRentabilidad(row, precioMap, fechaValorizacion))
                .collect(Collectors.toList());
    }

    // ===== MÉTODOS PRIVADOS DE MAPEO =====

    private ReporteMovimientoDto mapearReporteMovimiento(Object[] row) {
        return ReporteMovimientoDto.builder()
                .fecha((LocalDate) row[0])
                .fechaLiquidacion((LocalDate) row[1])
                .empresa((String) row[2])
                .custodio((String) row[3])
                .cuenta((String) row[4])
                .instrumentoNemo((String) row[5])
                .instrumentoNombre((String) row[6])
                .producto((String) row[7])
                .tipoInstrumento("N/A") // No disponible
                .tipoMovimiento((String) row[8])
                .signo((String) row[9])
                .cantidad((BigDecimal) row[10])
                .precio((BigDecimal) row[11])
                .monto((BigDecimal) row[12])
                .costeado((Boolean) row[13])
                .paraRevision((Boolean) row[14])
                .numeroOperacion((String) row[15])
                .build();
    }

    private ReporteKardexDto mapearReporteKardex(Object[] row) {
        return ReporteKardexDto.builder()
                .fecha((LocalDate) row[0])
                .instrumentoNemo((String) row[1])
                .instrumentoNombre((String) row[2])
                .custodio((String) row[3])
                .cuenta((String) row[4])
                .tipoMovimiento((String) row[5])
                .cantidadEntrada((BigDecimal) row[6])
                .costoEntrada((BigDecimal) row[7])
                .cantidadSalida((BigDecimal) row[8])
                .costoSalida((BigDecimal) row[9])
                .saldoCantidad((BigDecimal) row[10])
                .costoTotal((BigDecimal) row[11])
                .costoPromedio((BigDecimal) row[12])
                .build();
    }

    private ReporteUtilidadRealizadaDto mapearReporteUtilidad(Object[] row) {
        return ReporteUtilidadRealizadaDto.builder()
                .fecha((LocalDate) row[0])
                .instrumentoNemo((String) row[1])
                .instrumentoNombre((String) row[2])
                .custodio((String) row[3])
                .cuenta((String) row[4])
                .cantidad((BigDecimal) row[5])
                .costoPromedio((BigDecimal) row[6])
                .precioVenta((BigDecimal) row[7])
                .costoTotal((BigDecimal) row[8])
                .valorVenta((BigDecimal) row[9])
                .utilidad((BigDecimal) row[10])
                .porcentajeUtilidad((BigDecimal) row[11])
                .build();
    }

    private ReporteEvolucionDto mapearReporteEvolucion(Object[] row) {
        return ReporteEvolucionDto.builder()
                .fecha((LocalDate) row[0])
                .totalInstrumentos(((Long) row[1]).intValue())
                .totalCuentas(((Long) row[2]).intValue())
                .cantidadTotal((BigDecimal) row[3])
                .valorTotal((BigDecimal) row[4])
                .build();
    }

    private ReporteConcentracionDto mapearReporteConcentracion(Object[] row, BigDecimal valorTotal) {
        BigDecimal valor = (BigDecimal) row[4];
        BigDecimal porcentaje = valor.divide(valorTotal, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));

        return ReporteConcentracionDto.builder()
                .instrumentoNemo((String) row[0])
                .instrumentoNombre((String) row[1])
                .producto((String) row[2])
                .tipoInstrumento("N/A") // No disponible en el modelo
                .cantidadTotal((BigDecimal) row[3])
                .valorTotal(valor)
                .costoPromedio((BigDecimal) row[5])
                .porcentajeConcentracion(porcentaje)
                .build();
    }

    private ReporteRentabilidadDto mapearReporteRentabilidad(
            Object[] row, 
            java.util.Map<Long, BigDecimal> precioMap,
            LocalDate fechaValorizacion) {

        Long instrumentoId = (Long) row[0];
        BigDecimal cantidad = (BigDecimal) row[3];
        BigDecimal costoTotal = (BigDecimal) row[4];
        BigDecimal costoPromedio = (BigDecimal) row[5];

        BigDecimal precioMercado = precioMap.getOrDefault(instrumentoId, BigDecimal.ZERO);
        BigDecimal valorMercado = precioMercado.multiply(cantidad);
        BigDecimal rentabilidad = valorMercado.subtract(costoTotal);
        BigDecimal porcentajeRentabilidad = costoTotal.compareTo(BigDecimal.ZERO) > 0
                ? rentabilidad.divide(costoTotal, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;

        return ReporteRentabilidadDto.builder()
                .instrumentoNemo((String) row[1])
                .instrumentoNombre((String) row[2])
                .cantidad(cantidad)
                .costoPromedio(costoPromedio)
                .costoTotal(costoTotal)
                .precioMercado(precioMercado)
                .valorMercado(valorMercado)
                .rentabilidad(rentabilidad)
                .porcentajeRentabilidad(porcentajeRentabilidad)
                .fechaValorizacion(fechaValorizacion)
                .build();
    }

    // ===== DTOs INTERNOS =====

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReporteMovimientoDto {
        private LocalDate fecha;
        private LocalDate fechaLiquidacion;
        private String empresa;
        private String custodio;
        private String cuenta;
        private String instrumentoNemo;
        private String instrumentoNombre;
        private String producto;
        private String tipoInstrumento;
        private String tipoMovimiento;
        private String signo;
        private BigDecimal cantidad;
        private BigDecimal precio;
        private BigDecimal monto;
        private Boolean costeado;
        private Boolean paraRevision;
        private String numeroOperacion;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReporteKardexDto {
        private LocalDate fecha;
        private String instrumentoNemo;
        private String instrumentoNombre;
        private String custodio;
        private String cuenta;
        private String tipoMovimiento;
        private BigDecimal cantidadEntrada;
        private BigDecimal costoEntrada;
        private BigDecimal cantidadSalida;
        private BigDecimal costoSalida;
        private BigDecimal saldoCantidad;
        private BigDecimal costoTotal;
        private BigDecimal costoPromedio;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReporteUtilidadRealizadaDto {
        private LocalDate fecha;
        private String instrumentoNemo;
        private String instrumentoNombre;
        private String custodio;
        private String cuenta;
        private BigDecimal cantidad;
        private BigDecimal costoPromedio;
        private BigDecimal precioVenta;
        private BigDecimal costoTotal;
        private BigDecimal valorVenta;
        private BigDecimal utilidad;
        private BigDecimal porcentajeUtilidad;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReporteEvolucionDto {
        private LocalDate fecha;
        private Integer totalInstrumentos;
        private Integer totalCuentas;
        private BigDecimal cantidadTotal;
        private BigDecimal valorTotal;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReporteConcentracionDto {
        private String instrumentoNemo;
        private String instrumentoNombre;
        private String producto;
        private String tipoInstrumento;
        private BigDecimal cantidadTotal;
        private BigDecimal valorTotal;
        private BigDecimal costoPromedio;
        private BigDecimal porcentajeConcentracion;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReporteRentabilidadDto {
        private String instrumentoNemo;
        private String instrumentoNombre;
        private BigDecimal cantidad;
        private BigDecimal costoPromedio;
        private BigDecimal costoTotal;
        private BigDecimal precioMercado;
        private BigDecimal valorMercado;
        private BigDecimal rentabilidad;
        private BigDecimal porcentajeRentabilidad;
        private LocalDate fechaValorizacion;
    }
}