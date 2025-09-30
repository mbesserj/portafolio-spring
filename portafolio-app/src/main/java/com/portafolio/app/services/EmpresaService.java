package com.portafolio.app.services;

import com.portafolio.model.dto.EmpresaDto;
import java.util.List;

public interface EmpresaService {
    List<EmpresaDto> findAll();
    EmpresaDto save(EmpresaDto empresaDTO);
}