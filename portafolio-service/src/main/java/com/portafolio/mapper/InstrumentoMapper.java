package com.portafolio.mapper;

import com.portafolio.model.dto.ActualizarInstrumentoDto;
import com.portafolio.model.dto.CrearInstrumentoDto;
import com.portafolio.model.dto.InstrumentoDto;
import com.portafolio.model.entities.InstrumentoEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import org.mapstruct.IterableMapping;

@Mapper(componentModel = "spring")
public interface InstrumentoMapper {

    // ==================== Mapeo Principal (Entity a DTO) ====================
    @Mapping(source = "producto.id", target = "productoId")
    @Mapping(source = "producto.producto", target = "productoNombre")
    @Mapping(source = "totalTransacciones", target = "totalTransacciones")
    @Named("toDtoComplete")
    InstrumentoDto toDtoComplete(InstrumentoEntity entity);

    @Named("toDtoBasic")
    @Mapping(target = "productoId", ignore = true)
    @Mapping(target = "productoNombre", ignore = true)
    @Mapping(target = "totalTransacciones", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    InstrumentoDto toDtoBasic(InstrumentoEntity entity);
    
    // ==================== Mapeo Inverso (DTO a Entity) ====================
    @Mapping(target = "producto", ignore = true) // El servicio se encarga de asignar el producto
    @Mapping(target = "transacciones", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "creadoPor", ignore = true)
    @Mapping(target = "modificadoPor", ignore = true)
    InstrumentoEntity toEntity(CrearInstrumentoDto dto);

    // ==================== Mapeo para Actualizaciones = "==================="
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "instrumentoNemo", ignore = true) 
    @Mapping(target = "producto", ignore = true)
    @Mapping(target = "transacciones", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "creadoPor", ignore = true)
    @Mapping(target = "modificadoPor", ignore = true)
    void updateEntityFromDto(ActualizarInstrumentoDto dto, @MappingTarget InstrumentoEntity entity);
    
    // ==================== Mapeos de Listas ====================
    @Named("toDtoBasicList")
    @IterableMapping(qualifiedByName = "toDtoBasic")
    List<InstrumentoDto> toDtoBasicList(List<InstrumentoEntity> entities);

    @Named("toDtoCompleteList")
    @IterableMapping(qualifiedByName = "toDtoComplete")
    List<InstrumentoDto> toDtoCompleteList(List<InstrumentoEntity> entities);
}