package com.costing.service;

import com.model.dto.AjustePropuestoDto;
import com.model.dto.CostingGroupDto;
import com.model.enums.TipoAjuste;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Servicio orquestador principal que coordina todas las operaciones de costeo.
 * Actúa como fachada para los diferentes servicios especializados y maneja
 * la lógica de negocio de alto nivel.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CostingOrchestratorService {

    // Servicios especializados
    private final CostingServiceImpl costingService;
    private final AjustesService ajustesService;
    private final KardexQueryService kardexQueryService;
    private final TransaccionManagementService transaccionManagementService;
    private final CostingReportsService reportsService;

    // ===== OPERACIONES PRINCIPALES DE COSTEO =====

    /**
     * Ejecuta el proceso completo de costeo para una empresa.
     */
    @Transactional
    public Map<String, Object> ejecutarCosteoCompleto(Long empresaId, LocalDate fechaCorte) {
        log.info("=== INICIANDO COSTEO COMPLETO para empresa {} hasta {} ===", empresaId, fechaCorte);
        
        try {
            LocalDate fechaProceso = fechaCorte != null ? fechaCorte : LocalDate.now();
            
            // 1. Obtener estadísticas iniciales
            Map<String, Long> estadisticasIniciales = transaccionManagementService
                    .obtenerEstadisticasTransacciones(empresaId);
            
            // 2. Ejecutar el costeo
            int gruposProcesados = costingService.procesarCosteo(fechaProceso);
            
            // 3. Obtener estadísticas finales
            Map<String, Long> estadisticasFinales = transaccionManagementService
                    .obtenerEstadisticasTransacciones(empresaId);
            
            // 4. Calcular métricas de procesamiento
            long transaccionesCosteadasNuevas = estadisticasFinales.getOrDefault("COSTEADAS", 0L) - 
                    estadisticasIniciales.getOrDefault("COSTEADAS", 0L);
            
            long transaccionesNuevasParaRevision = estadisticasFinales.getOrDefault("PARA_REVISION", 0L) - 
                    estadisticasIniciales.getOrDefault("PARA_REVISION", 0L);
            
            log.info("=== COSTEO COMPLETADO: {} grupos, {} transacciones costeadas, {} para revisión ===", 
                    gruposProcesados, transaccionesCosteadasNuevas, transaccionesNuevasParaRevision);
            
            return Map.of(
                    "success", true,
                    "fechaProceso", fechaProceso,
                    "fechaEjecucion", LocalDate.now(),
                    "gruposProcesados", gruposProcesados,
                    "transaccionesCosteadas", transaccionesCosteadasNuevas,
                    "transaccionesParaRevision", transaccionesNuevasParaRevision,
                    "estadisticasIniciales", estadisticasIniciales,
                    "estadisticasFinales", estadisticasFinales
            );
            
        } catch (Exception e) {
            log.error("Error en costeo completo para empresa {}: {}", empresaId, e.getMessage(), e);
            return Map.of(
                    "success", false,
                    "error", e.getMessage(),
                    "fechaEjecucion", LocalDate.now()
            );
        }
    }

    /**
     * Procesa un grupo específico con validaciones y manejo de errores.
     */
    @Transactional
    public Map<String, Object> procesarGrupoEspecifico(CostingGroupDto grupo, LocalDate fechaCorte) {
        log.info("Procesando grupo específico: {}", grupo.getDescripcionGrupo());
        
        try {
            // 1. Validar que el grupo esté completo
            if (!grupo.isCompleto()) {
                return Map.of(
                        "success", false,
                        "error", "El grupo no tiene todos los campos requeridos"
                );
            }
            
            // 2. Obtener estadísticas del grupo antes del proceso
            Map<String, Object> estadisticasAntes = transaccionManagementService
                    .obtenerResumenGrupo(grupo.getEmpresaId(), grupo.getCustodioId(), 
                            grupo.getInstrumentoId(), grupo.getCuenta());
            
            // 3. Procesar el grupo
            costingService.procesarGrupo(grupo, fechaCorte);
            
            // 4. Obtener estadísticas después del proceso
            Map<String, Object> estadisticasDespues = transaccionManagementService
                    .obtenerResumenGrupo(grupo.getEmpresaId(), grupo.getCustodioId(), 
                            grupo.getInstrumentoId(), grupo.getCuenta());
            
            // 5. Verificar consistencia del kardex
            Map<String, Object> consistencia = kardexQueryService
                    .verificarConsistenciaKardex(grupo.getEmpresaId(), grupo.getCustodioId(), 
                            grupo.getInstrumentoId(), grupo.getCuenta());
            
            log.info("Grupo procesado exitosamente: {}", grupo.getDescripcionGrupo());
            
            return Map.of(
                    "success", true,
                    "grupo", grupo,
                    "fechaProceso", fechaCorte != null ? fechaCorte : LocalDate.now(),
                    "estadisticasAntes", estadisticasAntes,
                    "estadisticasDespues", estadisticasDespues,
                    "consistenciaKardex", consistencia
            );
            
        } catch (Exception e) {
            log.error("Error procesando grupo {}: {}", grupo.getDescripcionGrupo(), e.getMessage(), e);
            return Map.of(
                    "success", false,
                    "grupo", grupo,
                    "error", e.getMessage()
            );
        }
    }

    // ===== GESTIÓN DE AJUSTES =====

    /**
     * Analiza y propone ajustes para transacciones problemáticas.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> analizarYProponerAjustes(Long empresaId) {
        log.info("Analizando transacciones problemáticas y proponiendo ajustes para empresa: {}", empresaId);
        
        try {
            // 1. Obtener transacciones para revisión
            var transaccionesRevision = transaccionManagementService
                    .obtenerTransaccionesParaRevision(empresaId);
            
            // 2. Generar propuestas de ajuste para cada transacción problemática
            List<Map<String, Object>> propuestasAjustes = transaccionesRevision.stream()
                    .limit(50) // Limitar para no sobrecargar
                    .map(tx -> {
                        try {
                            // Proponer ajuste de ingreso (más común)
                            AjustePropuestoDto ajusteIngreso = ajustesService
                                    .proponerAjusteManual(tx.getId(), TipoAjuste.INGRESO);
                            
                            // Proponer ajuste de egreso como alternativa
                            AjustePropuestoDto ajusteEgreso = ajustesService
                                    .proponerAjusteManual(tx.getId(), TipoAjuste.EGRESO);
                            
                            return Map.of(
                                    "transaccion", tx,
                                    "ajusteIngreso", ajusteIngreso,
                                    "ajusteEgreso", ajusteEgreso,
                                    "recomendacion", determinarRecomendacion(ajusteIngreso, ajusteEgreso)
                            );
                            
                        } catch (Exception e) {
                            log.warn("Error proponiendo ajustes para transacción {}: {}", tx.getId(), e.getMessage());
                            return Map.of(
                                    "transaccion", tx,
                                    "error", e.getMessage()
                            );
                        }
                    })
                    .toList();
            
            // 3. Estadísticas de propuestas
            long propuestasExitosas = propuestasAjustes.stream()
                    .mapToLong(p -> p.containsKey("error") ? 0 : 1)
                    .sum();
            
            return Map.of(
                    "success", true,
                    "empresaId", empresaId,
                    "fechaAnalisis", LocalDate.now(),
                    "totalTransaccionesRevision", transaccionesRevision.size(),
                    "propuestasGeneradas", propuestasExitosas,
                    "propuestas", propuestasAjustes
            );
            
        } catch (Exception e) {
            log.error("Error analizando ajustes para empresa {}: {}", empresaId, e.getMessage(), e);
            return Map.of(
                    "success", false,
                    "error", e.getMessage()
            );
        }
    }

    /**
     * Ejecuta ajustes en lote para múltiples transacciones.
     */
    @Transactional
    public Map<String, Object> ejecutarAjustesEnLote(List<Map<String, Object>> ajustesEjecutar) {
        log.info("Ejecutando {} ajustes en lote", ajustesEjecutar.size());
        
        int ajustesExitosos = 0;
        int ajustesFallidos = 0;
        List<Map<String, Object>> resultados = new ArrayList<>();
        
        for (Map<String, Object> ajusteRequest : ajustesEjecutar) {
            try {
                Long transaccionId = Long.valueOf(ajusteRequest.get("transaccionId").toString());
                TipoAjuste tipo = TipoAjuste.valueOf(ajusteRequest.get("tipo").toString());
                BigDecimal cantidad = new BigDecimal(ajusteRequest.get("cantidad").toString());
                BigDecimal precio = new BigDecimal(ajusteRequest.get("precio").toString());
                String observaciones = ajusteRequest.get("observaciones").toString();
                
                var ajusteCreado = ajustesService.crearAjusteManual(
                        transaccionId, tipo, cantidad, precio, observaciones);
                
                resultados.add(Map.of(
                        "transaccionId", transaccionId,
                        "ajusteId", ajusteCreado.getId(),
                        "success", true
                ));
                
                ajustesExitosos++;
                
            } catch (Exception e) {
                log.error("Error creando ajuste: {}", e.getMessage(), e);
                
                resultados.add(Map.of(
                        "transaccionId", ajusteRequest.get("transaccionId"),
                        "success", false,
                        "error", e.getMessage()
                ));
                
                ajustesFallidos++;
            }
        }
        
        log.info("Ajustes en lote completados: {} exitosos, {} fallidos", ajustesExitosos, ajustesFallidos);
        
        return Map.of(
                "success", ajustesFallidos == 0,
                "fechaEjecucion", LocalDate.now(),
                "totalProcesados", ajustesEjecutar.size(),
                "exitosos", ajustesExitosos,
                "fallidos", ajustesFallidos,
                "resultados", resultados
        );
    }

    // ===== CONSULTAS Y REPORTES =====

    /**
     * Obtiene un dashboard completo del estado de costeo para una empresa.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerDashboardCosteo(Long empresaId) {
        log.debug("Generando dashboard de costeo para empresa: {}", empresaId);
        
        try {
            // 1. Estadísticas generales
            Map<String, Long> estadisticasTransacciones = transaccionManagementService
                    .obtenerEstadisticasTransacciones(empresaId);
            
            // 2. Grupos de costeo
            List<CostingGroupDto> grupos = costingService.obtenerGruposCosteo();
            
            // 3. Saldos por empresa
            var saldosActuales = kardexQueryService.obtenerSaldosPorEmpresa(empresaId);
            
            // 4. Transacciones problemáticas (últimas 20)
            var transaccionesProblematicas = reportsService
                    .generarReporteTransaccionesProblematicas(empresaId);
            
            // 5. Inventario valorizado
            var inventarioValorado = kardexQueryService.obtenerInventarioValorado(empresaId);
            
            // 6. Estadísticas de costeo
            var estadisticasCosteo = kardexQueryService
                    .obtenerEstadisticasCosteo(empresaId, LocalDate.now().minusMonths(1), LocalDate.now());
            
            return Map.of(
                    "empresaId", empresaId,
                    "fechaGeneracion", LocalDate.now(),
                    "estadisticasTransacciones", estadisticasTransacciones,
                    "totalGrupos", grupos.size(),
                    "gruposRecientes", grupos.stream().limit(10).toList(),
                    "saldosActuales", saldosActuales.stream().limit(20).toList(),
                    "transaccionesProblematicas", transaccionesProblematicas,
                    "inventarioValorado", inventarioValorado.stream().limit(15).toList(),
                    "estadisticasCosteo", estadisticasCosteo
            );
            
        } catch (Exception e) {
            log.error("Error generando dashboard para empresa {}: {}", empresaId, e.getMessage(), e);
            return Map.of(
                    "success", false,
                    "error", e.getMessage()
            );
        }
    }

    /**
     * Genera un reporte completo de estado del sistema.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> generarReporteEstadoSistema(Long empresaId, LocalDate fechaInicio, LocalDate fechaFin) {
        log.info("Generando reporte completo del sistema para empresa {} del {} al {}", 
                empresaId, fechaInicio, fechaFin);
        
        try {
            return Map.of(
                    "empresaId", empresaId,
                    "periodo", Map.of("inicio", fechaInicio, "fin", fechaFin),
                    "fechaGeneracion", LocalDate.now(),
                    
                    // Reportes especializados
                    "inventarioValorado", reportsService.generarReporteInventarioValorado(empresaId, fechaFin),
                    "movimientos", reportsService.generarReporteMovimientos(empresaId, fechaInicio, fechaFin, null, null),
                    "utilidades", reportsService.generarReporteUtilidades(empresaId, fechaInicio, fechaFin),
                    "rendimiento", reportsService.generarReporteRendimiento(empresaId),
                    "transaccionesProblematicas", reportsService.generarReporteTransaccionesProblematicas(empresaId)
            );
            
        } catch (Exception e) {
            log.error("Error generando reporte completo para empresa {}: {}", empresaId, e.getMessage(), e);
            return Map.of(
                    "success", false,
                    "error", e.getMessage()
            );
        }
    }

    // ===== OPERACIONES DE MANTENIMIENTO =====

    /**
     * Ejecuta un proceso de validación y limpieza del sistema.
     */
    @Transactional
    public Map<String, Object> ejecutarMantenimientoSistema(Long empresaId, boolean modoReparacion) {
        log.warn("=== INICIANDO MANTENIMIENTO DEL SISTEMA para empresa {} (modo reparación: {}) ===", 
                empresaId, modoReparacion);
        
        try {
            List<Map<String, Object>> inconsistenciasDetectadas = new ArrayList<>();
            
            // 1. Verificar consistencia de todos los grupos
            List<CostingGroupDto> grupos = costingService.obtenerGruposCosteo()
                    .stream()
                    .filter(g -> g.getEmpresaId().equals(empresaId))
                    .toList();
            
            for (CostingGroupDto grupo : grupos) {
                Map<String, Object> consistencia = kardexQueryService
                        .verificarConsistenciaKardex(grupo.getEmpresaId(), grupo.getCustodioId(), 
                                grupo.getInstrumentoId(), grupo.getCuenta());
                
                if (!(Boolean) consistencia.get("consistente")) {
                    inconsistenciasDetectadas.add(Map.of(
                            "grupo", grupo.getDescripcionGrupo(),
                            "tipo", "INCONSISTENCIA_KARDEX",
                            "detalle", consistencia.get("mensaje")
                    ));
                    
                    // Si está en modo reparación, intentar corregir
                    if (modoReparacion) {
                        try {
                            costingService.reiniciarGrupo(
                                    grupo.getEmpresaId(), grupo.getCustodioId(),
                                    grupo.getInstrumentoId(), grupo.getCuenta(),
                                    LocalDate.now().minusYears(1) // Reset conservador
                            );
                            log.info("Grupo reparado: {}", grupo.getDescripcionGrupo());
                        } catch (Exception e) {
                            log.error("Error reparando grupo {}: {}", grupo.getDescripcionGrupo(), e.getMessage());
                        }
                    }
                }
            }
            
            // 2. Buscar transacciones huérfanas (sin relaciones válidas)
            // Este paso requeriría queries específicas según tu modelo de datos
            
            log.warn("=== MANTENIMIENTO COMPLETADO: {} inconsistencias detectadas ===", 
                    inconsistenciasDetectadas.size());
            
            return Map.of(
                    "success", true,
                    "empresaId", empresaId,
                    "fechaEjecucion", LocalDate.now(),
                    "modoReparacion", modoReparacion,
                    "gruposVerificados", grupos.size(),
                    "inconsistenciasDetectadas", inconsistenciasDetectadas.size(),
                    "inconsistencias", inconsistenciasDetectadas
            );
            
        } catch (Exception e) {
            log.error("Error en mantenimiento del sistema para empresa {}: {}", empresaId, e.getMessage(), e);
            return Map.of(
                    "success", false,
                    "error", e.getMessage()
            );
        }
    }

    // ===== MÉTODOS AUXILIARES =====

    /**
     * Determina qué tipo de ajuste es más recomendable.
     */
    private String determinarRecomendacion(AjustePropuestoDto ajusteIngreso, AjustePropuestoDto ajusteEgreso) {
        // Lógica simple: preferir ajuste de ingreso si la cantidad es pequeña
        if (ajusteIngreso.getCantidad().compareTo(new BigDecimal("100")) <= 0) {
            return "INGRESO - Cantidad pequeña, probablemente falta inventario";
        } else {
            return "EGRESO - Cantidad grande, revisar si el egreso es correcto";
        }
    }
}