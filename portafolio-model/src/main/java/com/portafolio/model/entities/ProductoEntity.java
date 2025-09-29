package com.portafolio.model.entities;

import com.portafolio.model.utiles.BaseEntity;
import jakarta.persistence.*;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "productos")
@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@EqualsAndHashCode(callSuper = true) 
public class ProductoEntity extends BaseEntity implements Serializable {

    @Column(name = "producto", nullable = false, unique = true)
    private String producto;
    
    @Column(name = "detalle_producto")
    private String detalleProducto;

}