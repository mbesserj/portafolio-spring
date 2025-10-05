package com.portafolio.ui.mapper;

import com.portafolio.model.dto.CierreContableDto;
import com.portafolio.model.entities.CierreContableEntity;
import com.portafolio.model.entities.EmpresaEntity;
import com.portafolio.model.entities.CustodioEntity;
import com.portafolio.model.entities.InstrumentoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.BeanMapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.Named;

import java.util.List;
import org.mapstruct.IterableMapping;

/**
 * Mapper para convertir entre CierreContableEntity y CierreContableDto
 */
@Mapper(componentModel = "spring")
public interface CierreContableMapper {

    // ==================== Entity to DTO ====================
    
    @Mapping(source = "id", target = "id")
    @Mapping(source = "ejercicio", target = "ejercicio")
    @Mapping(source = "cantidadCierre", target = "cantidadCierre")
    @Mapping(source = "valorCierre", target = "valorCierre")
    
    // Empresa
    @Mapping(source = "empresa.id", target = "empresaId")
    @Mapping(source = "empresa.razonSocial", target = "empresaRazonSocial")
    @Mapping(source = "empresa.rut", target = "empresaRut")
    
    // Custodio
    @Mapping(source = "custodio.id", target = "custodioId")
    @Mapping(source = "custodio.nombreCustodio", target = "custodioNombre")
    
    // Instrumento
    @Mapping(source = "instrumento.id", target = "instrumentoId")
    @Mapping(source = "instrumento.instrumentoNemo", target = "instrumentoNemo")
    @Mapping(source = "instrumento.instrumentoNombre", target = "instrumentoNombre")
    
    // Auditoría
    @Mapping(source = "fechaCreacion", target = "fechaCreacion")
    @Mapping(source = "fechaModificacion", target = "fechaModificacion")
    @Mapping(source = "creadoPor", target = "creadoPor")
    @Mapping(source = "modificadoPor", target = "modificadoPor")
    
    // Campos calculados - se calcularán en el DTO
    @Mapping(target = "valorUnitario", ignore = true)
    @Mapping(target = "claveCierre", ignore = true)
    CierreContableDto toDto(CierreContableEntity entity);
    
    // ==================== DTO to Entity ====================
    
    @Mapping(source = "ejercicio", target = "ejercicio")
    @Mapping(source = "cantidadCierre", target = "cantidadCierre")
    @Mapping(source = "valorCierre", target = "valorCierre")
    
    // Relaciones - se ignorarán para que se asignen externamente
    @Mapping(target = "empresa", ignore = true)
    @Mapping(target = "custodio", ignore = true)
    @Mapping(target = "instrumento", ignore = true)
    
    // Campos de BaseEntity - ignoramos para que JPA los maneje
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "creadoPor", ignore = true)
    @Mapping(target = "modificadoPor", ignore = true)
    @Named("basicMapping")
    CierreContableEntity toEntity(CierreContableDto dto);
    
    // ==================== Mapping with entities ====================
    
    /**
     * Convierte DTO a Entity incluyendo las entidades relacionadas
     */
    @Mapping(source = "dto.ejercicio", target = "ejercicio")
    @Mapping(source = "dto.cantidadCierre", target = "cantidadCierre")
    @Mapping(source = "dto.valorCierre", target = "valorCierre")
    @Mapping(source = "empresa", target = "empresa")
    @Mapping(source = "custodio", target = "custodio")
    @Mapping(source = "instrumento", target = "instrumento")
    
    // Campos de BaseEntity - ignoramos
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "creadoPor", ignore = true)
    @Mapping(target = "modificadoPor", ignore = true)
    CierreContableEntity toEntityWithRelations(
            CierreContableDto dto,
            EmpresaEntity empresa,
            CustodioEntity custodio,
            InstrumentoEntity instrumento
    );
    
    // ==================== Update Methods ====================
    
    @Mapping(source = "ejercicio", target = "ejercicio")
    @Mapping(source = "cantidadCierre", target = "cantidadCierre")
    @Mapping(source = "valorCierre", target = "valorCierre")
    
    // Ignoramos relaciones y auditoría
    @Mapping(target = "empresa", ignore = true)
    @Mapping(target = "custodio", ignore = true)
    @Mapping(target = "instrumento", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "creadoPor", ignore = true)
    @Mapping(target = "modificadoPor", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(CierreContableDto dto, @MappingTarget CierreContableEntity entity);
    
    // ==================== List Mappings ====================
    
    @Named("toDtoList")
    List<CierreContableDto> toDtoList(List<CierreContableEntity> entities);
    
    @Named("basicMappingList")
    @IterableMapping(qualifiedByName = "basicMapping")
    List<CierreContableEntity> toEntityList(List<CierreContableDto> dtos);
    
    // ==================== Custom Mapping Methods ====================
    
    /**
     * Convierte Entity a DTO con campos calculados completos
     */
    @Named("toDtoComplete")
    default CierreContableDto toDtoComplete(CierreContableEntity entity) {
        if (entity == null) {
            return null;
        }
        
        CierreContableDto dto = toDto(entity);
        
        // Calculamos campos adicionales
        dto.setValorUnitario(entity.getValorUnitario());
        dto.setClaveCierre(entity.getClaveCierre());
        
        return dto;
    }
    
    /**
     * Método para crear una nueva entidad con valores por defecto
     */
    default CierreContableEntity createNewCierre(CierreContableDto dto,
                                                EmpresaEntity empresa,
                                                CustodioEntity custodio,
                                                InstrumentoEntity instrumento) {
        return CierreContableEntity.builder()
                .ejercicio(dto.getEjercicio())
                .empresa(empresa)
                .custodio(custodio)
                .instrumento(instrumento)
                .cantidadCierre(dto.getCantidadCierre())
                .valorCierre(dto.getValorCierre())
                .build();
    }
    
    /**
     * Convierte resultado de consulta agregada a DTO simple
     */
    default CierreContableDto fromResumenArray(Object[] resultado) {
        if (resultado == null || resultado.length < 4) {
            return null;
        }
        
        return CierreContableDto.builder()
                .empresaRazonSocial((String) resultado[0])
                .cantidadCierre((java.math.BigDecimal) resultado[2])
                .valorCierre((java.math.BigDecimal) resultado[3])
                .build();
    }
}