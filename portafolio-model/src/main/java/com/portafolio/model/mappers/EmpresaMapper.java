package com.portafolio.model.mappers;

import com.portafolio.model.dto.EmpresaDto;
import com.portafolio.model.entities.CustodioEntity;
import com.portafolio.model.entities.EmpresaEntity;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class EmpresaMapper {

    public EmpresaDto toDto(EmpresaEntity entity) {
        if (entity == null) {
            return null;
        }

        return EmpresaDto.builder()
                .id(entity.getId())
                .rut(entity.getRut())
                .razonSocial(entity.getRazonSocial())
                .fechaCreado(entity.getFechaCreado())
                .grupoEmpresaId(entity.getGrupoEmpresa() != null ? entity.getGrupoEmpresa().getId() : null)
                .grupoEmpresaNombre(entity.getGrupoEmpresa() != null ? entity.getGrupoEmpresa().getNombreGrupo() : null)
                .custodiosIds(entity.getCustodios().stream()
                        .map(CustodioEntity::getId)
                        .collect(Collectors.toList()))
                .build();
    }

    public EmpresaEntity toEntity(EmpresaDto dto) {
        if (dto == null) {
            return null;
        }

        EmpresaEntity entity = new EmpresaEntity();
        entity.setId(dto.getId());
        entity.setRut(dto.getRut());
        entity.setRazonSocial(dto.getRazonSocial());
        entity.setFechaCreado(dto.getFechaCreado());
        
        return entity;
    }
}