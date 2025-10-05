package com.portafolio.etl.mapper;

import com.portafolio.model.dto.CartolaFynsaDto;
import com.portafolio.etl.interfaces.CargaMapperInterfaz;
import com.portafolio.etl.util.ExcelRowUtils;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("fynsa_T_Mapper") // Nombre único para la hoja de Transacciones
public class CartolaFynsa_T_Mapper implements CargaMapperInterfaz<CartolaFynsaDto> {

    private final ExcelRowUtils rowUtils;

    @Autowired
    public CartolaFynsa_T_Mapper(ExcelRowUtils rowUtils) {
        this.rowUtils = rowUtils;
    }

    @Override
    public CartolaFynsaDto map(Row row, int rowNum, String fileName) {
        // ... (lógica para saltar fila) ...

        try {
            CartolaFynsaDto dto = new CartolaFynsaDto();
            dto.setTipoClase("T");
            dto.setRowNum(rowNum);

            // Lógica específica para la hoja de Transacciones ("T")
            dto.setTransactionDate(rowUtils.getLocalDate(row.getCell(0)));
            dto.setRazonSocial(rowUtils.getString(row.getCell(1)));
            // ... etc., para todos los campos de la sección de Transacciones ...
            dto.setMoneda(rowUtils.getString(row.getCell(15)));

            return dto;
        } catch (Exception e) {
            // ... (manejo de excepciones) ...
            return null;
        }
    }
}