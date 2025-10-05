package com.service.mapper;

import com.model.dto.GrupoEmpresaDto;
import com.model.entities.EmpresaEntity;
import com.model.entities.GrupoEmpresaEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.IterableMapping;

@Mapper(componentModel = "spring")
public interface GrupoEmpresaMapper {

    // ==================== Mapeos Principales (Entity a DTO) ====================
    @Named("toDtoBasic")
    @Mapping(target = "totalEmpresas", ignore = true)
    @Mapping(target = "empresaIds", ignore = true)
    GrupoEmpresaDto toDtoBasic(GrupoEmpresaEntity entity);

    @Named("toDtoComplete")
    default GrupoEmpresaDto toDtoComplete(GrupoEmpresaEntity entity) {
        if (entity == null) {
            return null;
        }

        GrupoEmpresaDto dto = new GrupoEmpresaDto();
        dto.setId(entity.getId());
        dto.setNombreGrupo(entity.getNombreGrupo());
        dto.setFechaCreacion(entity.getFechaCreacion());
        dto.setFechaModificacion(entity.getFechaModificacion());

        if (entity.getEmpresas() != null) {
            dto.setTotalEmpresas(entity.getEmpresas().size());
            dto.setEmpresaIds(entity.getEmpresas().stream().map(EmpresaEntity::getId).collect(Collectors.toList()));
        } else {
            dto.setTotalEmpresas(0);
        }

        return dto;
    }

    // ==================== Mapeo Inverso (DTO a Entity) ====================
    @Mapping(target = "empresas", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "creadoPor", ignore = true)
    @Mapping(target = "modificadoPor", ignore = true)
    GrupoEmpresaEntity toEntity(GrupoEmpresaDto dto);

    // ==================== Mapeo para Actualizaciones ====================
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "empresas", ignore = true)
    @Mapping(target = "creadoPor", ignore = true)
    @Mapping(target = "modificadoPor", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(GrupoEmpresaDto dto, @MappingTarget GrupoEmpresaEntity entity);

    // ==================== Mapeos de Listas ====================
    @Named("toDtoBasicList")
    @IterableMapping(qualifiedByName = "toDtoBasic")
    List<GrupoEmpresaDto> toDtoBasicList(List<GrupoEmpresaEntity> entities);

    @Named("toDtoCompleteList")
    default List<GrupoEmpresaDto> toDtoCompleteList(List<GrupoEmpresaEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream().map(this::toDtoComplete).collect(Collectors.toList());
    }
}
