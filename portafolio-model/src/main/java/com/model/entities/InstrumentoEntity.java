package com.model.entities;

import com.model.utiles.BaseEntity;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "instrumentos", uniqueConstraints = @UniqueConstraint(columnNames = {"nemo"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true, exclude = {"producto", "transacciones"}) // Excluimos relaciones
public class InstrumentoEntity extends BaseEntity implements Serializable {

    @Column(name = "nemo", nullable = false, unique = true)
    private String instrumentoNemo;

    @Column(name = "instrumento", nullable = false)
    private String instrumentoNombre;

    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "producto_id", referencedColumnName = "id", nullable = false)
    private ProductoEntity producto;
    
    @OneToMany(mappedBy = "instrumento", fetch = FetchType.LAZY)
    @Builder.Default
    private List<TransaccionEntity> transacciones = new LinkedList<>();

    // ==================== Métodos de conveniencia ====================
    public int getTotalTransacciones() {
        return transacciones != null ? transacciones.size() : 0;
    }
}