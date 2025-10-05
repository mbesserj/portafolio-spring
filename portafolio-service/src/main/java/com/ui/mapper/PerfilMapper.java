package com.portafolio.ui.mapper;

import com.portafolio.model.dto.PerfilDto;
import com.portafolio.model.entities.PerfilEntity;
import com.portafolio.model.entities.UsuarioEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.Set;
import org.mapstruct.IterableMapping;

@Mapper(componentModel = "spring")
public interface PerfilMapper {

    // ==================== Mapeos Principales (Entity a DTO) ====================

    @Named("toDtoComplete")
    @Mapping(source = "usuarios", target = "totalUsuarios", qualifiedByName = "countUsuarios")
    PerfilDto toDtoComplete(PerfilEntity entity);

    @Named("toDtoBasic")
    @Mapping(target = "totalUsuarios", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    PerfilDto toDtoBasic(PerfilEntity entity);

    // Método calificador para contar usuarios sin cargar la colección entera si es LAZY
    @Named("countUsuarios")
    default Integer countUsuarios(Set<UsuarioEntity> usuarios) {
        return usuarios != null ? usuarios.size() : 0;
    }

    // ==================== Mapeo Inverso (DTO a Entity) ====================
    
    @Mapping(target = "usuarios", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "creadoPor", ignore = true)
    @Mapping(target = "modificadoPor", ignore = true)
    PerfilEntity toEntity(PerfilDto dto);

    // ==================== Mapeo para Actualizaciones ====================
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuarios", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "creadoPor", ignore = true)
    @Mapping(target = "modificadoPor", ignore = true)
    void updateEntityFromDto(PerfilDto dto, @MappingTarget PerfilEntity entity);

    // ==================== Mapeos de Listas ====================
    
    @Named("toDtoBasicList")
    @IterableMapping(qualifiedByName = "toDtoBasic")
    List<PerfilDto> toDtoBasicList(List<PerfilEntity> entities);

    @Named("toDtoCompleteList")
    @IterableMapping(qualifiedByName = "toDtoComplete")
    List<PerfilDto> toDtoCompleteList(List<PerfilEntity> entities);
}