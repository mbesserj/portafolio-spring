package com.service.interfaces;

import com.model.dto.ActualizarCustodioDto;
import com.model.dto.CrearCustodioDto;
import com.model.dto.CustodioDto;
import com.model.entities.CustodioEntity;
import java.util.List;

public interface CustodioService {

    List<CustodioDto> obtenerTodos(boolean detallesCompletos);

    CustodioDto obtenerPorId(Long id);

    CustodioDto crearCustodio(CrearCustodioDto crearDto);

    CustodioDto actualizarCustodio(Long id, ActualizarCustodioDto actualizarDto);

    void eliminarCustodio(Long id);
    
    CustodioEntity findOrCreateByNombre(String custodioNombre);
}