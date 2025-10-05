package com.model.dto;

import com.model.entities.CargaTransaccionEntity;
import com.model.utiles.Pk;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;

/**
 * DTO genérico para la carga de transacciones. Esta clase sirve como un
 * contenedor de datos intermedio que puede ser mapeado desde varios tipos de
 * archivos de origen (MovimientoTitulo, MovimientoCaja, Stock).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CargaTransaccionDto {

    private LocalDate fechaTransaccion;
    private int rowNum;
    private String tipoClase;

    // Campos comunes a todos los archivos
    private String razonSocial;
    private String rut;
    private String cuenta;
    private String custodioNombre;
    private String tipoMovimiento;
    private String movimientoCaja;
    private String folio;
    private String producto;
    private String instrumentoNemo;
    private String instrumentoNombre;
    private String moneda;
    private BigDecimal cantidad;
    private BigDecimal precio;
    private BigDecimal monto;
    private BigDecimal montoTotal;
    private BigDecimal comision;
    private BigDecimal gastos;
    private BigDecimal iva;

    // Campos específicos de Stock
    private String cuentaPsh;
    private BigDecimal cantLibre;
    private BigDecimal cantGarantia;
    private BigDecimal cantPlazo;
    private BigDecimal cantVc;
    private BigDecimal cantTotal;
    private BigDecimal montoClp;
    private BigDecimal montoUsd;

}