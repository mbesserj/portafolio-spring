package com.portafolio.ui.factory;

import com.portafolio.masterdata.interfaces.CustodioService;
import com.portafolio.masterdata.interfaces.TransaccionService;
import com.portafolio.masterdata.interfaces.TipoMovimientoService;
import com.portafolio.masterdata.interfaces.InstrumentoService;
import com.portafolio.masterdata.interfaces.EmpresaService;
import com.portafolio.costing.api.CostingApi;
import com.portafolio.model.dto.*;
import com.portafolio.model.entities.*;
import com.portafolio.service.implement.AuthenticationService;
import com.portafolio.service.implement.UsuarioService;
import com.portafolio.normalizar.service.NormalizationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AppFacade {

    private static final Logger logger = LoggerFactory.getLogger(AppFacade.class);

    // Dependencias inyectadas por Spring (reemplaza a ServiceContainer)
    private final AuthenticationService authenticationService;
    private final UsuarioService usuarioService;
    private final PerfilService perfilService;
    private final EtlService etlService;
    private final NormalizationService normalizationService;
    private final CostingApi costingApi;
    private final FiltroService filtroService;
    private final EmpresaService empresaService;
    private final CustodioService custodioService;
    private final InstrumentoService instrumentoService;
    private final TransaccionService transaccionService;
    private final OperacionesTrxsService operacionesTrxsService;
    private final KardexApi kardexApi;
    private final SaldoActualService saldoActualService;
    private final SaldoMensualService saldoMensualService;
    private final ResumenPortafolioService resumenPortafolioService;
    private final ResultadoInstrumentoService resultadoInstrumentoService;
    private final ResumenHistoricoService resumenHistoricoService;
    private final ResumenSaldoEmpresaService resumenSaldoEmpresaService;
    private final ConfrontaService confrontaService;
    private final ProblemasTrxsService problemasTrxsService;
    private final TipoMovimientoService tipoMovimientosService;
    private final FusionInstrumentoService fusionInstrumentoService;

    // --- MÉTODOS DE LA FACHADA ---
    
    public ServiceResult<Boolean> autenticarUsuario(String usuario, String contrasena) {
        return executeServiceCall(() -> {
            AuthenticationService.AuthenticationResult authResult = authenticationService.autenticar(usuario, contrasena);
            return authResult.isSuccess();
        }, "Usuario o contraseña incorrectos.");
    }

    public ServiceResult<Boolean> hayUsuariosRegistrados() {
        return executeServiceCall(usuarioService::hayUsuariosRegistrados, "No se pudo verificar la existencia de usuarios.");
    }

    public ServiceResult<UsuarioDto> crearUsuarioAdmin(String usuario, String contrasena) {
        return executeServiceCall(() -> usuarioService.crearUsuarioAdmin(usuario, contrasena), "Error al crear el usuario administrador.");
    }

    public ServiceResult<UsuarioDto> registrarNuevoUsuario(String usuario, String password, String email) {
        return executeServiceCall(() -> usuarioService.registrarNuevoUsuario(usuario, password, email), "Error al registrar nuevo usuario.");
    }

    public ServiceResult<List<UsuarioEntity>> obtenerUsuariosPorPerfil(PerfilEntity perfil) {
        return executeServiceCall(() -> usuarioService.obtenerUsuariosPorPerfil(perfil), "Error al obtener usuarios por perfil.");
    }

    public ServiceResult<Void> cambiarPerfilDeUsuario(Long usuarioId, PerfilEntity perfilOrigen, PerfilEntity perfilDestino) {
        return executeServiceCall(() -> {
            usuarioService.cambiarPerfilDeUsuario(usuarioId, perfilOrigen, perfilDestino);
            return null;
        }, "Error al cambiar perfil de usuario.");
    }

    public ServiceResult<Void> desactivarUsuario(Long usuarioId) {
        return executeServiceCall(() -> {
            usuarioService.desactivarUsuario(usuarioId);
            return null;
        }, "Error al desactivar usuario.");
    }

    // --- MÉTODOS HELPER (y el resto de la clase) ---

    private <T> ServiceResult<T> executeServiceCall(ServiceCallable<T> serviceCall, String errorMessage) {
        try {
            return ServiceResult.success(serviceCall.call());
        } catch (Exception e) {
            logger.error("{} - Causa: {}", errorMessage, e.getMessage(), e);
            return ServiceResult.error(errorMessage, e);
        }
    }

    private ServiceResult<Void> executeServiceCall(ServiceRunnable serviceCall, String errorMessage) {
        try {
            serviceCall.run();
            return ServiceResult.success(null);
        } catch (Exception e) {
            logger.error("{} - Causa: {}", errorMessage, e.getMessage(), e);
            return ServiceResult.error(errorMessage, e);
        }
    }

    @FunctionalInterface
    private interface ServiceCallable<T> {
        T call() throws Exception;
    }

    @FunctionalInterface
    private interface ServiceRunnable {
        void run() throws Exception;
    }
}