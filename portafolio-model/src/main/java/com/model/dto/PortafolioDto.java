package com.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortafolioDto {

    private Long id;
    private String nombrePortafolio;
    private String descripcion;
    
    // =========== Estadísticas ===========
    private Integer totalTransacciones;
    
    // =========== Listas de IDs relacionados (opcional pero útil) ===========
    private Set<Long> transaccionIds;
    
    // =========== Metadatos de auditoría ===========
    private LocalDate fechaCreacion;
    private LocalDate fechaModificacion;
}