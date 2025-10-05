package com.model.entities;

import com.model.utiles.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import lombok.Builder;

@Entity
@Table(name = "empresas",
    indexes = {
        @Index(name = "idx_empresa_rut", columnList = "rut", unique = true),
        @Index(name = "idx_empresa_razon_social", columnList = "razonsocial"),
        @Index(name = "idx_empresa_grupo", columnList = "grupo_empresa_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder  
@EqualsAndHashCode(callSuper = true, exclude = {"custodios", "cuentas", "transacciones", "grupoEmpresa"})
public class EmpresaEntity extends BaseEntity implements Serializable {

    @NotBlank(message = "El RUT no puede estar vacío.")
    @Size(max = 14, message = "El RUT no puede tener más de 14 caracteres.")
    @Column(name = "rut", unique = true, nullable = false, length = 14)
    private String rut;

    @NotBlank(message = "La Razón Social no puede estar vacía.")
    @Size(max = 255, message = "La Razón Social no puede tener más de 255 caracteres.")
    @Column(name = "razonsocial", nullable = false, length = 255)
    private String razonSocial;

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TransaccionEntity> transacciones = new LinkedList<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "grupo_empresa_id")
    private GrupoEmpresaEntity grupoEmpresa;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "empresa_custodio",
            joinColumns = @JoinColumn(name = "empresa_id"),
            inverseJoinColumns = @JoinColumn(name = "custodio_id")
    )
    @Builder.Default
    private Set<CustodioEntity> custodios = new HashSet<>();

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<CuentaEntity> cuentas = new HashSet<>();

    // ==================== Métodos de conveniencia ====================

    /**
     * Añade un custodio a la empresa
     */
    public void addCustodio(CustodioEntity custodio) {
        if (custodio != null) {
            this.custodios.add(custodio);
            custodio.getEmpresas().add(this);
        }
    }

    /**
     * Remueve un custodio de la empresa
     */
    public void removeCustodio(CustodioEntity custodio) {
        if (custodio != null) {
            this.custodios.remove(custodio);
            custodio.getEmpresas().remove(this);
        }
    }

    /**
     * Añade una transacción a la empresa
     */
    public void addTransaccion(TransaccionEntity transaccion) {
        if (transaccion != null) {
            this.transacciones.add(transaccion);
            transaccion.setEmpresa(this);
        }
    }

    /**
     * Remueve una transacción de la empresa
     */
    public void removeTransaccion(TransaccionEntity transaccion) {
        if (transaccion != null) {
            this.transacciones.remove(transaccion);
            transaccion.setEmpresa(null);
        }
    }

    /**
     * Añade una cuenta a la empresa
     */
    public void addCuenta(CuentaEntity cuenta) {
        if (cuenta != null) {
            this.cuentas.add(cuenta);
            cuenta.setEmpresa(this);
        }
    }

    /**
     * Remueve una cuenta de la empresa
     */
    public void removeCuenta(CuentaEntity cuenta) {
        if (cuenta != null) {
            this.cuentas.remove(cuenta);
            cuenta.setEmpresa(null);
        }
    }

    /**
     * Obtiene el número total de custodios asociados
     */
    public int getTotalCustodios() {
        return custodios != null ? custodios.size() : 0;
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
     * Verifica si la empresa tiene custodios asociados
     */
    public boolean tieneCustodios() {
        return custodios != null && !custodios.isEmpty();
    }

    /**
     * Verifica si la empresa tiene transacciones
     */
    public boolean tieneTransacciones() {
        return transacciones != null && !transacciones.isEmpty();
    }

    /**
     * Verifica si la empresa tiene cuentas
     */
    public boolean tieneCuentas() {
        return cuentas != null && !cuentas.isEmpty();
    }

    /**
     * Verifica si la empresa pertenece a un grupo
     */
    public boolean perteneceAGrupo() {
        return grupoEmpresa != null;
    }

    /**
     * Obtiene el nombre del grupo o "Sin grupo"
     */
    public String getNombreGrupo() {
        return grupoEmpresa != null ? grupoEmpresa.getNombreGrupo() : "Sin grupo";
    }

    /**
     * Formatea el RUT para mostrar (ej: 12.345.678-9)
     */
    public String getRutFormateado() {
        if (rut == null || rut.length() < 2) {
            return rut;
        }
        // Implementación básica de formato RUT chileno
        String numero = rut.substring(0, rut.length() - 1);
        String dv = rut.substring(rut.length() - 1);
        
        if (numero.length() > 6) {
            return numero.substring(0, numero.length() - 6) + "." +
                   numero.substring(numero.length() - 6, numero.length() - 3) + "." +
                   numero.substring(numero.length() - 3) + "-" + dv;
        }
        return rut;
    }

    @Override
    public String toString() {
        return razonSocial;
    }
}