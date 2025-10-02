package com.service.mapper;

import com.model.dto.AuditoriaDto;
import com.model.entities.AuditoriaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.BeanMapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.Named;

import java.util.List;

/**
 * Mapper para convertir entre AuditoriaEntity y AuditoriaDto
 */
@Mapper(componentModel = "spring")
public interface AuditoriaMapper {

    // ==================== Entity to DTO ====================
    
    @Mapping(source = "id", target = "id")
    @Mapping(source = "fechaAuditoria", target = "fechaAuditoria")
    @Mapping(source = "fechaArchivo", target = "fechaArchivo")
    @Mapping(source = "fechaDatos", target = "fechaDatos")
    @Mapping(source = "tipoEntidad", target = "tipoEntidad")
    @Mapping(source = "valorClave", target = "valorClave")
    @Mapping(source = "descripcion", target = "descripcion")
    @Mapping(source = "archivoOrigen", target = "archivoOrigen")
    @Mapping(source = "filaNumero", target = "filaNumero")
    @Mapping(source = "motivo", target = "motivo")
    @Mapping(source = "registrosInsertados", target = "registrosInsertados")
    @Mapping(source = "registrosRechazados", target = "registrosRechazados")
    @Mapping(source = "registrosDuplicados", target = "registrosDuplicados")
    @Mapping(source = "fechaCreacion", target = "fechaCreacion")
    @Mapping(source = "fechaModificacion", target = "fechaModificacion")
    @Mapping(source = "creadoPor", target = "creadoPor")
    @Mapping(source = "modificadoPor", target = "modificadoPor")
    AuditoriaDto toDto(AuditoriaEntity entity);
    
    // ==================== DTO to Entity ====================
    
    @Mapping(source = "fechaAuditoria", target = "fechaAuditoria")
    @Mapping(source = "fechaArchivo", target = "fechaArchivo")
    @Mapping(source = "fechaDatos", target = "fechaDatos")
    @Mapping(source = "tipoEntidad", target = "tipoEntidad")
    @Mapping(source = "valorClave", target = "valorClave")
    @Mapping(source = "descripcion", target = "descripcion")
    @Mapping(source = "archivoOrigen", target = "archivoOrigen")
    @Mapping(source = "filaNumero", target = "filaNumero")
    @Mapping(source = "motivo", target = "motivo")
    @Mapping(source = "registrosInsertados", target = "registrosInsertados")
    @Mapping(source = "registrosRechazados", target = "registrosRechazados")
    @Mapping(source = "registrosDuplicados", target = "registrosDuplicados")
    
    // Campos de BaseEntity - ignoramos para que JPA los maneje
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "creadoPor", ignore = true)
    @Mapping(target = "modificadoPor", ignore = true)
    @Named("basicMapping")
    AuditoriaEntity toEntity(AuditoriaDto dto);
    
    // ==================== Update Methods ====================
    
    @Mapping(source = "fechaAuditoria", target = "fechaAuditoria")
    @Mapping(source = "fechaArchivo", target = "fechaArchivo")
    @Mapping(source = "fechaDatos", target = "fechaDatos")
    @Mapping(source = "tipoEntidad", target = "tipoEntidad")
    @Mapping(source = "valorClave", target = "valorClave")
    @Mapping(source = "descripcion", target = "descripcion")
    @Mapping(source = "archivoOrigen", target = "archivoOrigen")
    @Mapping(source = "filaNumero", target = "filaNumero")
    @Mapping(source = "motivo", target = "motivo")
    @Mapping(source = "registrosInsertados", target = "registrosInsertados")
    @Mapping(source = "registrosRechazados", target = "registrosRechazados")
    @Mapping(source = "registrosDuplicados", target = "registrosDuplicados")
    
    // Campos de BaseEntity - ignoramos
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "creadoPor", ignore = true)
    @Mapping(target = "modificadoPor", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(AuditoriaDto dto, @MappingTarget AuditoriaEntity entity);
    
    // ==================== List Mappings ====================
    
    List<AuditoriaDto> toDtoList(List<AuditoriaEntity> entities);
    
    @Named("basicMappingList")
    List<AuditoriaEntity> toEntityList(List<AuditoriaDto> dtos);
    
    // ==================== Métodos personalizados ====================
    
    /**
     * Método para crear una nueva auditoría con valores por defecto
     */
    default AuditoriaEntity createNewAuditoria(AuditoriaDto dto) {
        return AuditoriaEntity.builder()
                .fechaAuditoria(dto.getFechaAuditoria())
                .fechaArchivo(dto.getFechaArchivo())
                .fechaDatos(dto.getFechaDatos())
                .tipoEntidad(dto.getTipoEntidad())
                .valorClave(dto.getValorClave())
                .descripcion(dto.getDescripcion())
                .archivoOrigen(dto.getArchivoOrigen())
                .filaNumero(dto.getFilaNumero())
                .motivo(dto.getMotivo())
                .registrosInsertados(dto.getRegistrosInsertados() != null ? dto.getRegistrosInsertados() : 0)
                .registrosRechazados(dto.getRegistrosRechazados() != null ? dto.getRegistrosRechazados() : 0)
                .registrosDuplicados(dto.getRegistrosDuplicados() != null ? dto.getRegistrosDuplicados() : 0)
                .build();
    }
}