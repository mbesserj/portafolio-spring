package com.model.entities;

import com.model.utiles.BaseEntity;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "perfiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true, exclude = "usuarios")
public class PerfilEntity extends BaseEntity implements Serializable {

    @Column(name = "perfil", unique = true, nullable = false)
    private String perfil; 

    @ManyToMany(mappedBy = "perfiles", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<UsuarioEntity> usuarios = new HashSet<>();
    
    public PerfilEntity(String perfil) {
        this.perfil = perfil;
    }

    // ==================== Métodos de conveniencia (Mejora) ====================
    public void addUsuario(UsuarioEntity usuario) {
        if (usuario != null) {
            this.usuarios.add(usuario);
            usuario.getPerfiles().add(this);
        }
    }

    public void removeUsuario(UsuarioEntity usuario) {
        if (usuario != null) {
            this.usuarios.remove(usuario);
            usuario.getPerfiles().remove(this);
        }
    }
}