package com.portafolio.costing.engine;

import com.portafolio.model.entities.TransaccionEntity;
import com.portafolio.model.enums.TipoEnumsCosteo;
import com.portafolio.persistence.repositorio.TransaccionRepository;
import com.portafolio.persistence.repositorio.KardexRepository;
import com.portafolio.persistence.repositorio.SaldoKardexRepository;
import com.portafolio.persistence.repositorio.TipoMovimientoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Motor principal de costeo FIFO adaptado para Spring.
 * Coordina el procesamiento de transacciones usando el algoritmo FIFO.
 */
@RequiredArgsConstructor
@Component
@Slf4j
public class FifoCostingEngine {

    private final EntityManager entityManager;
    private final TransaccionRepository transaccionRepository;
    private final KardexRepository kardexRepository;
    private final SaldoKardexRepository saldoKardexRepository;
    private final TipoMovimientoRepository tipoMovimientoRepository;

    /**
     * Procesa todas las transacciones pendientes de costeo.
     * Spring maneja la transacción automáticamente con @Transactional.
     */
    @Transactional
    public int procesarCosteo() {
        log.info("=== Iniciando proceso de costeo FIFO ===");
        
        // 1. OBTENER TRANSACCIONES NO COSTEADAS
        List<TransaccionEntity> transacciones = obtenerTransaccionesPendientes();
        log.info("Transacciones encontradas para procesar: {}", transacciones.size());

        if (transacciones.isEmpty()) {
            log.info("No hay transacciones pendientes de costeo");
            return 0;
        }

        // 2. AGRUPAR POR CLAVE (Empresa|Cuenta|Custodio|Instrumento)
        Map<String, List<TransaccionEntity>> grupos = transacciones.stream()
                .collect(Collectors.groupingBy(this::generarClaveAgrupacion));

        log.info("Grupos de costeo identificados: {}", grupos.size());

        // 3. PROCESAR CADA GRUPO
        int gruposProcesados = 0;
        for (Map.Entry<String, List<TransaccionEntity>> entry : grupos.entrySet()) {
            String claveGrupo = entry.getKey();
            List<TransaccionEntity> transaccionesGrupo = entry.getValue();
            
            log.info("Procesando grupo: {} ({} transacciones)", 
                    claveGrupo, transaccionesGrupo.size());
            
            try {
                procesarGrupo(claveGrupo, transaccionesGrupo);
                gruposProcesados++;
            } catch (Exception e) {
                log.error("Error procesando grupo {}: {}", claveGrupo, e.getMessage(), e);
                // Continúa con el siguiente grupo
            }
        }

        entityManager.flush();
        log.info("=== Proceso de costeo completado: {}/{} grupos procesados ===", 
                gruposProcesados, grupos.size());
        
        return gruposProcesados;
    }

    /**
     * Procesa un grupo específico de transacciones.
     */
    private void procesarGrupo(String claveGrupo, List<TransaccionEntity> transacciones) {
        // Crear procesador específico para este grupo
        CostingGroupProcessor processor = new CostingGroupProcessor(
                claveGrupo,
                transacciones,
                entityManager,
                kardexRepository,
                saldoKardexRepository,
                tipoMovimientoRepository
        );
        
        // Delegar el procesamiento
        processor.process();
    }

    /**
     * Obtiene las transacciones pendientes de costeo, ordenadas correctamente.
     * El orden es crítico para el algoritmo FIFO:
     * 1. Por fecha ascendente
     * 2. Saldos iniciales primero
     * 3. Ingresos antes que egresos
     * 4. Por ID ascendente
     */
    private List<TransaccionEntity> obtenerTransaccionesPendientes() {
        return entityManager.createQuery("""
            SELECT t FROM TransaccionEntity t
            JOIN FETCH t.empresa
            JOIN FETCH t.custodio
            JOIN FETCH t.instrumento
            JOIN FETCH t.tipoMovimiento tm
            JOIN FETCH tm.movimientoContable mc
            WHERE mc.tipoContable <> :noCostear
              AND t.costeado = false
              AND t.paraRevision = false
              AND t.ignorarEnCosteo = false
            ORDER BY t.fechaTransaccion ASC,
                     CASE WHEN tm.esSaldoInicial = true THEN 0 ELSE 1 END,
                     CASE WHEN mc.tipoContable = 'INGRESO' THEN 2 ELSE 3 END,
                     t.id ASC
            """, TransaccionEntity.class)
                .setParameter("noCostear", TipoEnumsCosteo.NO_COSTEAR)
                .getResultList();
    }

    /**
     * Genera la clave de agrupación para una transacción.
     * Formato: empresaId|cuenta|custodioId|instrumentoId
     */
    private String generarClaveAgrupacion(TransaccionEntity t) {
        return String.format("%d|%s|%d|%d",
                t.getEmpresa().getId(),
                t.getCuenta(),
                t.getCustodio().getId(),
                t.getInstrumento().getId());
    }
}