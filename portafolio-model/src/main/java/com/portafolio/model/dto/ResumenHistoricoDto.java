package com.portafolio.model.dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.portafolio.model.interfaces.InstrumentoData;

@Getter
@Setter
@NoArgsConstructor
public class ResumenHistoricoDto implements InstrumentoData {

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

    @Override
    public Long getInstrumentoId() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public String getNemo() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public String getNombreInstrumento() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}