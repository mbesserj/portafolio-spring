package com.model.entities;

import com.model.utiles.BaseEntity;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "productos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true, exclude = "instrumentos") 
public class ProductoEntity extends BaseEntity implements Serializable {

    @Column(name = "producto", nullable = false, unique = true)
    private String producto;
    
    @Column(name = "detalle_producto")
    private String detalleProducto;
    
    @OneToMany(mappedBy = "producto", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<InstrumentoEntity> instrumentos = new HashSet<>();
    
    // ==================== Métodos de conveniencia (Mejora) ====================
    public void addInstrumento(InstrumentoEntity instrumento) {
        if (instrumento != null) {
            this.instrumentos.add(instrumento);
            instrumento.setProducto(this);
        }
    }

    public void removeInstrumento(InstrumentoEntity instrumento) {
        if (instrumento != null) {
            this.instrumentos.remove(instrumento);
            instrumento.setProducto(null);
        }
    }
}