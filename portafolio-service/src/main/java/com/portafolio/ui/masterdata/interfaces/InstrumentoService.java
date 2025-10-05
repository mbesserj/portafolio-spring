package com.portafolio.ui.masterdata.interfaces;

import com.portafolio.model.dto.ActualizarInstrumentoDto;
import com.portafolio.model.dto.CrearInstrumentoDto;
import com.portafolio.model.dto.InstrumentoDto;
import com.portafolio.model.entities.InstrumentoEntity;
import com.portafolio.model.entities.ProductoEntity;
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