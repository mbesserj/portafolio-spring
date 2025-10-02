package com.service.mapper;

import com.model.utiles.Pk;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.time.LocalDate;

@Mapper(componentModel = "spring")
public interface PkMapper {

    @Mapping(source = "fechaTransaccion", target = "fechaTransaccion")
    @Mapping(source = "rowNum", target = "rowNum")
    @Mapping(source = "tipoClase", target = "tipoClase")
    Pk toPk(LocalDate fechaTransaccion, Integer rowNum, String tipoClase);
}