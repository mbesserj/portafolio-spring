package com.portafolio.model.entities;

import com.portafolio.model.utiles.BaseEntity;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true, exclude = "perfiles")
public class UsuarioEntity extends BaseEntity implements Serializable {

    @Column(name = "usuario", unique = true, nullable = false)
    private String usuario;
    
    @Column(name = "correo", unique = true)
    private String correo;
    
    @Column(name = "password", nullable = false)
    private String password;
    
    @Column(name = "fecha_inactivo")
    private LocalDate fechaInactivo;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "usuarios_perfiles",
        joinColumns = @JoinColumn(name = "usuario_id"),
        inverseJoinColumns = @JoinColumn(name = "perfil_id")
    )
    @Builder.Default
    private Set<PerfilEntity> perfiles = new HashSet<>();

    // ==================== Métodos de conveniencia (Mejora) ====================
    public void addPerfil(PerfilEntity perfil) {
        if (perfil != null) {
            this.perfiles.add(perfil);
            perfil.getUsuarios().add(this);
        }
    }

    public void removePerfil(PerfilEntity perfil) {
        if (perfil != null) {
            this.perfiles.remove(perfil);
            perfil.getUsuarios().remove(this);
        }
    }

    public boolean isActivo() {
        return this.fechaInactivo == null;
    }
}