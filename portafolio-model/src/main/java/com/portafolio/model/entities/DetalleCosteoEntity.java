package com.portafolio.model.entities;

import com.portafolio.model.utiles.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "detalle_costeos",
    indexes = {
        @Index(name = "idx_detalle_costeo_clave", columnList = "clave_agrupacion"),
        @Index(name = "idx_detalle_costeo_ingreso", columnList = "ingreso_id"),
        @Index(name = "idx_detalle_costeo_egreso", columnList = "egreso_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true, exclude = {"ingreso", "egreso"})
public class DetalleCosteoEntity extends BaseEntity implements Serializable {

    @Column(name = "clave_agrupacion", length = 255, nullable = false)
    private String claveAgrupacion;
    
    @Column(name = "cantidad_usada", precision = 19, scale = 6)
    private BigDecimal cantidadUsada;
    
    @Column(name = "costo_parcial", precision = 19, scale = 6)
    private BigDecimal costoParcial;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingreso_id")
    private TransaccionEntity ingreso;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "egreso_id")
    private TransaccionEntity egreso;

    // ==================== Métodos de negocio para el motor de costeo ====================

    /**
     * Calcula el costo unitario
     * @return costo por unidad (costo_parcial / cantidad_usada)
     */
    public BigDecimal getCostoUnitario() {
        if (cantidadUsada == null || cantidadUsada.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        if (costoParcial == null) {
            return BigDecimal.ZERO;
        }
        return costoParcial.divide(cantidadUsada, 6, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Verifica si el detalle de costeo está completo
     */
    public boolean isCompleto() {
        return claveAgrupacion != null && 
               cantidadUsada != null && 
               costoParcial != null &&
               ingreso != null && 
               egreso != null;
    }

    /**
     * Obtiene una descripción para debugging del motor de costeo
     */
    public String getDescripcionCosteo() {
        return String.format("Costeo[clave=%s, cantidad=%s, costo=%s, unitario=%s]",
                claveAgrupacion,
                cantidadUsada,
                costoParcial,
                getCostoUnitario());
    }

    @Override
    public String toString() {
        return String.format("DetalleCosteoEntity[id=%d, clave=%s, cantidad=%s, costo=%s]",
                getId(), claveAgrupacion, cantidadUsada, costoParcial);
    }
}