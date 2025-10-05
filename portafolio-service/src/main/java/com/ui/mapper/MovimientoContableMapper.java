package com.portafolio.ui.mapper;

import com.portafolio.model.dto.MovimientoContableDto;
import com.portafolio.model.entities.MovimientoContableEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MovimientoContableMapper {

    // ==================== Mapeo Principal (Entity a DTO) ====================
    MovimientoContableDto toDto(MovimientoContableEntity entity);

    // ==================== Mapeo Inverso (DTO a Entity) ====================
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "creadoPor", ignore = true)
    @Mapping(target = "modificadoPor", ignore = true)
    MovimientoContableEntity toEntity(MovimientoContableDto dto);

    // ==================== Mapeo para Actualizaciones ====================
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tipoContable", ignore = true) // El tipo no debería ser editable
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "creadoPor", ignore = true)
    @Mapping(target = "modificadoPor", ignore = true)
    void updateEntityFromDto(MovimientoContableDto dto, @MappingTarget MovimientoContableEntity entity);
    
    // ==================== Mapeos de Listas ====================
    List<MovimientoContableDto> toDtoList(List<MovimientoContableEntity> entities);
}
