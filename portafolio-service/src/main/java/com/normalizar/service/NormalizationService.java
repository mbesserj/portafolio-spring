package com.normalizar.service;

import com.model.entities.CargaTransaccionEntity;
import com.model.entities.TransaccionEntity;
import com.normalizar.cache.EntityCacheManager;
import com.normalizar.processor.NormalizationProcessor;
import com.persistence.repositorio.CargaTransaccionRepository;
import com.persistence.repositorio.TransaccionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Servicio Spring para normalización de datos.
 * Migración del NormalizarDataService original.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NormalizationService {

    private final CargaTransaccionRepository cargaTransaccionRepository;
    private final TransaccionRepository transaccionRepository;
    private final EntityCacheManager cacheManager;
    private final NormalizationProcessor processor;

    /**
     * Ejecuta la normalización completa de registros no procesados.
     */
    @Transactional
    public NormalizationResult ejecutarNormalizacion() {
        return ejecutarNormalizacion(false);
    }

    /**
     * Ejecuta la normalización como carga inicial (saldos iniciales).
     */
    @Transactional
    public NormalizationResult ejecutarCargaInicial() {
        return ejecutarNormalizacion(true);
    }

    /**
     * Ejecuta la normalización con configuración específica.
     */
    @Transactional
    public NormalizationResult ejecutarNormalizacion(boolean esCargaInicial) {
        long startTime = System.currentTimeMillis();
        
        log.info("=== INICIANDO NORMALIZACIÓN {} ===", 
                esCargaInicial ? "CARGA INICIAL" : "REGULAR");

        try {
            // 1. Obtener registros pendientes de procesar
            List<CargaTransaccionEntity> registrosPendientes = 
                    cargaTransaccionRepository.findByProcesadoFalse();

            if (registrosPendientes.isEmpty()) {
                log.info("No hay registros pendientes de normalizar");
                return NormalizationResult.builder()
                        .exitosos(0)
                        .fallidos(0)
                        .duracionMs(System.currentTimeMillis() - startTime)
                        .mensaje("No hay registros pendientes")
                        .build();
            }

            log.info("Encontrados {} registros para normalizar", registrosPendientes.size());

            // 2. Procesar registros
            NormalizationResult resultado = processor.procesarRegistros(
                    registrosPendientes, esCargaInicial);

            // 3. Limpiar caché después del procesamiento
            cacheManager.limpiarCache();

            long duracion = System.currentTimeMillis() - startTime;
            resultado.setDuracionMs(duracion);

            log.info("=== NORMALIZACIÓN COMPLETADA: {} exitosos, {} fallidos en {}ms ===",
                    resultado.getExitosos(), resultado.getFallidos(), duracion);

            return resultado;

        } catch (Exception e) {
            log.error("Error durante la normalización", e);
            return NormalizationResult.builder()
                    .exitosos(0)
                    .fallidos(0)
                    .duracionMs(System.currentTimeMillis() - startTime)
                    .mensaje("Error durante normalización: " + e.getMessage())
                    .error(true)
                    .build();
        }
    }

    /**
     * Normaliza registros de una fecha específica.
     */
    @Transactional
    public NormalizationResult normalizarPorFecha(LocalDate fecha, boolean esCargaInicial) {
        log.info("Normalizando registros de fecha: {}", fecha);

        List<CargaTransaccionEntity> registrosFecha = 
                cargaTransaccionRepository.findByIdFechaTransaccionAndProcesadoFalse(fecha);

        if (registrosFecha.isEmpty()) {
            return NormalizationResult.builder()
                    .exitosos(0)
                    .fallidos(0)
                    .mensaje("No hay registros pendientes para la fecha: " + fecha)
                    .build();
        }

        return processor.procesarRegistros(registrosFecha, esCargaInicial);
    }

    /**
     * Obtiene estadísticas de normalización.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticas() {
        long totalRegistros = cargaTransaccionRepository.count();
        long procesados = cargaTransaccionRepository.countByProcesadoTrue();
        long pendientes = cargaTransaccionRepository.countByProcesadoFalse();

        Map<String, Integer> estadisticasCache = cacheManager.getEstadisticasCache();

        return Map.of(
                "totalRegistrosCarga", totalRegistros,
                "procesados", procesados,
                "pendientes", pendientes,
                "porcentajeProcesado", totalRegistros > 0 ? (procesados * 100.0 / totalRegistros) : 0,
                "cache", estadisticasCache
        );
    }

    /**
     * Reinicia el estado de procesamiento para registros específicos.
     */
    @Transactional
    public int reiniciarProcesamiento(LocalDate fechaDesde, LocalDate fechaHasta) {
        log.warn("Reiniciando procesamiento de registros desde {} hasta {}", fechaDesde, fechaHasta);

        int registrosReiniciados = cargaTransaccionRepository
                .resetProcesadoByFechaRange(fechaDesde, fechaHasta);

        log.info("Reiniciados {} registros para reprocesamiento", registrosReiniciados);
        return registrosReiniciados;
    }

    /**
     * Limpia registros procesados antiguos para liberar espacio.
     */
    @Transactional
    public int limpiarRegistrosAntiguos(LocalDate fechaHasta) {
        log.info("Limpiando registros procesados hasta: {}", fechaHasta);

        int registrosEliminados = cargaTransaccionRepository
                .deleteByProcesadoTrueAndIdFechaTransaccionBefore(fechaHasta);

        log.info("Eliminados {} registros procesados antiguos", registrosEliminados);
        return registrosEliminados;
    }

    /**
     * DTO para resultados de normalización.
     */
    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class NormalizationResult {
        private int exitosos;
        private int fallidos;
        private long duracionMs;
        private String mensaje;
        private boolean error;
        
        /**
         * Calcula el porcentaje de éxito.
         */
        public double getPorcentajeExito() {
            int total = exitosos + fallidos;
            return total > 0 ? (exitosos * 100.0 / total) : 0.0;
        }
        
        /**
         * Verifica si el proceso fue exitoso.
         */
        public boolean isExitoso() {
            return !error && fallidos == 0 && exitosos > 0;
        }
    }
}