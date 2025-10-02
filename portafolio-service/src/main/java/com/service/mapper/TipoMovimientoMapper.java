package com.service.mapper;

import com.model.dto.ActualizarTipoMovimientoDto;
import com.model.dto.CrearTipoMovimientoDto;
import com.model.dto.TipoMovimientoDto;
import com.model.entities.TipoMovimientoEntity;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring")
public interface TipoMovimientoMapper {

    @Mapping(source = "movimientoContable.id", target = "movimientoContableId")
    @Mapping(source = "movimientoContable.tipoContable", target = "tipoContable")
    @Mapping(source = "movimientoContable.descripcionContable", target = "descripcionContable")
    TipoMovimientoDto toDto(TipoMovimientoEntity entity);

    @Mapping(target = "movimientoContable", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "creadoPor", ignore = true)
    @Mapping(target = "modificadoPor", ignore = true)
    @Mapping(target = "id", ignore = true)
    TipoMovimientoEntity toEntity(CrearTipoMovimientoDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tipoMovimiento", ignore = true) 
    @Mapping(target = "movimientoContable", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "creadoPor", ignore = true)
    @Mapping(target = "modificadoPor", ignore = true)
    void updateEntityFromDto(ActualizarTipoMovimientoDto dto, @MappingTarget TipoMovimientoEntity entity);

    List<TipoMovimientoDto> toDtoList(List<TipoMovimientoEntity> entities);
}