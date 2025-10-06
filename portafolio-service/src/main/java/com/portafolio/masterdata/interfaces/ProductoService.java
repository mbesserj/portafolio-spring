package com.portafolio.masterdata.interfaces;

import com.portafolio.model.dto.ActualizarProductoDto;
import com.portafolio.model.dto.CrearProductoDto;
import com.portafolio.model.dto.ProductoDto;
import com.portafolio.model.entities.ProductoEntity;
import java.util.List;

public interface ProductoService {

    List<ProductoDto> obtenerTodos();

    ProductoDto obtenerPorId(Long id);

    ProductoDto crearProducto(CrearProductoDto crearDto);

    ProductoDto actualizarProducto(Long id, ActualizarProductoDto actualizarDto);

    void eliminarProducto(Long id);
    
    ProductoEntity findOrCreate(String producto);
    
}