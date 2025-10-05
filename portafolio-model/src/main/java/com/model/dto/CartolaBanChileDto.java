package com.portafolio.model.dto;

import com.portafolio.model.utiles.Pk;
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
public class CartolaBanChileDto {

    private LocalDate fechaTransaccion;
    private int rowNum;
    private String tipoClase;
        
    // --- Todos los demás campos de "movimientos" y "saldos" se mantienen igual ---
    
    // Sección de movimientos
    private String clienteMovimiento;
    private String rutMovimiento;
    private String cuentaMovimiento;
    private LocalDate fechaLiquidacion;
    private LocalDate fechaMovimiento;
    private String productoMovimiento;
    private String movimientoCaja;
    private String operacion;
    private String instrumentoNemo;
    private String instrumentoNombre;
    private String detalle;
    private BigDecimal cantidad;
    private String monedaOrigen;
    private BigDecimal precio;
    private BigDecimal comision;
    private BigDecimal iva;
    private BigDecimal montoTransadoMO;
    private BigDecimal montoTransadoClp;

    // Sección de saldos
    private String clienteSaldo;
    private LocalDate fechaSaldo;
    private String cuentaSaldo;
    private String productoSaldo;
    private String instrumentoSaldo;
    private String nombreSaldo;
    private String emisor;
    private String monedaOrigenSaldo;
    private BigDecimal montoInicialOrigen;
    private BigDecimal ingresoNetoOrigen;
    private BigDecimal montoFinalOrigen;
    private BigDecimal montoFinalClp;
    private BigDecimal nominalesFinal;
    private BigDecimal precioTasaSaldo;
    private BigDecimal variacionPeriodoOrigen;
    private BigDecimal rentabilidadPeriodoOrigen;

}