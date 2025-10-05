package com.service.interfaces;

import com.model.dto.ActualizarProductoDto;
import com.model.dto.CrearProductoDto;
import com.model.dto.ProductoDto;
import com.model.entities.ProductoEntity;
import java.util.List;

public interface ProductoService {

    List<ProductoDto> obtenerTodos();

    ProductoDto obtenerPorId(Long id);

    ProductoDto crearProducto(CrearProductoDto crearDto);

    ProductoDto actualizarProducto(Long id, ActualizarProductoDto actualizarDto);

    void eliminarProducto(Long id);
    
    ProductoEntity findOrCreate(String producto);
    
}