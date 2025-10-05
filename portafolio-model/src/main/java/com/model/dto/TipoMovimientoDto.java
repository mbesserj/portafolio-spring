package com.model.dto;

import com.model.enums.TipoEnumsCosteo;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoMovimientoDto {

    private Long id;
    private String tipoMovimiento;
    private String descripcion;
    private boolean esSaldoInicial;
    
    // =========== Información de la Relación Aplanada ===========
    private Long movimientoContableId;
    private TipoEnumsCosteo tipoContable; // Del MovimientoContable
    private String descripcionContable; // Del MovimientoContable
    
    // =========== Metadatos de auditoría ===========
    private LocalDate fechaCreacion;
}