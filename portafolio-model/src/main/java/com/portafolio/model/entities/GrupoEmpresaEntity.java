package com.portafolio.model.entities;

import com.portafolio.model.utiles.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "grupo_empresas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true) 
public class GrupoEmpresaEntity extends BaseEntity implements Serializable {

    @Column(name = "nombre_grupo")
    private String nombreGrupo;

    @OneToMany(mappedBy = "grupoEmpresa", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<EmpresaEntity> empresas = new LinkedList<>();
}