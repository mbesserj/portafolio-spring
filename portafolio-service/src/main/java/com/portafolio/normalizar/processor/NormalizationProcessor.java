package com.portafolio.normalizar.processor;

import com.portafolio.model.entities.CustodioEntity;
import com.portafolio.model.entities.TipoMovimientoEntity;
import com.portafolio.model.entities.EmpresaEntity;
import com.portafolio.model.entities.InstrumentoEntity;
import com.portafolio.model.entities.TransaccionEntity;
import com.portafolio.model.entities.ProductoEntity;
import com.portafolio.model.entities.CargaTransaccionEntity;
import com.portafolio.normalizar.service.NormalizationService.NormalizationResult;
import com.portafolio.persistence.repositorio.TransaccionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Procesador que contiene la lógica de normalización de datos.
 * Migración de NormalizarDatos original.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NormalizationProcessor {

    private final EntityCacheManager cacheManager;
    private final TransaccionRepository transaccionRepository;

    /**
     * Procesa una lista de registros de carga convirtiéndolos en transacciones.
     */
    public NormalizationResult procesarRegistros(
            List<CargaTransaccionEntity> registros, 
            boolean esCargaInicial) {

        log.info("Procesando {} registros (carga inicial: {})", registros.size(), esCargaInicial);

        int exitosos = 0;
        int fallidos = 0;

        for (CargaTransaccionEntity carga : registros) {
            try {
                TransaccionEntity transaccion = normalizarRegistro(carga, esCargaInicial);
                
                if (transaccion != null) {
                    transaccionRepository.save(transaccion);
                    marcarComoProcesado(carga);
                    exitosos++;
                    
                    if (exitosos % 100 == 0) {
                        log.debug("Procesados {} registros...", exitosos);
                    }
                } else {
                    fallidos++;
                    log.warn("No se pudo normalizar registro: folio={}, nemo={}", 
                            carga.getFolio(), carga.getInstrumentoNemo());
                }

            } catch (Exception e) {
                fallidos++;
                log.error("Error normalizando registro: folio={}, nemo={}, error={}", 
                        carga.getFolio(), carga.getInstrumentoNemo(), e.getMessage(), e);
                marcarConError(carga, e.getMessage());
            }
        }

        return NormalizationResult.builder()
                .exitosos(exitosos)
                .fallidos(fallidos)
                .mensaje(String.format("Procesados %d exitosos, %d fallidos", exitosos, fallidos))
                .build();
    }

    /**
     * Normaliza un registro individual convirtiéndolo en transacción.
     */
    private TransaccionEntity normalizarRegistro(CargaTransaccionEntity carga, boolean esCargaInicial) {
        // 1. Resolver entidades relacionadas usando el caché
        ProductoEntity producto = cacheManager.getProducto(carga.getProducto());
        EmpresaEntity empresa = cacheManager.getEmpresa(carga.getRazonSocial(), carga.getRut());
        CustodioEntity custodio = cacheManager.getCustodio(carga.getCustodioNombre());
        InstrumentoEntity instrumento = cacheManager.getInstrumento(
                carga.getInstrumentoNemo(), 
                carga.getInstrumentoNombre(), 
                producto);

        // 2. Determinar tipo de movimiento
        TipoMovimientoEntity tipoMovimiento = determinarTipoMovimiento(carga, esCargaInicial);

        // 3. Validar que todas las entidades requeridas estén presentes
        if (empresa == null || custodio == null || instrumento == null || tipoMovimiento == null) {
            log.warn("Entidades faltantes para registro: empresa={}, custodio={}, instrumento={}, tipoMovimiento={}", 
                    empresa != null, custodio != null, instrumento != null, tipoMovimiento != null);
            return null;
        }

        // 4. Crear la transacción
        return crearTransaccion(carga, empresa, custodio, instrumento, tipoMovimiento);
    }

    /**
     * Determina el tipo de movimiento según el contexto.
     */
    private TipoMovimientoEntity determinarTipoMovimiento(CargaTransaccionEntity carga, boolean esCargaInicial) {
        if (esCargaInicial) {
            return cacheManager.getTipoMovimiento("SALDO INICIAL", "Carga de Saldo Inicial");
        } else {
            String tipo = carga.getTipoMovimiento();
            if (tipo == null || tipo.trim().isEmpty()) {
                // Tipo por defecto si no se especifica
                tipo = "MOVIMIENTO";
            }
            return cacheManager.getTipoMovimiento(tipo, "Normalizado desde carga");
        }
    }

    /**
     * Crea una nueva transacción a partir de los datos de carga.
     */
    private TransaccionEntity crearTransaccion(
            CargaTransaccionEntity carga,
            EmpresaEntity empresa,
            CustodioEntity custodio,
            InstrumentoEntity instrumento,
            TipoMovimientoEntity tipoMovimiento) {

        TransaccionEntity transaccion = TransaccionEntity.builder()
                .empresa(empresa)
                .custodio(custodio)
                .instrumento(instrumento)
                .tipoMovimiento(tipoMovimiento)
                .fechaTransaccion(carga.getFechaTransaccion()) // Campo directo
                .folio(carga.getFolio())
                .cuenta(carga.getCuenta())
                
                // Asignación segura de valores numéricos con valores por defecto
                .cantidad(obtenerValorSeguro(carga.getCantidad()))
                .precio(obtenerValorSeguro(carga.getPrecio()))
                .montoTotal(obtenerValorSeguro(carga.getMontoTotal()))
                .comision(obtenerValorSeguro(carga.getComision())) // Campo correcto
                .gastos(obtenerValorSeguro(carga.getGastos()))
                .iva(obtenerValorSeguro(carga.getIva()))
                .monto(obtenerValorSeguro(carga.getMonto()))
                .montoClp(obtenerValorSeguro(carga.getMontoClp()))
                
                // Campos de texto
                .moneda(carga.getMoneda())
                .glosa(construirGlosa(carga))
                
                // Estados iniciales
                .costeado(false)
                .paraRevision(false)
                .ignorarEnCosteo(false)
                
                .build();

        return transaccion;
    }

    /**
     * Obtiene un valor BigDecimal seguro, usando ZERO si es null.
     */
    private BigDecimal obtenerValorSeguro(BigDecimal valor) {
        return Optional.ofNullable(valor).orElse(BigDecimal.ZERO);
    }

    /**
     * Construye una glosa descriptiva para la transacción.
     */
    private String construirGlosa(CargaTransaccionEntity carga) {
        StringBuilder glosa = new StringBuilder("Normalizado automáticamente");
        
        // Agregar información del tipo de movimiento si existe
        if (carga.getTipoMovimiento() != null && !carga.getTipoMovimiento().trim().isEmpty()) {
            glosa.append(" - ").append(carga.getTipoMovimiento().trim());
        }
        
        // Agregar información del custodio
        if (carga.getCustodioNombre() != null && !carga.getCustodioNombre().trim().isEmpty()) {
            glosa.append(" (").append(carga.getCustodioNombre().trim()).append(")");
        }
        
        return glosa.toString();
    }

    /**
     * Marca un registro como procesado exitosamente.
     */
    private void marcarComoProcesado(CargaTransaccionEntity carga) {
        carga.setProcesado(true);
        // CargaTransaccionEntity no tiene campo observaciones, solo marcamos como procesado
    }

    /**
     * Marca un registro con error durante el procesamiento.
     */
    private void marcarConError(CargaTransaccionEntity carga, String mensajeError) {
        carga.setProcesado(false);
        // CargaTransaccionEntity no tiene campo observaciones, 
        // solo logueamos el error y mantenemos procesado = false
        log.warn("Error procesando registro folio {}: {}", carga.getFolio(), mensajeError);
    }
}