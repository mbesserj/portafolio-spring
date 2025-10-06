package com.portafolio.masterdata.interfaces;

import com.portafolio.model.dto.ActualizarTipoMovimientoDto;
import com.portafolio.model.dto.CrearTipoMovimientoDto;
import com.portafolio.model.dto.TipoMovimientoDto;
import com.portafolio.model.entities.TipoMovimientoEntity;
import java.util.List;

public interface TipoMovimientoService {

    List<TipoMovimientoDto> obtenerTodos();

    TipoMovimientoDto obtenerPorId(Long id);

    TipoMovimientoDto crearTipoMovimiento(CrearTipoMovimientoDto crearDto);

    TipoMovimientoDto actualizarTipoMovimiento(Long id, ActualizarTipoMovimientoDto actualizarDto);

    void eliminarTipoMovimiento(Long id);
    
    TipoMovimientoEntity findOrCreate(String tipoMovimientoNombre, String descripcionMovimiento, Long movimientoContableId);
    
    TipoMovimientoEntity findOrCreate(String tipoMovimientoNombre, String descripcionMovimiento);
}