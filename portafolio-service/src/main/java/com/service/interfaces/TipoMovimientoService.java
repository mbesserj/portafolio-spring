package com.service.interfaces;

import com.model.dto.ActualizarTipoMovimientoDto;
import com.model.dto.CrearTipoMovimientoDto;
import com.model.dto.TipoMovimientoDto;
import com.model.entities.TipoMovimientoEntity;
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