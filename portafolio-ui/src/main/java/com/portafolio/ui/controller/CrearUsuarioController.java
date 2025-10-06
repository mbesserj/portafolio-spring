package com.portafolio.ui.controller;

import com.portafolio.model.entities.UsuarioEntity;
import com.portafolio.masterdata.implement.UsuarioService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CrearUsuarioController {

    @FXML private TextField txtUsuario;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblMensaje;
    @FXML private Button btnCrear;

    private UsuarioService usuarioService;
    private UsuarioEntity nuevoUsuario = null;

    public CrearUsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @FXML
    void handleCrearUsuario(ActionEvent event) {
        try {
            usuarioService.registrarNuevoUsuario(
                    txtUsuario.getText(),
                    txtPassword.getText(),
                    txtEmail.getText()
            );

            // Crear usuario básico para satisfacer la interfaz
            this.nuevoUsuario = new UsuarioEntity();

            Stage stage = (Stage) btnCrear.getScene().getWindow();
            stage.close();

        } catch (Exception e) {
            lblMensaje.setText(e.getMessage());
            this.nuevoUsuario = null;
        }
    }

    public UsuarioEntity getNuevoUsuario() {
        return nuevoUsuario;
    }
}