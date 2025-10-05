package com.portafolio.normalizar.processor;

import com.portafolio.model.entities.CustodioEntity;
import com.portafolio.model.entities.TipoMovimientoEntity;
import com.portafolio.model.entities.EmpresaEntity;
import com.portafolio.model.entities.InstrumentoEntity;
import com.portafolio.model.entities.ProductoEntity;
import com.portafolio.persistence.repositorio.ProductoRepository;
import com.portafolio.persistence.repositorio.InstrumentoRepository;
import com.portafolio.persistence.repositorio.CustodioRepository;
import com.portafolio.persistence.repositorio.TipoMovimientoRepository;
import com.portafolio.persistence.repositorio.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Gestor de caché para entidades durante el proceso de normalización.
 * Migración Spring del EntidadCacheManager original.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EntityCacheManager {

    private final EmpresaRepository empresaRepository;
    private final CustodioRepository custodioRepository;
    private final ProductoRepository productoRepository;
    private final InstrumentoRepository instrumentoRepository;
    private final TipoMovimientoRepository tipoMovimientoRepository;

    // Cachés en memoria para evitar consultas repetidas
    private final Map<String, EmpresaEntity> empresaCache = new HashMap<>();
    private final Map<String, CustodioEntity> custodioCache = new HashMap<>();
    private final Map<String, ProductoEntity> productoCache = new HashMap<>();
    private final Map<String, InstrumentoEntity> instrumentoCache = new HashMap<>();
    private final Map<String, TipoMovimientoEntity> tipoMovimientoCache = new HashMap<>();

    /**
     * Obtiene o crea una empresa por RUT y razón social.
     */
    public EmpresaEntity getEmpresa(String razonSocial, String rut) {
        // Normalizar RUT como clave del caché
        String rutNormalizado = normalizarRut(rut);
        if (rutNormalizado.isEmpty()) {
            log.warn("RUT vacío o nulo para empresa: {}", razonSocial);
            return null;
        }

        return empresaCache.computeIfAbsent(rutNormalizado, k -> {
            log.debug("Buscando/creando empresa con RUT: {}", rutNormalizado);
            return findOrCreateEmpresa(razonSocial, rutNormalizado);
        });
    }

    /**
     * Obtiene o crea un custodio por nombre.
     */
    public CustodioEntity getCustodio(String nombre) {
        String nombreNormalizado = normalizarNombreCustodio(nombre);
        
        return custodioCache.computeIfAbsent(nombreNormalizado, k -> {
            log.debug("Buscando/creando custodio: {}", nombreNormalizado);
            return findOrCreateCustodio(nombreNormalizado);
        });
    }

    /**
     * Obtiene o crea un producto por cuenta.
     */
    public ProductoEntity getProducto(String cuenta) {
        if (cuenta == null || cuenta.trim().isEmpty()) {
            return null;
        }
        
        return productoCache.computeIfAbsent(cuenta.trim(), k -> {
            log.debug("Buscando/creando producto: {}", cuenta);
            return findOrCreateProducto(cuenta.trim());
        });
    }

    /**
     * Obtiene o crea un instrumento por nemónico, nombre y producto.
     */
    public InstrumentoEntity getInstrumento(String nemo, String nombre, ProductoEntity producto) {
        String key = construirClaveInstrumento(nemo, nombre, producto);
        
        return instrumentoCache.computeIfAbsent(key, k -> {
            log.debug("Buscando/creando instrumento: {} - {}", nemo, nombre);
            return findOrCreateInstrumento(nemo, nombre, producto);
        });
    }

    /**
     * Obtiene o crea un tipo de movimiento.
     */
    public TipoMovimientoEntity getTipoMovimiento(String tipoMovimiento, String descripcion) {
        if (tipoMovimiento == null || tipoMovimiento.trim().isEmpty()) {
            return null;
        }
        
        return tipoMovimientoCache.computeIfAbsent(tipoMovimiento.trim(), k -> {
            log.debug("Buscando/creando tipo movimiento: {}", tipoMovimiento);
            return findOrCreateTipoMovimiento(tipoMovimiento.trim(), descripcion);
        });
    }

    /**
     * Limpia todos los cachés. Útil entre procesamiento de archivos grandes.
     */
    public void limpiarCache() {
        empresaCache.clear();
        custodioCache.clear();
        productoCache.clear();
        instrumentoCache.clear();
        tipoMovimientoCache.clear();
        log.info("Cachés de entidades limpiados");
    }

    /**
     * Obtiene estadísticas de uso del caché.
     */
    public Map<String, Integer> getEstadisticasCache() {
        return Map.of(
                "empresas", empresaCache.size(),
                "custodios", custodioCache.size(),
                "productos", productoCache.size(),
                "instrumentos", instrumentoCache.size(),
                "tiposMovimiento", tipoMovimientoCache.size()
        );
    }

    // ===== MÉTODOS PRIVADOS =====

    private String normalizarRut(String rut) {
        if (rut == null) return "";
        return rut.replace(".", "").replace("-", "").trim().toUpperCase();
    }

    private String normalizarNombreCustodio(String nombre) {
        if (nombre == null) return "";
        return nombre.trim()
                .replace("Peshing", "Pershing") // Corrección común
                .replace("  ", " "); // Espacios dobles
    }

    private String construirClaveInstrumento(String nemo, String nombre, ProductoEntity producto) {
        String productoId = (producto != null) ? producto.getId().toString() : "null";
        return String.format("%s|%s|%s", 
                nemo != null ? nemo.trim() : "",
                nombre != null ? nombre.trim() : "",
                productoId);
    }

    private EmpresaEntity findOrCreateEmpresa(String razonSocial, String rut) {
        // Buscar por RUT primero
        return empresaRepository.findByRut(rut)
                .orElseGet(() -> {
                    // Si no existe, crear nueva empresa
                    EmpresaEntity nueva = EmpresaEntity.builder()
                            .rut(rut)
                            .razonSocial(razonSocial != null ? razonSocial.trim() : "")
                            .build();
                    return empresaRepository.save(nueva);
                });
    }

    private CustodioEntity findOrCreateCustodio(String nombre) {
        return custodioRepository.findByNombre(nombre)
                .orElseGet(() -> {
                    CustodioEntity nuevo = CustodioEntity.builder()
                            .nombreCustodio(nombre)
                            .build();
                    return custodioRepository.save(nuevo);
                });
    }

    private ProductoEntity findOrCreateProducto(String cuenta) {
        return productoRepository.findByProducto(cuenta)
                .orElseGet(() -> {
                    ProductoEntity nuevo = ProductoEntity.builder()
                            .producto(cuenta)
                            .build();
                    return productoRepository.save(nuevo);
                });
    }

    private InstrumentoEntity findOrCreateInstrumento(String nemo, String nombre, ProductoEntity producto) {
        return instrumentoRepository.findByInstrumentoNemo(nemo)
                .orElseGet(() -> {
                    InstrumentoEntity nuevo = InstrumentoEntity.builder()
                            .instrumentoNemo(nemo != null ? nemo.trim() : "")
                            .instrumentoNombre(nombre != null ? nombre.trim() : "")
                            .producto(producto)
                            .build();
                    return instrumentoRepository.save(nuevo);
                });
    }

    private TipoMovimientoEntity findOrCreateTipoMovimiento(String tipo, String descripcion) {
        return tipoMovimientoRepository.findByTipoMovimiento(tipo)
                .orElseGet(() -> {
                    TipoMovimientoEntity nuevo = TipoMovimientoEntity.builder()
                            .tipoMovimiento(tipo)
                            .descripcion(descripcion != null ? descripcion : tipo)
                            .build();
                    return tipoMovimientoRepository.save(nuevo);
                });
    }
}