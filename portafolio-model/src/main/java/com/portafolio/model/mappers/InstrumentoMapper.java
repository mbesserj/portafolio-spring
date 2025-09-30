package com.portafolio.model.mappers;

import com.portafolio.model.dto.InstrumentoDto;
import com.portafolio.model.entities.InstrumentoEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InstrumentoMapper {
    InstrumentoDto toDto(InstrumentoEntity entity);
    InstrumentoEntity toEntity(InstrumentoDto dto);
}
