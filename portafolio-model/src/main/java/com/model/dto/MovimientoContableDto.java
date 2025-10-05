package com.portafolio.model.dto;

import com.portafolio.model.enums.TipoEnumsCosteo;
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
public class MovimientoContableDto {

    private Long id;
    private TipoEnumsCosteo tipoContable;
    private String descripcionContable;
    
    // =========== Metadatos de auditoría ===========
    private LocalDate fechaCreacion;
    private LocalDate fechaModificacion;
}