package com.portafolio.model.entities;

import com.portafolio.model.utiles.BaseEntity;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "portafolios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder 
@EqualsAndHashCode(callSuper = true)
public class PortafolioEntity extends BaseEntity implements Serializable {

    @Column(name = "nombre_portafolio", nullable = false)
    private String nombrePortafolio;
    
    @Column(name = "descripcion")
    private String descripcion;

    @OneToMany(mappedBy = "portafolio", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<PortafolioTransaccionEntity> portafolioTransacciones = new HashSet<>();

}