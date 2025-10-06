
package com.portafolio.ui.App;

import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class SpringJavaFXApplication extends MainApp {

    private ConfigurableApplicationContext applicationContext;

    @Override
    public void init() {
        // Este método se ejecuta antes de start()
        // Aquí iniciamos el contexto de Spring
        applicationContext = new SpringApplicationBuilder(MainApp.class).run();
    }

    @Override
    public void start(Stage primaryStage) {
        // Pasamos el contexto de Spring a la lógica de arranque de MainApp
        super.setApplicationContext(applicationContext, primaryStage);
        super.start(primaryStage);
    }

    @Override
    public void stop() {
        try {
            // Al cerrar la aplicación, también cerramos el contexto de Spring
            super.stop();
            applicationContext.close();
        } catch (Exception ex) {
            System.getLogger(SpringJavaFXApplication.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }
}