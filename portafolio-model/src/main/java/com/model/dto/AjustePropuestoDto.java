package com.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder 
public class AjustePropuestoDto {

    private LocalDate fecha;
    private String tipo;
    private BigDecimal cantidad;
    private BigDecimal precio;
    private String observacion;
    private BigDecimal saldoAnteriorCantidad;
    private LocalDate saldoAnteriorFecha;

}