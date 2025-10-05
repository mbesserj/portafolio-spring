package com.model.dto;

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
public class AuditoriaDto {

    // =========== Campos básicos ===========
    private Long id;
    private LocalDate fechaAuditoria;
    private LocalDate fechaArchivo;
    private LocalDate fechaDatos;
    
    // =========== Identificación del registro ===========
    private String tipoEntidad;
    private String valorClave;
    private String descripcion;
    
    // =========== Información del archivo ===========
    private String archivoOrigen;
    private Integer filaNumero;
    private String motivo;
    
    // =========== Estadísticas de procesamiento ===========
    private Integer registrosInsertados;
    private Integer registrosRechazados;
    private Integer registrosDuplicados;
    
    // =========== Metadatos de auditoría ===========
    private LocalDate fechaCreacion;
    private LocalDate fechaModificacion;
    private String creadoPor;
    private String modificadoPor;
    
    // =========== Métodos de conveniencia ===========
    
    /**
     * Obtiene el total de registros procesados
     */
    public Integer getTotalRegistrosProcesados() {
        int total = 0;
        if (registrosInsertados != null) total += registrosInsertados;
        if (registrosRechazados != null) total += registrosRechazados;
        if (registrosDuplicados != null) total += registrosDuplicados;
        return total;
    }
    
    /**
     * Obtiene el porcentaje de éxito en el procesamiento
     */
    public Double getPorcentajeExito() {
        Integer total = getTotalRegistrosProcesados();
        if (total == 0) return 0.0;
        if (registrosInsertados == null) return 0.0;
        return (registrosInsertados.doubleValue() / total.doubleValue()) * 100.0;
    }
}