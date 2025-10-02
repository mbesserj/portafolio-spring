package com.service.interfaces;

import com.model.dto.EmpresaDto;
import com.model.dto.CrearEmpresaDto;
import com.model.dto.ActualizarEmpresaDto;
import com.model.entities.EmpresaEntity;
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