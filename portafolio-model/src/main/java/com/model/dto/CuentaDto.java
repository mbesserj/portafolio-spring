package com.portafolio.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuentaDto {

    // =========== Campos básicos ===========
    private Long id;
    private String cuenta;
    
    // =========== Datos desnormalizados de las relaciones ===========
    private Long empresaId;
    private String empresaRazonSocial;
    private String empresaRut;
    
    private Long custodioId;
    private String custodioNombre;
    
    // =========== Metadatos de auditoría ===========
    private LocalDate fechaCreacion;
    private LocalDate fechaModificacion;
    private String creadoPor;
    private String modificadoPor;
    
    // =========== Campos adicionales para presentación ===========
    private String claveCuenta;        // Para identificación única
    private String descripcionCompleta; // Para mostrar en UI
    
    // =========== Métodos de conveniencia ===========
    
    /**
     * Genera la descripción completa de la cuenta
     */
    public String getDescripcionCompleta() {
        return String.format("Cuenta %s - %s (%s)",
                cuenta != null ? cuenta : "N/A",
                empresaRazonSocial != null ? empresaRazonSocial : "N/A",
                custodioNombre != null ? custodioNombre : "N/A");
    }
    
    /**
     * Genera la clave única de la cuenta
     */
    public String getClaveCuenta() {
        if (cuenta == null || empresaId == null || custodioId == null) {
            return "invalid_key";
        }
        return cuenta + "|" + empresaId + "|" + custodioId;
    }
    
    /**
     * Verifica si la cuenta tiene información completa
     */
    public boolean isCompleta() {
        return cuenta != null && 
               !cuenta.trim().isEmpty() &&
               empresaId != null && 
               custodioId != null;
    }
    
    /**
     * Obtiene una versión corta para mostrar en listas
     */
    public String getDisplayName() {
        return String.format("%s (%s)", 
                cuenta != null ? cuenta : "N/A",
                custodioNombre != null ? custodioNombre : "N/A");
    }
}