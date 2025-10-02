package com.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActualizarProductoDto {
    private Long productoId;
    private String detalleProducto;
}