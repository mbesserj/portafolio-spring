package com.service.implement;

import com.model.entities.*;
import com.persistence.repositorio.*;
import com.service.interfaces.*;
import com.service.mapper.TransaccionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CargaProcesoServiceImpl implements CargaProcesoService {

    private static final Logger logger = LoggerFactory.getLogger(CargaProcesoServiceImpl.class);

    // --- Spring inyecta automáticamente TODOS los componentes necesarios ---
    private final CargaTransaccionRepository cargaTransaccionRepository;
    private final TransaccionRepository transaccionRepository;
    private final TransaccionMapper transaccionMapper;
    
    private final ProductoService productoService;
    private final EmpresaService empresaService;
    private final CustodioService custodioService;
    private final InstrumentoService instrumentoService;
    private final TipoMovimientoService tipoMovimientoService;

    @Autowired
    public CargaProcesoServiceImpl(CargaTransaccionRepository cargaTransaccionRepository, 
                                 TransaccionRepository transaccionRepository, 
                                 TransaccionMapper transaccionMapper, 
                                 ProductoService productoService, 
                                 EmpresaService empresaService, 
                                 CustodioService custodioService, 
                                 InstrumentoService instrumentoService, 
                                 TipoMovimientoService tipoMovimientoService) {
        this.cargaTransaccionRepository = cargaTransaccionRepository;
        this.transaccionRepository = transaccionRepository;
        this.transaccionMapper = transaccionMapper;
        this.productoService = productoService;
        this.empresaService = empresaService;
        this.custodioService = custodioService;
        this.instrumentoService = instrumentoService;
        this.tipoMovimientoService = tipoMovimientoService;
    }

    @Override
    @Transactional
    public void procesar(boolean esCargaInicial) {
        logger.info("Iniciando proceso de normalización de transacciones...");

        List<CargaTransaccionEntity> registros = cargaTransaccionRepository.findByProcesadoFalse();
        logger.info("Se encontraron {} registros para normalizar.", registros.size());

        int exitosos = 0;
        int fallidos = 0;

        for (CargaTransaccionEntity carga : registros) {
            try {
                // 1. NORMALIZACIÓN: Usamos los servicios expertos para "buscar o crear"
                ProductoEntity producto = productoService.findOrCreate(carga.getProducto());
                EmpresaEntity empresa = empresaService.findOrCreateByRut(carga.getRazonSocial(), carga.getRut());
                CustodioEntity custodio = custodioService.findOrCreateByNombre(carga.getCustodioNombre());
                InstrumentoEntity instrumento = instrumentoService.findOrCreate(carga.getInstrumentoNemo(), carga.getInstrumentoNombre(), producto);
                
                String tipoMovimientoNombre = esCargaInicial ? "SALDO INICIAL" : carga.getTipoMovimiento();
                String descripcionMovimiento = esCargaInicial ? "Carga de Saldo Inicial" : "Normalizado desde carga";
                TipoMovimientoEntity tipoMovimiento = tipoMovimientoService.findOrCreate(tipoMovimientoNombre, descripcionMovimiento);

                // 2. TRANSFORMACIÓN: Usamos el mapper para crear la entidad final
                TransaccionEntity transaccion = transaccionMapper.fromCargaTransaccion(
                    carga, empresa, instrumento, custodio, tipoMovimiento
                );

                // 3. CARGA: Guardamos la transacción final
                transaccionRepository.save(transaccion);

                // 4. ACTUALIZACIÓN: Marcamos el registro de staging como procesado
                carga.setProcesado(true);
                cargaTransaccionRepository.save(carga);
                
                exitosos++;

            } catch (Exception e) {
                logger.error("Fallo al normalizar registro con Folio '{}'. Error: {}", carga.getFolio(), e.getMessage());
                fallidos++;
                // Aquí podrías añadir lógica para marcar el registro de carga con un estado de ERROR
            }
        }
        logger.info("Normalización completada. Registros exitosos: {}, Fallidos: {}.", exitosos, fallidos);
    }
}