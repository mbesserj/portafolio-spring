package com.portafolio.model.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransaccionDto {

    private Long id;
    private LocalDate fechaTransaccion;
    private String folio;
    private String cuenta;
    private String glosa;
    private BigDecimal cantidad;
    private BigDecimal precio;
    private BigDecimal comision;
    private BigDecimal gastos;
    private BigDecimal iva;
    private BigDecimal montoTotal;
    private BigDecimal monto;
    private BigDecimal montoClp;
    private String moneda;
    private boolean costeado;

    private Long empresaId;
    private String empresaRazonSocial;
    private Long instrumentoId;
    private String instrumentoDisplayName;
    private Long custodioId;
    private String custodioNombre;
    private Long tipoMovimientoId;
    private String tipoMovimientoNombre;
    private String claveAgrupacion;
}
