package com.model.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaldoDto {

    private Long id;
    private LocalDate fecha;
    private String cuenta;
    private BigDecimal cantLibre;
    private BigDecimal cantGarantia;
    private BigDecimal cantPlazo;
    private BigDecimal cantVc;
    private BigDecimal cantidad;
    private BigDecimal precio;
    private BigDecimal montoClp;
    private BigDecimal montoUsd;
    private String moneda;
    
    // =========== Información de Relaciones Aplanadas ===========
    private Long instrumentoId;
    private String instrumentoNemo;
    private Long custodioId;
    private String custodioNombre;
    private Long empresaId;
    private String empresaRazonSocial;
    
    // =========== Metadatos de auditoría ===========
    private LocalDate fechaCreacion;
}