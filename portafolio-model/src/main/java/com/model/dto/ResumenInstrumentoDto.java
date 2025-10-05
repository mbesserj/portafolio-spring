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
public class ResumenInstrumentoDto {

    private Long instrumentoId;
    private String nemo;
    private String nombreInstrumento;
    private BigDecimal saldoDisponible;
    private BigDecimal costoFifo;
    private BigDecimal valorDeMercado;
    private BigDecimal totalDividendos;
    private BigDecimal totalGastos;
    private BigDecimal utilidadRealizada;
    private BigDecimal utilidadNoRealizada;
    private BigDecimal rentabilidad;
    private BigDecimal totalCompras; 
}