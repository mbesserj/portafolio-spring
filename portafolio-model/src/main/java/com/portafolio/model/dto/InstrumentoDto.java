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
public class InstrumentoDto {

    // =========== Campos básicos ===========
    private Long id;
    private String instrumentoNemo;
    private String instrumentoNombre;
    
    // =========== Información de la relación ===========
    private Long productoId;
    private String productoNombre;
    
    // =========== Estadísticas ===========
    private Integer totalTransacciones;
    
    // =========== Metadatos de auditoría ===========
    private LocalDate fechaCreacion;
    private LocalDate fechaModificacion;

    // =========== Métodos de conveniencia ===========
    public String getDisplayName() {
        return String.format("%s (%s)", instrumentoNombre, instrumentoNemo);
    }
}