package com.portafolio.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrearInstrumentoDto {
    private String instrumentoNemo;
    private String instrumentoNombre;
    private Long productoId; 
}