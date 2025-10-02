package com.service.mapper;

import com.model.dto.PortafolioDto;
import com.model.entities.PortafolioEntity;
import com.model.entities.PortafolioTransaccionEntity;
import org.mapstruct.*;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface PortafolioMapper {

    // ==================== Mapeos Principales (Entity a DTO) ====================
    @Named("toDtoBasic")
    @Mapping(target = "totalTransacciones", ignore = true)
    @Mapping(target = "transaccionIds", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    PortafolioDto toDtoBasic(PortafolioEntity entity);

    @Named("toDtoComplete")
    default PortafolioDto toDtoComplete(PortafolioEntity entity) {
        if (entity == null) return null;

        PortafolioDto dto = new PortafolioDto();
        dto.setId(entity.getId());
        dto.setNombrePortafolio(entity.getNombrePortafolio());
        dto.setDescripcion(entity.getDescripcion());
        dto.setFechaCreacion(entity.getFechaCreacion());
        dto.setFechaModificacion(entity.getFechaModificacion());

        if (entity.getPortafolioTransacciones() != null) {
            dto.setTotalTransacciones(entity.getPortafolioTransacciones().size());
            dto.setTransaccionIds(entity.getPortafolioTransacciones().stream()
                .map(PortafolioTransaccionEntity::getId)
                .collect(Collectors.toSet()));
        } else {
            dto.setTotalTransacciones(0);
        }
        
        return dto;
    }

    // ==================== Mapeo Inverso (DTO a Entity) ====================
    @Mapping(target = "portafolioTransacciones", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "creadoPor", ignore = true)
    @Mapping(target = "modificadoPor", ignore = true)
    PortafolioEntity toEntity(PortafolioDto dto);

    // ==================== Mapeo para Actualizaciones ====================
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "portafolioTransacciones", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "creadoPor", ignore = true)
    @Mapping(target = "modificadoPor", ignore = true)
    void updateEntityFromDto(PortafolioDto dto, @MappingTarget PortafolioEntity entity);

    // ==================== Mapeos de Listas ====================
    @Named("toDtoBasicList")
    @IterableMapping(qualifiedByName = "toDtoBasic")
    List<PortafolioDto> toDtoBasicList(List<PortafolioEntity> entities);

    @Named("toDtoCompleteList")
    default List<PortafolioDto> toDtoCompleteList(List<PortafolioEntity> entities) {
        if (entities == null) return null;
        return entities.stream().map(this::toDtoComplete).collect(Collectors.toList());
    }
}