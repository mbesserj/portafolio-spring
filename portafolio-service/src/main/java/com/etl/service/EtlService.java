package com.etl.service;

import com.model.dto.ResultadoCargaDto;
import com.model.enums.ListaEnumsCustodios;
import java.io.InputStream;

public interface EtlService {

    /**
     * Procesa un archivo (cartola) de un custodio específico, lee sus datos
     * y los carga en la tabla de staging correspondiente.
     *
     * @param inputStream El flujo de datos del archivo a procesar.
     * @param custodio El enum que identifica al custodio (banco).
     * @return Un objeto ResultadoCargaDto con el resumen de la operación.
     */
    ResultadoCargaDto procesarArchivo(InputStream inputStream, ListaEnumsCustodios custodio);
}