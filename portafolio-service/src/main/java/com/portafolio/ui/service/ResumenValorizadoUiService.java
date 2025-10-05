package com.portafolio.ui.service;

import com.portafolio.persistence.repositorio.SaldoKardexRepository;
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
 * Servicio de UI para inventario valorizado.
 * Combina saldos con precios de mercado para calcular valorizaciones.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResumenValorizadoUiService {

    private final EntityManager entityManager;
    private final SaldoKardexRepository saldoKardexRepository;
    private final PrecioRepository precioRepository;

    /**
     * Obtiene el inventario valorizado completo por empresa.
     * Incluye costo, precio de mercado y utilidad/pérdida no realizada.
     */
    public List<InventarioValorizadoDto> obtenerInventarioValorizado(Long empresaId, LocalDate fechaValorizacion) {
        log.debug("Obteniendo inventario valorizado para empresa {} a fecha {}", empresaId, fechaValorizacion);

        // 1. Obtener saldos actuales
        List<Object[]> saldos = entityManager.createQuery("""
            SELECT 
                s.id,
                i.instrumentoNemo,
                i.nombre as instrumentoNombre,
                i.id as instrumentoId,
                c.nombre as custodioNombre,
                s.cuenta,
                s.saldoCantidad,
                s.costoTotal,
                s.costoPromedio,
                i.moneda.codigo as monedaCodigo
            FROM SaldoKardexEntity s
            JOIN s.instrumento i
            JOIN s.custodio c
            WHERE s.empresa.id = :empresaId
              AND s.saldoCantidad > 0
            ORDER BY i.instrumentoNemo ASC
            """, Object[].class)
                .setParameter("empresaId", empresaId)
                .getResultList();

        // 2. Obtener precios de mercado para los instrumentos
        List<Long> instrumentosIds = saldos.stream()
                .map(row -> (Long) row[3])
                .distinct()
                .collect(Collectors.toList());

        Map<Long, BigDecimal> preciosPorInstrumento = obtenerPreciosMercado(instrumentosIds, fechaValorizacion);

        // 3. Mapear y calcular valorizaciones
        return saldos.stream()
                .map(row -> calcularValorizacion(row, preciosPorInstrumento))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene resumen consolidado por instrumento.
     */
    public List<ResumenPorInstrumentoValorizadoDto> obtenerResumenPorInstrumento(
            Long empresaId, LocalDate fechaValorizacion) {
        
        log.debug("Obteniendo resumen valorizado por instrumento para empresa {}", empresaId);

        List<Object[]> resultados = entityManager.createQuery("""
            SELECT 
                i.id as instrumentoId,
                i.instrumentoNemo,
                i.nombre as instrumentoNombre,
                i.moneda.codigo as monedaCodigo,
                COUNT(DISTINCT s.cuenta),
                COALESCE(SUM(s.saldoCantidad), 0),
                COALESCE(SUM(s.costoTotal), 0),
                COALESCE(AVG(s.costoPromedio), 0)
            FROM SaldoKardexEntity s
            JOIN s.instrumento i
            WHERE s.empresa.id = :empresaId
              AND s.saldoCantidad > 0
            GROUP BY i.id, i.instrumentoNemo, i.nombre, i.moneda.codigo
            ORDER BY i.instrumentoNemo ASC
            """, Object[].class)
                .setParameter("empresaId", empresaId)
                .getResultList();

        // Obtener precios
        List<Long> instrumentosIds = resultados.stream()
                .map(row -> (Long) row[0])
                .collect(Collectors.toList());
        
        Map<Long, BigDecimal> preciosPorInstrumento = obtenerPreciosMercado(instrumentosIds, fechaValorizacion);

        return resultados.stream()
                .map(row -> calcularResumenInstrumento(row, preciosPorInstrumento))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene las utilidades/pérdidas no realizadas totales.
     */
    public ResultadoValorizacionDto obtenerResultadoTotal(Long empresaId, LocalDate fechaValorizacion) {
        log.debug("Calculando resultado total valorizado para empresa {}", empresaId);

        List<InventarioValorizadoDto> inventario = obtenerInventarioValorizado(empresaId, fechaValorizacion);

        BigDecimal costoTotal = inventario.stream()
                .map(InventarioValorizadoDto::getCostoTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal valorMercadoTotal = inventario.stream()
                .map(InventarioValorizadoDto::getValorMercado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal utilidadPerdida = valorMercadoTotal.subtract(costoTotal);
        BigDecimal porcentajeVariacion = costoTotal.compareTo(BigDecimal.ZERO) > 0
                ? utilidadPerdida.divide(costoTotal, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;

        return ResultadoValorizacionDto.builder()
                .fechaValorizacion(fechaValorizacion)
                .costoTotal(costoTotal)
                .valorMercadoTotal(valorMercadoTotal)
                .utilidadPerdidaNoRealizada(utilidadPerdida)
                .porcentajeVariacion(porcentajeVariacion)
                .totalInstrumentos((long) inventario.size())
                .build();
    }

    /**
     * Obtiene top gainers (instrumentos con mayor ganancia).
     */
    public List<TopMoversDto> obtenerTopGainers(Long empresaId, LocalDate fechaValorizacion, int limite) {
        log.debug("Obteniendo top {} gainers para empresa {}", limite, empresaId);

        List<InventarioValorizadoDto> inventario = obtenerInventarioValorizado(empresaId, fechaValorizacion);

        return inventario.stream()
                .filter(inv -> inv.getPorcentajeVariacion().compareTo(BigDecimal.ZERO) > 0)
                .sorted((a, b) -> b.getPorcentajeVariacion().compareTo(a.getPorcentajeVariacion()))
                .limit(limite)
                .map(this::mapearATopMover)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene top losers (instrumentos con mayor pérdida).
     */
    public List<TopMoversDto> obtenerTopLosers(Long empresaId, LocalDate fechaValorizacion, int limite) {
        log.debug("Obteniendo top {} losers para empresa {}", limite, empresaId);

        List<InventarioValorizadoDto> inventario = obtenerInventarioValorizado(empresaId, fechaValorizacion);

        return inventario.stream()
                .filter(inv -> inv.getPorcentajeVariacion().compareTo(BigDecimal.ZERO) < 0)
                .sorted((a, b) -> a.getPorcentajeVariacion().compareTo(b.getPorcentajeVariacion()))
                .limit(limite)
                .map(this::mapearATopMover)
                .collect(Collectors.toList());
    }

    // ===== MÉTODOS PRIVADOS =====

    /**
     * Obtiene los precios de mercado más recientes para los instrumentos dados.
     */
    private Map<Long, BigDecimal> obtenerPreciosMercado(List<Long> instrumentosIds, LocalDate fechaValorizacion) {
        if (instrumentosIds.isEmpty()) {
            return Map.of();
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

        return precios.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (BigDecimal) row[1]
                ));
    }

    /**
     * Calcula la valorización para un saldo individual.
     */
    private InventarioValorizadoDto calcularValorizacion(Object[] row, Map<Long, BigDecimal> preciosPorInstrumento) {
        Long instrumentoId = (Long) row[3];
        BigDecimal cantidad = (BigDecimal) row[6];
        BigDecimal costoTotal = (BigDecimal) row[7];
        BigDecimal costoPromedio = (BigDecimal) row[8];

        // Precio de mercado
        BigDecimal precioMercado = preciosPorInstrumento.getOrDefault(instrumentoId, BigDecimal.ZERO);
        BigDecimal valorMercado = precioMercado.multiply(cantidad);

        // Utilidad/Pérdida
        BigDecimal utilidadPerdida = valorMercado.subtract(costoTotal);
        BigDecimal porcentajeVariacion = costoTotal.compareTo(BigDecimal.ZERO) > 0
                ? utilidadPerdida.divide(costoTotal, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;

        return InventarioValorizadoDto.builder()
                .instrumentoNemo((String) row[1])
                .instrumentoNombre((String) row[2])
                .custodioNombre((String) row[4])
                .cuenta((String) row[5])
                .cantidad(cantidad)
                .costoPromedio(costoPromedio)
                .costoTotal(costoTotal)
                .precioMercado(precioMercado)
                .valorMercado(valorMercado)
                .utilidadPerdida(utilidadPerdida)
                .porcentajeVariacion(porcentajeVariacion)
                .moneda((String) row[9])
                .build();
    }

    /**
     * Calcula el resumen valorizado por instrumento.
     */
    private ResumenPorInstrumentoValorizadoDto calcularResumenInstrumento(
            Object[] row, Map<Long, BigDecimal> preciosPorInstrumento) {
        
        Long instrumentoId = (Long) row[0];
        BigDecimal cantidad = (BigDecimal) row[5];
        BigDecimal costoTotal = (BigDecimal) row[6];

        BigDecimal precioMercado = preciosPorInstrumento.getOrDefault(instrumentoId, BigDecimal.ZERO);
        BigDecimal valorMercado = precioMercado.multiply(cantidad);
        BigDecimal utilidadPerdida = valorMercado.subtract(costoTotal);
        BigDecimal porcentajeVariacion = costoTotal.compareTo(BigDecimal.ZERO) > 0
                ? utilidadPerdida.divide(costoTotal, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;

        return ResumenPorInstrumentoValorizadoDto.builder()
                .instrumentoNemo((String) row[1])
                .instrumentoNombre((String) row[2])
                .moneda((String) row[3])
                .totalCuentas(((Long) row[4]).intValue())
                .cantidadTotal(cantidad)
                .costoTotal(costoTotal)
                .precioMercado(precioMercado)
                .valorMercado(valorMercado)
                .utilidadPerdida(utilidadPerdida)
                .porcentajeVariacion(porcentajeVariacion)
                .build();
    }

    /**
     * Mapea inventario valorizado a top mover.
     */
    private TopMoversDto mapearATopMover(InventarioValorizadoDto inv) {
        return TopMoversDto.builder()
                .instrumentoNemo(inv.getInstrumentoNemo())
                .instrumentoNombre(inv.getInstrumentoNombre())
                .cantidad(inv.getCantidad())
                .costoTotal(inv.getCostoTotal())
                .valorMercado(inv.getValorMercado())
                .utilidadPerdida(inv.getUtilidadPerdida())
                .porcentajeVariacion(inv.getPorcentajeVariacion())
                .build();
    }

    // ===== DTOs INTERNOS =====

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class InventarioValorizadoDto {
        private String instrumentoNemo;
        private String instrumentoNombre;
        private String custodioNombre;
        private String cuenta;
        private BigDecimal cantidad;
        private BigDecimal costoPromedio;
        private BigDecimal costoTotal;
        private BigDecimal precioMercado;
        private BigDecimal valorMercado;
        private BigDecimal utilidadPerdida;
        private BigDecimal porcentajeVariacion;
        private String moneda;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ResumenPorInstrumentoValorizadoDto {
        private String instrumentoNemo;
        private String instrumentoNombre;
        private String moneda;
        private Integer totalCuentas;
        private BigDecimal cantidadTotal;
        private BigDecimal costoTotal;
        private BigDecimal precioMercado;
        private BigDecimal valorMercado;
        private BigDecimal utilidadPerdida;
        private BigDecimal porcentajeVariacion;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ResultadoValorizacionDto {
        private LocalDate fechaValorizacion;
        private BigDecimal costoTotal;
        private BigDecimal valorMercadoTotal;
        private BigDecimal utilidadPerdidaNoRealizada;
        private BigDecimal porcentajeVariacion;
        private Long totalInstrumentos;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TopMoversDto {
        private String instrumentoNemo;
        private String instrumentoNombre;
        private BigDecimal cantidad;
        private BigDecimal costoTotal;
        private BigDecimal valorMercado;
        private BigDecimal utilidadPerdida;
        private BigDecimal porcentajeVariacion;
    }
}