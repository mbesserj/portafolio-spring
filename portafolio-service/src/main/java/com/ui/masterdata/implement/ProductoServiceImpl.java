package com.portafolio.ui.masterdata.implement;

import com.portafolio.model.dto.ActualizarProductoDto;
import com.portafolio.model.dto.CrearProductoDto;
import com.portafolio.model.dto.ProductoDto;
import com.portafolio.model.entities.ProductoEntity;
import com.portafolio.persistence.repositorio.ProductoRepository;
import com.portafolio.ui.masterdata.interfaces.ProductoService;
import com.portafolio.ui.mapper.ProductoMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;

    public ProductoServiceImpl(ProductoRepository productoRepository, ProductoMapper productoMapper) {
        this.productoRepository = productoRepository;
        this.productoMapper = productoMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoDto> obtenerTodos() {
        List<ProductoEntity> productos = productoRepository.findAllOrderByProductoAsc();
        return productoMapper.toDtoList(productos);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoDto obtenerPorId(Long id) {
        ProductoEntity producto = productoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con id: " + id));
        return productoMapper.toDto(producto);
    }

    @Override
    public ProductoDto crearProducto(CrearProductoDto crearDto) {
        if (productoRepository.existsByProducto(crearDto.getProducto())) {
            throw new IllegalArgumentException("Ya existe un producto con el nombre: " + crearDto.getProducto());
        }

        ProductoEntity nuevoProducto = productoMapper.toEntity(crearDto);
        ProductoEntity productoGuardado = productoRepository.save(nuevoProducto);

        return productoMapper.toDto(productoGuardado);
    }

    @Override
    public ProductoDto actualizarProducto(Long id, ActualizarProductoDto actualizarDto) {
        ProductoEntity producto = productoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con id: " + id));

        productoMapper.updateEntityFromDto(actualizarDto, producto);

        ProductoEntity productoActualizado = productoRepository.save(producto);
        return productoMapper.toDto(productoActualizado);
    }

    @Override
    public void eliminarProducto(Long id) {
        ProductoEntity producto = productoRepository.findByIdWithInstrumentos(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con id: " + id));

        if (producto.getInstrumentos() != null && !producto.getInstrumentos().isEmpty()) {
            throw new IllegalStateException("No se puede eliminar el producto '" + producto.getProducto() + "' porque tiene " + producto.getInstrumentos().size() + " instrumentos asociados.");
        }

        productoRepository.delete(producto);
    }

    @Override
    public ProductoEntity findOrCreate(String nombreProducto) {
        return productoRepository.findByProducto(nombreProducto)
                .orElseGet(() -> {
                    if (nombreProducto == null || nombreProducto.trim().isEmpty()) {
                        throw new IllegalArgumentException("El nombre del producto no puede ser nulo o vacío.");
                    }
                    ProductoEntity nuevoProducto = new ProductoEntity();
                    nuevoProducto.setProducto(nombreProducto);
                    return productoRepository.save(nuevoProducto);
                });
    }
}