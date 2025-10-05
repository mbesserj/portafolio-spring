package com.model.entities;

import com.model.utiles.BaseEntity;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "grupo_empresas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true, exclude = "empresas") // Excluimos la colección
public class GrupoEmpresaEntity extends BaseEntity implements Serializable {

    @Column(name = "nombre_grupo", nullable = false, unique = true)
    private String nombreGrupo;

    @OneToMany(mappedBy = "grupoEmpresa", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<EmpresaEntity> empresas = new LinkedList<>();

    // ==================== Métodos de conveniencia (Mejora) ====================
    public void addEmpresa(EmpresaEntity empresa) {
        if (empresa != null) {
            this.empresas.add(empresa);
            empresa.setGrupoEmpresa(this);
        }
    }

    public void removeEmpresa(EmpresaEntity empresa) {
        if (empresa != null) {
            this.empresas.remove(empresa);
            empresa.setGrupoEmpresa(null);
        }
    }
}