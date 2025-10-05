package com.portafolio.ui.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de UI para consultas de posiciones actuales.
 * Muestra el detalle completo de cada posición con toda su información.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResumenPosicionesUiService {

    private final EntityManager entityManager;

    /**
     * Obtiene todas las posiciones actuales por empresa.
     * Una posición es un saldo con toda su información detallada.
     */
    public List<PosicionDto> obtenerPosicionesActuales(Long empresaId) {
        log.debug("Obteniendo posiciones actuales para empresa: {}", empresaId);

        List<Object[]> resultados = entityManager.createQuery("""
            SELECT 
                s.id,
                e.razonSocial,
                c.nombreCustodio,
                i.instrumentoNemo,
                i.instrumentoNombre,
                p.producto,
                s.cuenta,
                s.saldoCantidad,
                s.costoTotal,
                s.costoPromedio,
                s.fechaUltimaActualizacion
            FROM SaldoKardexEntity s
            JOIN s.empresa e
            JOIN s.custodio c
            JOIN s.instrumento i
            LEFT JOIN i.producto p
            WHERE s.empresa.id = :empresaId
              AND s.saldoCantidad > 0
            ORDER BY i.instrumentoNemo ASC, s.cuenta ASC
            """, Object[].class)
                .setParameter("empresaId", empresaId)
                .getResultList();

        return resultados.stream()
                .map(this::mapearPosicion)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene posiciones filtradas por custodio.
     */
    public List<PosicionDto> obtenerPosicionesPorCustodio(Long empresaId, Long custodioId) {
        log.debug("Obteniendo posiciones para empresa {} en custodio {}", empresaId, custodioId);

        List<Object[]> resultados = entityManager.createQuery("""
            SELECT 
                s.id,
                e.razonSocial,
                c.nombreCustodio,
                i.instrumentoNemo,
                i.instrumentoNombre,
                p.producto,
                s.cuenta,
                s.saldoCantidad,
                s.costoTotal,
                s.costoPromedio,
                s.fechaUltimaActualizacion
            FROM SaldoKardexEntity s
            JOIN s.empresa e
            JOIN s.custodio c
            JOIN s.instrumento i
            LEFT JOIN i.producto p
            WHERE s.empresa.id = :empresaId
              AND s.custodio.id = :custodioId
              AND s.saldoCantidad > 0
            ORDER BY i.instrumentoNemo ASC
            """, Object[].class)
                .setParameter("empresaId", empresaId)
                .setParameter("custodioId", custodioId)
                .getResultList();

        return resultados.stream()
                .map(this::mapearPosicion)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene una posición específica por ID.
     */
    public PosicionDetalleDto obtenerDetallePosicion(Long saldoKardexId) {
        log.debug("Obteniendo detalle de posición: {}", saldoKardexId);

        Object[] resultado = entityManager.createQuery("""
            SELECT 
                s.id,
                e.razonSocial,
                c.nombreCustodio,
                i.instrumentoNemo,
                i.instrumentoNombre,
                p.producto,
                s.cuenta,
                s.saldoCantidad,
                s.costoTotal,
                s.costoPromedio,
                s.fechaUltimaActualizacion,
                (SELECT COUNT(k) FROM KardexEntity k WHERE k.claveAgrupacion = s.claveAgrupacion),
                (SELECT MIN(k.fechaTransaccion) FROM KardexEntity k WHERE k.claveAgrupacion = s.claveAgrupacion),
                (SELECT MAX(k.fechaTransaccion) FROM KardexEntity k WHERE k.claveAgrupacion = s.claveAgrupacion)
            FROM SaldoKardexEntity s
            JOIN s.empresa e
            JOIN s.custodio c
            JOIN s.instrumento i
            LEFT JOIN i.producto p
            WHERE s.id = :saldoKardexId
            """, Object[].class)
                .setParameter("saldoKardexId", saldoKardexId)
                .getSingleResult();

        return mapearPosicionDetalle(resultado);
    }

    // ===== MÉTODOS PRIVADOS =====

    private PosicionDto mapearPosicion(Object[] row) {
        return PosicionDto.builder()
                .saldoKardexId((Long) row[0])
                .empresaNombre((String) row[1])
                .custodioNombre((String) row[2])
                .instrumentoNemo((String) row[3])
                .instrumentoNombre((String) row[4])
                .productoNombre((String) row[5])
                .cuenta((String) row[6])
                .cantidad((BigDecimal) row[7])
                .costoTotal((BigDecimal) row[8])
                .costoPromedio((BigDecimal) row[9])
                .fechaActualizacion((LocalDate) row[10])
                .build();
    }

    private PosicionDetalleDto mapearPosicionDetalle(Object[] row) {
        return PosicionDetalleDto.builder()
                .saldoKardexId((Long) row[0])
                .empresaNombre((String) row[1])
                .custodioNombre((String) row[2])
                .instrumentoNemo((String) row[3])
                .instrumentoNombre((String) row[4])
                .productoNombre((String) row[5])
                .cuenta((String) row[6])
                .cantidad((BigDecimal) row[7])
                .costoTotal((BigDecimal) row[8])
                .costoPromedio((BigDecimal) row[9])
                .fechaActualizacion((LocalDate) row[10])
                .totalMovimientos(((Long) row[11]).intValue())
                .fechaPrimerMovimiento((LocalDate) row[12])
                .fechaUltimoMovimiento((LocalDate) row[13])
                .build();
    }

    // ===== DTOs =====

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PosicionDto {
        private Long saldoKardexId;
        private String empresaNombre;
        private String custodioNombre;
        private String instrumentoNemo;
        private String instrumentoNombre;
        private String productoNombre;
        private String cuenta;
        private BigDecimal cantidad;
        private BigDecimal costoTotal;
        private BigDecimal costoPromedio;
        private LocalDate fechaActualizacion;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PosicionDetalleDto {
        private Long saldoKardexId;
        private String empresaNombre;
        private String custodioNombre;
        private String instrumentoNemo;
        private String instrumentoNombre;
        private String productoNombre;
        private String cuenta;
        private BigDecimal cantidad;
        private BigDecimal costoTotal;
        private BigDecimal costoPromedio;
        private LocalDate fechaActualizacion;
        private Integer totalMovimientos;
        private LocalDate fechaPrimerMovimiento;
        private LocalDate fechaUltimoMovimiento;
    }
}