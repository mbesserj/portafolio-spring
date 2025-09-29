package com.portafolio.model.mappers;

import com.portafolio.model.dto.EmpresaDTO;
import com.portafolio.model.entities.CustodioEntity;
import com.portafolio.model.entities.EmpresaEntity;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class EmpresaMapper {

    public EmpresaDTO toDTO(EmpresaEntity entity) {
        if (entity == null) {
            return null;
        }

        return EmpresaDTO.builder()
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

    public EmpresaEntity toEntity(EmpresaDTO dto) {
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

    public EmpresaDTO toDto(EmpresaEntity savedEntity) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}