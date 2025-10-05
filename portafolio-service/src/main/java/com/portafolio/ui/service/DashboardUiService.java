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
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio de UI para el dashboard principal.
 * Proporciona KPIs, métricas y resúmenes consolidados.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardUiService {

    private final EntityManager entityManager;
    private final ResumenSaldoUiService resumenSaldoService;
    private final ResumenValorizadoUiService resumenValorizadoService;
    private final ResumenTransaccionesUiService resumenTransaccionesService;

    /**
     * Obtiene el dashboard completo con todos los KPIs principales.
     */
    public DashboardCompletoDto obtenerDashboardCompleto(Long empresaId, LocalDate fechaReferencia) {
        log.debug("Generando dashboard completo para empresa {} a fecha {}", empresaId, fechaReferencia);

        return DashboardCompletoDto.builder()
                .fechaReferencia(fechaReferencia)
                .kpisInventario(obtenerKpisInventario(empresaId))
                .kpisValorizacion(obtenerKpisValorizacion(empresaId, fechaReferencia))
                .kpisTransacciones(obtenerKpisTransacciones(empresaId, fechaReferencia))
                .distribucionPorInstrumento(obtenerDistribucionPorInstrumento(empresaId))
                .distribucionPorCustodio(obtenerDistribucionPorCustodio(empresaId))
                .evolucionSaldos(obtenerEvolucionSaldos(empresaId, fechaReferencia))
                .alertas(obtenerAlertas(empresaId))
                .build();
    }

    /**
     * Obtiene KPIs del inventario actual.
     */
    public KpisInventarioDto obtenerKpisInventario(Long empresaId) {
        log.debug("Calculando KPIs de inventario para empresa {}", empresaId);

        Object[] resultado = entityManager.createQuery("""
            SELECT 
                COUNT(DISTINCT s.instrumento.id),
                COUNT(DISTINCT s.custodio.id),
                COUNT(DISTINCT s.cuenta),
                COALESCE(SUM(s.saldoCantidad), 0),
                COALESCE(SUM(s.costoTotal), 0),
                COALESCE(AVG(s.costoPromedio), 0)
            FROM SaldoKardexEntity s
            WHERE s.empresa.id = :empresaId
              AND s.saldoCantidad > 0
            """, Object[].class)
                .setParameter("empresaId", empresaId)
                .getSingleResult();

        return KpisInventarioDto.builder()
                .totalInstrumentos(((Long) resultado[0]).intValue())
                .totalCustodios(((Long) resultado[1]).intValue())
                .totalCuentas(((Long) resultado[2]).intValue())
                .cantidadTotalTitulos((BigDecimal) resultado[3])
                .valorTotalCosto((BigDecimal) resultado[4])
                .costoPromedioGlobal((BigDecimal) resultado[5])
                .build();
    }

    /**
     * Obtiene KPIs de valorización.
     */
    public KpisValorizacionDto obtenerKpisValorizacion(Long empresaId, LocalDate fechaValorizacion) {
        log.debug("Calculando KPIs de valorización para empresa {}", empresaId);

        var resultado = resumenValorizadoService.obtenerResultadoTotal(empresaId, fechaValorizacion);

        // Top gainers y losers
        var topGainers = resumenValorizadoService.obtenerTopGainers(empresaId, fechaValorizacion, 5);
        var topLosers = resumenValorizadoService.obtenerTopLosers(empresaId, fechaValorizacion, 5);

        return KpisValorizacionDto.builder()
                .fechaValorizacion(fechaValorizacion)
                .costoTotal(resultado.getCostoTotal())
                .valorMercadoTotal(resultado.getValorMercadoTotal())
                .utilidadPerdidaNoRealizada(resultado.getUtilidadPerdidaNoRealizada())
                .porcentajeVariacion(resultado.getPorcentajeVariacion())
                .topGainers(topGainers)
                .topLosers(topLosers)
                .build();
    }

    /**
     * Obtiene KPIs de transacciones del mes actual.
     */
    public KpisTransaccionesDto obtenerKpisTransacciones(Long empresaId, LocalDate fechaReferencia) {
        log.debug("Calculando KPIs de transacciones para empresa {}", empresaId);

        // Transacciones del mes actual
        LocalDate inicioMes = fechaReferencia.withDayOfMonth(1);
        LocalDate finMes = fechaReferencia.withDayOfMonth(fechaReferencia.lengthOfMonth());

        var estadisticasMes = resumenTransaccionesService.obtenerEstadisticas(
                empresaId, inicioMes, finMes);

        // Comparar con mes anterior
        LocalDate inicioMesAnterior = inicioMes.minusMonths(1);
        LocalDate finMesAnterior = inicioMes.minusDays(1);

        var estadisticasMesAnterior = resumenTransaccionesService.obtenerEstadisticas(
                empresaId, inicioMesAnterior, finMesAnterior);

        // Transacciones pendientes
        var noCosteadas = resumenTransaccionesService.obtenerTransaccionesNoCosteadas(empresaId);
        var paraRevision = resumenTransaccionesService.obtenerTransaccionesParaRevision(empresaId);

        return KpisTransaccionesDto.builder()
                .mesActual(estadisticasMes)
                .mesAnterior(estadisticasMesAnterior)
                .transaccionesNoCosteadas(noCosteadas.size())
                .transaccionesParaRevision(paraRevision.size())
                .build();
    }

    /**
     * Obtiene la distribución del portafolio por instrumento (top 10).
     */
    public List<DistribucionDto> obtenerDistribucionPorInstrumento(Long empresaId) {
        log.debug("Calculando distribución por instrumento para empresa {}", empresaId);

        // Obtener valor total para calcular porcentajes
        BigDecimal valorTotal = resumenSaldoService.obtenerValorTotalInventario(empresaId);

        if (valorTotal.compareTo(BigDecimal.ZERO) == 0) {
            return List.of();
        }

        List<Object[]> resultados = entityManager.createQuery("""
            SELECT 
                i.instrumentoNemo,
                i.nombre as instrumentoNombre,
                COALESCE(SUM(s.costoTotal), 0)
            FROM SaldoKardexEntity s
            JOIN s.instrumento i
            WHERE s.empresa.id = :empresaId
              AND s.saldoCantidad > 0
            GROUP BY i.instrumentoNemo, i.nombre
            ORDER BY SUM(s.costoTotal) DESC
            """, Object[].class)
                .setParameter("empresaId", empresaId)
                .setMaxResults(10)
                .getResultList();

        return resultados.stream()
                .map(row -> {
                    BigDecimal valor = (BigDecimal) row[2];
                    BigDecimal porcentaje = valor.divide(valorTotal, 4, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100"));

                    return DistribucionDto.builder()
                            .codigo((String) row[0])
                            .nombre((String) row[1])
                            .valor(valor)
                            .porcentaje(porcentaje)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Obtiene la distribución del portafolio por custodio.
     */
    public List<DistribucionDto> obtenerDistribucionPorCustodio(Long empresaId) {
        log.debug("Calculando distribución por custodio para empresa {}", empresaId);

        BigDecimal valorTotal = resumenSaldoService.obtenerValorTotalInventario(empresaId);

        if (valorTotal.compareTo(BigDecimal.ZERO) == 0) {
            return List.of();
        }

        List<Object[]> resultados = entityManager.createQuery("""
            SELECT 
                c.nombre as custodioNombre,
                COALESCE(SUM(s.costoTotal), 0)
            FROM SaldoKardexEntity s
            JOIN s.custodio c
            WHERE s.empresa.id = :empresaId
              AND s.saldoCantidad > 0
            GROUP BY c.nombre
            ORDER BY SUM(s.costoTotal) DESC
            """, Object[].class)
                .setParameter("empresaId", empresaId)
                .getResultList();

        return resultados.stream()
                .map(row -> {
                    BigDecimal valor = (BigDecimal) row[1];
                    BigDecimal porcentaje = valor.divide(valorTotal, 4, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100"));

                    return DistribucionDto.builder()
                            .codigo((String) row[0])
                            .nombre((String) row[0])
                            .valor(valor)
                            .porcentaje(porcentaje)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Obtiene la evolución de saldos de los últimos N meses.
     */
    public List<EvolucionSaldosDto> obtenerEvolucionSaldos(Long empresaId, LocalDate fechaReferencia) {
        log.debug("Calculando evolución de saldos para empresa {}", empresaId);

        int mesesHistorico = 12;
        List<EvolucionSaldosDto> evolucion = new java.util.ArrayList<>();

        for (int i = mesesHistorico - 1; i >= 0; i--) {
            LocalDate fechaMes = fechaReferencia.minusMonths(i).withDayOfMonth(1);
            LocalDate ultimoDiaMes = fechaMes.withDayOfMonth(fechaMes.lengthOfMonth());

            Object[] resultado = entityManager.createQuery("""
                SELECT 
                    COALESCE(SUM(sd.saldoValor), 0),
                    COUNT(DISTINCT sd.instrumento.id)
                FROM SaldosDiariosEntity sd
                WHERE sd.empresa.id = :empresaId
                  AND sd.fecha = :fecha
                  AND sd.saldoCantidad > 0
                """, Object[].class)
                    .setParameter("empresaId", empresaId)
                    .setParameter("fecha", ultimoDiaMes)
                    .getSingleResult();

            evolucion.add(EvolucionSaldosDto.builder()
                    .fecha(ultimoDiaMes)
                    .valorTotal((BigDecimal) resultado[0])
                    .totalInstrumentos(((Long) resultado[1]).intValue())
                    .build());
        }

        return evolucion;
    }

    /**
     * Obtiene alertas y notificaciones para el dashboard.
     */
    public AlertasDto obtenerAlertas(Long empresaId) {
        log.debug("Obteniendo alertas para empresa {}", empresaId);

        // Transacciones sin costear
        Long transaccionesNoCosteadas = entityManager.createQuery("""
            SELECT COUNT(t)
            FROM TransaccionEntity t
            WHERE t.empresa.id = :empresaId
              AND t.costeado = false
            """, Long.class)
                .setParameter("empresaId", empresaId)
                .getSingleResult();

        // Transacciones para revisión
        Long transaccionesParaRevision = entityManager.createQuery("""
            SELECT COUNT(t)
            FROM TransaccionEntity t
            WHERE t.empresa.id = :empresaId
              AND t.paraRevision = true
            """, Long.class)
                .setParameter("empresaId", empresaId)
                .getSingleResult();

        // Instrumentos sin precio actualizado (más de 30 días)
        LocalDate hace30Dias = LocalDate.now().minusDays(30);
        Long instrumentosSinPrecio = entityManager.createQuery("""
            SELECT COUNT(DISTINCT s.instrumento.id)
            FROM SaldoKardexEntity s
            LEFT JOIN PrecioEntity p ON p.instrumento.id = s.instrumento.id
                AND p.fecha = (
                    SELECT MAX(p2.fecha)
                    FROM PrecioEntity p2
                    WHERE p2.instrumento.id = s.instrumento.id
                )
            WHERE s.empresa.id = :empresaId
              AND s.saldoCantidad > 0
              AND (p.fecha IS NULL OR p.fecha < :fecha)
            """, Long.class)
                .setParameter("empresaId", empresaId)
                .setParameter("fecha", hace30Dias)
                .getSingleResult();

        return AlertasDto.builder()
                .transaccionesNoCosteadas(transaccionesNoCosteadas.intValue())
                .transaccionesParaRevision(transaccionesParaRevision.intValue())
                .instrumentosSinPrecioActualizado(instrumentosSinPrecio.intValue())
                .build();
    }

    // ===== DTOs INTERNOS =====

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DashboardCompletoDto {
        private LocalDate fechaReferencia;
        private KpisInventarioDto kpisInventario;
        private KpisValorizacionDto kpisValorizacion;
        private KpisTransaccionesDto kpisTransacciones;
        private List<DistribucionDto> distribucionPorInstrumento;
        private List<DistribucionDto> distribucionPorCustodio;
        private List<EvolucionSaldosDto> evolucionSaldos;
        private AlertasDto alertas;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class KpisInventarioDto {
        private Integer totalInstrumentos;
        private Integer totalCustodios;
        private Integer totalCuentas;
        private BigDecimal cantidadTotalTitulos;
        private BigDecimal valorTotalCosto;
        private BigDecimal costoPromedioGlobal;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class KpisValorizacionDto {
        private LocalDate fechaValorizacion;
        private BigDecimal costoTotal;
        private BigDecimal valorMercadoTotal;
        private BigDecimal utilidadPerdidaNoRealizada;
        private BigDecimal porcentajeVariacion;
        private List<ResumenValorizadoUiService.TopMoversDto> topGainers;
        private List<ResumenValorizadoUiService.TopMoversDto> topLosers;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class KpisTransaccionesDto {
        private ResumenTransaccionesUiService.EstadisticasTransaccionesDto mesActual;
        private ResumenTransaccionesUiService.EstadisticasTransaccionesDto mesAnterior;
        private Integer transaccionesNoCosteadas;
        private Integer transaccionesParaRevision;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DistribucionDto {
        private String codigo;
        private String nombre;
        private BigDecimal valor;
        private BigDecimal porcentaje;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class EvolucionSaldosDto {
        private LocalDate fecha;
        private BigDecimal valorTotal;
        private Integer totalInstrumentos;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AlertasDto {
        private Integer transaccionesNoCosteadas;
        private Integer transaccionesParaRevision;
        private Integer instrumentosSinPrecioActualizado;
    }
}