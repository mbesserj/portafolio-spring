package com.ui.factory;

import com.ui.controller.*;
import com.serv.factory.ServiceContainer;
import com.serv.service.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Supplier;

public class ControllerFactory {

    private final AppFacade appFacade;
    private final ResourceBundle bundle;
    private NavigatorService navigatorService;
    private final ServiceContainer serviceContainer;

    // Usamos un mapa para registrar cómo se crea cada controlador.
    private final Map<Class<?>, Supplier<?>> controllerRegistry = new HashMap<>();

    public ControllerFactory(AppFacade appFacade, ResourceBundle bundle) {
        this.appFacade = appFacade;
        this.bundle = bundle;
        this.serviceContainer = ServiceContainer.getInstance();
    }

    public AppFacade getFacade() {
        return this.appFacade;
    }

    public void setNavigatorService(NavigatorService navigatorService) {
        this.navigatorService = navigatorService;
        registerControllers();
    }

    private void registerControllers() {
        // --- Controladores principales ---
        controllerRegistry.put(AppController.class,
                () -> new AppController(appFacade, navigatorService, bundle));

        // --- Controladores con AppFacade y Bundle ---
        controllerRegistry.put(KardexController.class,
                () -> new KardexController(appFacade, bundle));
        controllerRegistry.put(OperacionesTrxsController.class,
                () -> new OperacionesTrxsController(appFacade, bundle));
        controllerRegistry.put(LoginController.class,
                () -> new LoginController(appFacade, bundle));
        controllerRegistry.put(CrearAdminController.class,
                () -> new CrearAdminController(appFacade, bundle));
        controllerRegistry.put(SaldosController.class, 
                () -> new SaldosController(appFacade, bundle));
        controllerRegistry.put(ResumenPortafolioController.class, 
                () -> new ResumenPortafolioController(appFacade, bundle));
        controllerRegistry.put(ResultadoInstrumentoController.class,
                () -> new ResultadoInstrumentoController(appFacade, bundle));
        controllerRegistry.put(ResumenHistoricoController.class,
                () -> new ResumenHistoricoController(appFacade, bundle));

        // --- Controladores que necesitan servicios específicos ---
        controllerRegistry.put(SaldoMensualController.class, () -> 
            new SaldoMensualController(
                serviceContainer.getService(SaldoMensualService.class),
                serviceContainer.getService(EmpresaService.class),
                serviceContainer.getService(CustodioService.class)
            ));

        controllerRegistry.put(ResumenSaldosController.class, () ->
            new ResumenSaldosController(
                serviceContainer.getService(ResumenSaldoEmpresaService.class)
            ));

        controllerRegistry.put(ConfrontaSaldosController.class, () ->
            new ConfrontaSaldosController(
                serviceContainer.getService(ConfrontaService.class)
            ));

        controllerRegistry.put(ProblemasTrxsController.class, () ->
            new ProblemasTrxsController(
                serviceContainer.getService(ProblemasTrxsService.class),
                serviceContainer.getService(EmpresaService.class),
                serviceContainer.getService(CustodioService.class)
            ));

        controllerRegistry.put(TipoMovimientosController.class, () ->
            new TipoMovimientosController(
                serviceContainer.getService(TipoMovimientosService.class)
            ));

        controllerRegistry.put(AdminUsuariosController.class, () ->
            new AdminUsuariosController(
                serviceContainer.getService(UsuarioService.class),
                serviceContainer.getService(PerfilService.class)
            ));

        controllerRegistry.put(CrearUsuarioController.class, () ->
            new CrearUsuarioController(
                serviceContainer.getService(UsuarioService.class)
            ));

        controllerRegistry.put(TransaccionManualController.class, () ->
            new TransaccionManualController(
                serviceContainer.getService(TransaccionService.class),
                serviceContainer.getService(TipoMovimientosService.class),
                serviceContainer.getService(InstrumentoService.class),
                serviceContainer.getService(EmpresaService.class),
                serviceContainer.getService(CustodioService.class)
            ));

        // --- Controladores simples sin dependencias ---
        controllerRegistry.put(AjusteManualController.class, 
                () -> new AjusteManualController());
        controllerRegistry.put(KardexDetallesController.class,
                () -> new KardexDetallesController());
        controllerRegistry.put(TransaccionDetallesController.class,
                () -> new TransaccionDetallesController());
        controllerRegistry.put(ResultadosProcesoController.class,
                () -> new ResultadosProcesoController());
        controllerRegistry.put(CuadraturaSaldosController.class,
                () -> new CuadraturaSaldosController());
    }

    @SuppressWarnings("unchecked")
    public <T> T createController(Class<T> controllerClass) {
        Supplier<?> supplier = controllerRegistry.get(controllerClass);
        if (supplier != null) {
            return (T) supplier.get();
        }

        // Fallback para controladores simples sin dependencias
        try {
            return controllerClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("No se encontró un registro para crear el controlador: " 
                + controllerClass.getSimpleName(), e);
        }
    }

    public boolean isServiceAvailable(Class<?> serviceClass) {
        try {
            return serviceContainer.getService(serviceClass) != null;
        } catch (Exception e) {
            return false;
        }
    }
}