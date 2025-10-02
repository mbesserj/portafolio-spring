
package com.model.dto;

import com.model.enums.TipoEnumsCosteo; // <-- IMPORTANTE: Importa tu enum
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoMovimientoEstado {

    private Long id;
    private String descripcion;
    private String movimiento;
    private TipoEnumsCosteo estado;
}