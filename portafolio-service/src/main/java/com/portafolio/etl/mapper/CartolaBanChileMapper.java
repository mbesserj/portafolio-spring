package com.portafolio.etl.mapper; // O la nueva ubicación que decidas, ej: com.portafolio.mapper

import com.portafolio.model.dto.CartolaBanChileDto;
import com.portafolio.model.entities.CargaTransaccionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface CartolaBanChileMapper {

    /**
     * Convierte el DTO de la cartola de BanChile a la entidad de staging
     * (CargaTransaccionEntity). Utiliza expresiones de MapStruct para manejar
     * la lógica condicional basada en si el registro es de tipo Saldo ('S') o
     * Movimiento ('T').
     *
     * @param dto El objeto de transferencia de datos leído del archivo Excel.
     * @return La entidad lista para ser guardada en la tabla de staging.
     */
    @Mappings({
        // === 1. Asignación de la clave primaria (campos directos) ===
        @Mapping(source = "fechaTransaccion", target = "fechaTransaccion"),
        @Mapping(source = "rowNum", target = "rowNum"),
        @Mapping(source = "tipoClase", target = "tipoClase"),

        // === 2. Asignación de valores fijos o derivados ===
        @Mapping(target = "custodioNombre", constant = "BanChile"),
        @Mapping(target = "procesado", constant = "false"),
        @Mapping(source = "rutMovimiento", target = "rut"),

        // === 3. Lógica Condicional: mapeo basado en tipoClase ("S" o "T") ===
        @Mapping(target = "razonSocial", expression = "java(\"S\".equals(dto.getTipoClase()) ? dto.getClienteSaldo() : dto.getClienteMovimiento())"),
        @Mapping(target = "cuenta", expression = "java(\"S\".equals(dto.getTipoClase()) ? dto.getCuentaSaldo() : dto.getCuentaMovimiento())"),
        @Mapping(target = "producto", expression = "java(\"S\".equals(dto.getTipoClase()) ? dto.getProductoSaldo() : dto.getProductoMovimiento())"),
        @Mapping(target = "instrumentoNemo", expression = "java(\"S\".equals(dto.getTipoClase()) ? dto.getInstrumentoSaldo() : dto.getInstrumentoNemo())"),
        @Mapping(target = "instrumentoNombre", expression = "java(\"S\".equals(dto.getTipoClase()) ? dto.getNombreSaldo() : dto.getInstrumentoNombre())"),
        @Mapping(target = "moneda", expression = "java(\"S\".equals(dto.getTipoClase()) ? dto.getMonedaOrigenSaldo() : dto.getMonedaOrigen())"),
        @Mapping(target = "cantidad", expression = "java(\"S\".equals(dto.getTipoClase()) ? dto.getNominalesFinal() : dto.getCantidad())"),
        @Mapping(target = "precio", expression = "java(\"S\".equals(dto.getTipoClase()) ? dto.getPrecioTasaSaldo() : dto.getPrecio())"),
        @Mapping(target = "monto", expression = "java(\"S\".equals(dto.getTipoClase()) ? dto.getMontoFinalOrigen() : dto.getMontoTransadoMO())"),
        @Mapping(target = "montoClp", expression = "java(\"S\".equals(dto.getTipoClase()) ? dto.getMontoFinalClp() : dto.getMontoTransadoClp())"),
        @Mapping(target = "montoTotal", expression = "java(\"S\".equals(dto.getTipoClase()) ? null : dto.getMontoTransadoClp())"),
        @Mapping(target = "movimientoCaja", expression = "java(\"S\".equals(dto.getTipoClase()) ? \"Saldo\" : dto.getMovimientoCaja())"),
        @Mapping(target = "tipoMovimiento", expression = "java(\"S\".equals(dto.getTipoClase()) ? \"Saldo\" : dto.getOperacion())"),
        @Mapping(target = "glosa", expression = "java(\"S\".equals(dto.getTipoClase()) ? null : dto.getDetalle())"),

        // === 4. Campos que solo existen para el tipo "T" (Movimientos) ===
        @Mapping(source = "comision", target = "comision"),
        @Mapping(source = "iva", target = "iva"),
        
        // === 5. Campos que se deben ignorar para BanChile ===
        @Mapping(target = "gastos", ignore = true),
        @Mapping(target = "folio", ignore = true),
        @Mapping(target = "cuentaPsh", ignore = true),
        @Mapping(target = "cantLibre", ignore = true),
        @Mapping(target = "cantGarantia", ignore = true),
        @Mapping(target = "cantPlazo", ignore = true),
        @Mapping(target = "cantVc", ignore = true),
        @Mapping(target = "cantTotal", ignore = true),
        @Mapping(target = "montoUsd", ignore = true),

        @Mapping(target = "fechaCreacion", ignore = true),
        @Mapping(target = "fechaModificacion", ignore = true),
        @Mapping(target = "creadoPor", ignore = true),
        @Mapping(target = "modificadoPor", ignore = true)
    })
    CargaTransaccionEntity toCargaTransaccionEntity(CartolaBanChileDto dto);
}