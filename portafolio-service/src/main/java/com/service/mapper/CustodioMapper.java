package com.service.mapper;

import com.model.dto.CrearCustodioDto;
import com.model.dto.CustodioDto;
import com.model.entities.CustodioEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.BeanMapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.Named;
import com.model.dto.ActualizarCustodioDto;

import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.IterableMapping;

/**
 * Mapper para convertir entre CustodioEntity y CustodioDto
 */
@Mapper(componentModel = "spring")
public interface CustodioMapper {

    // ==================== Entity to DTO ====================
    @Mapping(source = "id", target = "id")
    @Mapping(source = "nombreCustodio", target = "nombreCustodio")

    // Auditoría
    @Mapping(source = "fechaCreacion", target = "fechaCreacion")
    @Mapping(source = "fechaModificacion", target = "fechaModificacion")
    @Mapping(source = "creadoPor", target = "creadoPor")
    @Mapping(source = "modificadoPor", target = "modificadoPor")

    // Estadísticas y colecciones - se calcularán con métodos custom
    @Mapping(target = "totalEmpresas", ignore = true)
    @Mapping(target = "totalTransacciones", ignore = true)
    @Mapping(target = "totalCuentas", ignore = true)
    @Mapping(target = "empresaIds", ignore = true)
    @Mapping(target = "cuentaIds", ignore = true)
    @Mapping(target = "transaccionIds", ignore = true)
    @Mapping(target = "tieneEmpresas", ignore = true)
    @Mapping(target = "tieneTransacciones", ignore = true)
    @Mapping(target = "tieneCuentas", ignore = true)
    CustodioDto toDto(CustodioEntity entity);

    // ==================== DTO to Entity ====================
    @Mapping(source = "nombreCustodio", target = "nombreCustodio")

    // Colecciones - se ignorarán para evitar problemas de cascada
    @Mapping(target = "empresas", ignore = true)
    @Mapping(target = "transacciones", ignore = true)
    @Mapping(target = "cuentas", ignore = true)

    // Campos de BaseEntity - ignoramos para que JPA los maneje
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "creadoPor", ignore = true)
    @Mapping(target = "modificadoPor", ignore = true)
    @Named("basicMapping")
    CustodioEntity toEntity(CustodioDto dto);

    // ==================== Update Methods ====================
    @Mapping(source = "nombreCustodio", target = "nombreCustodio")

    // Ignoramos colecciones y auditoría
    @Mapping(target = "empresas", ignore = true)
    @Mapping(target = "transacciones", ignore = true)
    @Mapping(target = "cuentas", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "creadoPor", ignore = true)
    @Mapping(target = "modificadoPor", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(ActualizarCustodioDto dto, @MappingTarget CustodioEntity entity); 

    // ==================== List Mappings ====================
    @Named("toDtoList")
    List<CustodioDto> toDtoList(List<CustodioEntity> entities);

    @Named("basicMappingList")
    @IterableMapping(qualifiedByName = "basicMapping")
    List<CustodioEntity> toEntityList(List<CustodioDto> dtos);

    // ==================== Custom Mapping Methods ====================
    /**
     * Convierte Entity a DTO con estadísticas completas
     */
    @Named("toDtoComplete")
    default CustodioDto toDtoComplete(CustodioEntity entity) {
        if (entity == null) {
            return null;
        }

        CustodioDto dto = toDto(entity);

        // Calculamos estadísticas
        dto.setTotalEmpresas(entity.getTotalEmpresas());
        dto.setTotalTransacciones(entity.getTotalTransacciones());
        dto.setTotalCuentas(entity.getTotalCuentas());

        // Flags de existencia
        dto.setTieneEmpresas(entity.tieneEmpresas());
        dto.setTieneTransacciones(entity.tieneTransacciones());
        dto.setTieneCuentas(entity.tieneCuentas());

        // IDs de las colecciones (sin cargar entidades completas)
        if (entity.getEmpresas() != null) {
            dto.setEmpresaIds(entity.getEmpresas().stream()
                    .map(empresa -> empresa.getId())
                    .collect(Collectors.toList()));
        }

        if (entity.getCuentas() != null) {
            dto.setCuentaIds(entity.getCuentas().stream()
                    .map(cuenta -> cuenta.getId())
                    .collect(Collectors.toList()));
        }

        if (entity.getTransacciones() != null) {
            dto.setTransaccionIds(entity.getTransacciones().stream()
                    .map(transaccion -> transaccion.getId())
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    /**
     * Convierte Entity a DTO básico (solo información esencial)
     */
    @Named("toDtoBasic")
    default CustodioDto toDtoBasic(CustodioEntity entity) {
        if (entity == null) {
            return null;
        }

        return CustodioDto.builder()
                .id(entity.getId())
                .nombreCustodio(entity.getNombreCustodio())
                .totalEmpresas(entity.getTotalEmpresas())
                .tieneEmpresas(entity.tieneEmpresas())
                .build();
    }

    /**
     * Método para crear una nueva entidad
     */
    default CustodioEntity createNewCustodio(CrearCustodioDto dto) { 
        if (dto == null) {
            return null;
        }
        return CustodioEntity.builder()
                .nombreCustodio(dto.getNombreCustodio())
                .build();
    }

    /**
     * Convierte resultado de consulta de estadísticas a DTO
     */
    default CustodioDto fromEstadisticasArray(Object[] resultado) {
        if (resultado == null || resultado.length < 5) {
            return null;
        }

        return CustodioDto.builder()
                .id((Long) resultado[0])
                .nombreCustodio((String) resultado[1])
                .totalEmpresas(((Number) resultado[2]).intValue())
                .totalCuentas(((Number) resultado[3]).intValue())
                .totalTransacciones(((Number) resultado[4]).intValue())
                .build();
    }

    /**
     * Mapea lista de entidades a DTOs básicos (para listas de selección)
     */
    @Named("toDtoBasicList")
    default List<CustodioDto> toDtoBasicList(List<CustodioEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toDtoBasic)
                .collect(Collectors.toList());
    }

    /**
     * Mapea lista de entidades a DTOs completos (con estadísticas)
     */
    @Named("toDtoCompleteList")
    default List<CustodioDto> toDtoCompleteList(List<CustodioEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toDtoComplete)
                .collect(Collectors.toList());
    }
}
