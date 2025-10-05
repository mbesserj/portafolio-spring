package com.portafolio.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CierreContableDto {

    // =========== Campos básicos ===========
    private Long id;
    private Integer ejercicio;
    private BigDecimal cantidadCierre;
    private BigDecimal valorCierre;
    private BigDecimal valorUnitario;  // Campo calculado
    
    // =========== Datos desnormalizados de las relaciones ===========
    private Long empresaId;
    private String empresaRazonSocial;
    private String empresaRut;
    
    private Long custodioId;
    private String custodioNombre;
    
    private Long instrumentoId;
    private String instrumentoNemo;
    private String instrumentoNombre;
    
    // =========== Metadatos de auditoría ===========
    private LocalDate fechaCreacion;
    private LocalDate fechaModificacion;
    private String creadoPor;
    private String modificadoPor;
    
    // =========== Campos adicionales para reportes ===========
    private String claveCierre;  // Para identificación única
    
    // =========== Métodos de conveniencia ===========
    
    /**
     * Calcula el valor unitario
     */
    public BigDecimal getValorUnitario() {
        if (cantidadCierre == null || cantidadCierre.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        if (valorCierre == null) {
            return BigDecimal.ZERO;
        }
        return valorCierre.divide(cantidadCierre, 4, BigDecimal.ROUND_HALF_UP);
    }
    
    /**
     * Genera descripción completa del cierre
     */
    public String getDescripcionCompleta() {
        return String.format("%s - %s (%s) - %s - Ejercicio %d",
                empresaRazonSocial != null ? empresaRazonSocial : "N/A",
                custodioNombre != null ? custodioNombre : "N/A",
                instrumentoNemo != null ? instrumentoNemo : "N/A",
                instrumentoNombre != null ? instrumentoNombre : "N/A",
                ejercicio != null ? ejercicio : 0);
    }
    
    /**
     * Verifica si el cierre tiene información completa
     */
    public boolean isCompleto() {
        return ejercicio != null && 
               empresaId != null && 
               custodioId != null && 
               instrumentoId != null &&
               cantidadCierre != null && 
               valorCierre != null;
    }
}