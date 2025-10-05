package com.portafolio.ui.mapper;

import com.portafolio.model.dto.EmpresaDto;
import com.portafolio.model.entities.CustodioEntity;
import com.portafolio.model.entities.CuentaEntity;
import com.portafolio.model.entities.EmpresaEntity;
import com.portafolio.model.entities.TransaccionEntity;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre EmpresaEntity y EmpresaDto, siguiendo un patrón avanzado.
 */
@Mapper(componentModel = "spring")
public interface EmpresaMapper {

    // ==================== Mapeo Principal (Entity a DTO Completo) ====================

    /**
     * Mapeo principal que se encarga de la mayoría de campos.
     * Los campos calculados o que requieren lógica se completan en el método default.
     */
    @Mapping(source = "grupoEmpresa.id", target = "grupoEmpresaId")
    @Mapping(source = "grupoEmpresa.nombreGrupo", target = "grupoEmpresaNombre")
    // Ignoramos los campos que calcularemos manualmente en el método 'toDtoComplete'
    @Mapping(target = "rutFormateado", ignore = true)
    @Mapping(target = "perteneceAGrupo", ignore = true)
    @Mapping(target = "totalCustodios", ignore = true)
    @Mapping(target = "totalTransacciones", ignore = true)
    @Mapping(target = "totalCuentas", ignore = true)
    @Mapping(target = "custodioIds", ignore = true)
    @Mapping(target = "cuentaIds", ignore = true)
    @Mapping(target = "transaccionIds", ignore = true)
    @Mapping(target = "tieneCustodios", ignore = true)
    @Mapping(target = "tieneTransacciones", ignore = true)
    @Mapping(target = "tieneCuentas", ignore = true)
    EmpresaDto toDto(EmpresaEntity entity);

    /**
     * Método orquestador para un mapeo completo, ideal para vistas de detalle.
     * Usa el mapeo base y añade la lógica de negocio y cálculos.
     */
    @Named("toDtoComplete")
    default EmpresaDto toDtoComplete(EmpresaEntity entity) {
        if (entity == null) {
            return null;
        }
        // 1. Llama al mapeo básico generado por MapStruct
        EmpresaDto dto = toDto(entity);

        // 2. Completa los campos calculados y de conveniencia
        dto.setRutFormateado(entity.getRutFormateado());
        dto.setPerteneceAGrupo(entity.perteneceAGrupo());
        
        // Estadísticas
        dto.setTotalCustodios(entity.getTotalCustodios());
        dto.setTotalTransacciones(entity.getTotalTransacciones());
        dto.setTotalCuentas(entity.getTotalCuentas());

        // Flags de existencia
        dto.setTieneCustodios(entity.tieneCustodios());
        dto.setTieneTransacciones(entity.tieneTransacciones());
        dto.setTieneCuentas(entity.tieneCuentas());

        // IDs de las colecciones (manejando colecciones no inicializadas)
        if (entity.getCustodios() != null) {
            dto.setCustodioIds(entity.getCustodios().stream().map(CustodioEntity::getId).collect(Collectors.toList()));
        }
        if (entity.getCuentas() != null) {
            dto.setCuentaIds(entity.getCuentas().stream().map(CuentaEntity::getId).collect(Collectors.toList()));
        }
        if (entity.getTransacciones() != null) {
            dto.setTransaccionIds(entity.getTransacciones().stream().map(TransaccionEntity::getId).collect(Collectors.toList()));
        }

        return dto;
    }

    // ==================== Mapeo Básico (Para Vistas de Lista) ====================
    
    /**
     * Crea una vista simplificada del DTO, ideal para listas o dropdowns.
     */
    @Named("toDtoBasic")
    default EmpresaDto toDtoBasic(EmpresaEntity entity) {
        if (entity == null) return null;
        return EmpresaDto.builder()
                .id(entity.getId())
                .rutFormateado(entity.getRutFormateado())
                .razonSocial(entity.getRazonSocial())
                .grupoEmpresaNombre(entity.getNombreGrupo())
                .build();
    }

    // ==================== Mapeo Inverso (DTO a Entity) ====================
    
    /**
     * Convierte un DTO a una Entidad, principalmente para la creación.
     * Ignora relaciones complejas, que deben ser manejadas por el servicio.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "custodios", ignore = true)
    @Mapping(target = "cuentas", ignore = true)
    @Mapping(target = "transacciones", ignore = true)
    @Mapping(target = "grupoEmpresa", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "creadoPor", ignore = true)
    @Mapping(target = "modificadoPor", ignore = true)
    EmpresaEntity toEntity(EmpresaDto dto);

    // ==================== Mapeo para Actualizaciones ====================
    
    /**
     * Actualiza una entidad existente a partir de un DTO.
     * Ignora propiedades nulas en el DTO para no sobrescribir datos existentes.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rut", ignore = true) // El RUT no debería ser modificable
    @Mapping(target = "custodios", ignore = true)
    @Mapping(target = "cuentas", ignore = true)
    @Mapping(target = "transacciones", ignore = true)
    @Mapping(target = "grupoEmpresa", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "creadoPor", ignore = true)
    @Mapping(target = "modificadoPor", ignore = true)
    void updateEntityFromDto(EmpresaDto dto, @MappingTarget EmpresaEntity entity);

    // ==================== Mapeos de Listas ====================

    @Named("toDtoBasicList")
    List<EmpresaDto> toDtoBasicList(List<EmpresaEntity> entities);

    @Named("toDtoCompleteList")
    default List<EmpresaDto> toDtoCompleteList(List<EmpresaEntity> entities) {
        if (entities == null) return null;
        return entities.stream().map(this::toDtoComplete).collect(Collectors.toList());
    }
}