package com.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrupoEmpresaDto {

    // =========== Campos básicos ===========
    private Long id;
    private String nombreGrupo;
    
    // =========== Estadísticas ===========
    private Integer totalEmpresas;
    
    // =========== Listas de IDs relacionados ===========
    private List<Long> empresaIds;
    
    // =========== Metadatos de auditoría ===========
    private LocalDate fechaCreacion;
    private LocalDate fechaModificacion;
}