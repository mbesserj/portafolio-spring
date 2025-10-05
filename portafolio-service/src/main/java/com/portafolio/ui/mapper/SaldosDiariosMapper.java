package com.portafolio.ui.mapper;

import com.portafolio.model.dto.SaldosDiariosDto;
import com.portafolio.model.entities.SaldosDiariosEntity;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring")
public interface SaldosDiariosMapper {

    @Mapping(source = "empresa.id", target = "empresaId")
    @Mapping(source = "empresa.razonSocial", target = "empresaRazonSocial")
    @Mapping(source = "custodio.id", target = "custodioId")
    @Mapping(source = "custodio.nombreCustodio", target = "custodioNombre")
    @Mapping(source = "instrumento.id", target = "instrumentoId")
    @Mapping(source = "instrumento.instrumentoNombre", target = "instrumentoDisplayName")
    SaldosDiariosDto toDto(SaldosDiariosEntity entity);

    @Mapping(target = "empresa", ignore = true)
    @Mapping(target = "custodio", ignore = true)
    @Mapping(target = "instrumento", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "creadoPor", ignore = true)
    @Mapping(target = "modificadoPor", ignore = true)
    SaldosDiariosEntity toEntity(SaldosDiariosDto dto);
    
    List<SaldosDiariosDto> toDtoList(List<SaldosDiariosEntity> entities);
}