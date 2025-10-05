package com.portafolio.ui.mapper;

import com.portafolio.model.dto.KardexDto;
import com.portafolio.model.entities.KardexEntity;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring")
public interface KardexMapper {

    // ==================== Mapeo Principal (Entity a DTO) ====================
    @Mapping(source = "transaccion.id", target = "transaccionId")
    @Mapping(source = "custodio.id", target = "custodioId")
    @Mapping(source = "custodio.nombreCustodio", target = "custodioNombre")
    @Mapping(source = "empresa.id", target = "empresaId")
    @Mapping(source = "empresa.razonSocial", target = "empresaRazonSocial")
    @Mapping(source = "instrumento.id", target = "instrumentoId")
    @Mapping(source = "instrumento.instrumentoNombre", target = "instrumentoDisplayName") // O un método getDisplayName si existe
    @Named("toDtoComplete")
    KardexDto toDto(KardexEntity entity);

    // ==================== Mapeo Inverso (DTO a Entity) ====================
    @Mapping(target = "transaccion", ignore = true)
    @Mapping(target = "custodio", ignore = true)
    @Mapping(target = "empresa", ignore = true)
    @Mapping(target = "instrumento", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "creadoPor", ignore = true)
    @Mapping(target = "modificadoPor", ignore = true)
    @Mapping(target = "claveAgrupacion", ignore = true)
    @Mapping(target = "fechaCosteo", ignore = true)
    KardexEntity toEntity(KardexDto dto);

    // ==================== Mapeo para Actualizaciones ====================
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "creadoPor", ignore = true)
    @Mapping(target = "modificadoPor", ignore = true)
    @Mapping(target = "transaccion", ignore = true)
    @Mapping(target = "custodio", ignore = true)
    @Mapping(target = "empresa", ignore = true)
    @Mapping(target = "instrumento", ignore = true)
    @Mapping(target = "claveAgrupacion", ignore = true)
    @Mapping(target = "fechaCosteo", ignore = true)
    void updateEntityFromDto(KardexDto dto, @MappingTarget KardexEntity entity);

    // ==================== Mapeos de Listas ====================
    @IterableMapping(qualifiedByName = "toDtoComplete")
    List<KardexDto> toDtoList(List<KardexEntity> entities);
}
