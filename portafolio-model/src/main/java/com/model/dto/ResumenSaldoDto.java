
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
public class ResumenSaldoDto {
    private Long instrumentoId;
    private String instrumentoNemo;
    private String instrumentoNombre;
    private Long empresaId;
    private Long custodioId;
    private BigDecimal saldoCantidad;
    private BigDecimal costoTotal;
    private BigDecimal costoUnitario;
    private BigDecimal cantidadMercado;
    private BigDecimal precioMercado;
    private BigDecimal valorMercado;
    private BigDecimal utilidadNoRealizada;
}