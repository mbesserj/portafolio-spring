package com.portafolio.ui.App;

import com.portafolio.ui.controller.NavigatorService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor // Lombok generará el constructor por nosotros
public class MainApp implements ApplicationRunner {

    private final NavigatorService navigatorService; // Spring inyectará esto automáticamente

    @Override
    public void run(org.springframework.boot.ApplicationArguments args) throws Exception {
        // Lanza la aplicación JavaFX
        Application.launch(JavaFxApp.class, args.getSourceArgs());
    }

    /**
     * Clase anidada que representa la aplicación JavaFX real.
     * Esto es necesario porque el método launch() de JavaFX requiere
     * una clase que herede de Application.
     */
    public static class JavaFxApp extends Application {
        private static NavigatorService staticNavigatorService;

        @Override
        public void init() throws Exception {
            // Este truco nos permite pasar el NavigatorService al contexto de JavaFX
            MainApp.this.navigatorService.setPrimaryStage(new Stage());
            staticNavigatorService = MainApp.this.navigatorService;
        }

        @Override
        public void start(Stage primaryStage) {
            // Usa el NavigatorService para iniciar la lógica de login y la ventana principal
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