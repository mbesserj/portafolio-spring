package com.portafolio.app.services;

import com.portafolio.model.dto.InstrumentoDto; // Importar DTO
import java.util.List;

public interface InstrumentoService {
    
    // El método ahora devuelve una lista de DTOs
    List<InstrumentoDto> findAll(); 
    
    // El método ahora recibe y devuelve un DTO
    InstrumentoDto save(InstrumentoDto instrumentoDto); 
}