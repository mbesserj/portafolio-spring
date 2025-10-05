package com.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CrearTipoMovimientoDto {
    private String tipoMovimiento;
    private String descripcion;
    private boolean esSaldoInicial;
    private Long movimientoContableId; 
}