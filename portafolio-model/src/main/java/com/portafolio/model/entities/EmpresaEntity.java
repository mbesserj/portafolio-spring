package com.portafolio.model.entities;

import com.portafolio.model.utiles.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDate;
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
@Table(name = "empresas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true, exclude = {"custodios", "cuentas", "transacciones"})
public class EmpresaEntity extends BaseEntity implements Serializable {

    @NotBlank(message = "El RUT no puede estar vacío.")
    @Size(max = 14, message = "El RUT no puede tener más de 14 caracteres.")
    @Column(name = "rut", unique = true, nullable = false)
    private String rut;

    @NotBlank(message = "La Razón Social no puede estar vacía.")
    @Size(max = 255, message = "La Razón Social no puede tener más de 255 caracteres.")
    @Column(name = "razonsocial", nullable = false)
    private String razonSocial;

    @Column(name = "fecha_creado", nullable = false)
    @Builder.Default
    private LocalDate fechaCreado = LocalDate.now();

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TransaccionEntity> transacciones = new LinkedList<>();

    @ManyToOne(optional = true)
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

    @Override
    public String toString() {
        return razonSocial; 
    }
}