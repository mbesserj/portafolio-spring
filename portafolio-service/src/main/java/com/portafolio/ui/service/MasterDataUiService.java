package com.portafolio.ui.service;

import com.portafolio.model.entities.*;
import com.portafolio.persistence.repositorio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de UI para gestión de datos maestros.
 * Proporciona acceso a empresas, custodios, instrumentos, etc.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MasterDataUiService {

    private final EmpresaRepository empresaRepository;
    private final CustodioRepository custodioRepository;
    private final InstrumentoRepository instrumentoRepository;
    private final ProductoRepository productoRepository;
    private final TipoMovimientoRepository tipoMovimientoRepository;

    // ===== EMPRESAS =====

    /**
     * Obtiene todas las empresas activas.
     */
    public List<EmpresaEntity> obtenerTodasEmpresas() {
        log.debug("Obteniendo todas las empresas");
        return empresaRepository.findAll();
    }

    /**
     * Obtiene una empresa por ID.
     */
    public Optional<EmpresaEntity> obtenerEmpresaPorId(Long id) {
        log.debug("Obteniendo empresa con ID: {}", id);
        return empresaRepository.findById(id);
    }

    /**
     * Busca empresas por RUT.
     */
    public Optional<EmpresaEntity> buscarEmpresaPorRut(String rut) {
        log.debug("Buscando empresa con RUT: {}", rut);
        return empresaRepository.findByRut(rut);
    }

    // ===== CUSTODIOS =====

    /**
     * Obtiene todos los custodios.
     */
    public List<CustodioEntity> obtenerTodosCustodios() {
        log.debug("Obteniendo todos los custodios");
        return custodioRepository.findAll();
    }

    /**
     * Obtiene un custodio por ID.
     */
    public Optional<CustodioEntity> obtenerCustodioPorId(Long id) {
        log.debug("Obteniendo custodio con ID: {}", id);
        return custodioRepository.findById(id);
    }

    /**
     * Busca custodios por nombre (case insensitive).
     */
    public List<CustodioEntity> buscarCustodiosPorNombre(String nombre) {
        log.debug("Buscando custodios con nombre: {}", nombre);
        return custodioRepository.findByNombreContainingIgnoreCase(nombre);
    }

    // ===== INSTRUMENTOS =====

    /**
     * Obtiene todos los instrumentos.
     */
    public List<InstrumentoEntity> obtenerTodosInstrumentos() {
        log.debug("Obteniendo todos los instrumentos");
        return instrumentoRepository.findAll();
    }

    /**
     * Obtiene un instrumento por ID.
     */
    public Optional<InstrumentoEntity> obtenerInstrumentoPorId(Long id) {
        log.debug("Obteniendo instrumento con ID: {}", id);
        return instrumentoRepository.findById(id);
    }

    /**
     * Busca un instrumento por NEMO.
     */
    public Optional<InstrumentoEntity> buscarInstrumentoPorNemo(String nemo) {
        log.debug("Buscando instrumento con NEMO: {}", nemo);
        return instrumentoRepository.findByInstrumentoNemo(nemo);
    }

    /**
     * Busca instrumentos por nombre (case insensitive).
     */
    public List<InstrumentoEntity> buscarInstrumentosPorNombre(String nombre) {
        log.debug("Buscando instrumentos con nombre: {}", nombre);
        return instrumentoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    /**
     * Obtiene instrumentos por producto.
     */
    public List<InstrumentoEntity> obtenerInstrumentosPorProducto(Long productoId) {
        log.debug("Obteniendo instrumentos del producto ID: {}", productoId);
        return instrumentoRepository.findByProducto_Id(productoId);
    }

    // ===== PRODUCTOS =====

    /**
     * Obtiene todos los productos.
     */
    public List<ProductoEntity> obtenerTodosProductos() {
        log.debug("Obteniendo todos los productos");
        return productoRepository.findAll();
    }

    /**
     * Obtiene un producto por ID.
     */
    public Optional<ProductoEntity> obtenerProductoPorId(Long id) {
        log.debug("Obteniendo producto con ID: {}", id);
        return productoRepository.findById(id);
    }

    /**
     * Busca productos por nombre.
     */
    public List<ProductoEntity> buscarProductosPorNombre(String nombre) {
        log.debug("Buscando productos con nombre: {}", nombre);
        return productoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    // ===== TIPOS DE MOVIMIENTO =====

    /**
     * Obtiene todos los tipos de movimiento.
     */
    public List<TipoMovimientoEntity> obtenerTodosTiposMovimiento() {
        log.debug("Obteniendo todos los tipos de movimiento");
        return tipoMovimientoRepository.findAll();
    }

    /**
     * Obtiene un tipo de movimiento por ID.
     */
    public Optional<TipoMovimientoEntity> obtenerTipoMovimientoPorId(Long id) {
        log.debug("Obteniendo tipo de movimiento con ID: {}", id);
        return tipoMovimientoRepository.findById(id);
    }

    /**
     * Busca tipo de movimiento por tipo.
     */
    public Optional<TipoMovimientoEntity> buscarTipoMovimientoPorTipo(String tipoMovimiento) {
        log.debug("Buscando tipo de movimiento: {}", tipoMovimiento);
        return tipoMovimientoRepository.findByTipoMovimiento(tipoMovimiento);
    }

    // ===== MÉTODOS ÚTILES PARA COMBOS/LISTADOS DE UI =====

    /**
     * Obtiene lista simple de empresas para combo box.
     * Retorna: [id, nombre, rut]
     */
    public List<SimpleItemDto> obtenerEmpresasParaCombo() {
        return empresaRepository.findAll().stream()
                .map(e -> new SimpleItemDto(e.getId(), e.getNombre(), e.getRut()))
                .toList();
    }

    /**
     * Obtiene lista simple de custodios para combo box.
     */
    public List<SimpleItemDto> obtenerCustodiosParaCombo() {
        return custodioRepository.findAll().stream()
                .map(c -> new SimpleItemDto(c.getId(), c.getNombre(), null))
                .toList();
    }

    /**
     * Obtiene lista simple de instrumentos para combo box.
     */
    public List<SimpleItemDto> obtenerInstrumentosParaCombo() {
        return instrumentoRepository.findAll().stream()
                .map(i -> new SimpleItemDto(i.getId(), 
                        i.getInstrumentoNemo() + " - " + i.getNombre(), 
                        i.getInstrumentoNemo()))
                .toList();
    }

    /**
     * Obtiene lista simple de productos para combo box.
     */
    public List<SimpleItemDto> obtenerProductosParaCombo() {
        return productoRepository.findAll().stream()
                .map(p -> new SimpleItemDto(p.getId(), p.getNombre(), null))
                .toList();
    }

    /**
     * Obtiene lista simple de tipos de movimiento para combo box.
     */
    public List<SimpleItemDto> obtenerTiposMovimientoParaCombo() {
        return tipoMovimientoRepository.findAll().stream()
                .map(tm -> new SimpleItemDto(tm.getId(), 
                        tm.getOperacion() + " (" + tm.getSigno() + ")", 
                        tm.getOperacion()))
                .toList();
    }

    // ===== DTO SIMPLE PARA COMBOS =====

    /**
     * DTO simple para uso en combo boxes y listas de la UI.
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class SimpleItemDto {
        private Long id;
        private String displayText;  // Texto a mostrar
        private String code;          // Código opcional (NEMO, RUT, etc)
    }
}