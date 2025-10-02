package com.model.dto;

import com.model.utiles.Pk;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartolaFynsaDto {

    // Campos para construir la clave primaria de la entidad de staging
    private LocalDate transactionDate;
    private int rowNum;
    private String tipoClase;

    // Campos comunes y específicos leídos del archivo Fynsa
    private String razonSocial;
    private String rut;
    private String cuenta;
    private String cuentaPsh;
    private String custodio;
    private String instrumentoNemo;
    private String instrumentoNombre;
    private String tipoMovimiento;
    private String folio;
    private BigDecimal cantidad;
    private BigDecimal precio;
    private BigDecimal monto;
    private BigDecimal comisiones;
    private BigDecimal gastos;
    private BigDecimal montoTotal;
    private String moneda;
    
    // Campos de Stock
    private BigDecimal cantLibre;
    private BigDecimal cantGarantia;
    private BigDecimal cantPlazo;
    private BigDecimal cantVc;
    private BigDecimal cantTotal;
    private BigDecimal montoClp;
    private BigDecimal montoUsd;
    
}