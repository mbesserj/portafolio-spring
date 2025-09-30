package com.portafolio.model.entities;

import com.portafolio.model.enums.TipoEnumsCosteo;
import com.portafolio.model.utiles.BaseEntity;
import jakarta.persistence.*;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tipos_contables")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true) 
public class MovimientoContableEntity extends BaseEntity implements Serializable {

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_contable")
    private TipoEnumsCosteo tipoContable;

    @Column(name = "descripcion")
    private String descripcionContable;
    
}