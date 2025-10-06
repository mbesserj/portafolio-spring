package com.portafolio.ui.controller;

import com.portafolio.masterdata.implement.TipoMovimientoServiceImpl;
import com.portafolio.masterdata.implement.UsuarioService;
import com.portafolio.masterdata.implement.UsuarioService.UserRegistrationResult;
import com.portafolio.masterdata.interfaces.CustodioService;
import com.portafolio.masterdata.interfaces.EmpresaService;
import com.portafolio.masterdata.interfaces.InstrumentoService;
import com.portafolio.masterdata.interfaces.TransaccionService;
import com.portafolio.model.dto.*;
import com.portafolio.model.entities.*;
import com.portafolio.model.enums.*;
import com.portafolio.ui.factory.ServiceResult;
import com.portafolio.ui.service.AuthenticationService;
import com.portafolio.ui.service.ConfrontaService;
import com.portafolio.ui.service.CostingService;
import com.portafolio.ui.service.FiltroService;
import com.portafolio.ui.service.FusionInstrumentoService;
import com.portafolio.ui.service.KardexService;
import com.portafolio.ui.service.NormalizarService;
import com.portafolio.ui.service.OperacionesTrxsService;
import com.portafolio.ui.service.PerfilService;
import com.portafolio.ui.service.ProblemasTrxsService;
import com.portafolio.ui.service.ProcesoCargaDiariaService;
import com.portafolio.ui.service.ProcesoCargaInicialService;
import com.portafolio.ui.service.ResultadoInstrumentoService;
import com.portafolio.ui.service.ResumenHistoricoService;
import com.portafolio.ui.service.ResumenPortafolioService;
import com.portafolio.ui.service.ResumenSaldoEmpresaService;
import com.portafolio.ui.service.SaldoActualService;
import com.portafolio.ui.service.SaldoMensualService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Fachada completa de la aplicación que proporciona una interfaz unificada
 * para que los controladores de UI accedan a todos los servicios del negocio.
 */
public class AppFacade {

    private static final Logger logger = LoggerFactory.getLogger(AppFacade.class);
    private final ServiceContainer container;

    public AppFacade(ServiceContainer container) {
        this.container = container;
    }

    // --- INTERFACES FUNCIONALES PARA MANEJO DE EXCEPCIONES ---
    @FunctionalInterface
    private interface ServiceCallable<T> {
        T call() throws Exception;
    }

    @FunctionalInterface
    private interface ServiceRunnable {
        void run() throws Exception;
    }

    // --- ACCESO GENÉRICO A SERVICIOS ---
    public <T> T getService(Class<T> serviceClass) {
        return container.getService(serviceClass);
    }

    // --- AUTENTICACIÓN Y USUARIOS ---
    public ServiceResult<Boolean> autenticarUsuario(String usuario, String contrasena) {
        return executeServiceCall(
                () -> {
                    AuthenticationService authService = container.getService(AuthenticationService.class);
                    AuthenticationService.AuthenticationResult authResult = authService.autenticar(usuario, contrasena);
                    return authResult.isSuccess();
                },
                "Usuario o contraseña incorrectos."
        );
    }

    public ServiceResult<Boolean> hayUsuariosRegistrados() {
        return executeServiceCall(
                () -> container.getService(UsuarioService.class).hayUsuariosRegistrados(),
                "No se pudo verificar la existencia de usuarios."
        );
    }

    public ServiceResult<UsuarioService.UserRegistrationResult> crearUsuarioAdmin(String usuario, String contrasena) {
        return executeServiceCall(
                () -> container.getService(UsuarioService.class).crearUsuarioAdmin(usuario, contrasena),
                "Error al crear el usuario administrador."
        );
    }

    public ServiceResult<UserRegistrationResult> registrarNuevoUsuario(String usuario, String password, String email) {
        return executeServiceCall(
                () -> container.getService(UsuarioService.class).registrarNuevoUsuario(usuario, password, email),
                "Error al registrar nuevo usuario."
        );
    }

    public ServiceResult<List<UsuarioEntity>> obtenerUsuariosPorPerfil(PerfilEntity perfil) {
        return executeServiceCall(
                () -> container.getService(UsuarioService.class).obtenerUsuariosPorPerfil(perfil),
                "Error al obtener usuarios por perfil."
        );
    }

    public ServiceResult<Void> cambiarPerfilDeUsuario(Long usuarioId, PerfilEntity perfilOrigen, PerfilEntity perfilDestino) {
        return executeServiceCall(
                () -> container.getService(UsuarioService.class).cambiarPerfilDeUsuario(usuarioId, perfilOrigen, perfilDestino),
                "Error al cambiar perfil de usuario."
        );
    }

    public ServiceResult<Void> desactivarUsuario(Long usuarioId) {
        return executeServiceCall(
                () -> container.getService(UsuarioService.class).desactivarUsuario(usuarioId),
                "Error al desactivar usuario."
        );
    }

    // --- PERFILES ---
    public ServiceResult<List<PerfilEntity>> obtenerTodosPerfiles() {
        return executeServiceCall(
                () -> container.getService(PerfilService.class).obtenerTodos(),
                "Error al obtener la lista de perfiles."
        );
    }

    public ServiceResult<PerfilEntity> buscarOCrearPerfil(String nombre) {
        return executeServiceCall(
                () -> container.getService(PerfilService.class).buscarOCrearPorNombre(nombre),
                "Error al buscar o crear el perfil."
        );
    }

    // --- PROCESOS DE CARGA ---
    public ServiceResult<ResultadoCargaDto> ejecutarCargaDiaria(ListaEnumsCustodios custodio, File archivo) {
        return executeServiceCall(
                () -> container.getService(ProcesoCargaDiariaService.class).ejecutar(custodio, archivo),
                "Error en carga diaria."
        );
    }

    public ServiceResult<ResultadoCargaDto> ejecutarCargaInicial(ListaEnumsCustodios custodio, File archivo) {
        return executeServiceCall(
                () -> container.getService(ProcesoCargaInicialService.class).ejecutar(custodio, archivo),
                "Error en carga inicial."
        );
    }

    public ServiceResult<ResultadoCargaDto> reprocesarNormalizacion() {
        return executeServiceCall(
                () -> container.getService(NormalizarService.class).ejecutar(),
                "Error durante el reprocesamiento de la normalización."
        );
    }

    // --- COSTEO ---
    public ServiceResult<Void> iniciarCosteoCompleto() {
        return executeServiceCall(
                () -> container.getService(CostingService.class).ejecutarCosteoCompleto(),
                "Error al iniciar el costeo completo."
        );
    }

    public ServiceResult<Void> eliminarAjuste(Long transaccionId) {
        return executeServiceCall(
                () -> container.getService(CostingService.class).eliminarAjuste(transaccionId),
                "Error al eliminar el ajuste."
        );
    }

    public ServiceResult<Void> recostearGrupoPorTransaccion(Long transaccionId) {
        return executeServiceCall(() -> {
            TransaccionEntity tx = container.getService(TransaccionService.class).obtenerTransaccionPorId(transaccionId);
            if (tx == null) {
                throw new IllegalArgumentException("No se encontró la transacción con ID: " + transaccionId);
            }
            String claveAgrupacion = tx.getClaveAgrupacion();
            container.getService(CostingService.class).recostearGrupo(claveAgrupacion);
        }, "Error al recostear el grupo.");
    }

    public ServiceResult<AjustePropuestoDto> proponerAjuste(Long transaccionId, TipoAjuste tipo) {
        return executeServiceCall(
                () -> container.getService(CostingService.class).proponerAjuste(transaccionId, tipo),
                "Error al proponer el ajuste."
        );
    }

    public ServiceResult<Void> crearAjuste(Long transaccionId, TipoAjuste tipo, BigDecimal cantidad, BigDecimal precio) {
        return executeServiceCall(
                () -> container.getService(CostingService.class).crearAjuste(transaccionId, tipo, cantidad, precio),
                "Error al crear el ajuste."
        );
    }

    // --- FILTROS Y CONSULTAS BÁSICAS ---
    public ServiceResult<List<EmpresaEntity>> obtenerEmpresasConTransacciones() {
        return executeServiceCall(
                () -> container.getService(FiltroService.class).obtenerEmpresasConTransacciones(),
                "No se pudieron cargar las empresas."
        );
    }

    public ServiceResult<List<CustodioEntity>> obtenerCustodiosConTransacciones(Long empresaId) {
        return executeServiceCall(
                () -> container.getService(FiltroService.class).obtenerCustodiosConTransacciones(empresaId),
                "No se pudieron cargar los custodios."
        );
    }

    public ServiceResult<List<String>> obtenerCuentasConTransacciones(Long empresaId, Long custodioId) {
        return executeServiceCall(
                () -> container.getService(FiltroService.class).obtenerCuentasConTransacciones(empresaId, custodioId),
                "No se pudieron cargar las cuentas."
        );
    }

    public ServiceResult<List<InstrumentoEntity>> obtenerInstrumentosConTransacciones(Long empresaId, Long custodioId, String cuenta) {
        return executeServiceCall(
                () -> container.getService(FiltroService.class).obtenerInstrumentosConTransacciones(empresaId, custodioId, cuenta),
                "No se pudieron cargar los instrumentos."
        );
    }

    // --- EMPRESAS ---
    public List<EmpresaEntity> obtenerEmpresaTodas() {
        try {
            return container.getService(EmpresaService.class).obtenerTodas();
        } catch (Exception e) {
            logger.error("Error al obtener todas las empresas", e);
            return new ArrayList<>();
        }
    }

    public ServiceResult<List<EmpresaEntity>> obtenerTodasEmpresas() {
        return executeServiceCall(
                () -> container.getService(EmpresaService.class).obtenerTodas(),
                "Error al obtener la lista de empresas."
        );
    }

    // --- CUSTODIOS ---
    public List<CustodioEntity> obtenerTodos() {
        try {
            return container.getService(CustodioService.class).obtenerTodos();
        } catch (Exception e) {
            logger.error("Error al obtener todos los custodios", e);
            return new ArrayList<>();
        }
    }

    public ServiceResult<List<CustodioEntity>> obtenerTodosCustodios() {
        return executeServiceCall(
                () -> container.getService(CustodioService.class).obtenerTodos(),
                "Error al obtener la lista de custodios."
        );
    }

    public ServiceResult<List<CustodioEntity>> obtenerCustodiosPorEmpresa(Long empresaId) {
        return executeServiceCall(
                () -> container.getService(CustodioService.class).obtenerCustodiosPorEmpresa(empresaId),
                "Error al obtener custodios por empresa."
        );
    }

    public ServiceResult<List<String>> obtenerCuentasPorCustodioYEmpresa(Long custodioId, Long empresaId) {
        return executeServiceCall(
                () -> container.getService(CustodioService.class).obtenerCuentasPorCustodioYEmpresa(custodioId, empresaId),
                "Error al obtener cuentas por custodio y empresa."
        );
    }

    // --- INSTRUMENTOS ---
    public ServiceResult<InstrumentoEntity> buscarOCrearInstrumento(String nemo, String nombre) {
        return executeServiceCall(
                () -> container.getService(InstrumentoService.class).buscarOCrear(nemo, nombre),
                "Error al buscar o crear instrumento."
        );
    }

    public ServiceResult<List<InstrumentoEntity>> obtenerTodosInstrumentos() {
        return executeServiceCall(
                () -> container.getService(InstrumentoService.class).obtenerTodos(),
                "Error al obtener la lista de instrumentos."
        );
    }

    public ServiceResult<Void> fusionarInstrumentos(Long idInstrumentoAntiguo, Long idInstrumentoNuevo) {
        return executeServiceCall(
                () -> container.getService(FusionInstrumentoService.class).fusionarYPrepararRecosteo(idInstrumentoAntiguo, idInstrumentoNuevo),
                "La fusión de instrumentos falló."
        );
    }

    // --- TRANSACCIONES ---
    public ServiceResult<TransaccionEntity> obtenerTransaccionPorId(Long transaccionId) {
        return executeServiceCall(
                () -> container.getService(TransaccionService.class).obtenerTransaccionPorId(transaccionId),
                "No se pudo obtener la transacción."
        );
    }

    public ServiceResult<Void> toggleIgnorarEnCosteo(Long transaccionId) {
        return executeServiceCall(
                () -> container.getService(TransaccionService.class).toggleIgnorarEnCosteo(transaccionId),
                "Error al actualizar el estado de la transacción."
        );
    }

    public ServiceResult<Void> crearTransaccionManual(TransaccionManualDto dto) {
        return executeServiceCall(
                () -> container.getService(TransaccionService.class).crearTransaccionManual(dto),
                "Error al crear transacción manual."
        );
    }

    // --- OPERACIONES TRANSACCIONES ---
    public ServiceResult<List<OperacionesTrxsDto>> obtenerOperacionesPorGrupo(
            Long empresaId, Long custodioId, String cuenta, Long instrumentoId, Long instrumentoNuevoId) {
        return executeServiceCall(() -> {
            EmpresaService empresaService = container.getService(EmpresaService.class);
            CustodioService custodioService = container.getService(CustodioService.class);
            InstrumentoService instrumentoService = container.getService(InstrumentoService.class);
            
            String razonSocial = empresaService.obtenerPorId(empresaId).getRazonSocial();
            String nombreCustodio = custodioService.obtenerPorId(custodioId).getNombreCustodio();
            
            List<String> nemos = new ArrayList<>();
            nemos.add(instrumentoService.obtenerPorId(instrumentoId).getInstrumentoNemo());
            if (instrumentoNuevoId != null && !instrumentoNuevoId.equals(instrumentoId)) {
                nemos.add(instrumentoService.obtenerPorId(instrumentoNuevoId).getInstrumentoNemo());
            }
            
            return container.getService(OperacionesTrxsService.class).obtenerTransaccionesPorGrupo(razonSocial, nombreCustodio, cuenta, nemos);
        }, "No se pudieron cargar las operaciones.");
    }

    // --- KARDEX ---
    public ServiceResult<List<KardexReporteDto>> obtenerMovimientosKardex(
            Long empresaId, Long custodioId, String cuenta, Long instrumentoId) {
        return executeServiceCall(
                () -> container.getService(KardexService.class).obtenerMovimientosPorGrupo(empresaId, custodioId, cuenta, instrumentoId),
                "No se pudieron obtener los movimientos de kárdex."
        );
    }

    // --- SALDOS ---
    public ServiceResult<List<ResumenSaldoDto>> obtenerSaldosValorizados(Long empresaId, Long custodioId) {
        return executeServiceCall(
                () -> container.getService(SaldoActualService.class).obtenerSaldosValorizados(empresaId, custodioId),
                "Error al obtener los saldos valorizados."
        );
    }

    public ServiceResult<List<SaldoMensualDto>> obtenerSaldosMensuales(String empresa, String custodio, Integer anio, String moneda) {
        return executeServiceCall(
                () -> container.getService(SaldoMensualService.class).obtenerSaldosMensuales(empresa, custodio, anio, moneda),
                "Error al obtener saldos mensuales."
        );
    }

    // --- RESÚMENES ---
    public ServiceResult<List<ResumenInstrumentoDto>> obtenerResumenPortafolio(Long empresaId, Long custodioId, String cuenta) {
        return executeServiceCall(
                () -> container.getService(ResumenPortafolioService.class).obtenerResumenPortafolio(empresaId, custodioId, cuenta),
                "Error al obtener el resumen de portafolio."
        );
    }

    public ServiceResult<List<ResultadoInstrumentoDto>> obtenerHistorialResultados(Long empresaId, Long custodioId, String cuenta, Long instrumentoId) {
        return executeServiceCall(
                () -> container.getService(ResultadoInstrumentoService.class).obtenerHistorialResultados(empresaId, custodioId, cuenta, instrumentoId),
                "Error al obtener el historial de resultados del instrumento."
        );
    }

    public ServiceResult<List<ResumenHistoricoDto>> generarReporteHistorico(Long empresaId, Long custodioId, String cuenta) {
        return executeServiceCall(
                () -> container.getService(ResumenHistoricoService.class).generarReporte(empresaId, custodioId, cuenta),
                "Error al generar reporte histórico."
        );
    }

    public ServiceResult<List<ResumenSaldoEmpresaDto>> obtenerResumenSaldos() {
        return executeServiceCall(
                () -> container.getService(ResumenSaldoEmpresaService.class).obtenerResumenSaldos(),
                "Error al obtener resumen de saldos de empresa."
        );
    }

    // --- CONFRONTA SALDOS ---
    public ServiceResult<List<ConfrontaSaldoDto>> obtenerDiferenciasDeSaldos() {
        return executeServiceCall(
                () -> container.getService(ConfrontaService.class).obtenerDiferenciasDeSaldos(),
                "Error al obtener diferencias de saldos."
        );
    }

    // --- PROBLEMAS TRANSACCIONES ---
    public ServiceResult<List<ProblemasTrxsDto>> obtenerTransaccionesConProblemas(String empresa, String custodio) {
        return executeServiceCall(
                () -> container.getService(ProblemasTrxsService.class).obtenerTransaccionesConProblemas(empresa, custodio),
                "Error al obtener transacciones con problemas."
        );
    }

    // --- TIPOS DE MOVIMIENTO ---
    public ServiceResult<List<TipoMovimientoEntity>> obtenerTodosLosTipos() {
        return executeServiceCall(
                () -> container.getService(TipoMovimientoServiceImpl.class).obtenerTodosLosTipos(),
                "Error al obtener tipos de movimiento."
        );
    }

    public ServiceResult<List<TipoMovimientoEstadoDto>> obtenerMovimientosConCriteria() {
        return executeServiceCall(
                () -> container.getService(TipoMovimientoServiceImpl.class).obtenerMovimientosConCriteria(),
                "Error al obtener movimientos con criterios."
        );
    }

    public ServiceResult<Void> actualizarEstadoContable(Long id, TipoEnumsCosteo nuevoEstado) {
        return executeServiceCall(
                () -> container.getService(TipoMovimientoServiceImpl.class).actualizarEstadoContable(id, nuevoEstado),
                "Error al actualizar estado contable."
        );
    }

    // --- MÉTODOS HELPER PARA EJECUTAR LLAMADAS A SERVICIOS ---
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
}