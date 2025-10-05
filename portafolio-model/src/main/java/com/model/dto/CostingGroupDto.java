package com.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO que representa un grupo de costeo.
 * Un grupo está definido por: Empresa + Custodio + Instrumento + Cuenta
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CostingGroupDto {

    /**
     * ID de la empresa
     */
    private Long empresaId;

    /**
     * ID del custodio
     */
    private Long custodioId;

    /**
     * ID del instrumento
     */
    private Long instrumentoId;

    /**
     * Número de cuenta
     */
    private String cuenta;

    /**
     * Clave de agrupación compuesta (empresaId|cuenta|custodioId|instrumentoId)
     */
    private String claveAgrupacion;

    /**
     * Nombre de la empresa
     */
    private String empresaNombre;

    /**
     * Nombre del custodio
     */
    private String custodioNombre;

    /**
     * Nemónico del instrumento
     */
    private String instrumentoNemonico;

    /**
     * Primera fecha de transacción en el grupo
     */
    private LocalDate fechaPrimeraTransaccion;

    /**
     * Última fecha de transacción en el grupo
     */
    private LocalDate fechaUltimaTransaccion;

    /**
     * Total de movimientos de kardex en el grupo
     */
    private Long totalMovimientos;

    /**
     * Indica si el grupo tiene transacciones pendientes de costeo
     */
    private Boolean tienePendientes;

    /**
     * Indica si el grupo tiene transacciones marcadas para revisión
     */
    private Boolean tieneParaRevision;

    /**
     * Constructor para queries de JPA Criteria
     */
    public CostingGroupDto(Long empresaId, Long custodioId, Long instrumentoId, String cuenta,
                          String claveAgrupacion, String empresaNombre, String custodioNombre,
                          String instrumentoNemonico, LocalDate fechaPrimera, LocalDate fechaUltima,
                          Long totalMovimientos) {
        this.empresaId = empresaId;
        this.custodioId = custodioId;
        this.instrumentoId = instrumentoId;
        this.cuenta = cuenta;
        this.claveAgrupacion = claveAgrupacion;
        this.empresaNombre = empresaNombre;
        this.custodioNombre = custodioNombre;
        this.instrumentoNemonico = instrumentoNemonico;
        this.fechaPrimeraTransaccion = fechaPrimera;
        this.fechaUltimaTransaccion = fechaUltima;
        this.totalMovimientos = totalMovimientos;
    }

    /**
     * Genera la clave de agrupación a partir de los IDs.
     */
    public void generarClaveAgrupacion() {
        if (empresaId != null && custodioId != null && instrumentoId != null && cuenta != null) {
            this.claveAgrupacion = String.format("%d|%s|%d|%d", 
                    empresaId, cuenta, custodioId, instrumentoId);
        }
    }

    /**
     * Descripción amigable del grupo para mostrar en UI.
     */
    public String getDescripcionGrupo() {
        return String.format("%s - %s - %s", 
                empresaNombre != null ? empresaNombre : "N/A",
                instrumentoNemonico != null ? instrumentoNemonico : "N/A",
                cuenta != null ? cuenta : "N/A");
    }

    /**
     * Verifica si el grupo está completo (tiene todos los campos requeridos).
     */
    public boolean isCompleto() {
        return empresaId != null && custodioId != null && 
               instrumentoId != null && cuenta != null && 
               !cuenta.trim().isEmpty();
    }
}