package com.portafolio.model.utiles;

import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder 
@MappedSuperclass 
public abstract class BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  
    
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDate fechaCreacion;  
    
    @Column(name = "fecha_modificacion")
    private LocalDate fechaModificacion;  
    
    @Column(name = "creado_por", nullable = false, updatable = false)
    private String creadoPor;  
    
    @Column(name = "modificado_por")
    private String modificadoPor;  
    
    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDate.now();
        this.creadoPor = "sistema";
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.fechaModificacion = LocalDate.now();
        this.modificadoPor = "sistema";
    }
}