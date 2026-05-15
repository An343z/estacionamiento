package com.estacionamiento.ui.modules;

import com.estacionamiento.controladores.ConvenioController;
import com.estacionamiento.modelos.ConvenioRestaurante;
import com.estacionamiento.modelos.Restaurante;
import com.estacionamiento.ui.Session;
import com.estacionamiento.ui.UI;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Módulo de Convenios con Restaurantes.
 * ConveniosImpl y ConveniosModule ya existen en UsuariosImpl.java —
 * este archivo NO los redefine; solo agrega el módulo completo de Restaurantes
 * como clase independiente.
 *
 * IMPORTANTE: NO copiar este archivo si ya tienes ConveniosModule.java.
 * Solo úsalo para reemplazar si el tuyo causó errores de duplicado.
 */
class RestaurantesImpl extends VBox {

    private final ConvenioController ctrl = new ConvenioController();
    private ObservableList<Restaurante> datos;
    private TableView<Restaurante> tabla;

    RestaurantesImpl() {
        setPadding(new Insets(24, 28, 24, 28));
        setSpacing(16);
        setStyle("-fx-background-color:" + UI.BG + ";");
        construir();
        cargar();
    }

    private void construir() {
        Button btnNuevo = UI.btnPrimario("+ Nuevo restaurante");
        btnNuevo.setOnAction(e -> formulario(null));

        HBox header = UI.encabezado(
                "Restaurantes",
                "Establecimientos asociados al estacionamiento",
                btnNuevo);

        tabla = new TableView<>();
        UI.estilizarTabla(tabla);
        datos = FXCollections.observableArrayList();
        tabla.setItems(datos);
        VBox.setVgrow(tabla, Priority.ALWAYS);

        TableColumn<Restaurante, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(50);

        TableColumn<Restaurante, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colNombre.setPrefWidth(200);

        TableColumn<Restaurante, String> colDesc = new TableColumn<>("Descripción");
        colDesc.setCellValueFactory(new PropertyValueFactory<>("descripcion"));

        TableColumn<Restaurante, String> colTel = new TableColumn<>("Teléfono");
        colTel.setCellValueFactory(new PropertyValueFactory<>("telefono"));

        TableColumn<Restaurante, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Restaurante, String> colComision = new TableColumn<>("Comisión %");
        colComision.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        String.format("%.0f%%", c.getValue().getComisionPorcentaje())));

        TableColumn<Restaurante, Boolean> colActivo = new TableColumn<>("Estado");
        colActivo.setCellValueFactory(new PropertyValueFactory<>("activo"));
        colActivo.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Boolean val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setGraphic(null); return; }
                setGraphic(UI.badge(val ? "Activo" : "Inactivo",
                        val ? UI.badgeGreen() : UI.badgeGray()));
            }
        });

        TableColumn<Restaurante, Void> colAcc = new TableColumn<>("Acciones");
        colAcc.setCellFactory(col -> new TableCell<>() {
            final Button edit = UI.btnSecundario("✏️ Editar");
            final Button del  = UI.btnPeligro("🗑️");
            {
                edit.setStyle(edit.getStyle() + UI.BTN_SMALL);
                del.setStyle(del.getStyle() + UI.BTN_SMALL);
                edit.setOnAction(e -> formulario(getTableView().getItems().get(getIndex())));
                del.setOnAction(e -> {
                    Restaurante r = getTableView().getItems().get(getIndex());
                    if (UI.confirmar("Desactivar", "¿Desactivar \"" + r.getNombre() + "\"?")) {
                        ctrl.desactivarRestaurante(r.getId());
                        cargar();
                    }
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                HBox h = new HBox(6, edit, del);
                h.setAlignment(Pos.CENTER_LEFT);
                setGraphic(h);
            }
        });
        colAcc.setPrefWidth(150);

        tabla.getColumns().addAll(colId, colNombre, colDesc, colTel, colEmail, colComision, colActivo, colAcc);
        tabla.setPlaceholder(UI.panelVacio("🏪", "No hay restaurantes registrados"));

        getChildren().addAll(header, tabla);
    }

    private void cargar() {
        try {
            int estId = resolverEstId();
            datos.setAll(ctrl.obtenerRestaurantesPorEstacionamiento(estId));
        } catch (Exception e) {
            UI.mostrarError("Error", e.getMessage());
        }
    }

    private void formulario(Restaurante editar) {
        Stage v = new Stage();
        v.initModality(Modality.APPLICATION_MODAL);
        v.setTitle(editar == null ? "Nuevo restaurante" : "Editar restaurante");
        v.setResizable(false);

        VBox cont = new VBox(14);
        cont.setPadding(new Insets(24));
        cont.setPrefWidth(460);

        Label titulo = new Label(editar == null ? "🏪 Nuevo restaurante" : "✏️ Editar restaurante");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 15));

        TextField fNombre   = UI.campo("Nombre del restaurante");
        TextField fDesc     = UI.campo("Descripción");
        TextField fTelefono = UI.campo("Teléfono");
        TextField fEmail    = UI.campo("Email");
        TextField fComision = UI.campo("Ej: 10 (porcentaje)");

        if (editar != null) {
            fNombre.setText(editar.getNombre());
            fDesc.setText(editar.getDescripcion() != null ? editar.getDescripcion() : "");
            fTelefono.setText(editar.getTelefono() != null ? editar.getTelefono() : "");
            fEmail.setText(editar.getEmail() != null ? editar.getEmail() : "");
            fComision.setText(String.valueOf(editar.getComisionPorcentaje()));
        }

        GridPane grid = new GridPane();
        grid.setHgap(12); grid.setVgap(12);
        grid.add(UI.grupoCampo("Nombre *", fNombre),       0, 0, 2, 1);
        grid.add(UI.grupoCampo("Teléfono", fTelefono),     0, 1);
        grid.add(UI.grupoCampo("Email", fEmail),            1, 1);
        grid.add(UI.grupoCampo("Descripción", fDesc),      0, 2, 2, 1);
        grid.add(UI.grupoCampo("Comisión %", fComision),   0, 3);
        ColumnConstraints cc = new ColumnConstraints();
        cc.setPercentWidth(50);
        grid.getColumnConstraints().addAll(cc,
                new ColumnConstraints() {{ setPercentWidth(50); }});

        Label err = UI.errorLabel();
        Button guardar  = UI.btnPrimario(editar == null ? "Guardar" : "Actualizar");
        Button cancelar = UI.btnSecundario("Cancelar");
        cancelar.setOnAction(e -> v.close());

        guardar.setOnAction(e -> {
            try {
                Restaurante r = editar != null ? editar : new Restaurante();
                r.setNombre(fNombre.getText().trim());
                r.setDescripcion(fDesc.getText().trim());
                r.setTelefono(fTelefono.getText().trim());
                r.setEmail(fEmail.getText().trim());
                r.setEstacionamientoId(resolverEstId());
                r.setActivo(true);
                try {
                    r.setComisionPorcentaje(Double.parseDouble(fComision.getText().trim()));
                } catch (NumberFormatException ignored) {
                    r.setComisionPorcentaje(0);
                }

                boolean ok = editar == null
                        ? ctrl.crearRestaurante(r)
                        : ctrl.actualizarRestaurante(r);

                if (ok) { cargar(); v.close(); }
                else UI.setError(err, "Error al guardar.");
            } catch (IllegalArgumentException ex) {
                UI.setError(err, ex.getMessage());
            }
        });

        HBox bRow = new HBox(10, cancelar, guardar);
        bRow.setAlignment(Pos.CENTER_RIGHT);
        cont.getChildren().addAll(titulo, UI.separador(), grid, err, UI.separador(), bRow);
        v.setScene(new Scene(cont));
        v.showAndWait();
    }

    private int resolverEstId() {
        Session s = Session.getInstance();
        Integer id = s.getEstacionamientoActualId();
        if (id == null) id = s.getEstacionamientoId();
        return id != null ? id : 1;
    }
}

/**
 * Módulo público de Restaurantes.
 * Se accede desde MainView con: case CONVENIOS -> new RestaurantesModule()
 * (o agrega RESTAURANTES al Sidebar)
 */
public class RestaurantesModule extends RestaurantesImpl {
    public RestaurantesModule() { super(); }
}
