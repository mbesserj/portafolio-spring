package com.portafolio.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerfilDto {

    private Long id;
    private String perfil;
    
    // =========== Estadísticas ===========
    private Integer totalUsuarios;
    
    // =========== Metadatos de auditoría ===========
    private LocalDate fechaCreacion;
}