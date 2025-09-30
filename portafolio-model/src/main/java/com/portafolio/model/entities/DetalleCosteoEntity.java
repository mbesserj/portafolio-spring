package com.portafolio.model.entities;

import com.portafolio.model.utiles.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "detalle_costeos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class DetalleCosteoEntity extends BaseEntity implements Serializable {

    @Column(name = "clave_agrupacion", length = 255)
    private String claveAgrupacion;
    
    @Column(name = "cantidad_usada")
    private BigDecimal cantidadUsada;
    
    @Column(name = "costo_parcial")
    private BigDecimal costoParcial;
    
    @ManyToOne
    private TransaccionEntity ingreso;

    @ManyToOne
    private TransaccionEntity egreso;

}