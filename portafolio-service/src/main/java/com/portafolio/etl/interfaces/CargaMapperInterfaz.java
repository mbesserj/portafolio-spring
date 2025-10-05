package com.portafolio.etl.interfaces;

import org.apache.poi.ss.usermodel.Row;
import org.hibernate.MappingException;

/**
 * Contrato para cualquier clase que sepa cómo mapear una fila de Excel a un DTO.
 * @param <T> El tipo de DTO que se producirá (ej. CartolaFynsaDto).
 */
public interface CargaMapperInterfaz<T> {
    T map(Row row, int rowNum, String fileName) throws MappingException;
}