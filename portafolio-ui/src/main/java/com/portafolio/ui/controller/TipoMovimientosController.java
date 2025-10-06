package com.portafolio.ui.controller;

import com.portafolio.model.dto.TipoMovimientoEstadoDto;
import com.portafolio.model.enums.TipoEnumsCosteo;
import com.portafolio.ui.service.TipoMovimientoService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;

/**
 * Controlador para la vista TipoMovimientosView.fxml. Gestiona la visualización
 * y edición de los tipos de movimiento en una ventana MODAL.
 */
public class TipoMovimientosController {

    @FXML
    private TableView<TipoMovimientoEstadoDto> tablaMovimientos;
    @FXML
    private TableColumn<TipoMovimientoEstadoDto, Long> colId;
    @FXML
    private TableColumn<TipoMovimientoEstadoDto, String> colDescripcion;
    @FXML
    private TableColumn<TipoMovimientoEstadoDto, String> colMovimiento;
    @FXML
    private TableColumn<TipoMovimientoEstadoDto, TipoEnumsCosteo> colEstado;

    private final TipoMovimientoService service;

    public TipoMovimientosController(TipoMovimientoService service) {
        this.service = service;
    }
    
    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colMovimiento.setCellValueFactory(new PropertyValueFactory<>("movimiento"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        tablaMovimientos.setEditable(true);
        colEstado.setCellFactory(ComboBoxTableCell.forTableColumn(TipoEnumsCosteo.values()));

        colEstado.setOnEditCommit(event -> {
            TipoMovimientoEstadoDto dtoEditado = event.getRowValue();
            TipoEnumsCosteo nuevoEstado = event.getNewValue();
            dtoEditado.setEstado(nuevoEstado);
            
            try {
                service.actualizarEstadoContable(dtoEditado.getId(), nuevoEstado);
            } catch (Exception e) {
                System.err.println("Falló la actualización: " + e.getMessage());
                cargarDatos();
            }
        });

        cargarDatos();
    }

    /**
     * CAMBIO: La lógica de este método ahora cierra su propia ventana (Stage).
     */
    @FXML
    private void handleCerrar(ActionEvent event) {
        Stage stage = (Stage) tablaMovimientos.getScene().getWindow();
        stage.close();
    }

    private void cargarDatos() {
        List<TipoMovimientoEstadoDto> datos = service.obtenerMovimientosConCriteria();
        tablaMovimientos.setItems(FXCollections.observableArrayList(datos));
    }
}