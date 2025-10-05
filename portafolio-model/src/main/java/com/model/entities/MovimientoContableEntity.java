package com.portafolio.model.entities;

import com.portafolio.model.enums.TipoEnumsCosteo;
import com.portafolio.model.utiles.BaseEntity;
import jakarta.persistence.*;
import java.io.Serializable;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "tipos_contables", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"tipo_contable"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true) // No hay relaciones que excluir
public class MovimientoContableEntity extends BaseEntity implements Serializable {

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_contable", nullable = false, unique = true)
    private TipoEnumsCosteo tipoContable;

    @Column(name = "descripcion", nullable = false)
    private String descripcionContable;
}