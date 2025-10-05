package com.portafolio.model.entities;

import com.portafolio.model.utiles.BaseEntity;
import jakarta.persistence.*;
import java.io.Serializable;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "tipo_movimientos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true, exclude = "movimientoContable") 
public class TipoMovimientoEntity extends BaseEntity implements Serializable {

    @Column(name = "tipo_movimiento", nullable = false, unique = true)
    private String tipoMovimiento;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "es_saldo_inicial", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean esSaldoInicial;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movimiento_contable_id")
    private MovimientoContableEntity movimientoContable;

    public TipoMovimientoEntity(String tipoMovimiento, String descripcion) {
        this.tipoMovimiento = tipoMovimiento;
        this.descripcion = descripcion;
    }
}