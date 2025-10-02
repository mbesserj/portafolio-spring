package com.service.mapper;

import com.model.dto.CartolaBanChileDto;
import com.model.entities.CargaTransaccionEntity;
import org.mapstruct.Mapper;
import java.time.LocalDate;

@Mapper(componentModel = "spring")
public interface CartolaBanChileMapper {

    /**
     * Contiene la lógica de negocio para convertir un DTO de Cartola BanChile
     * en la entidad de staging CargaTransaccionEntity.
     * Este método reemplaza al antiguo toEntity() del DTO.
     */
    default CargaTransaccionEntity toCargaTransaccionEntity(CartolaBanChileDto dto) {
        if (dto == null || dto.getFechaTransaccion() == null) {
            return null;
        }

        // Extraemos los datos de la clave primaria del DTO
        String tipoClase = dto.getTipoClase();
        LocalDate fecha = dto.getFechaTransaccion();
        int rowNum = dto.getRowNum();

        CargaTransaccionEntity entity = new CargaTransaccionEntity();
        
        // Asignamos directamente los campos de la clave, ya que CargaTransaccionEntity no usa @EmbeddedId
        entity.setFechaTransaccion(fecha);
        entity.setRowNum(rowNum);
        entity.setTipoClase(tipoClase);
        
        // --- La misma lógica if/else que tenías en toEntity() ---
        if ("S".equals(tipoClase)) {
            entity.setRazonSocial(dto.getClienteSaldo());
            entity.setCuenta(dto.getCuentaSaldo());
            entity.setProducto(dto.getProductoSaldo());
            entity.setInstrumentoNemo(dto.getInstrumentoSaldo());
            entity.setInstrumentoNombre(dto.getNombreSaldo());
            entity.setMoneda(dto.getMonedaOrigenSaldo());
            entity.setCantidad(dto.getNominalesFinal());
            entity.setPrecio(dto.getPrecioTasaSaldo());
            entity.setMonto(dto.getMontoFinalOrigen());
            entity.setMontoTotal(null); // Según tu lógica original
            entity.setMontoClp(dto.getMontoFinalClp());
            entity.setMovimientoCaja("Saldo");
            entity.setTipoMovimiento("Saldo");
        } else { // "T" o cualquier otro tipo de movimiento
            entity.setRazonSocial(dto.getClienteMovimiento());
            entity.setCuenta(dto.getCuentaMovimiento());
            entity.setProducto(dto.getProductoMovimiento());
            entity.setInstrumentoNemo(dto.getInstrumentoNemo());
            entity.setInstrumentoNombre(dto.getInstrumentoNombre());
            entity.setMoneda(dto.getMonedaOrigen());
            entity.setCantidad(dto.getCantidad());
            entity.setPrecio(dto.getPrecio());
            entity.setComision(dto.getComision());
            entity.setIva(dto.getIva());
            entity.setMonto(dto.getMontoTransadoClp());
            entity.setMontoTotal(dto.getMontoTransadoClp());
            entity.setMontoClp(dto.getMontoTransadoClp());
            entity.setMovimientoCaja(dto.getMovimientoCaja());
            entity.setTipoMovimiento(dto.getOperacion());
        }

        // Asignamos valores fijos o derivados
        entity.setCustodioNombre("BanChile");
        entity.setRut(dto.getRutMovimiento()); 
        entity.setFolio(null); 
        entity.setProcesado(false);

        return entity;
    }
}