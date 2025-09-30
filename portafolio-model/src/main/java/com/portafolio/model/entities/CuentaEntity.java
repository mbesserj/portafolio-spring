package com.portafolio.model.entities;

import com.portafolio.model.utiles.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cuentas", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"cuenta", "empresa_id", "custodio_id"}, name = "uk_cuenta_empresa_custodio")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true, exclude = {"empresa", "custodio"})
public class CuentaEntity extends BaseEntity {

    @Column(name = "cuenta", nullable = false)
    private String cuenta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private EmpresaEntity empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "custodio_id", nullable = false)
    private CustodioEntity custodio;

}