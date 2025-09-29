
package com.portafolio.app.services;

import com.portafolio.model.dto.CargaTransaccionDto;
import java.util.List;

public interface CargaTransaccionService {
    List<CargaTransaccionDto> findAll();
    CargaTransaccionDto save(CargaTransaccionDto cargaTransaccionDto);
}