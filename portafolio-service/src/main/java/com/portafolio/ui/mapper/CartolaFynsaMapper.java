package com.portafolio.ui.mapper;

import com.portafolio.model.dto.CartolaFynsaDto;
import com.portafolio.model.entities.CargaTransaccionEntity;
import org.mapstruct.Mapper;
import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface CartolaFynsaMapper {

    /**
     * Convierte el DTO intermedio de Fynsa a la entidad de staging final.
     * Este método implementa la lógica condicional para manejar los diferentes
     * tipos de registros (Saldos, Transacciones, Caja).
     */
    default CargaTransaccionEntity toCargaTransaccionEntity(CartolaFynsaDto dto) {
        if (dto == null) {
            return null;
        }

        CargaTransaccionEntity entity = new CargaTransaccionEntity();
        String tipoClase = dto.getTipoClase();

        // 1. Asignamos los campos de la clave primaria
        entity.setFechaTransaccion(dto.getTransactionDate());
        entity.setRowNum(dto.getRowNum());
        entity.setTipoClase(tipoClase);

        // 2. Asignamos los campos comunes que siempre están presentes
        entity.setRazonSocial(dto.getRazonSocial());
        entity.setRut(dto.getRut());
        entity.setCustodioNombre(dto.getCustodio());
        entity.setInstrumentoNemo(dto.getInstrumentoNemo());
        entity.setInstrumentoNombre(dto.getInstrumentoNombre());
        entity.setMoneda(dto.getMoneda());

        // 3. Lógica condicional según el tipo de registro
        if ("S".equalsIgnoreCase(tipoClase)) { // Saldos (Stock)
            entity.setCuenta(dto.getCuenta());
            entity.setCuentaPsh(dto.getCuentaPsh());
            entity.setCantLibre(dto.getCantLibre());
            entity.setCantGarantia(dto.getCantGarantia());
            entity.setCantPlazo(dto.getCantPlazo());
            entity.setCantVc(dto.getCantVc());
            entity.setCantidad(dto.getCantTotal()); 
            entity.setPrecio(dto.getPrecio());
            entity.setMontoClp(dto.getMontoClp());
            entity.setMontoUsd(dto.getMontoUsd());
            entity.setTipoMovimiento("SALDO");

        } else if ("T".equalsIgnoreCase(tipoClase)) { // Transacciones
            entity.setCuenta(dto.getCuenta());
            entity.setTipoMovimiento(dto.getTipoMovimiento());
            entity.setFolio(dto.getFolio());
            entity.setCantidad(dto.getCantidad());
            entity.setPrecio(dto.getPrecio());
            entity.setMonto(dto.getMonto());
            entity.setComision(dto.getComisiones());
            entity.setGastos(dto.getGastos());
            entity.setMontoTotal(dto.getMontoTotal());

        } else if ("C".equalsIgnoreCase(tipoClase)) { // Caja
            entity.setCuenta(dto.getCuenta());
            entity.setTipoMovimiento(dto.getTipoMovimiento());
            entity.setFolio(dto.getFolio());
            entity.setPrecio(dto.getPrecio());
            entity.setMonto(dto.getMonto());
            // Según tu lógica original, estos se asignan a cero
            entity.setComision(BigDecimal.ZERO);
            entity.setGastos(BigDecimal.ZERO);
            // Y el monto total es igual al monto
            entity.setMontoTotal(dto.getMonto());
        }

        // Asignamos el estado de procesado
        entity.setProcesado(false);

        return entity;
    }
}