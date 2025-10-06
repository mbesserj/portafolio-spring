package com.portafolio.ui.service;

import com.portafolio.model.dto.ConfrontaSaldoDto;
import com.portafolio.persistence.repositorio.ConfrontaRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * Servicio que orquesta la confrontación de saldos utilizando Spring Data JPA.
 */
@Service
@RequiredArgsConstructor 
public class ConfrontaService {

    private static final Logger logger = LoggerFactory.getLogger(ConfrontaService.class);

    private final ConfrontaRepository confrontaRepository;

    /**
     * Obtiene las diferencias de saldos para la fecha de corte más reciente.
     * La anotación @Transactional(readOnly = true) optimiza la consulta.
     *
     * @return Lista de diferencias de saldos, o lista vacía si no hay datos o hay un error.
     */
    @Transactional(readOnly = true)
    public List<ConfrontaSaldoDto> obtenerDiferenciasDeSaldos() {
        try {
            // La lógica ahora es una simple llamada al método del repositorio.
            List<ConfrontaSaldoDto> diferencias = confrontaRepository.obtenerDiferenciasDeSaldos();
            logger.info("Se encontraron {} diferencias de saldos.", diferencias.size());
            return diferencias;
        } catch (Exception e) {
            logger.error("Error crítico en el servicio de confronta al obtener diferencias.", e);
            // En caso de un error inesperado, devolvemos una lista vacía para no romper la UI.
            return Collections.emptyList();
        }
    }
}