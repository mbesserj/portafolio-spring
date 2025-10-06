package com.portafolio.model.dto;

import com.portafolio.model.enums.TipoAjuste;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para representar una propuesta de ajuste de costeo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AjustePropuestoDto {

    /**
     * Fecha del ajuste propuesto
     */
    private LocalDate fecha;

    /**
     * Tipo de movimiento del ajuste
     */
    private String tipoMovimiento;

    /**
     * Cantidad del ajuste
     */
    private BigDecimal cantidad;

    /**
     * Precio unitario del ajuste
     */
    private BigDecimal precio;

    /**
     * Monto total del ajuste (cantidad * precio)
     */
    private BigDecimal monto;

    /**
     * Observaciones del ajuste
     */
    private String observaciones;

    /**
     * Saldo de cantidad antes del ajuste
     */
    private BigDecimal saldoAnteriorCantidad;

    /**
     * Fecha del saldo anterior
     */
    private LocalDate saldoAnteriorFecha;

    /**
     * Tipo de ajuste (INGRESO/EGRESO)
     */
    private TipoAjuste tipoAjuste;

    /**
     * ID de la transacción de referencia
     */
    private Long transaccionReferenciaId;

    /**
     * Estado del ajuste
     */
    private String estado;

    /**
     * Prioridad del ajuste (ALTA, MEDIA, BAJA)
     */
    private String prioridad;

    /**
     * Impacto estimado del ajuste
     */
    private String impactoEstimado;

    /**
     * Constructor para compatibilidad con código existente
     */
    public AjustePropuestoDto(LocalDate fecha, String tipoMovimiento, BigDecimal cantidad, 
                             BigDecimal precio, String observaciones) {
        this.fecha = fecha;
        this.tipoMovimiento = tipoMovimiento;
        this.cantidad = cantidad;
        this.precio = precio;
        this.observaciones = observaciones;
        this.monto = cantidad != null && precio != null ? cantidad.multiply(precio) : BigDecimal.ZERO;
    }

    /**
     * Calcula el monto total automáticamente
     */
    public void calcularMonto() {
        if (cantidad != null && precio != null) {
            this.monto = cantidad.multiply(precio);
        } else {
            this.monto = BigDecimal.ZERO;
        }
    }

    /**
     * Verifica si el ajuste es válido
     */
    public boolean isValido() {
        return fecha != null && 
               cantidad != null && cantidad.compareTo(BigDecimal.ZERO) > 0 &&
               precio != null && precio.compareTo(BigDecimal.ZERO) >= 0 &&
               tipoAjuste != null;
    }

    /**
     * Obtiene una descripción resumida del ajuste
     */
    public String getDescripcionResumida() {
        return String.format("%s - %s: %s x %s = %s", 
                tipoAjuste != null ? tipoAjuste : "N/A",
                fecha != null ? fecha.toString() : "N/A",
                cantidad != null ? cantidad : "0",
                precio != null ? precio : "0",
                monto != null ? monto : "0");
    }
}