package com.portafolio.model.dto;

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
public class ResumenHistoricoDto {

    private Long instrumentoId;
    private String nemo;
    private String nombreInstrumento; 

    private BigDecimal totalCostoFifo;
    private BigDecimal totalGasto;
    private BigDecimal totalDividendo;
    private BigDecimal totalUtilidad;
    private BigDecimal totalTotal;

    // Constructor para la fila de "TOTALES"
    public ResumenHistoricoDto(String nemo) {
        this.nemo = nemo;
    }

}