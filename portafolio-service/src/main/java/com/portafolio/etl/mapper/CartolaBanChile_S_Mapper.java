package com.portafolio.etl.mapper;

import com.portafolio.model.dto.CartolaBanChileDto;
import com.portafolio.etl.interfaces.CargaMapperInterfaz;
import com.portafolio.etl.util.ExcelRowUtils;
import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("banchile_S_Mapper") // Nombre único para la fábrica de estrategias
public class CartolaBanChile_S_Mapper implements CargaMapperInterfaz<CartolaBanChileDto> {

    private static final Logger logger = LoggerFactory.getLogger(CartolaBanChile_S_Mapper.class);

    private final ExcelRowUtils rowUtils;

    @Autowired
    public CartolaBanChile_S_Mapper(ExcelRowUtils rowUtils) {
        this.rowUtils = rowUtils;
    }

    @Override
    public CartolaBanChileDto map(Row row, int rowNum, String fileName) {
        if (rowUtils.shouldSkipRow(row)) { // Usamos la utilidad
            return null;
        }

        try {
            CartolaBanChileDto dto = new CartolaBanChileDto();
            
            // Lógica específica para la hoja de Saldos ("S")
            dto.setClienteSaldo(rowUtils.getString(row.getCell(0)));
            dto.setFechaSaldo(rowUtils.getLocalDate(row.getCell(1)));
            dto.setCuentaSaldo(rowUtils.getString(row.getCell(2)));
            dto.setProductoSaldo(rowUtils.getString(row.getCell(3)));
            dto.setInstrumentoSaldo(rowUtils.getString(row.getCell(4)));
            // ... etc., para todos los campos de la sección de saldos ...
            dto.setRentabilidadPeriodoOrigen(rowUtils.getBigDecimal(row.getCell(15)));

            // Creamos la clave primaria
            dto.setFechaTransaccion(dto.getFechaSaldo());
            dto.setRowNum(rowNum);
            dto.setTipoClase("S");

            return dto;

        } catch (Exception e) {
            logger.error("Formato de fecha inválido en la fila. Error: " + e.getMessage());
            return null;
        }
    }
}