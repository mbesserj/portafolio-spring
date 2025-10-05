package com.service.interfaces;

import com.model.dto.ActualizarInstrumentoDto;
import com.model.dto.CrearInstrumentoDto;
import com.model.dto.InstrumentoDto;
import com.model.entities.InstrumentoEntity;
import com.model.entities.ProductoEntity;
import java.util.List;

public interface InstrumentoService {

    List<InstrumentoDto> obtenerTodos();

    InstrumentoDto obtenerPorId(Long id);

    InstrumentoDto crearInstrumento(CrearInstrumentoDto crearDto);

    InstrumentoDto actualizarInstrumento(Long id, ActualizarInstrumentoDto actualizarDto);

    void eliminarInstrumento(Long id);
        
    InstrumentoEntity findOrCreate(String instrumentoNemo, String instrumentoNombre, Long productoId);
    
    InstrumentoEntity findOrCreate(String instrumentoNemo, String instrumentoNombre, ProductoEntity producto);

}