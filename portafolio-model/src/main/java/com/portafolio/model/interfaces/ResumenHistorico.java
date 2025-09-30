package com.portafolio.model.interfaces;

import com.portafolio.model.dto.ResumenHistoricoDto;
import java.util.List;

public interface ResumenHistorico {
    List<ResumenHistoricoDto> obtenerResumenHistorico(Long empresaId, Long custodioId, String cuenta);
}