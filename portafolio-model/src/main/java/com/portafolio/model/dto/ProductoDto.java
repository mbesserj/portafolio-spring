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
public class ProductoDto {

    private Long id;
    private String producto;
    private String detalleProducto;
    
    // =========== Estadísticas ===========
    private Integer totalInstrumentos;
    
    // =========== Metadatos de auditoría ===========
    private LocalDate fechaCreacion;
    private LocalDate fechaModificacion;
}