package com.portafolio.ui.masterdata.interfaces;

import com.portafolio.model.dto.GrupoEmpresaDto;
import java.util.List;

public interface GrupoEmpresaService {

    List<GrupoEmpresaDto> findAllBasic();

    GrupoEmpresaDto findByIdComplete(Long id);

    GrupoEmpresaDto save(GrupoEmpresaDto dto);

    GrupoEmpresaDto update(Long id, GrupoEmpresaDto dto);

    void deleteById(Long id);
}