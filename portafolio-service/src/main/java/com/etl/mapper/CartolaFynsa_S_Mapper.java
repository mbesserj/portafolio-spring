package com.etl.mapper;

import com.model.dto.CartolaFynsaDto;
import com.etl.interfaces.CargaMapperInterfaz;
import com.service.util.ExcelRowUtils;
import org.apache.poi.ss.usermodel.Row;
import org.hibernate.MappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("fynsa_S_Mapper") // Nombre único para la hoja de Saldos (Stock)
public class CartolaFynsa_S_Mapper implements CargaMapperInterfaz<CartolaFynsaDto> {

    private static final Logger logger = LoggerFactory.getLogger(CartolaFynsa_S_Mapper.class);
    private final ExcelRowUtils rowUtils;

    @Autowired
    public CartolaFynsa_S_Mapper(ExcelRowUtils rowUtils) {
        this.rowUtils = rowUtils;
    }

    @Override
    public CartolaFynsaDto map(Row row, int rowNum, String fileName) throws MappingException {
        if (rowUtils.shouldSkipRow(row)) {
            return null;
        }

        try {
            CartolaFynsaDto dto = new CartolaFynsaDto();
            dto.setTipoClase("S");
            dto.setRowNum(rowNum);

            // --- Lógica completa para la hoja de Saldos ("S") ---
            dto.setTransactionDate(rowUtils.getLocalDate(row.getCell(0)));
            dto.setRazonSocial(rowUtils.getString(row.getCell(1)));
            dto.setRut(rowUtils.getString(row.getCell(2)));
            dto.setCuenta(rowUtils.getString(row.getCell(3)));
            dto.setCuentaPsh(rowUtils.getString(row.getCell(4)));
            dto.setCustodio(rowUtils.getString(row.getCell(5)));
            dto.setInstrumentoNemo(rowUtils.getString(row.getCell(6)));
            dto.setInstrumentoNombre(rowUtils.getString(row.getCell(7)));
            dto.setCantLibre(rowUtils.getBigDecimal(row.getCell(8)));
            dto.setCantGarantia(rowUtils.getBigDecimal(row.getCell(9)));
            dto.setCantPlazo(rowUtils.getBigDecimal(row.getCell(10)));
            dto.setCantVc(rowUtils.getBigDecimal(row.getCell(11)));
            dto.setCantTotal(rowUtils.getBigDecimal(row.getCell(12)));
            dto.setPrecio(rowUtils.getBigDecimal(row.getCell(13)));
            dto.setMontoClp(rowUtils.getBigDecimal(row.getCell(14)));
            dto.setMontoUsd(rowUtils.getBigDecimal(row.getCell(15)));
            dto.setMoneda(rowUtils.getString(row.getCell(16)));

            // Validación final
            if (dto.getInstrumentoNemo() == null || dto.getInstrumentoNemo().isBlank()) {
                logger.warn("Se omitió la fila {} del archivo {} porque InstrumentoNemo está vacío.", rowNum, fileName);
                return null;
            }

            return dto;
            
        } catch (Exception e) {
            throw new MappingException("Error al mapear la fila " + rowNum + " del archivo " + fileName, e);
        }
    }
}