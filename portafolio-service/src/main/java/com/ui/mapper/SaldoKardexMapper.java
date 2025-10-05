package com.portafolio.ui.mapper;

import com.portafolio.model.dto.SaldoKardexDto;
import com.portafolio.model.entities.SaldoKardexEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SaldoKardexMapper {

    @Mapping(source = "empresa.id", target = "empresaId")
    @Mapping(source = "empresa.razonSocial", target = "empresaRazonSocial")
    @Mapping(source = "custodio.id", target = "custodioId")
    @Mapping(source = "custodio.nombreCustodio", target = "custodioNombre")
    @Mapping(source = "instrumento.id", target = "instrumentoId")
    @Mapping(source = "instrumento.instrumentoNombre", target = "instrumentoDisplayName")
    SaldoKardexDto toDto(SaldoKardexEntity entity);

    @Mapping(target = "empresa", ignore = true)
    @Mapping(target = "custodio", ignore = true)
    @Mapping(target = "instrumento", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "creadoPor", ignore = true)
    @Mapping(target = "modificadoPor", ignore = true)
    SaldoKardexEntity toEntity(SaldoKardexDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "empresa", ignore = true)
    @Mapping(target = "custodio", ignore = true)
    @Mapping(target = "instrumento", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "creadoPor", ignore = true)
    @Mapping(target = "modificadoPor", ignore = true)
    void updateEntityFromDto(SaldoKardexDto dto, @MappingTarget SaldoKardexEntity entity);

    List<SaldoKardexDto> toDtoList(List<SaldoKardexEntity> entities);
}