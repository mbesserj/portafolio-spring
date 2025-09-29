package com.portafolio.model.mappers;

import com.portafolio.model.dto.CargaTransaccionDto;
import com.portafolio.model.entities.CargaTransaccionEntity;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy; // Importar esto si es necesario

// Se añade unmappedTargetPolicy = ReportingPolicy.IGNORE
// Esto le dice a MapStruct que, si hay una propiedad en la Entidad (Source) 
// que no existe en el DTO (Target), simplemente la IGNORE y no lance un error.
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CargaTransaccionMapper {
    
    // Ahora, los métodos son limpios.
    CargaTransaccionDto toDto(CargaTransaccionEntity entity);
    
    // Y podemos seguir usando la configuración inversa para toEntity
    // (Esto ignora automáticamente los campos de auditoría que el DTO no provee).
    @InheritInverseConfiguration
    CargaTransaccionEntity toEntity(CargaTransaccionDto dto);
}