package com.portafolio.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustodioDto {

    // =========== Campos básicos ===========
    private Long id;
    private String nombreCustodio;
    
    // =========== Estadísticas (sin cargar las colecciones completas) ===========
    private Integer totalEmpresas;
    private Integer totalTransacciones;
    private Integer totalCuentas;
    
    // =========== Listas de IDs relacionados (para operaciones) ===========
    private List<Long> empresaIds;
    private List<Long> cuentaIds;
    private List<Long> transaccionIds;
    
    // =========== Metadatos de auditoría ===========
    private LocalDate fechaCreacion;
    private LocalDate fechaModificacion;
    private String creadoPor;
    private String modificadoPor;
    
    // =========== Campos adicionales para presentación ===========
    private boolean tieneEmpresas;
    private boolean tieneTransacciones;
    private boolean tieneCuentas;
    
    // =========== Métodos de conveniencia ===========
    
    /**
     * Verifica si el custodio está activo (tiene empresas asociadas)
     */
    public boolean isActivo() {
        return totalEmpresas != null && totalEmpresas > 0;
    }
    
    /**
     * Obtiene el nombre para mostrar en listas
     */
    public String getDisplayName() {
        return nombreCustodio != null ? nombreCustodio : "Sin nombre";
    }
    
    /**
     * Obtiene una descripción con estadísticas
     */
    public String getDescripcionConEstadisticas() {
        return String.format("%s (%d empresas, %d cuentas, %d transacciones)",
                getDisplayName(),
                totalEmpresas != null ? totalEmpresas : 0,
                totalCuentas != null ? totalCuentas : 0,
                totalTransacciones != null ? totalTransacciones : 0);
    }
    
    /**
     * Verifica si el custodio tiene información completa
     */
    public boolean isCompleto() {
        return nombreCustodio != null && !nombreCustodio.trim().isEmpty();
    }
    
    /**
     * Calcula el promedio de transacciones por empresa
     */
    public Double getPromedioTransaccionesPorEmpresa() {
        if (totalEmpresas == null || totalEmpresas == 0 || totalTransacciones == null) {
            return 0.0;
        }
        return totalTransacciones.doubleValue() / totalEmpresas.doubleValue();
    }
    
    /**
     * Calcula el promedio de cuentas por empresa
     */
    public Double getPromedioCuentasPorEmpresa() {
        if (totalEmpresas == null || totalEmpresas == 0 || totalCuentas == null) {
            return 0.0;
        }
        return totalCuentas.doubleValue() / totalEmpresas.doubleValue();
    }
}