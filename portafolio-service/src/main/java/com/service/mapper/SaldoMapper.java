package com.service.mapper;

import com.model.dto.CargaTransaccionDto;
import com.model.dto.SaldoDto;
import com.model.entities.CustodioEntity;
import com.model.entities.EmpresaEntity;
import com.model.entities.InstrumentoEntity;
import com.model.entities.SaldoEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SaldoMapper {

    // ==================== Mapeo Principal (Entity a DTO) ====================
    @Mapping(source = "instrumento.id", target = "instrumentoId")
    @Mapping(source = "instrumento.instrumentoNemo", target = "instrumentoNemo")
    @Mapping(source = "custodio.id", target = "custodioId")
    @Mapping(source = "custodio.nombreCustodio", target = "custodioNombre")
    @Mapping(source = "empresa.id", target = "empresaId")
    @Mapping(source = "empresa.razonSocial", target = "empresaRazonSocial")
    SaldoDto toDto(SaldoEntity entity);

    // ==================== Mapeo Inverso (DTO a Entity) ====================
    @Mapping(target = "instrumento", ignore = true)
    @Mapping(target = "custodio", ignore = true)
    @Mapping(target = "empresa", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "creadoPor", ignore = true)
    @Mapping(target = "modificadoPor", ignore = true)
    @Mapping(target = "cuentaPsh", ignore = true)
    SaldoEntity toEntity(SaldoDto dto);

    // ==================== Lógica del Constructor Movida al Mapper ====================
    /**
     * Crea una SaldoEntity a partir de un DTO de carga y las entidades ya
     * resueltas. Esta es la nueva ubicación para la lógica de tu antiguo
     * constructor. El servicio se encargaría de buscar las entidades y pasarlas
     * a este método.
     */
    @Mapping(source = "cargaDto.fechaTransaccion", target = "fecha")
    @Mapping(source = "cargaDto.cuenta", target = "cuenta")
    @Mapping(source = "cargaDto.producto", target = "cuentaPsh")
    @Mapping(source = "empresa", target = "empresa")
    @Mapping(source = "custodio", target = "custodio")
    @Mapping(source = "instrumento", target = "instrumento")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaModificacion", ignore = true)
    @Mapping(target = "creadoPor", ignore = true)
    @Mapping(target = "modificadoPor", ignore = true)
    SaldoEntity toEntityFromCarga(CargaTransaccionDto cargaDto, EmpresaEntity empresa, CustodioEntity custodio, InstrumentoEntity instrumento);

    // ==================== Mapeos de Listas ====================
    List<SaldoDto> toDtoList(List<SaldoEntity> entities);
}
