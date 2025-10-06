package com.portafolio.ui.factory;

import com.portafolio.model.dto.*;
import com.portafolio.model.entities.*;
import com.portafolio.model.enums.*;
import com.portafolio.costing.api.CostingApi;
import com.portafolio.masterdata.implement.UsuarioService;
import com.portafolio.masterdata.interfaces.CustodioService;
import com.portafolio.masterdata.interfaces.EmpresaService;
import com.portafolio.masterdata.interfaces.InstrumentoService;
import com.portafolio.masterdata.interfaces.TransaccionService;
import com.portafolio.normalizar.service.NormalizationService;
import com.portafolio.ui.service.AuthenticationService;
import com.portafolio.ui.service.BorrarContenidoTablasService;
import com.portafolio.ui.service.FiltroService;
import com.portafolio.ui.service.FusionInstrumentoService;
import com.portafolio.ui.service.KardexService;
import com.portafolio.ui.service.OperacionesTrxsService;
import com.portafolio.ui.service.ProcesoCargaDiariaService;
import com.portafolio.ui.service.SaldoActualService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppFacade {

    private final AuthenticationService authenticationService;
    private final UsuarioService usuarioService;
    private final ProcesoCargaDiariaService procesoDiarioOrchestrator;
    private final BorrarContenidoTablasService limpiezaService;
    private final NormalizationService normalizationService;
    private final CostingApi costingApi;
    private final FiltroService filtroService;
    private final EmpresaService empresaService;
    private final CustodioService custodioService;
    private final InstrumentoService instrumentoService;
    private final FusionInstrumentoService fusionInstrumentoService;
    private final TransaccionService transaccionService;
    private final OperacionesTrxsService operacionesTrxsService;
    private final KardexService kardexApi;
    private final SaldoActualService saldoActualService;

    // Métodos de autenticación y usuarios
    public ServiceResult<Boolean> autenticarUsuario(String usuario, String contrasena) {
        return execute(() -> authenticationService.autenticar(usuario, contrasena).isSuccess());
    }

    public ServiceResult<Boolean> hayUsuariosRegistrados() {
        return execute(usuarioService::hayUsuariosRegistrados);
    }

    public ServiceResult<UsuarioService.UserRegistrationResult> crearUsuarioAdmin(String usuario, String contrasena) {
        return execute(() -> usuarioService.crearUsuarioAdmin(usuario, contrasena));
    }

    public ServiceResult<ResultadoCargaDto> ejecutarCargaDiaria(ListaEnumsCustodios custodio, File archivo) {
        return execute(() -> procesoDiarioOrchestrator.ejecutar(custodio, archivo));
    }

    public ServiceResult<ResultadoCargaDto> ejecutarCargaInicial(ListaEnumsCustodios custodio, File file) {
        return execute(() -> {
            limpiezaService.limpiarDatosDeNegocio();
            return procesoDiarioOrchestrator.ejecutar(custodio, file);
        });
    }

    public ServiceResult<ResultadoCargaDto> reprocesarNormalizacion() {
        return execute(normalizationService::ejecutarNormalizacion);
    }

    public ServiceResult<Void> iniciarCosteoCompleto() {
        return execute(() -> {
            costingApi.ejecutarCosteoCompleto();
            return null;
        });
    }

    public ServiceResult<List<EmpresaEntity>> obtenerEmpresasConTransacciones() {
        return execute(filtroService::obtenerEmpresasConTransacciones);
    }

    public ServiceResult<List<CustodioEntity>> obtenerCustodiosConTransacciones(Long empresaId) {
        return execute(() -> filtroService.obtenerCustodiosConTransacciones(empresaId));
    }

    public ServiceResult<List<String>> obtenerCuentasConTransacciones(Long empresaId, Long custodioId) {
        return execute(() -> filtroService.obtenerCuentasConTransacciones(empresaId, custodioId));
    }

    public ServiceResult<List<InstrumentoEntity>> obtenerInstrumentosConTransacciones(Long empresaId, Long custodioId, String cuenta) {
        return execute(() -> filtroService.obtenerInstrumentosConTransacciones(empresaId, custodioId, cuenta));
    }

    // Helper para encapsular llamadas a servicio

    private <T> ServiceResult<T> execute(ServiceCallable<T> callable) {
        try {
            return ServiceResult.success(callable.call());
        } catch (Exception e) {
            return ServiceResult.error(e.getMessage(), e);
        }
    }

    @FunctionalInterface
    private interface ServiceCallable<T> {
        T call() throws Exception;
    }
}