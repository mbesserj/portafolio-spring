package com.model.entities;

import com.model.utiles.BaseEntity;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "portafolios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true, exclude = "portafolioTransacciones") // ✅ MEJORA: Excluimos la colección
public class PortafolioEntity extends BaseEntity implements Serializable {

    @Column(name = "nombre_portafolio", nullable = false, unique = true)
    private String nombrePortafolio;
    
    @Column(name = "descripcion")
    private String descripcion;

    @OneToMany(mappedBy = "portafolio", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<PortafolioTransaccionEntity> portafolioTransacciones = new HashSet<>();

    // ==================== Métodos de conveniencia (Mejora) ====================
    public void addPortafolioTransaccion(PortafolioTransaccionEntity transaccion) {
        if (transaccion != null) {
            this.portafolioTransacciones.add(transaccion);
            transaccion.setPortafolio(this);
        }
    }

    public void removePortafolioTransaccion(PortafolioTransaccionEntity transaccion) {
        if (transaccion != null) {
            this.portafolioTransacciones.remove(transaccion);
            transaccion.setPortafolio(null);
        }
    }
}