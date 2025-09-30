package com.portafolio.model.entities;

import com.portafolio.model.utiles.BaseEntity;
import jakarta.persistence.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "custodios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true, exclude = {"empresas", "cuentas", "transacciones"})
public class CustodioEntity extends BaseEntity implements Serializable {

    @Column(name = "custodio", nullable = false)
    private String nombreCustodio;

    @ManyToMany(mappedBy = "custodios", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<EmpresaEntity> empresas = new HashSet<>();

    @OneToMany(mappedBy = "custodio", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TransaccionEntity> transacciones = new LinkedList<>();

    @OneToMany(mappedBy = "custodio", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<CuentaEntity> cuentas = new HashSet<>();

    @Override
    public String toString() {
        return nombreCustodio; 
    }
}