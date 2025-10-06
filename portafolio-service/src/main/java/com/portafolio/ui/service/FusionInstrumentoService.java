package com.portafolio.ui.service;

import com.portafolio.model.entities.InstrumentoEntity;
import com.portafolio.persistence.repositorio.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FusionInstrumentoService {

    private static final Logger logger = LoggerFactory.getLogger(FusionInstrumentoService.class);

    // Spring inyecta todos los repositorios necesarios
    private final InstrumentoRepository instrumentoRepository;
    private final TransaccionRepository transaccionRepository;
    private final SaldoRepository saldoRepository;
    private final DetalleCosteoRepository detalleCosteoRepository;
    private final KardexRepository kardexRepository;
    private final SaldosDiariosRepository saldosDiariosRepository;

    /**
     * Fusiona dos instrumentos y prepara todas las transacciones para recosteo.
     * La anotación @Transactional asegura que toda la operación se ejecute en una
     * única transacción: si algo falla, se revierte todo automáticamente.
     *
     * @param idInstrumentoAntiguo ID del instrumento que será eliminado.
     * @param idInstrumentoNuevo   ID del instrumento que permanecerá.
     */
    @Transactional
    public void fusionarYPrepararRecosteo(Long idInstrumentoAntiguo, Long idInstrumentoNuevo) {
        // Validaciones de entrada
        if (idInstrumentoAntiguo == null || idInstrumentoNuevo == null || idInstrumentoAntiguo.equals(idInstrumentoNuevo)) {
            throw new IllegalArgumentException("IDs de instrumento inválidos para la fusión.");
        }

        logger.info("Iniciando fusión del instrumento ID {} en el instrumento ID {}", idInstrumentoAntiguo, idInstrumentoNuevo);

        // 1. Verificar existencia de ambos instrumentos usando el repositorio
        InstrumentoEntity instrumentoAntiguo = instrumentoRepository.findById(idInstrumentoAntiguo)
                .orElseThrow(() -> new EntityNotFoundException("Instrumento antiguo no encontrado con ID: " + idInstrumentoAntiguo));
        InstrumentoEntity instrumentoNuevo = instrumentoRepository.findById(idInstrumentoNuevo)
                .orElseThrow(() -> new EntityNotFoundException("Instrumento nuevo no encontrado con ID: " + idInstrumentoNuevo));

        List<InstrumentoEntity> ambosInstrumentos = List.of(instrumentoAntiguo, instrumentoNuevo);

        // 2. LIMPIEZA TOTAL DE DATOS CALCULADOS DE AMBOS INSTRUMENTOS
        logger.info("Limpiando datos calculados para AMBOS instrumentos...");
        detalleCosteoRepository.limpiarPorInstrumentos(ambosInstrumentos);
        kardexRepository.limpiarPorInstrumentos(ambosInstrumentos);
        saldosDiariosRepository.limpiarPorInstrumentos(ambosInstrumentos);

        // 3. REASIGNAR DATOS FUENTE (NO CALCULADOS)
        logger.info("Reasignando registros de TransaccionEntity...");
        int transaccionesActualizadas = transaccionRepository.reasignarInstrumento(instrumentoNuevo, instrumentoAntiguo);
        logger.info("{} registros de TransaccionEntity fueron actualizados", transaccionesActualizadas);

        logger.info("Reasignando registros de SaldoEntity...");
        int saldosActualizados = saldoRepository.reasignarInstrumento(instrumentoNuevo, instrumentoAntiguo);
        logger.info("{} registros de SaldoEntity fueron actualizados", saldosActualizados);
        
        // 4. MARCAR TODAS LAS TRANSACCIONES DEL GRUPO UNIFICADO PARA RECOSTEO
        logger.info("Marcando transacciones del instrumento ID {} para recosteo...", idInstrumentoNuevo);
        int transaccionesParaRecostear = transaccionRepository.marcarParaRecosteo(instrumentoNuevo);
        logger.info("{} transacciones marcadas para ser costeadas de nuevo", transaccionesParaRecostear);

        // 5. ELIMINAR EL INSTRUMENTO ANTIGUO
        logger.info("Eliminando el instrumento antiguo (ID: {})...", idInstrumentoAntiguo);
        instrumentoRepository.delete(instrumentoAntiguo);

        logger.info("Fusión y preparación para recosteo completada con éxito.");
    }
}