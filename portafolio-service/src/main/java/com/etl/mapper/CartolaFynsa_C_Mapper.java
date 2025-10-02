package com.etl.mapper;

import com.model.dto.CartolaFynsaDto;
import com.etl.interfaces.CargaMapperInterfaz;
import com.service.util.ExcelRowUtils;
import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import org.hibernate.MappingException;

@Component("fynsa_C_Mapper") // Nombre único para la hoja de Caja
public class CartolaFynsa_C_Mapper implements CargaMapperInterfaz<CartolaFynsaDto> {

    private static final Logger logger = LoggerFactory.getLogger(CartolaFynsa_C_Mapper.class);
    private final ExcelRowUtils rowUtils;

    @Autowired
    public CartolaFynsa_C_Mapper(ExcelRowUtils rowUtils) {
        this.rowUtils = rowUtils;
    }

    @Override
    public CartolaFynsaDto map(Row row, int rowNum, String fileName) throws MappingException {
        if (rowUtils.shouldSkipRow(row)) {
            return null;
        }

        try {
            // Lógica especial de Fynsa: si el folio es nulo o "0", se omite la fila
            String folio = rowUtils.getString(row.getCell(6));
            if (folio == null || "0".equals(folio)) {
                logger.warn("Omitiendo fila {} porque el folio es nulo o '0'.", rowNum);
                return null;
            }

            CartolaFynsaDto dto = new CartolaFynsaDto();
            dto.setTipoClase("C");
            dto.setRowNum(rowNum);

            // --- Lógica completa para la hoja de Caja ("C") ---
            dto.setTransactionDate(rowUtils.getLocalDate(row.getCell(0)));
            dto.setRazonSocial(rowUtils.getString(row.getCell(1)));
            dto.setRut(rowUtils.getString(row.getCell(2)));
            dto.setCuenta(rowUtils.getString(row.getCell(3)));
            dto.setCustodio(rowUtils.getString(row.getCell(4)));
            dto.setTipoMovimiento(rowUtils.getString(row.getCell(5)));
            dto.setFolio(folio);
            dto.setInstrumentoNemo(rowUtils.getString(row.getCell(7)));
            dto.setInstrumentoNombre(rowUtils.getString(row.getCell(8)));
            // La cantidad en la hoja de Caja se omite según tu lógica original
            dto.setPrecio(rowUtils.getBigDecimal(row.getCell(10)));
            dto.setMonto(rowUtils.getBigDecimal(row.getCell(11)));
            dto.setMoneda(rowUtils.getString(row.getCell(12)));
            
            // Asignación de valores por defecto para Caja
            dto.setComisiones(BigDecimal.ZERO);
            dto.setGastos(BigDecimal.ZERO);
            dto.setMontoTotal(dto.getMonto());

            return dto;

        } catch (Exception e) {
            throw new MappingException("Error al mapear la fila " + rowNum + " del archivo " + fileName, e);
        }
    }
}