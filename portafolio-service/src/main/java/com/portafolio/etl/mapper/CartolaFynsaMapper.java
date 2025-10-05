package com.portafolio.etl.mapper; // O la nueva ubicación que decidas, ej: com.portafolio.mapper

import com.portafolio.model.dto.CartolaFynsaDto;
import com.portafolio.model.entities.CargaTransaccionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface CartolaFynsaMapper {

    /**
     * Convierte el DTO intermedio de Fynsa a la entidad de staging final.
     * Implementa la lógica condicional para manejar los diferentes tipos de registros
     * (Saldos 'S', Transacciones 'T', Caja 'C') de forma declarativa con MapStruct.
     *
     * @param dto El objeto de transferencia de datos leído del archivo Fynsa.
     * @return La entidad CargaTransaccionEntity lista para ser guardada.
     */
    @Mappings({
        // === 1. Campos de Clave Primaria y Comunes ===
        @Mapping(source = "transactionDate", target = "fechaTransaccion"),
        @Mapping(source = "rowNum", target = "rowNum"),
        @Mapping(source = "tipoClase", target = "tipoClase"),
        @Mapping(source = "razonSocial", target = "razonSocial"),
        @Mapping(source = "rut", target = "rut"),
        @Mapping(source = "custodio", target = "custodioNombre"),
        @Mapping(source = "instrumentoNemo", target = "instrumentoNemo"),
        @Mapping(source = "instrumentoNombre", target = "instrumentoNombre"),
        @Mapping(source = "moneda", target = "moneda"),
        @Mapping(source = "cuenta", target = "cuenta"), // Es común a los 3 tipos

        // === 2. Campos Condicionales ===
        @Mapping(target = "tipoMovimiento", expression = "java(\"S\".equalsIgnoreCase(dto.getTipoClase()) ? \"SALDO\" : dto.getTipoMovimiento())"),
        @Mapping(target = "cantidad", expression = "java(\"S\".equalsIgnoreCase(dto.getTipoClase()) ? dto.getCantTotal() : dto.getCantidad())"),
        
        // --- Campos solo para Saldos ('S') ---
        @Mapping(target = "cuentaPsh", expression = "java(\"S\".equalsIgnoreCase(dto.getTipoClase()) ? dto.getCuentaPsh() : null)"),
        @Mapping(target = "cantLibre", expression = "java(\"S\".equalsIgnoreCase(dto.getTipoClase()) ? dto.getCantLibre() : null)"),
        @Mapping(target = "cantGarantia", expression = "java(\"S\".equalsIgnoreCase(dto.getTipoClase()) ? dto.getCantGarantia() : null)"),
        @Mapping(target = "cantPlazo", expression = "java(\"S\".equalsIgnoreCase(dto.getTipoClase()) ? dto.getCantPlazo() : null)"),
        @Mapping(target = "cantVc", expression = "java(\"S\".equalsIgnoreCase(dto.getTipoClase()) ? dto.getCantVc() : null)"),
        @Mapping(target = "montoClp", expression = "java(\"S\".equalsIgnoreCase(dto.getTipoClase()) ? dto.getMontoClp() : null)"),
        @Mapping(target = "montoUsd", expression = "java(\"S\".equalsIgnoreCase(dto.getTipoClase()) ? dto.getMontoUsd() : null)"),

        // --- Campos para Transacciones ('T') y Caja ('C') ---
        @Mapping(target = "folio", expression = "java(\"S\".equalsIgnoreCase(dto.getTipoClase()) ? null : dto.getFolio())"),
        @Mapping(target = "monto", expression = "java(\"S\".equalsIgnoreCase(dto.getTipoClase()) ? null : dto.getMonto())"),
        
        // --- Lógica más compleja para 'comision', 'gastos' y 'montoTotal' ---
        @Mapping(target = "comision", expression = "java(\"C\".equalsIgnoreCase(dto.getTipoClase()) ? java.math.BigDecimal.ZERO : dto.getComisiones())"),
        @Mapping(target = "gastos", expression = "java(\"C\".equalsIgnoreCase(dto.getTipoClase()) ? java.math.BigDecimal.ZERO : dto.getGastos())"),
        @Mapping(target = "montoTotal", expression = "java(\"C\".equalsIgnoreCase(dto.getTipoClase()) ? dto.getMonto() : dto.getMontoTotal())"),
        
        // --- Campos que se deben ignorar (no vienen del DTO o son manejados por JPA) ---
        @Mapping(target = "procesado", constant = "false"),
        @Mapping(target = "glosa", ignore = true),
        @Mapping(target = "iva", ignore = true),
        @Mapping(target = "producto", ignore = true),
        @Mapping(target = "movimientoCaja", ignore = true),
        @Mapping(target = "fechaCreacion", ignore = true),
        @Mapping(target = "fechaModificacion", ignore = true),
        @Mapping(target = "creadoPor", ignore = true),
        @Mapping(target = "modificadoPor", ignore = true)
    })
    CargaTransaccionEntity toCargaTransaccionEntity(CartolaFynsaDto dto);
}