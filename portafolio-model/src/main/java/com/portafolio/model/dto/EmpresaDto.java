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
public class EmpresaDto {

    // =========== Campos básicos ===========
    private Long id;
    private String rut;
    private String rutFormateado;
    private String razonSocial;
    
    // =========== Grupo empresarial ===========
    private Long grupoEmpresaId;
    private String grupoEmpresaNombre;
    private boolean perteneceAGrupo;
    
    // =========== Estadísticas (sin cargar las colecciones completas) ===========
    private Integer totalCustodios;
    private Integer totalTransacciones;
    private Integer totalCuentas;
    
    // =========== Listas de IDs relacionados (para operaciones) ===========
    private List<Long> custodioIds;
    private List<Long> cuentaIds;
    private List<Long> transaccionIds;
    
    // =========== Metadatos de auditoría ===========
    private LocalDate fechaCreacion;
    private LocalDate fechaModificacion;
    private String creadoPor;
    private String modificadoPor;
    
    // =========== Campos adicionales para presentación ===========
    private boolean tieneCustodios;
    private boolean tieneTransacciones;
    private boolean tieneCuentas;
    
    // =========== Métodos de conveniencia ===========
    
    /**
     * Verifica si la empresa está activa (tiene custodios o transacciones)
     */
    public boolean isActiva() {
        return (totalCustodios != null && totalCustodios > 0) || 
               (totalTransacciones != null && totalTransacciones > 0);
    }
    
    /**
     * Obtiene el nombre para mostrar en listas
     */
    public String getDisplayName() {
        return razonSocial != null ? razonSocial : "Sin nombre";
    }
    
    /**
     * Obtiene una descripción con estadísticas
     */
    public String getDescripcionConEstadisticas() {
        return String.format("%s (%d custodios, %d cuentas, %d transacciones)",
                getDisplayName(),
                totalCustodios != null ? totalCustodios : 0,
                totalCuentas != null ? totalCuentas : 0,
                totalTransacciones != null ? totalTransacciones : 0);
    }
    
    /**
     * Verifica si la empresa tiene información completa
     */
    public boolean isCompleta() {
        return rut != null && !rut.trim().isEmpty() &&
               razonSocial != null && !razonSocial.trim().isEmpty();
    }
    
    /**
     * Obtiene el nombre del grupo o "Sin grupo"
     */
    public String getNombreGrupo() {
        return grupoEmpresaNombre != null ? grupoEmpresaNombre : "Sin grupo";
    }
    
    /**
     * Obtiene una descripción completa con grupo
     */
    public String getDescripcionCompleta() {
        return String.format("%s (%s) - %s", 
                razonSocial != null ? razonSocial : "N/A",
                rutFormateado != null ? rutFormateado : rut,
                getNombreGrupo());
    }
    
    /**
     * Calcula el promedio de transacciones por custodio
     */
    public Double getPromedioTransaccionesPorCustodio() {
        if (totalCustodios == null || totalCustodios == 0 || totalTransacciones == null) {
            return 0.0;
        }
        return totalTransacciones.doubleValue() / totalCustodios.doubleValue();
    }
    
    /**
     * Calcula el promedio de cuentas por custodio
     */
    public Double getPromedioCuentasPorCustodio() {
        if (totalCustodios == null || totalCustodios == 0 || totalCuentas == null) {
            return 0.0;
        }
        return totalCuentas.doubleValue() / totalCustodios.doubleValue();
    }
    
    /**
     * Obtiene una versión resumida para listas de selección
     */
    public String getResumen() {
        return String.format("%s (%s)", 
                razonSocial != null ? razonSocial : "N/A",
                rutFormateado != null ? rutFormateado : rut);
    }
}