package com.portafolio.ui.mapper;

import com.portafolio.model.dto.ActualizarProductoDto;
import com.portafolio.model.dto.CrearProductoDto;
import com.portafolio.model.dto.ProductoDto;
import com.portafolio.model.entities.InstrumentoEntity;
import com.portafolio.model.entities.ProductoEntity;
import org.mapstruct.*;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface ProductoMapper {

    // ==================== Mapeos Principales (Entity a DTO) ====================
    @Mapping(source = "instrumentos", target = "totalInstrumentos", qualifiedByName = "countInstrumentos")
    @Named("toDtoComplete")
    ProductoDto toDtoComplete(ProductoEntity entity);

    @Named("toDtoBasic")
    @Mapping(target = "totalInstrumentos", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    ProductoDto toDtoBasic(ProductoEntity entity);
    
    @Named("countInstrumentos")
    default Integer countInstrumentos(Set<InstrumentoEntity> instrumentos) {
        return instrumentos != null ? instrumentos.size() : 0;
    }

    // ==================== Mapeo Inverso (DTO a Entity) ====================
    @Mapping(target = "instrumentos", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "creadoPor", ignore = true)
    @Mapping(target = "modificadoPor", ignore = true)
    ProductoEntity toEntity(CrearProductoDto dto);

    // ==================== Mapeo para Actualizaciones ====================
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "producto", ignore = true)
    @Mapping(target = "instrumentos", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "creadoPor", ignore = true)
    @Mapping(target = "modificadoPor", ignore = true)
    void updateEntityFromDto(ActualizarProductoDto dto, @MappingTarget ProductoEntity entity);

    // ==================== Mapeos de Listas ====================
    @Named("toDtoBasicList")
    @IterableMapping(qualifiedByName = "toDtoBasic")
    List<ProductoDto> toDtoBasicList(List<ProductoEntity> entities);

    @Named("toDtoCompleteList")
    @IterableMapping(qualifiedByName = "toDtoComplete")
    List<ProductoDto> toDtoCompleteList(List<ProductoEntity> entities);

    public List<ProductoDto> toDtoList(List<ProductoEntity> productos);

    @Mapping(target = "totalInstrumentos", ignore = true)
    public ProductoDto toDto(ProductoEntity producto);
    
}