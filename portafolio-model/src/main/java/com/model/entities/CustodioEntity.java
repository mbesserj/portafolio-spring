package com.model.entities;

import com.model.utiles.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import lombok.Builder;

@Entity
@Table(name = "custodios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder 
@EqualsAndHashCode(callSuper = true, exclude = {"empresas", "cuentas", "transacciones"})
public class CustodioEntity extends BaseEntity implements Serializable {

    @Column(name = "custodio", nullable = false, length = 255)
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

    // ==================== Métodos de conveniencia ====================

    /**
     * Añade una empresa al custodio
     */
    public void addEmpresa(EmpresaEntity empresa) {
        if (empresa != null) {
            this.empresas.add(empresa);
            empresa.getCustodios().add(this);
        }
    }

    /**
     * Remueve una empresa del custodio
     */
    public void removeEmpresa(EmpresaEntity empresa) {
        if (empresa != null) {
            this.empresas.remove(empresa);
            empresa.getCustodios().remove(this);
        }
    }

    /**
     * Añade una transacción al custodio
     */
    public void addTransaccion(TransaccionEntity transaccion) {
        if (transaccion != null) {
            this.transacciones.add(transaccion);
            transaccion.setCustodio(this);
        }
    }

    /**
     * Remueve una transacción del custodio
     */
    public void removeTransaccion(TransaccionEntity transaccion) {
        if (transaccion != null) {
            this.transacciones.remove(transaccion);
            transaccion.setCustodio(null);
        }
    }

    /**
     * Añade una cuenta al custodio
     */
    public void addCuenta(CuentaEntity cuenta) {
        if (cuenta != null) {
            this.cuentas.add(cuenta);
            cuenta.setCustodio(this);
        }
    }

    /**
     * Remueve una cuenta del custodio
     */
    public void removeCuenta(CuentaEntity cuenta) {
        if (cuenta != null) {
            this.cuentas.remove(cuenta);
            cuenta.setCustodio(null);
        }
    }

    /**
     * Obtiene el número total de empresas asociadas
     */
    public int getTotalEmpresas() {
        return empresas != null ? empresas.size() : 0;
    }

    /**
     * Obtiene el número total de transacciones
     */
    public int getTotalTransacciones() {
        return transacciones != null ? transacciones.size() : 0;
    }

    /**
     * Obtiene el número total de cuentas
     */
    public int getTotalCuentas() {
        return cuentas != null ? cuentas.size() : 0;
    }

    /**
     * Verifica si el custodio tiene empresas asociadas
     */
    public boolean tieneEmpresas() {
        return empresas != null && !empresas.isEmpty();
    }

    /**
     * Verifica si el custodio tiene transacciones
     */
    public boolean tieneTransacciones() {
        return transacciones != null && !transacciones.isEmpty();
    }

    /**
     * Verifica si el custodio tiene cuentas
     */
    public boolean tieneCuentas() {
        return cuentas != null && !cuentas.isEmpty();
    }

    @Override
    public String toString() {
        return nombreCustodio;
    }
}