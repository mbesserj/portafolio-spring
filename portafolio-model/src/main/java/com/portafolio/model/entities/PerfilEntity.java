
package com.portafolio.model.entities;

import com.portafolio.model.utiles.BaseEntity;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "perfiles")
@Data
@NoArgsConstructor
@AllArgsConstructor 
@EqualsAndHashCode(callSuper = true, exclude = "usuarios")
public class PerfilEntity extends BaseEntity implements Serializable {

    @Column(name = "perfil", unique = true, nullable = false)
    private String perfil; // Ej: "ADMINISTRADOR", "OPERADOR"

    @ManyToMany(mappedBy = "perfiles", fetch = FetchType.LAZY)
    private Set<UsuarioEntity> usuarios = new HashSet<>();
    
    public PerfilEntity(String perfil) {
        this.perfil = perfil;
    }
}