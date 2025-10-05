package com.portafolio.mapper;

import com.portafolio.model.dto.UsuarioDto;
import com.portafolio.model.entities.PerfilEntity;
import com.portafolio.model.entities.UsuarioEntity;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    // ==================== Entity to DTO ====================
    
    @Mapping(source = "perfiles", target = "perfiles", qualifiedByName = "perfilesToStrings")
    @Mapping(target = "isActivo", ignore = true)
    UsuarioDto toDto(UsuarioEntity entity);

    // ==================== DTO to Entity ====================

    @Mapping(target = "perfiles", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "creadoPor", ignore = true)
    @Mapping(target = "modificadoPor", ignore = true)
    @Mapping(target = "password", ignore = true)
    UsuarioEntity toEntity(UsuarioDto dto);
    
    // ==================== Update Method ====================

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuario", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "perfiles", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "creadoPor", ignore = true)
    @Mapping(target = "modificadoPor", ignore = true)
    void updateEntityFromDto(UsuarioDto dto, @MappingTarget UsuarioEntity entity);

    // ==================== Helper Methods ====================

    @Named("perfilesToStrings")
    default Set<String> perfilesToStrings(Set<PerfilEntity> perfiles) {
        if (perfiles == null) return null;
        return perfiles.stream().map(PerfilEntity::getPerfil).collect(Collectors.toSet());
    }
    
    List<UsuarioDto> toDtoList(List<UsuarioEntity> entities);
}