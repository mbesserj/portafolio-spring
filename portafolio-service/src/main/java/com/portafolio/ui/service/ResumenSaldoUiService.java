package com.portafolio.ui.service;

import com.portafolio.persistence.repositorio.SaldoKardexRepository;
import com.portafolio.persistence.repositorio.EmpresaRepository;
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
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Servicio de UI para resúmenes de saldos.
 * Proporciona consultas optimizadas SOLO para la interfaz de usuario.
 * NO contiene lógica de negocio, solo queries de lectura.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResumenSaldoUiService {

    private final EntityManager entityManager;
    private final SaldoKardexRepository saldoKardexRepository;
    private final EmpresaRepository empresaRepository;

    /**
     * Obtiene el resumen consolidado de saldos por empresa.
     * Retorna datos listos para mostrar en la UI.
     */
    public List<ResumenSaldoUiDto> obtenerResumenPorEmpresa(Long empresaId) {
        log.debug("Obteniendo resumen de saldos para empresa: {}", empresaId);

        // Query optimizada con proyección directa
        List<Object[]> resultados = entityManager.createQuery("""
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
            ORDER BY i.instrumentoNemo ASC, s.cuenta ASC
            """, Object[].class)
                .setParameter("empresaId", empresaId)
                .getResultList();

        return resultados.stream()
                .map(this::mapearResultadoADto)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene el resumen de saldos para una fecha histórica.
     */
    public List<ResumenSaldoUiDto> obtenerResumenPorFecha(Long empresaId, LocalDate fecha) {
        log.debug("Obteniendo resumen de saldos para empresa {} a fecha {}", empresaId, fecha);

        List<Object[]> resultados = entityManager.createQuery("""
            SELECT 
                i.instrumentoNemo,
                i.nombre as instrumentoNombre,
                c.nombre as custodioNombre,
                sd.cuenta,
                sd.saldoCantidad,
                sd.saldoValor
            FROM SaldosDiariosEntity sd
            JOIN sd.instrumento i
            JOIN sd.custodio c
            WHERE sd.empresa.id = :empresaId
              AND sd.fecha = :fecha
              AND sd.saldoCantidad > 0
            ORDER BY i.instrumentoNemo ASC, sd.cuenta ASC
            """, Object[].class)
                .setParameter("empresaId", empresaId)
                .setParameter("fecha", fecha)
                .getResultList();

        return resultados.stream()
                .map(row -> mapearSaldoDiarioADto(row, fecha))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene el valor total del inventario por empresa.
     */
    public BigDecimal obtenerValorTotalInventario(Long empresaId) {
        log.debug("Calculando valor total inventario para empresa: {}", empresaId);

        return entityManager.createQuery("""
            SELECT COALESCE(SUM(s.costoTotal), 0)
            FROM SaldoKardexEntity s
            WHERE s.empresa.id = :empresaId
              AND s.saldoCantidad > 0
            """, BigDecimal.class)
                .setParameter("empresaId", empresaId)
                .getSingleResult();
    }

    /**
     * Obtiene estadísticas resumidas de saldos para dashboard.
     */
    public EstadisticasSaldoDto obtenerEstadisticas(Long empresaId) {
        log.debug("Obteniendo estadísticas de saldos para empresa: {}", empresaId);

        Object[] resultado = entityManager.createQuery("""
            SELECT 
                COUNT(DISTINCT s.instrumento.id),
                COUNT(DISTINCT s.cuenta),
                COALESCE(SUM(s.saldoCantidad), 0),
                COALESCE(SUM(s.costoTotal), 0)
            FROM SaldoKardexEntity s
            WHERE s.empresa.id = :empresaId
              AND s.saldoCantidad > 0
            """, Object[].class)
                .setParameter("empresaId", empresaId)
                .getSingleResult();

        return EstadisticasSaldoDto.builder()
                .totalInstrumentos(((Long) resultado[0]).intValue())
                .totalCuentas(((Long) resultado[1]).intValue())
                .cantidadTotal((BigDecimal) resultado[2])
                .valorTotal((BigDecimal) resultado[3])
                .build();
    }

    /**
     * Obtiene el resumen agrupado por instrumento.
     */
    public List<ResumenPorInstrumentoDto> obtenerResumenPorInstrumento(Long empresaId) {
        log.debug("Obteniendo resumen por instrumento para empresa: {}", empresaId);

        List<Object[]> resultados = entityManager.createQuery("""
            SELECT 
                i.instrumentoNemo,
                i.nombre as instrumentoNombre,
                COUNT(DISTINCT s.cuenta),
                COALESCE(SUM(s.saldoCantidad), 0),
                COALESCE(SUM(s.costoTotal), 0)
            FROM SaldoKardexEntity s
            JOIN s.instrumento i
            WHERE s.empresa.id = :empresaId
              AND s.saldoCantidad > 0
            GROUP BY i.instrumentoNemo, i.nombre
            ORDER BY SUM(s.costoTotal) DESC
            """, Object[].class)
                .setParameter("empresaId", empresaId)
                .getResultList();

        return resultados.stream()
                .map(row -> ResumenPorInstrumentoDto.builder()
                        .instrumentoNemo((String) row[0])
                        .instrumentoNombre((String) row[1])
                        .totalCuentas(((Long) row[2]).intValue())
                        .cantidadTotal((BigDecimal) row[3])
                        .valorTotal((BigDecimal) row[4])
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Obtiene el resumen agrupado por custodio.
     */
    public List<ResumenPorCustodioDto> obtenerResumenPorCustodio(Long empresaId) {
        log.debug("Obteniendo resumen por custodio para empresa: {}", empresaId);

        List<Object[]> resultados = entityManager.createQuery("""
            SELECT 
                c.nombre as custodioNombre,
                COUNT(DISTINCT s.instrumento.id),
                COUNT(DISTINCT s.cuenta),
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
                .map(row -> ResumenPorCustodioDto.builder()
                        .custodioNombre((String) row[0])
                        .totalInstrumentos(((Long) row[1]).intValue())
                        .totalCuentas(((Long) row[2]).intValue())
                        .valorTotal((BigDecimal) row[3])
                        .build())
                .collect(Collectors.toList());
    }

    // ===== MÉTODOS PRIVADOS DE MAPEO =====

    private ResumenSaldoUiDto mapearResultadoADto(Object[] row) {
        return ResumenSaldoUiDto.builder()
                .instrumentoNemo((String) row[0])
                .instrumentoNombre((String) row[1])
                .custodioNombre((String) row[2])
                .cuenta((String) row[3])
                .cantidad((BigDecimal) row[4])
                .valorTotal((BigDecimal) row[5])
                .costoPromedio((BigDecimal) row[6])
                .fechaActualizacion((LocalDate) row[7])
                .build();
    }

    private ResumenSaldoUiDto mapearSaldoDiarioADto(Object[] row, LocalDate fecha) {
        BigDecimal cantidad = (BigDecimal) row[4];
        BigDecimal valor = (BigDecimal) row[5];
        BigDecimal costoPromedio = cantidad.compareTo(BigDecimal.ZERO) > 0
                ? valor.divide(cantidad, 6, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return ResumenSaldoUiDto.builder()
                .instrumentoNemo((String) row[0])
                .instrumentoNombre((String) row[1])
                .custodioNombre((String) row[2])
                .cuenta((String) row[3])
                .cantidad(cantidad)
                .valorTotal(valor)
                .costoPromedio(costoPromedio)
                .fechaActualizacion(fecha)
                .build();
    }

    // ===== DTOs INTERNOS PARA LA UI =====

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ResumenSaldoUiDto {
        private String instrumentoNemo;
        private String instrumentoNombre;
        private String custodioNombre;
        private String cuenta;
        private BigDecimal cantidad;
        private BigDecimal valorTotal;
        private BigDecimal costoPromedio;
        private LocalDate fechaActualizacion;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class EstadisticasSaldoDto {
        private Integer totalInstrumentos;
        private Integer totalCuentas;
        private BigDecimal cantidadTotal;
        private BigDecimal valorTotal;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ResumenPorInstrumentoDto {
        private String instrumentoNemo;
        private String instrumentoNombre;
        private Integer totalCuentas;
        private BigDecimal cantidadTotal;
        private BigDecimal valorTotal;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ResumenPorCustodioDto {
        private String custodioNombre;
        private Integer totalInstrumentos;
        private Integer totalCuentas;
        private BigDecimal valorTotal;
    }
}