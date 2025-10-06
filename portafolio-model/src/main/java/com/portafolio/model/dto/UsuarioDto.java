package com.portafolio.model.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioDto {

    private Long id;
    private String usuario;
    private String correo;
    private LocalDate fechaInactivo;
    private boolean isActivo;
    
    // =========== Relación Aplanada ===========
    private Set<String> perfiles;
    
    // =========== Metadatos de auditoría ===========
    private LocalDate fechaCreacion;

}