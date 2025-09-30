package com.portafolio.model.dto;

import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Corresponde a las clasificaciones de los activos
 * por ejemplo fondos mutuos, acciones, forwards, etc.
 */
@Data // Genera getters, setters, toString, equals y hashCode
@NoArgsConstructor // Genera constructor sin argumentos
public class ProductoDto {

    private Long id;
    private String producto;
    private LocalDate fechaCreado;

    public ProductoDto(String producto) {
        this.producto = producto;
    }    

    public ProductoDto(Long id, String producto, LocalDate fechaCreado) {
        this.id = id;
        this.producto = producto;
        this.fechaCreado = fechaCreado;
    }
}