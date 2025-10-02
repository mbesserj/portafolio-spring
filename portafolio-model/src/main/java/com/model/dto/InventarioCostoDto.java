package com.model.dto;

import java.math.BigDecimal;
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
public class InventarioCostoDto {
    private Long instrumentoId;
    private String instrumentoNemo;
    private String instrumentoNombre;
    private BigDecimal saldoCantidadFinal;
    private BigDecimal costoTotalFifo;
}