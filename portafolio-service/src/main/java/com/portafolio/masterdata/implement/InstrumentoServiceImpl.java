package com.portafolio.masterdata.implement;

import com.portafolio.model.dto.ActualizarInstrumentoDto;
import com.portafolio.model.dto.CrearInstrumentoDto;
import com.portafolio.model.dto.InstrumentoDto;
import com.portafolio.model.entities.InstrumentoEntity;
import com.portafolio.model.entities.ProductoEntity;
import com.portafolio.persistence.repositorio.InstrumentoRepository;
import com.portafolio.persistence.repositorio.ProductoRepository;
import com.portafolio.mapper.InstrumentoMapper;
import com.portafolio.masterdata.interfaces.InstrumentoService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InstrumentoServiceImpl implements InstrumentoService {

    private static final Logger logger = LoggerFactory.getLogger(InstrumentoServiceImpl.class);

    private final InstrumentoRepository instrumentoRepository;
    private final ProductoRepository productoRepository;
    private final InstrumentoMapper instrumentoMapper; // Inyectamos el mapper

    // --- OPERACIONES CRUD ---
    @Override
    public InstrumentoDto crearInstrumento(CrearInstrumentoDto crearDto) {
        if (instrumentoRepository.findByInstrumentoNemoIgnoreCase(crearDto.getInstrumentoNemo()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un instrumento con el nemo: " + crearDto.getInstrumentoNemo());
        }

        ProductoEntity producto = productoRepository.findById(crearDto.getProductoId())
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con id: " + crearDto.getProductoId()));

        InstrumentoEntity nuevoInstrumento = new InstrumentoEntity();
        nuevoInstrumento.setInstrumentoNemo(crearDto.getInstrumentoNemo().toUpperCase());
        nuevoInstrumento.setInstrumentoNombre(crearDto.getInstrumentoNombre());
        nuevoInstrumento.setProducto(producto);

        InstrumentoEntity instrumentoGuardado = instrumentoRepository.save(nuevoInstrumento);
        logger.info("Instrumento '{}' creado exitosamente.", instrumentoGuardado.getInstrumentoNemo());
        return instrumentoMapper.toDtoComplete(instrumentoGuardado);
    }

    @Override
    public InstrumentoDto actualizarInstrumento(Long id, ActualizarInstrumentoDto actualizarDto) {
        InstrumentoEntity instrumento = instrumentoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Instrumento no encontrado con id: " + id));

        // El mapper se encarga de actualizar solo los campos no nulos del DTO
        instrumentoMapper.updateEntityFromDto(actualizarDto, instrumento);

        // Si se provee un nuevo ID de producto, se actualiza la relación
        if (actualizarDto.getProductoId() != null) {
            ProductoEntity producto = productoRepository.findById(actualizarDto.getProductoId())
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con id: " + actualizarDto.getProductoId()));
            instrumento.setProducto(producto);
        }

        InstrumentoEntity instrumentoActualizado = instrumentoRepository.save(instrumento);
        return instrumentoMapper.toDtoComplete(instrumentoActualizado);
    }

    @Override
    public void eliminarInstrumento(Long id) {
        if (!instrumentoRepository.existsById(id)) {
            throw new EntityNotFoundException("Instrumento no encontrado con id: " + id);
        }
        // A futuro, añadir validación para no borrar instrumentos con transacciones asociadas
        instrumentoRepository.deleteById(id);
        logger.info("Instrumento con ID {} eliminado.", id);
    }

    // --- OPERACIONES DE LECTURA (Refactorizadas para devolver DTOs) ---
    @Transactional(readOnly = true)
    public List<InstrumentoDto> obtenerTodos(boolean detallesCompletos) {
        List<InstrumentoEntity> instrumentos = instrumentoRepository.findAllByOrderByInstrumentoNemoAsc();
        return detallesCompletos ? instrumentoMapper.toDtoCompleteList(instrumentos) : instrumentoMapper.toDtoBasicList(instrumentos);
    }

    @Override
    @Transactional(readOnly = true)
    public InstrumentoDto obtenerPorId(Long id) {
        InstrumentoEntity instrumento = instrumentoRepository.findByIdWithProducto(id) // Usar método optimizado
                .orElseThrow(() -> new EntityNotFoundException("Instrumento no encontrado con id: " + id));
        return instrumentoMapper.toDtoComplete(instrumento);
    }

    // --- LÓGICA DE NEGOCIO (Ya la tenías) ---
    @Transactional
    public InstrumentoEntity buscarOCrear(String nemo, String nombre) {
        return instrumentoRepository.findByInstrumentoNemoIgnoreCase(nemo)
                .orElseGet(() -> {
                    logger.info("El instrumento '{}' no existe. Se creará uno nuevo.", nemo);
                    ProductoEntity productoDefecto = productoRepository.findById(1L)
                            .orElseThrow(() -> new EntityNotFoundException("No se encontró el Producto por defecto (ID=1L)."));

                    InstrumentoEntity nuevoInstrumento = new InstrumentoEntity();
                    nuevoInstrumento.setInstrumentoNemo(nemo.toUpperCase());
                    nuevoInstrumento.setInstrumentoNombre(nombre);
                    nuevoInstrumento.setProducto(productoDefecto);

                    return instrumentoRepository.save(nuevoInstrumento);
                });
    }

    // El resto de tus métodos de búsqueda se mantienen igual, ya que están bien implementados
    @Transactional(readOnly = true)
    public List<InstrumentoEntity> obtenerInstrumentosConTransacciones(Long empresaId, Long custodioId, String cuenta) {
        if (empresaId == null || custodioId == null || cuenta == null) {
            return Collections.emptyList();
        }
        return instrumentoRepository.findInstrumentosConTransacciones(empresaId, custodioId, cuenta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InstrumentoDto> obtenerTodos() {
        // Llama al método existente con 'false' para obtener la lista básica
        return obtenerTodos(false);
    }

    @Override
    @Transactional
    public InstrumentoEntity findOrCreate(String instrumentoNemo, String instrumentoNombre, Long productoId) {
        if (productoId == null) {
            throw new IllegalArgumentException("El ID del producto no puede ser nulo.");
        }
        // Busca el producto en la base de datos
        ProductoEntity producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con id: " + productoId));

        // Llama a la otra versión del método para no repetir código
        return findOrCreate(instrumentoNemo, instrumentoNombre, producto);
    }

    @Override
    @Transactional
    public InstrumentoEntity findOrCreate(String instrumentoNemo, String instrumentoNombre, ProductoEntity producto) {
        if (instrumentoNemo == null || instrumentoNemo.trim().isEmpty()) {
            throw new IllegalArgumentException("El nemo del instrumento no puede ser nulo o vacío.");
        }
        if (producto == null) {
            throw new IllegalArgumentException("La entidad Producto no puede ser nula.");
        }

        return instrumentoRepository.findByInstrumentoNemoIgnoreCase(instrumentoNemo.trim())
                .orElseGet(() -> {
                    logger.info("El instrumento '{}' no existe. Se creará uno nuevo.", instrumentoNemo);

                    InstrumentoEntity nuevoInstrumento = new InstrumentoEntity();
                    nuevoInstrumento.setInstrumentoNemo(instrumentoNemo.trim().toUpperCase());
                    nuevoInstrumento.setInstrumentoNombre(instrumentoNombre);
                    nuevoInstrumento.setProducto(producto);

                    return instrumentoRepository.save(nuevoInstrumento);
                });
    }
}