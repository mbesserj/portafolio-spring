package com.portafolio.ui.App;

import com.portafolio.ui.controller.NavigatorService; // Asegúrate de que el import sea el correcto
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MainApp implements ApplicationRunner {

    // Spring inyectará automáticamente el NavigatorService que ya está en su contexto
    private final NavigatorService navigatorService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Le pasamos el control a la clase interna que sabe cómo arrancar JavaFX
        Application.launch(JavaFxApp.class, args.getSourceArgs());
    }

    /**
     * Clase estática interna para manejar el ciclo de vida de JavaFX.
     * Este es un patrón común para integrar JavaFX con Spring.
     */
    public static class JavaFxApp extends Application {
        
        // Usamos una instancia estática para pasar el servicio desde el contexto de Spring
        // al contexto de JavaFX.
        private static NavigatorService staticNavigatorService;

        @Override
        public void init() {
            // Antes de que la ventana se muestre, obtenemos la instancia del
            // NavigatorService que Spring ya creó para nosotros.
            staticNavigatorService = (NavigatorService) SpringJavaFXApplication.getApplicationContext().getBean("navigatorService");
        }

        @Override
        public void start(Stage primaryStage) {
            // Ahora usamos el NavigatorService para toda la lógica de la UI
            staticNavigatorService.setPrimaryStage(primaryStage);

            staticNavigatorService.getFacade().hayUsuariosRegistrados().ifSuccess(hayUsuarios -> {
                if (!hayUsuarios) {
                    if (!staticNavigatorService.mostrarVentanaCrearAdmin()) {
                        Platform.exit();
                        return;
                    }
                }

                if (!staticNavigatorService.mostrarVentanaLogin()) {
                    Platform.exit();
                    return;
                }

                staticNavigatorService.mostrarVentanaPrincipal();

            }).ifError(errorMessage -> {
                System.err.println("Error crítico al iniciar la aplicación: " + errorMessage);
                Platform.exit();
            });
        }
    }
}