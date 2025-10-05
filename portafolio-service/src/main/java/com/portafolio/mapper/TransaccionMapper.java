package com.portafolio.mapper;

import com.portafolio.model.dto.TransaccionDto;
import com.portafolio.model.entities.*;
import org.mapstruct.*;
import java.util.List;

@Mapper(componentModel = "spring")
public interface TransaccionMapper {

    // ==================== Mapeo de Entidad Final a DTO (Para mostrar) ====================
    
    @Mapping(source = "empresa.id", target = "empresaId")
    @Mapping(source = "empresa.razonSocial", target = "empresaRazonSocial")
    @Mapping(source = "instrumento.id", target = "instrumentoId")
    @Mapping(source = "instrumento.instrumentoNombre", target = "instrumentoDisplayName")
    @Mapping(source = "custodio.id", target = "custodioId")
    @Mapping(source = "custodio.nombreCustodio", target = "custodioNombre")
    @Mapping(source = "tipoMovimiento.id", target = "tipoMovimientoId")
    @Mapping(source = "tipoMovimiento.tipoMovimiento", target = "tipoMovimientoNombre")
    @Mapping(source = "claveAgrupacion", target = "claveAgrupacion")
    @Named("toDtoComplete")
    TransaccionDto toDto(TransaccionEntity entity);

    // ==================== Mapeo Inverso Simple (Para crear desde API) ====================
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "empresa", ignore = true)
    @Mapping(target = "instrumento", ignore = true)
    @Mapping(target = "custodio", ignore = true)
    @Mapping(target = "tipoMovimiento", ignore = true)
    @Mapping(target = "costeado", ignore = true)
    @Mapping(target = "paraRevision", ignore = true)
    @Mapping(target = "ignorarEnCosteo", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "creadoPor", ignore = true)
    @Mapping(target = "modificadoPor", ignore = true)
    TransaccionEntity toEntity(TransaccionDto dto);
    
    // ==================== Mapeo para Actualizaciones ====================
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "empresa", ignore = true)
    @Mapping(target = "instrumento", ignore = true)
    @Mapping(target = "custodio", ignore = true)
    @Mapping(target = "tipoMovimiento", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "creadoPor", ignore = true)
    @Mapping(target = "modificadoPor", ignore = true)
    @Mapping(target = "paraRevision", ignore = true)
    @Mapping(target = "ignorarEnCosteo", ignore = true)
    void updateEntityFromDto(TransaccionDto dto, @MappingTarget TransaccionEntity entity);

    // ==================== Mapeo de Staging a Entidad Final (Para normalizar) ====================
    
    /**
     * ✅ MÉTODO CORREGIDO: Ahora mapea directamente desde los campos de CargaTransaccionEntity.
     */
    @Mapping(source = "carga.fechaTransaccion", target = "fechaTransaccion") // <-- CORREGIDO
    @Mapping(source = "carga.folio", target = "folio")
    @Mapping(source = "carga.cuenta", target = "cuenta")
    @Mapping(source = "carga.cantidad", target = "cantidad")
    @Mapping(source = "carga.precio", target = "precio")
    @Mapping(source = "carga.comision", target = "comision")
    @Mapping(source = "carga.gastos", target = "gastos")
    @Mapping(source = "carga.iva", target = "iva")
    @Mapping(source = "carga.montoTotal", target = "montoTotal")
    @Mapping(source = "carga.monto", target = "monto")
    @Mapping(source = "carga.montoClp", target = "montoClp")
    @Mapping(source = "carga.moneda", target = "moneda")
    
    @Mapping(target = "glosa", constant = "Transacción normalizada desde carga")
    @Mapping(source = "empresa", target = "empresa")
    @Mapping(source = "custodio", target = "custodio")
    @Mapping(source = "instrumento", target = "instrumento")
    @Mapping(source = "tipoMovimiento", target = "tipoMovimiento")
    
    @Mapping(target = "costeado", constant = "false")
    @Mapping(target = "paraRevision", constant = "false")
    @Mapping(target = "ignorarEnCosteo", constant = "false")

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "creadoPor", ignore = true)
    @Mapping(target = "modificadoPor", ignore = true)
    TransaccionEntity fromCargaTransaccion(
            CargaTransaccionEntity carga,
            EmpresaEntity empresa,
            InstrumentoEntity instrumento,
            CustodioEntity custodio,
            TipoMovimientoEntity tipoMovimiento
    );
    
    // ==================== Mapeos de Listas ====================
    
    @IterableMapping(qualifiedByName = "toDtoComplete")
    List<TransaccionDto> toDtoList(List<TransaccionEntity> entities);
}