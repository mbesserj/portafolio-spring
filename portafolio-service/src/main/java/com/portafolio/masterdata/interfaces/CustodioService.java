package com.portafolio.masterdata.interfaces;

import com.portafolio.model.dto.ActualizarCustodioDto;
import com.portafolio.model.dto.CrearCustodioDto;
import com.portafolio.model.dto.CustodioDto;
import com.portafolio.model.entities.CustodioEntity;
import java.util.List;

public interface CustodioService {

    List<CustodioDto> obtenerTodos(boolean detallesCompletos);

    CustodioDto obtenerPorId(Long id);

    CustodioDto crearCustodio(CrearCustodioDto crearDto);

    CustodioDto actualizarCustodio(Long id, ActualizarCustodioDto actualizarDto);

    void eliminarCustodio(Long id);
    
    CustodioEntity findOrCreateByNombre(String custodioNombre);
}