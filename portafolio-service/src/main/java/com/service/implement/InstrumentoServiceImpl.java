package com.service.implement;

import com.model.dto.ActualizarInstrumentoDto;
import com.model.dto.CrearInstrumentoDto;
import com.model.dto.InstrumentoDto;
import com.model.entities.InstrumentoEntity;
import com.model.entities.ProductoEntity;
import com.persistence.repositorio.InstrumentoRepository;
import com.persistence.repositorio.ProductoRepository;
import com.service.interfaces.InstrumentoService;
import com.service.mapper.InstrumentoMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class InstrumentoServiceImpl implements InstrumentoService {

    private final InstrumentoRepository instrumentoRepository;
    private final ProductoRepository productoRepository;
    private final InstrumentoMapper instrumentoMapper;

    public InstrumentoServiceImpl(InstrumentoRepository instrumentoRepository,
            ProductoRepository productoRepository,
            InstrumentoMapper instrumentoMapper) {
        this.instrumentoRepository = instrumentoRepository;
        this.productoRepository = productoRepository;
        this.instrumentoMapper = instrumentoMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<InstrumentoDto> obtenerTodos() {
        List<InstrumentoEntity> instrumentos = instrumentoRepository.findAllWithProducto();
        return instrumentoMapper.toDtoCompleteList(instrumentos);
    }

    @Override
    @Transactional(readOnly = true)
    public InstrumentoDto obtenerPorId(Long id) {
        InstrumentoEntity instrumento = instrumentoRepository.findByIdWithProducto(id)
                .orElseThrow(() -> new EntityNotFoundException("Instrumento no encontrado con id: " + id));
        return instrumentoMapper.toDtoComplete(instrumento);
    }

    @Override
    public InstrumentoDto crearInstrumento(CrearInstrumentoDto crearDto) {
        if (instrumentoRepository.existsByInstrumentoNemo(crearDto.getInstrumentoNemo())) {
            throw new IllegalArgumentException("Ya existe un instrumento con el nemo: " + crearDto.getInstrumentoNemo());
        }

        // Lógica de orquestación: Buscamos la entidad Producto
        ProductoEntity productoAsociado = productoRepository.findById(crearDto.getProductoId())
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con id: " + crearDto.getProductoId()));

        // Mapeamos los datos básicos y asignamos la relación manualmente
        InstrumentoEntity nuevoInstrumento = instrumentoMapper.toEntity(crearDto);
        nuevoInstrumento.setProducto(productoAsociado);
        InstrumentoEntity instrumentoGuardado = instrumentoRepository.save(nuevoInstrumento);

        return instrumentoMapper.toDtoComplete(instrumentoGuardado);
    }

    @Override
    public InstrumentoDto actualizarInstrumento(Long id, ActualizarInstrumentoDto actualizarDto) {
        InstrumentoEntity instrumento = instrumentoRepository.findByIdWithProducto(id)
                .orElseThrow(() -> new EntityNotFoundException("Instrumento no encontrado con id: " + id));

        // Lógica para cambiar el producto si el ID es diferente
        if (!instrumento.getProducto().getId().equals(actualizarDto.getProductoId())) {
            ProductoEntity nuevoProducto = productoRepository.findById(actualizarDto.getProductoId())
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con id: " + actualizarDto.getProductoId()));
            instrumento.setProducto(nuevoProducto);
        }

        // Usamos el mapper para actualizar los campos restantes
        instrumentoMapper.updateEntityFromDto(actualizarDto, instrumento);

        InstrumentoEntity instrumentoActualizado = instrumentoRepository.save(instrumento);
        return instrumentoMapper.toDtoComplete(instrumentoActualizado);
    }

    @Override
    public void eliminarInstrumento(Long id) {
        InstrumentoEntity instrumento = instrumentoRepository.findByIdWithProducto(id)
                .orElseThrow(() -> new EntityNotFoundException("Instrumento no encontrado con id: " + id));

        // Regla de negocio: No permitir eliminar un instrumento si tiene transacciones asociadas.
        if (instrumento.getTotalTransacciones() > 0) {
            throw new IllegalStateException("No se puede eliminar el instrumento '" + instrumento.getInstrumentoNemo() + "' porque tiene transacciones asociadas.");
        }
        instrumentoRepository.delete(instrumento);
    }

    @Override
    public InstrumentoEntity findOrCreate(String instrumentoNemo, String instrumentoNombre, ProductoEntity producto) {
        if (instrumentoNemo == null || instrumentoNemo.trim().isEmpty()) {
            throw new IllegalArgumentException("El nemo del instrumento no puede ser nulo o vacío.");
        }
        if (producto == null) {
            throw new IllegalArgumentException("El producto no puede ser nulo para crear un instrumento.");
        }

        return instrumentoRepository.findByInstrumentoNemo(instrumentoNemo)
                .orElseGet(() -> {
                    InstrumentoEntity nuevoInstrumento = new InstrumentoEntity();
                    nuevoInstrumento.setInstrumentoNemo(instrumentoNemo);
                    nuevoInstrumento.setInstrumentoNombre(instrumentoNombre);
                    nuevoInstrumento.setProducto(producto); // Asigna la entidad directamente

                    return instrumentoRepository.save(nuevoInstrumento);
                });
    }

    @Override
    public InstrumentoEntity findOrCreate(String instrumentoNemo, String instrumentoNombre, Long productoId) {
        // 1. Realiza la búsqueda de la entidad necesaria.
        ProductoEntity productoAsociado = productoRepository.findById(productoId)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con id: " + productoId));

        // 2. Llama al método optimizado para evitar duplicar la lógica.
        return this.findOrCreate(instrumentoNemo, instrumentoNombre, productoAsociado);
    }
}