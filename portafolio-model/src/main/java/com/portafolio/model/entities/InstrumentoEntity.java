package com.portafolio.model.entities;

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
@Table(
        name = "instrumentos",
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"instrumento_nemo"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class InstrumentoEntity extends BaseEntity implements Serializable {

    @Column(name = "nemo", nullable = false, unique = true)
    private String instrumentoNemo;

    @Column(name = "instrumento", nullable = false)
    private String instrumentoNombre;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id", referencedColumnName = "id", nullable = false)
    private ProductoEntity producto;

}