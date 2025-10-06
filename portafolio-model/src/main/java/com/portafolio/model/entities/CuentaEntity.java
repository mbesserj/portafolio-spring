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

@Entity
@Table(name = "cuentas", 
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_cuenta_empresa_custodio",
            columnNames = {"cuenta", "empresa_id", "custodio_id"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder  
@EqualsAndHashCode(callSuper = true, exclude = {"empresa", "custodio"})
public class CuentaEntity extends BaseEntity implements Serializable {

    @Column(name = "cuenta", nullable = false, length = 100)
    private String cuenta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private EmpresaEntity empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "custodio_id", nullable = false)
    private CustodioEntity custodio;

    /**
     * Genera una clave única para identificar la cuenta
     * @return clave en formato "cuenta|empresaId|custodioId"
     */
    public String getClaveCuenta() {
        if (cuenta == null || empresa == null || custodio == null) {
            return "invalid_key";
        }
        return cuenta + "|" + empresa.getId() + "|" + custodio.getId();
    }

    /**
     * Obtiene la descripción completa de la cuenta
     * @return descripción con empresa y custodio
     */
    public String getDescripcionCompleta() {
        return String.format("Cuenta %s - %s (%s)",
                cuenta != null ? cuenta : "N/A",
                empresa != null ? empresa.getRazonSocial() : "N/A",
                custodio != null ? custodio.getNombreCustodio() : "N/A");
    }

    @Override
    public String toString() {
        return String.format("CuentaEntity[cuenta=%s, empresa=%s, custodio=%s]",
                cuenta,
                empresa != null ? empresa.getRazonSocial() : "null",
                custodio != null ? custodio.getNombreCustodio() : "null");
    }
}