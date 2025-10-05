package com.portafolio.ui.masterdata.interfaces;

import com.portafolio.model.dto.EmpresaDto;
import com.portafolio.model.dto.CrearEmpresaDto;
import com.portafolio.model.dto.ActualizarEmpresaDto;
import com.portafolio.model.entities.EmpresaEntity;
import java.util.List;

public interface EmpresaService {

    List<EmpresaDto> obtenerTodas(boolean detallesCompletos);

    EmpresaDto obtenerPorId(Long id);

    EmpresaDto crearEmpresa(CrearEmpresaDto crearDto);
    
    EmpresaDto actualizarEmpresa(Long id, ActualizarEmpresaDto actualizarDto);

    void eliminarEmpresa(Long id);

    EmpresaDto asociarCustodio(Long empresaId, Long custodioId);
    
    EmpresaDto desasociarCustodio(Long empresaId, Long custodioId);
    
    EmpresaEntity findOrCreateByRut(String razonSocial, String rut);
    
}