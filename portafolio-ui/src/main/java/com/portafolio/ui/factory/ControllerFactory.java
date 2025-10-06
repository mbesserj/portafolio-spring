package com.portafolio.ui.factory;

import com.portafolio.masterdata.interfaces.CustodioService;
import com.portafolio.masterdata.interfaces.TransaccionService;
import com.portafolio.masterdata.interfaces.TipoMovimientoService;
import com.portafolio.masterdata.interfaces.InstrumentoService;
import com.portafolio.masterdata.interfaces.EmpresaService;
import com.portafolio.ui.controller.*;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;

@Component
public class ControllerFactory {

    private final AppFacade appFacade;
    private final ResourceBundle bundle;
    private NavigatorService navigatorService;
    private final ApplicationContext springContext;
    private final Map<Class<?>, Supplier<?>> controllerRegistry = new HashMap<>();

    public ControllerFactory(AppFacade appFacade, ResourceBundle bundle, ApplicationContext springContext) {
        this.appFacade = appFacade;
        this.bundle = bundle;
        this.springContext = springContext;
    }

    public void setNavigatorService(NavigatorService navigatorService) {
        this.navigatorService = navigatorService;
        registerControllers();
    }

    private void registerControllers() {
        // Controladores que usan AppFacade
        controllerRegistry.put(AppController.class, () -> new AppController(appFacade, navigatorService, bundle));
        controllerRegistry.put(KardexController.class, () -> new KardexController(appFacade, bundle));
        controllerRegistry.put(OperacionesTrxsController.class, () -> new OperacionesTrxsController(appFacade, bundle));
        controllerRegistry.put(LoginController.class, () -> new LoginController(appFacade, bundle));
        controllerRegistry.put(CrearAdminController.class, () -> new CrearAdminController(appFacade, bundle));
        controllerRegistry.put(SaldosController.class, () -> new SaldosController(appFacade, bundle));
        controllerRegistry.put(ResumenPortafolioController.class, () -> new ResumenPortafolioController(appFacade, bundle));
        controllerRegistry.put(ResultadoInstrumentoController.class, () -> new ResultadoInstrumentoController(appFacade, bundle));
        controllerRegistry.put(ResumenHistoricoController.class, () -> new ResumenHistoricoController(appFacade, bundle));

        // Controladores que usan servicios específicos (obtenidos de Spring)
        controllerRegistry.put(SaldoMensualController.class, () -> new SaldoMensualController(springContext.getBean(SaldoMensualService.class), springContext.getBean(EmpresaService.class), springContext.getBean(CustodioService.class)));
        controllerRegistry.put(ResumenSaldosController.class, () -> new ResumenSaldosController(springContext.getBean(ResumenSaldoEmpresaService.class)));
        controllerRegistry.put(ConfrontaSaldosController.class, () -> new ConfrontaSaldosController(springContext.getBean(ConfrontaService.class)));
        controllerRegistry.put(ProblemasTrxsController.class, () -> new ProblemasTrxsController(springContext.getBean(ProblemasTrxsService.class), springContext.getBean(EmpresaService.class), springContext.getBean(CustodioService.class)));
        controllerRegistry.put(TipoMovimientosController.class, () -> new TipoMovimientosController(springContext.getBean(TipoMovimientoService.class)));
        controllerRegistry.put(AdminUsuariosController.class, () -> new AdminUsuariosController(springContext.getBean(UsuarioService.class), springContext.getBean(PerfilService.class)));
        controllerRegistry.put(CrearUsuarioController.class, () -> new CrearUsuarioController(springContext.getBean(UsuarioService.class)));
        controllerRegistry.put(TransaccionManualController.class, () -> new TransaccionManualController(springContext.getBean(TransaccionService.class), springContext.getBean(TipoMovimientoService.class), springContext.getBean(InstrumentoService.class), springContext.getBean(EmpresaService.class), springContext.getBean(CustodioService.class)));

        // Controladores simples
        controllerRegistry.put(AjusteManualController.class, AjusteManualController::new);
        controllerRegistry.put(KardexDetallesController.class, KardexDetallesController::new);
        controllerRegistry.put(TransaccionDetallesController.class, TransaccionDetallesController::new);
        controllerRegistry.put(ResultadosProcesoController.class, ResultadosProcesoController::new);
        controllerRegistry.put(CuadraturaSaldosController.class, CuadraturaSaldosController::new);
    }

    @SuppressWarnings("unchecked")
    public <T> T createController(Class<T> controllerClass) {
        Supplier<?> supplier = controllerRegistry.get(controllerClass);
        if (supplier != null) return (T) supplier.get();
        try {
            return controllerClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("No se encontró registro para crear el controlador: " + controllerClass.getSimpleName(), e);
        }
    }
}