package com.portafolio.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrearEmpresaDto {
    private String rut;
    private String razonSocial;
    private Long grupoEmpresaId; // Opcional
}

