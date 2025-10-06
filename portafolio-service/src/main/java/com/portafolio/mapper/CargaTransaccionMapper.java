package com.portafolio.mapper;

import com.portafolio.model.dto.CargaTransaccionDto;
import com.portafolio.model.entities.CargaTransaccionEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE 
)
public interface CargaTransaccionMapper {

    /**
     * Convierte Entity -> DTO.
     * No se necesitan anotaciones @Mapping porque todos los nombres de campo coinciden.
     */
    CargaTransaccionDto toDto(CargaTransaccionEntity entity);

    /**
     * Convierte DTO -> Entity.
     * No se necesitan anotaciones @Mapping porque todos los nombres de campo coinciden.
     */
    CargaTransaccionEntity toEntity(CargaTransaccionDto dto);

    /**
     * Actualiza una entidad existente desde un DTO.
     * Ignoramos explícitamente los campos de la clave primaria, ya que no deben cambiar.
     */
    @Mapping(target = "fechaTransaccion", ignore = true)
    @Mapping(target = "rowNum", ignore = true)
    @Mapping(target = "tipoClase", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(CargaTransaccionDto dto, @MappingTarget CargaTransaccionEntity entity);

    /**
     * Métodos para listas, generados automáticamente.
     */
    List<CargaTransaccionDto> toDtoList(List<CargaTransaccionEntity> entities);
    List<CargaTransaccionEntity> toEntityList(List<CargaTransaccionDto> dtos);
}