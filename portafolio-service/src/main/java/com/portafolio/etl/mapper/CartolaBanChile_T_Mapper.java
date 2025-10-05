package com.portafolio.etl.mapper;

import com.portafolio.model.dto.CartolaBanChileDto;
import com.portafolio.etl.interfaces.CargaMapperInterfaz;
import com.portafolio.etl.util.ExcelRowUtils;
import org.apache.poi.ss.usermodel.Row;
import org.hibernate.MappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("banchile_T_Mapper") // Nombre único para la hoja de Movimientos
public class CartolaBanChile_T_Mapper implements CargaMapperInterfaz<CartolaBanChileDto> {

    private static final Logger logger = LoggerFactory.getLogger(CartolaBanChile_T_Mapper.class);
    private final ExcelRowUtils rowUtils;

    @Autowired
    public CartolaBanChile_T_Mapper(ExcelRowUtils rowUtils) {
        this.rowUtils = rowUtils;
    }

    @Override
    public CartolaBanChileDto map(Row row, int rowNum, String fileName) throws MappingException {
        if (rowUtils.shouldSkipRow(row)) {
            return null;
        }

        try {
            CartolaBanChileDto dto = new CartolaBanChileDto();
            
            // --- Lógica completa para la hoja de Movimientos ("T") ---
            dto.setClienteMovimiento(rowUtils.getString(row.getCell(0)));
            dto.setCuentaMovimiento(rowUtils.getString(row.getCell(1)));
            dto.setFechaLiquidacion(rowUtils.getLocalDate(row.getCell(2)));
            dto.setFechaMovimiento(rowUtils.getLocalDate(row.getCell(3)));
            dto.setProductoMovimiento(rowUtils.getString(row.getCell(4)));
            dto.setMovimientoCaja(rowUtils.getString(row.getCell(5)));
            dto.setOperacion(rowUtils.getString(row.getCell(6)));
            dto.setInstrumentoNemo(rowUtils.getString(row.getCell(7)));
            dto.setInstrumentoNombre(rowUtils.getString(row.getCell(8)));
            dto.setCantidad(rowUtils.getBigDecimal(row.getCell(10)));
            dto.setMonedaOrigen(rowUtils.getString(row.getCell(11)));
            dto.setPrecio(rowUtils.getBigDecimal(row.getCell(12)));
            dto.setComision(rowUtils.getBigDecimal(row.getCell(13)));
            dto.setIva(rowUtils.getBigDecimal(row.getCell(14)));
            dto.setMontoTransadoMO(rowUtils.getBigDecimal(row.getCell(15)));
            dto.setMontoTransadoClp(rowUtils.getBigDecimal(row.getCell(16)));

            // Lógica especial para el tipo de clase "C" (Caja)
            String detalle = rowUtils.getString(row.getCell(9));
            dto.setDetalle(detalle);
            String tipoClase = "A Caja".equalsIgnoreCase(detalle) ? "C" : "T";

            // Asignamos los campos de la clave primaria para la entidad de staging
            dto.setFechaTransaccion(dto.getFechaMovimiento());
            dto.setRowNum(rowNum);
            dto.setTipoClase(tipoClase);
            
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