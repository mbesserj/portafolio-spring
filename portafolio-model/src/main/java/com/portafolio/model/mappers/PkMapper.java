package com.portafolio.model.mappers;

import com.portafolio.model.utiles.Pk;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.time.LocalDate;

@Mapper(componentModel = "spring")
public interface PkMapper {

    @Mapping(source = "transactionDate", target = "transactionDate")
    @Mapping(source = "rowNum", target = "rowNum")
    @Mapping(source = "tipoClase", target = "tipoClase")
    Pk toPk(LocalDate transactionDate, Integer rowNum, String tipoClase);
}