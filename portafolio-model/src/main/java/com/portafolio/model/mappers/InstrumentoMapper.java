package com.portafolio.model.mappers;

import com.portafolio.model.dto.InstrumentoDto;
import com.portafolio.model.entities.InstrumentoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring") // Le decimos que cree un Bean de Spring
public interface InstrumentoMapper {

    // Mapea de Entidad a DTO
    @Mapping(source = "instrumentoNombre", target = "instrumentoNombre")
    InstrumentoDto toDto(InstrumentoEntity entity);

    InstrumentoEntity toEntity(InstrumentoDto dto);
}