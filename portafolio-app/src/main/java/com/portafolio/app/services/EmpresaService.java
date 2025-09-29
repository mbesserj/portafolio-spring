package com.portafolio.app.services;

import com.portafolio.model.dto.EmpresaDTO;
import java.util.List;

public interface EmpresaService {
    List<EmpresaDTO> findAll();
    EmpresaDTO save(EmpresaDTO empresaDTO);
}