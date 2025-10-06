package com.portafolio.masterdata.implement;

import com.portafolio.model.dto.ActualizarTipoMovimientoDto;
import com.portafolio.model.dto.CrearTipoMovimientoDto;
import com.portafolio.model.dto.TipoMovimientoDto;
import com.portafolio.model.entities.MovimientoContableEntity;
import com.portafolio.model.entities.TipoMovimientoEntity;
import com.portafolio.persistence.repositorio.MovimientoContableRepository;
import com.portafolio.persistence.repositorio.TipoMovimientoRepository;
import com.portafolio.persistence.repositorio.TransaccionRepository;
import com.portafolio.masterdata.interfaces.TipoMovimientoService;
import com.portafolio.mapper.TipoMovimientoMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TipoMovimientoServiceImpl implements TipoMovimientoService {

    private final TipoMovimientoRepository tipoMovimientoRepository;
    private final MovimientoContableRepository movimientoContableRepository;
    private final TransaccionRepository transaccionRepository; // Necesario para la regla de eliminación
    private final TipoMovimientoMapper tipoMovimientoMapper;

    public TipoMovimientoServiceImpl(TipoMovimientoRepository tipoMovimientoRepository,
            MovimientoContableRepository movimientoContableRepository,
            TransaccionRepository transaccionRepository,
            TipoMovimientoMapper tipoMovimientoMapper) {
        this.tipoMovimientoRepository = tipoMovimientoRepository;
        this.movimientoContableRepository = movimientoContableRepository;
        this.transaccionRepository = transaccionRepository;
        this.tipoMovimientoMapper = tipoMovimientoMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoMovimientoDto> obtenerTodos() {
        return tipoMovimientoMapper.toDtoList(tipoMovimientoRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public TipoMovimientoDto obtenerPorId(Long id) {
        TipoMovimientoEntity entidad = tipoMovimientoRepository.findByIdWithMovimientoContable(id)
                .orElseThrow(() -> new EntityNotFoundException("Tipo de Movimiento no encontrado con id: " + id));
        return tipoMovimientoMapper.toDto(entidad);
    }

    @Override
    public TipoMovimientoDto crearTipoMovimiento(CrearTipoMovimientoDto crearDto) {
        if (tipoMovimientoRepository.findByTipoMovimiento(crearDto.getTipoMovimiento()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un tipo de movimiento con el nombre: " + crearDto.getTipoMovimiento());
        }

        MovimientoContableEntity contable = movimientoContableRepository.findById(crearDto.getMovimientoContableId())
                .orElseThrow(() -> new EntityNotFoundException("Movimiento Contable no encontrado con id: " + crearDto.getMovimientoContableId()));

        TipoMovimientoEntity nuevaEntidad = tipoMovimientoMapper.toEntity(crearDto);
        nuevaEntidad.setMovimientoContable(contable);

        TipoMovimientoEntity entidadGuardada = tipoMovimientoRepository.save(nuevaEntidad);
        return tipoMovimientoMapper.toDto(entidadGuardada);
    }

    @Override
    public TipoMovimientoDto actualizarTipoMovimiento(Long id, ActualizarTipoMovimientoDto actualizarDto) {
        TipoMovimientoEntity entidad = tipoMovimientoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tipo de Movimiento no encontrado con id: " + id));

        MovimientoContableEntity contable = movimientoContableRepository.findById(actualizarDto.getMovimientoContableId())
                .orElseThrow(() -> new EntityNotFoundException("Movimiento Contable no encontrado con id: " + actualizarDto.getMovimientoContableId()));

        // Actualizamos los campos
        tipoMovimientoMapper.updateEntityFromDto(actualizarDto, entidad);
        entidad.setMovimientoContable(contable);

        TipoMovimientoEntity entidadActualizada = tipoMovimientoRepository.save(entidad);
        return tipoMovimientoMapper.toDto(entidadActualizada);
    }

    @Override
    public void eliminarTipoMovimiento(Long id) {
        // Regla de negocio: Validar que el tipo de movimiento no esté en uso.
        if (!tipoMovimientoRepository.existsById(id)) {
            throw new EntityNotFoundException("Tipo de Movimiento no encontrado con id: " + id);
        }

        if (transaccionRepository.existsByTipoMovimientoId(id)) {
            throw new IllegalStateException("No se puede eliminar el tipo de movimiento porque está siendo utilizado en transacciones.");
        }

        tipoMovimientoRepository.deleteById(id);
    }

    @Override
    public TipoMovimientoEntity findOrCreate(String tipoMovimientoNombre, String descripcionMovimiento, Long movimientoContableId) {
        if (tipoMovimientoNombre == null || tipoMovimientoNombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del tipo de movimiento no puede ser nulo o vacío.");
        }

        return tipoMovimientoRepository.findByTipoMovimiento(tipoMovimientoNombre)
                .orElseGet(() -> {
                    // Lógica que se ejecuta SOLO si el tipo de movimiento no fue encontrado.
                    TipoMovimientoEntity nuevoTipoMovimiento = new TipoMovimientoEntity();
                    nuevoTipoMovimiento.setTipoMovimiento(tipoMovimientoNombre);
                    nuevoTipoMovimiento.setDescripcion(descripcionMovimiento);

                    // Asociamos el MovimientoContable solo si se proporcionó un ID.
                    if (movimientoContableId != null) {
                        MovimientoContableEntity contable = movimientoContableRepository.findById(movimientoContableId)
                                .orElseThrow(() -> new EntityNotFoundException("MovimientoContable no encontrado con id: " + movimientoContableId));
                        nuevoTipoMovimiento.setMovimientoContable(contable);
                    }

                    return tipoMovimientoRepository.save(nuevoTipoMovimiento);
                });
    }

    @Override
    public TipoMovimientoEntity findOrCreate(String tipoMovimientoNombre, String descripcionMovimiento) {
        return this.findOrCreate(tipoMovimientoNombre, descripcionMovimiento, null);
    }
}
