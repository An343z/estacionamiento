package com.estacionamiento.ui.modules;

import com.estacionamiento.controladores.*;
import com.estacionamiento.modelos.*;
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
import java.util.Comparator;
import java.util.List;

// ─────────────────────────────────────────────────────────────
//  USUARIOS
//  Tareas cubiertas:
//    • Gestión de usuarios: crear / editar / desactivar / cambiar contraseña
//    • Asignar usuarios a estacionamientos
//  El Login/Logout ya existe en LoginView y Sidebar; esta clase no los
//  duplica, pero sí agrega un botón "Cerrar sesión" contextual.
// ─────────────────────────────────────────────────────────────
class UsuariosImpl extends VBox {

    private final UsuarioController ctrl = new UsuarioController();
    private final EstacionamientoController estCtrl = new EstacionamientoController();

    private ObservableList<Usuario> datos;
    private TableView<Usuario> tabla;
    private TextField busqueda;
    private ComboBox<String> filtroEstado;
    private ComboBox<String> filtroRol;
    private Button btnRefrescar;

    UsuariosImpl() {
        setPadding(new Insets(24, 28, 24, 28));
        setSpacing(16);
        setStyle("-fx-background-color:" + UI.BG + ";");
        construir();
        cargar();
    }

    // ── Construcción principal ────────────────────────────────
    private void construir() {
        // Sólo el admin puede crear / editar usuarios
        Button btnNuevo = UI.btnPrimario("+ Nuevo empleado");
        btnNuevo.setOnAction(e -> formularioUsuario(null));
        btnNuevo.setVisible(Session.getInstance().isAdmin());
        btnNuevo.setManaged(Session.getInstance().isAdmin());

        busqueda = UI.campo("🔍  Buscar por nombre, usuario, rol, estacionamiento o email…");
        busqueda.textProperty().addListener((o, a, n) -> aplicarFiltro());

        filtroRol = UI.combo();
        filtroRol.getItems().addAll("Todos los roles", "Administrador Global", "Encargado", "Cajero");
        filtroRol.setValue("Todos los roles");
        filtroRol.valueProperty().addListener((o, a, n) -> aplicarFiltro());

        filtroEstado = UI.combo();
        filtroEstado.getItems().addAll("Todos", "Activos", "Inactivos");
        filtroEstado.setValue("Todos");
        filtroEstado.valueProperty().addListener((o, a, n) -> aplicarFiltro());

        btnRefrescar = UI.btnSecundario("⟳ Refrescar");
        btnRefrescar.setOnAction(e -> cargar());

        HBox barraFiltros = new HBox(10, busqueda, filtroRol, filtroEstado, btnRefrescar);
        barraFiltros.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(busqueda, Priority.ALWAYS);

        tabla = new TableView<>();
        UI.estilizarTabla(tabla);
        tabla.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        datos = FXCollections.observableArrayList();
        tabla.setItems(datos);

        // Columnas con anchos fijos
        colStr("Nombre", u -> u.getNombre() + " " + u.getApellido(), 180);
        col("Usuario", "usuario", 130);
        col("Email", "email", 220);
        colRol();
        colEstacionamiento();
        colEstado();
        colAcciones();

        tabla.setPlaceholder(UI.panelVacio("👥", "No hay empleados para mostrar"));

        // Envolver tabla en ScrollPane para permitir desplazamiento horizontal
        ScrollPane scrollTabla = new ScrollPane(tabla);
        scrollTabla.setFitToWidth(false);
        scrollTabla.setFitToHeight(true);
        scrollTabla.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollTabla.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollTabla.setPannable(true);
        scrollTabla.setStyle("-fx-background-color:transparent;");
        VBox.setVgrow(scrollTabla, Priority.ALWAYS);

        getChildren().addAll(
                UI.encabezado("Gestión de Empleados",
                        "Lista de empleados y control de accesos", btnNuevo),
                barraFiltros,
                scrollTabla);
    }


    // ── Columnas helper ──────────────────────────────────────
    private void col(String name, String prop, double w) {
        TableColumn<Usuario, ?> c = new TableColumn<>(name);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        c.setPrefWidth(w);
        tabla.getColumns().add(c);
    }

    @SuppressWarnings("unchecked")
    private void colStr(String name, java.util.function.Function<Usuario, String> fn, double w) {
        TableColumn<Usuario, String> c = new TableColumn<>(name);
        c.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(fn.apply(d.getValue())));
        c.setPrefWidth(w);
        tabla.getColumns().add(c);
    }

    private void colRol() {
        TableColumn<Usuario, String> c = new TableColumn<>("Rol");
        c.setCellValueFactory(d -> {
            String rolCompleto = ctrl.obtenerDescripcionRol(d.getValue().getRol());
            // ✅ Acortar texto para que quepa mejor
            return new javafx.beans.property.SimpleStringProperty(
                rolCompleto.replace("Encargado de Estacionamiento", "Encargado")
                           .replace("Administrador Global", "Admin Global")
            );
        });
        c.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) {
                    setGraphic(null);
                    return;
                }
                String estilo = switch (v) {
                    case "Admin Global" -> UI.badgeBlue();
                    case "Encargado"    -> UI.badgeAmber();
                    default             -> UI.badgeGray();
                };
                setGraphic(UI.badge(v, estilo));
            }
        });
        c.setPrefWidth(110);  // ✅ Más angosto porque el texto es corto
        tabla.getColumns().add(c);
    }

    private void colEstacionamiento() {
        TableColumn<Usuario, String> c = new TableColumn<>("Estacionamiento");
        c.setCellValueFactory(d -> {
            Usuario u = d.getValue();
            // Admin Global muestra "—" en lugar de "Sin estacionamiento"
            if (u.getRol() == 1) {
                return new javafx.beans.property.SimpleStringProperty("—");
            }
            String nom = u.getNombreEstacionamiento();
            // ✅ Acortar si es muy largo
            if (nom != null && nom.length() > 20) {
                nom = nom.substring(0, 18) + "…";
            }
            return new javafx.beans.property.SimpleStringProperty(
                    nom != null ? nom : (u.getEstacionamientoId() == null ? "—" : "ID: " + u.getEstacionamientoId()));
        });
        c.setPrefWidth(160);
        tabla.getColumns().add(c);
    }

    private void colEstado() {
        TableColumn<Usuario, String> c = new TableColumn<>("Estado");
        c.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().isActivo() ? "Activo" : "Inactivo"));
        c.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) {
                    setGraphic(null);
                    return;
                }
                setGraphic(UI.badge(v, "Activo".equals(v) ? UI.badgeGreen() : UI.badgeRed()));
            }
        });
        c.setPrefWidth(90);
        tabla.getColumns().add(c);
    }

    private void colAcciones() {
        boolean esAdmin = Session.getInstance().isAdmin();
        TableColumn<Usuario, Void> c = new TableColumn<>("Acciones");
        c.setCellFactory(col -> new TableCell<>() {
            final Button btnEdit = UI.btnSecundario("Editar");
            final Button btnToggle = UI.btnSecundario("Estado");
            final Button btnPasswd = UI.btnSecundario("Pass");
            final Button btnDelete = UI.btnPeligro("Borrar");
            {
                btnEdit.setStyle(btnEdit.getStyle() + UI.BTN_SMALL);
                btnToggle.setStyle(btnToggle.getStyle() + UI.BTN_SMALL);
                btnPasswd.setStyle(btnPasswd.getStyle() + UI.BTN_SMALL);
                btnDelete.setStyle(btnDelete.getStyle() + UI.BTN_SMALL);

                // Tooltips explicativos
                btnEdit.setTooltip(new Tooltip("Editar empleado"));
                btnToggle.setTooltip(new Tooltip("Activar o desactivar empleado"));
                btnPasswd.setTooltip(new Tooltip("Cambiar contraseña"));
                btnDelete.setTooltip(new Tooltip("Eliminar empleado"));

                btnEdit.setOnAction(e -> formularioUsuario(getTableView().getItems().get(getIndex())));
                btnToggle.setOnAction(e -> toggleActivo(getTableView().getItems().get(getIndex())));
                btnPasswd.setOnAction(e -> formularioCambiarPassword(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(e -> eliminarUsuario(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                HBox h = new HBox(4);
                h.setAlignment(Pos.CENTER_LEFT);
                Usuario u = getTableView().getItems().get(getIndex());
                btnToggle.setText(u.isActivo() ? "Desactivar" : "Activar");
                btnToggle.setTooltip(new Tooltip(u.isActivo() ? "Desactivar empleado" : "Activar empleado"));
                if (esAdmin) {
                    boolean esMismo = u.getId() == Session.getInstance().getUsuario().getId();
                    h.getChildren().addAll(btnEdit, btnToggle, btnPasswd);
                    if (!esMismo) {
                        h.getChildren().add(btnDelete);
                    } else {
                        btnToggle.setDisable(true);
                    }
                } else {
                    if (u.getId() == Session.getInstance().getUsuario().getId()) {
                        h.getChildren().add(btnPasswd);
                    }
                }
                setGraphic(h);
            }
        });
        c.setPrefWidth(280);
        tabla.getColumns().add(c);
    }

    // ── Datos ────────────────────────────────────────────────
    private void cargar() {
        try {
            if (Session.getInstance().isAdmin()) {
                List<Usuario> todos = ctrl.obtenerTodos();
                todos.sort(Comparator.comparing(Usuario::isActivo).reversed()
                        .thenComparing(u -> (u.getNombre() + " " + u.getApellido())));
                datos.setAll(todos);
            } else {
                Usuario yo = ctrl.obtenerUsuario(Session.getInstance().getUsuario().getId());
                datos.setAll(yo != null ? List.of(yo) : List.of());
            }
            aplicarFiltro();
        } catch (Exception e) {
            UI.mostrarError("Error", e.getMessage());
        }
    }

    private void aplicarFiltro() {
        try {
            String q = busqueda.getText();
            String estado = filtroEstado.getValue();
            String rol = filtroRol.getValue();

            List<Usuario> base = Session.getInstance().isAdmin() ? ctrl.obtenerTodos() : datos;

            List<Usuario> filtrados = base.stream()
                    .filter(u -> {
                        if (estado != null && !estado.equals("Todos")) {
                            if (estado.equals("Activos") && !u.isActivo()) return false;
                            if (estado.equals("Inactivos") && u.isActivo()) return false;
                        }
                        if (rol != null && !rol.equals("Todos los roles")) {
                            if (rol.equals("Administrador Global") && u.getRol() != 1) return false;
                            if (rol.equals("Encargado") && u.getRol() != 2) return false;
                            if (rol.equals("Cajero") && u.getRol() != 3) return false;
                        }
                        if (q == null || q.isBlank()) return true;
                        String lq = q.toLowerCase();
                        String nombreCompleto = (u.getNombre() + " " + u.getApellido()).toLowerCase();
                        String estacionamiento = u.getNombreEstacionamiento() != null ? u.getNombreEstacionamiento().toLowerCase() : "";
                        String rolText = ctrl.obtenerDescripcionRol(u.getRol()).toLowerCase();
                        return nombreCompleto.contains(lq)
                                || u.getUsuario().toLowerCase().contains(lq)
                                || (u.getEmail() != null && u.getEmail().toLowerCase().contains(lq))
                                || estacionamiento.contains(lq)
                                || rolText.contains(lq)
                                || (u.isActivo() ? "activo" : "inactivo").contains(lq);
                    })
                    .toList();

            datos.setAll(filtrados);
        } catch (Exception ex) {
            cargar();
        }
    }

    private void toggleActivo(Usuario u) {
        String acc = u.isActivo() ? "desactivar" : "activar";
        if (!UI.confirmar("Cambiar estado", "¿Deseas " + acc + " a " + u.getNombre() + "?"))
            return;
        u.setActivo(!u.isActivo());
        boolean ok = ctrl.actualizarUsuario(u);
        if (ok) {
            cargar();
            UI.mostrarInfo("Estado actualizado", "El empleado ha sido " + (u.isActivo() ? "activado" : "desactivado") + " correctamente.");
        } else {
            UI.mostrarError("Error", "No se pudo cambiar el estado.");
        }
    }

    private void eliminarUsuario(Usuario u) {
        if (!UI.confirmar("Eliminar empleado", "¿Estás seguro de eliminar a " + u.getNombre() + " " + u.getApellido() + "?"))
            return;

        boolean ok = ctrl.eliminarUsuario(u.getId());
        if (ok) {
            cargar();
            UI.mostrarInfo("Empleado eliminado", "El empleado fue eliminado correctamente.");
        } else {
            UI.mostrarError("Error", "No se pudo eliminar el empleado.");
        }
    }

    // ── Formulario crear/editar usuario ─────────────────────
    private void formularioUsuario(Usuario editar) {
        Stage ven = new Stage();
        ven.initModality(Modality.APPLICATION_MODAL);
        ven.setTitle(editar == null ? "Nuevo usuario" : "Editar usuario");
        ven.setResizable(false);

        VBox cont = new VBox(14);
        cont.setPadding(new Insets(24));
        cont.setPrefWidth(520);

        Label titulo = new Label(editar == null ? "➕ Nuevo usuario" : "✏️ Editar usuario");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 15));

        TextField fNombre = UI.campo("Nombre");
        TextField fApellido = UI.campo("Apellido");
        TextField fEmail = UI.campo("correo@ejemplo.com");
        TextField fUsuario = UI.campo("nombre.usuario");
        PasswordField fPass = UI.campoPassword("Contraseña");

        ComboBox<String> comboRol = UI.combo();
        comboRol.getItems().addAll(
                "1 - Administrador Global",
                "2 - Encargado de Estacionamiento",
                "3 - Cajero");
        comboRol.setValue("3 - Cajero");

        // Selector de estacionamiento
        ComboBox<Estacionamiento> comboEst = UI.combo();
        try {
            comboEst.getItems().addAll(estCtrl.obtenerTodosLosEstacionamientos());
        } catch (Exception ex) {
            /* sin estacionamientos */ }
        comboEst.setConverter(new javafx.util.StringConverter<Estacionamiento>() {
            @Override
            public String toString(Estacionamiento e) {
                return e != null ? e.getNombre() : "";
            }

            @Override
            public Estacionamiento fromString(String s) {
                return null;
            }
        });
        comboEst.setPromptText("Seleccionar estacionamiento…");

        // Ocultar/mostrar estacionamiento según rol
        VBox grupoEst = UI.grupoCampo("Estacionamiento asignado", comboEst);
        comboRol.setOnAction(e -> {
            boolean necesitaEst = !comboRol.getValue().startsWith("1");
            grupoEst.setVisible(necesitaEst);
            grupoEst.setManaged(necesitaEst);
        });

        CheckBox cbActivo = new CheckBox("Usuario activo");
        cbActivo.setSelected(true);

        if (editar != null) {
            fNombre.setText(editar.getNombre());
            fApellido.setText(editar.getApellido());
            fEmail.setText(editar.getEmail());
            fUsuario.setText(editar.getUsuario());
            fUsuario.setDisable(true);
            fPass.setPromptText("Dejar vacío para no cambiar");
            comboRol.setValue(editar.getRol() + " - " + ctrl.obtenerDescripcionRol(editar.getRol()));
            cbActivo.setSelected(editar.isActivo());
            if (editar.getEstacionamientoId() != null) {
                comboEst.getItems().stream()
                        .filter(est -> est.getId() == editar.getEstacionamientoId())
                        .findFirst().ifPresent(comboEst::setValue);
            }
            boolean necesitaEst = editar.getRol() != 1;
            grupoEst.setVisible(necesitaEst);
            grupoEst.setManaged(necesitaEst);
        } else {
            grupoEst.setVisible(true);
            grupoEst.setManaged(true);
        }

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.add(UI.grupoCampo("Nombre *", fNombre), 0, 0);
        grid.add(UI.grupoCampo("Apellido *", fApellido), 1, 0);
        grid.add(UI.grupoCampo("Email *", fEmail), 0, 1);
        grid.add(UI.grupoCampo("Usuario *", fUsuario), 1, 1);
        grid.add(UI.grupoCampo("Contraseña" + (editar == null ? " *" : ""), fPass), 0, 2);
        grid.add(UI.grupoCampo("Rol *", comboRol), 1, 2);
        grid.add(grupoEst, 0, 3, 2, 1);
        grid.add(cbActivo, 0, 4, 2, 1);
        ColumnConstraints cc = new ColumnConstraints();
        cc.setPercentWidth(50);
        grid.getColumnConstraints().addAll(cc, new ColumnConstraints() {
            {
                setPercentWidth(50);
            }
        });

        Label err = UI.errorLabel();
        Button guardar = UI.btnPrimario(editar == null ? "Crear usuario" : "Actualizar");
        Button cancelar = UI.btnSecundario("Cancelar");
        cancelar.setOnAction(e -> ven.close());

        guardar.setOnAction(e -> {
            Usuario u = editar != null ? editar : new Usuario();
            u.setNombre(fNombre.getText().trim());
            u.setApellido(fApellido.getText().trim());
            u.setEmail(fEmail.getText().trim());
            u.setUsuario(fUsuario.getText().trim());
            u.setActivo(cbActivo.isSelected());

            // Extraer número de rol
            String rolStr = comboRol.getValue();
            int rol = rolStr != null && !rolStr.isBlank() ? Integer.parseInt(rolStr.substring(0, 1)) : 0;
            u.setRol(rol);

            // Estacionamiento
            if (rol == 1) {
                u.setEstacionamientoId(null);
            } else {
                Estacionamiento sel = comboEst.getValue();
                u.setEstacionamientoId(sel != null ? sel.getId() : null);
            }

            // Contraseña
            String pass = fPass.getText();
            if (editar == null) {
                if (pass.isBlank()) {
                    UI.setError(err, "La contraseña es obligatoria para nuevos usuarios.");
                    return;
                }
                u.setContrasena(pass);
            } else {
                if (!pass.isBlank())
                    u.setContrasena(pass);
            }

            String error = ctrl.validarUsuario(u);
            if (error != null && editar == null) {
                UI.setError(err, error);
                return;
            }

            boolean ok = editar == null ? ctrl.crearUsuario(u) : ctrl.actualizarUsuario(u);
            if (ok) {
                cargar();
                ven.close();
                UI.mostrarInfo("Éxito", editar == null ? "Empleado creado correctamente." : "Empleado actualizado correctamente.");
            } else {
                UI.setError(err, "Error al guardar. Verifica los datos e intenta de nuevo.");
            }
        });

        HBox bRow = new HBox(10, cancelar, guardar);
        bRow.setAlignment(Pos.CENTER_RIGHT);
        cont.getChildren().addAll(titulo, UI.separador(), grid, err, UI.separador(), bRow);
        ven.setScene(new Scene(cont));
        ven.showAndWait();
    }

    // ── Formulario cambiar contraseña ────────────────────────
    private void formularioCambiarPassword(Usuario u) {
        Stage ven = new Stage();
        ven.initModality(Modality.APPLICATION_MODAL);
        ven.setTitle("Cambiar contraseña");
        ven.setResizable(false);

        VBox cont = new VBox(14);
        cont.setPadding(new Insets(24));
        cont.setPrefWidth(380);

        Label titulo = new Label("🔐 Cambiar contraseña — " + u.getNombre());
        titulo.setFont(Font.font("System", FontWeight.BOLD, 14));

        PasswordField fNueva = UI.campoPassword("Nueva contraseña");
        PasswordField fConfirm = UI.campoPassword("Confirmar contraseña");

        Label err = UI.errorLabel();
        Button guardar = UI.btnPrimario("Cambiar contraseña");
        Button cancelar = UI.btnSecundario("Cancelar");
        cancelar.setOnAction(e -> ven.close());

        guardar.setOnAction(e -> {
            String nueva = fNueva.getText();
            String confirm = fConfirm.getText();
            if (nueva.isBlank()) {
                UI.setError(err, "La nueva contraseña es obligatoria.");
                return;
            }
            if (nueva.length() < 6) {
                UI.setError(err, "La contraseña debe tener al menos 6 caracteres.");
                return;
            }
            if (!nueva.equals(confirm)) {
                UI.setError(err, "Las contraseñas no coinciden.");
                return;
            }

            u.setContrasena(nueva);
            boolean ok = ctrl.actualizarUsuario(u);
            if (ok) {
                UI.mostrarInfo("Éxito", "Contraseña actualizada correctamente.");
                ven.close();
            } else
                UI.setError(err, "Error al actualizar la contraseña.");
        });

        HBox bRow = new HBox(10, cancelar, guardar);
        bRow.setAlignment(Pos.CENTER_RIGHT);
        cont.getChildren().addAll(
                titulo, UI.separador(),
                UI.grupoCampo("Nueva contraseña *", fNueva),
                UI.grupoCampo("Confirmar contraseña *", fConfirm),
                err, UI.separador(), bRow);
        ven.setScene(new Scene(cont));
        ven.showAndWait();
    }
}

// ─────────────────────────────────────────────────────────────
// CONVENIOS – Integración del módulo de convenios en la UI
// Requiere que el DAO/Controller de ConvenioRestaurante exista.
// Si el equipo aún no lo implementó, la clase muestra un
// mensaje de "módulo pendiente de integración" sin romper la app.
// ─────────────────────────────────────────────────────────────
class ConveniosImpl extends VBox {

    private final EstacionamientoController estCtrl = new EstacionamientoController();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Se usa reflexión para no depender de si ConvenioDAO ya compila
    private Object convenioCtrl;

    ConveniosImpl() {
        setPadding(new Insets(24, 28, 24, 28));
        setSpacing(16);
        setStyle("-fx-background-color:" + UI.BG + ";");
        intentarCargarController();
        construir();
    }

    private void intentarCargarController() {
        try {
            Class<?> clazz = Class.forName("com.estacionamiento.controladores.ConvenioController");
            convenioCtrl = clazz.getDeclaredConstructor().newInstance();
        } catch (Exception ex) {
            convenioCtrl = null; // módulo aún no disponible
        }
    }

    private void construir() {
        Button btnNuevo = UI.btnPrimario("+ Nuevo convenio");
        btnNuevo.setDisable(convenioCtrl == null);

        HBox encabezado = UI.encabezado("Convenios con Restaurantes",
                "Acuerdos de validación y descuento por consumo", btnNuevo);

        if (convenioCtrl == null) {
            // Módulo pendiente – mostrar placeholder informativo
            VBox info = new VBox(16);
            info.setAlignment(Pos.CENTER);
            info.setPadding(new Insets(48));
            info.setStyle(UI.CARD);

            Label ico = new Label("🤝");
            ico.setFont(Font.font(48));

            Label msg = new Label("Módulo de Convenios");
            msg.setFont(Font.font("System", FontWeight.BOLD, 18));

            Label sub = new Label(
                    "Este módulo integra los convenios del restaurante a la interfaz.\n" +
                            "El equipo encargado del ConvenioController debe publicar su implementación.\n\n" +
                            "Cuando esté disponible, aquí aparecerá el listado de convenios con:\n" +
                            "  • Restaurante asociado\n  • Fecha de inicio y fin\n  • Estado (Vigente / Vencido / Cancelado)\n"
                            +
                            "  • Acciones: crear, editar, cancelar");
            sub.setStyle("-fx-text-fill:" + UI.MUTED + ";-fx-font-size:13px;");
            sub.setWrapText(true);
            sub.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

            Label badge = UI.badge("⏳ Pendiente de integración", UI.badgeAmber());

            info.getChildren().addAll(ico, msg, sub, badge);
            getChildren().addAll(encabezado, info);
            return;
        }

        // Si el controller sí está disponible, construir la tabla real
        TableView<ConvenioRestaurante> tabla = new TableView<>();
        UI.estilizarTabla(tabla);
        ObservableList<ConvenioRestaurante> datos = FXCollections.observableArrayList();
        tabla.setItems(datos);
        VBox.setVgrow(tabla, Priority.ALWAYS);

        // Columna restaurante ID
        TableColumn<ConvenioRestaurante, Integer> colRest = new TableColumn<>("Restaurante ID");
        colRest.setCellValueFactory(new PropertyValueFactory<>("restauranteId"));
        colRest.setPrefWidth(130);

        // Columna descripción
        TableColumn<ConvenioRestaurante, String> colDesc = new TableColumn<>("Descripción");
        colDesc.setCellValueFactory(new PropertyValueFactory<>("descripcion"));

        // Columna fechas
        TableColumn<ConvenioRestaurante, String> colFechas = new TableColumn<>("Vigencia");
        colFechas.setCellValueFactory(d -> {
            ConvenioRestaurante c = d.getValue();
            String desde = c.getFechaInicio() != null ? c.getFechaInicio().format(FMT) : "—";
            String hasta = c.getFechaFin() != null ? c.getFechaFin().format(FMT) : "—";
            return new javafx.beans.property.SimpleStringProperty(desde + " → " + hasta);
        });
        colFechas.setPrefWidth(180);

        // Columna estado
        TableColumn<ConvenioRestaurante, String> colEst = new TableColumn<>("Estado");
        colEst.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colEst.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) {
                    setGraphic(null);
                    return;
                }
                String estilo = switch (v) {
                    case "Vigente" -> UI.badgeGreen();
                    case "Vencido" -> UI.badgeGray();
                    case "Cancelado" -> UI.badgeRed();
                    default -> UI.badgeGray();
                };
                setGraphic(UI.badge(v, estilo));
            }
        });
        colEst.setPrefWidth(110);

        // Columna acciones
        TableColumn<ConvenioRestaurante, Void> colAcc = new TableColumn<>("Acciones");
        colAcc.setCellFactory(col -> new TableCell<>() {
            final Button btnEdit = UI.btnSecundario("✏️ Editar");
            final Button btnCancel = UI.btnPeligro("✖ Cancelar");
            {
                btnEdit.setStyle(btnEdit.getStyle() + UI.BTN_SMALL);
                btnCancel.setStyle(btnCancel.getStyle() + UI.BTN_SMALL);
                btnEdit.setOnAction(e -> formulario(getTableView().getItems().get(getIndex()), datos));
                btnCancel.setOnAction(e -> {
                    ConvenioRestaurante conv = getTableView().getItems().get(getIndex());
                    if (UI.confirmar("Cancelar convenio", "¿Cancelar el convenio #" + conv.getId() + "?")) {
                        conv.setEstado("Cancelado");
                        llamarActualizar(conv);
                        refrescar(datos);
                    }
                });
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                HBox h = new HBox(6, btnEdit, btnCancel);
                h.setAlignment(Pos.CENTER_LEFT);
                setGraphic(h);
            }
        });
        colAcc.setPrefWidth(170);

        tabla.getColumns().addAll(colRest, colDesc, colFechas, colEst, colAcc);
        tabla.setPlaceholder(UI.panelVacio("🤝", "No hay convenios registrados"));

        btnNuevo.setOnAction(e -> formulario(null, datos));
        refrescar(datos);

        getChildren().addAll(encabezado, tabla);
    }

    private void refrescar(ObservableList<ConvenioRestaurante> datos) {
        try {
            Session s = Session.getInstance();
            Integer estId = s.getEstacionamientoActualId();
            if (estId == null) {
                datos.clear();
                return;
            }

            // Llamada reflectiva al método obtenerPorEstacionamiento(int)
            java.lang.reflect.Method m = convenioCtrl.getClass()
                    .getMethod("obtenerPorEstacionamiento", int.class);
            @SuppressWarnings("unchecked")
            List<ConvenioRestaurante> lista = (List<ConvenioRestaurante>) m.invoke(convenioCtrl, estId);
            datos.setAll(lista);
        } catch (Exception ex) {
            UI.mostrarError("Error", "No se pudieron cargar los convenios: " + ex.getMessage());
        }
    }

    private void llamarActualizar(ConvenioRestaurante conv) {
        try {
            java.lang.reflect.Method m = convenioCtrl.getClass()
                    .getMethod("actualizarConvenio", ConvenioRestaurante.class);
            m.invoke(convenioCtrl, conv);
        } catch (Exception ex) {
            UI.mostrarError("Error", "No se pudo actualizar: " + ex.getMessage());
        }
    }

    private void formulario(ConvenioRestaurante editar, ObservableList<ConvenioRestaurante> datos) {
        Stage ven = new Stage();
        ven.initModality(Modality.APPLICATION_MODAL);
        ven.setTitle(editar == null ? "Nuevo convenio" : "Editar convenio");
        ven.setResizable(false);

        VBox cont = new VBox(14);
        cont.setPadding(new Insets(24));
        cont.setPrefWidth(460);

        Label titulo = new Label(editar == null ? "➕ Nuevo convenio" : "✏️ Editar convenio");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 15));

        TextField fRestId = UI.campo("ID del restaurante");
        TextField fDesc = UI.campo("Descripción del convenio");

        DatePicker dpInicio = new DatePicker(LocalDate.now());
        DatePicker dpFin = new DatePicker(LocalDate.now().plusYears(1));
        dpInicio.setMaxWidth(Double.MAX_VALUE);
        dpFin.setMaxWidth(Double.MAX_VALUE);
        dpInicio.setStyle(UI.FIELD);
        dpFin.setStyle(UI.FIELD);

        ComboBox<String> comboEstado = UI.combo();
        comboEstado.getItems().addAll("Vigente", "Vencido", "Cancelado");
        comboEstado.setValue("Vigente");

        if (editar != null) {
            fRestId.setText(String.valueOf(editar.getRestauranteId()));
            fDesc.setText(editar.getDescripcion());
            if (editar.getFechaInicio() != null)
                dpInicio.setValue(editar.getFechaInicio().toLocalDate());
            if (editar.getFechaFin() != null)
                dpFin.setValue(editar.getFechaFin().toLocalDate());
            if (editar.getEstado() != null)
                comboEstado.setValue(editar.getEstado());
        }

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.add(UI.grupoCampo("ID Restaurante *", fRestId), 0, 0);
        grid.add(UI.grupoCampo("Estado", comboEstado), 1, 0);
        grid.add(UI.grupoCampo("Descripción *", fDesc), 0, 1, 2, 1);
        grid.add(UI.grupoCampo("Fecha inicio", dpInicio), 0, 2);
        grid.add(UI.grupoCampo("Fecha fin", dpFin), 1, 2);
        ColumnConstraints cc = new ColumnConstraints();
        cc.setPercentWidth(50);
        grid.getColumnConstraints().addAll(cc, new ColumnConstraints() {
            {
                setPercentWidth(50);
            }
        });

        Label err = UI.errorLabel();
        Button guardar = UI.btnPrimario(editar == null ? "Guardar" : "Actualizar");
        Button cancelar = UI.btnSecundario("Cancelar");
        cancelar.setOnAction(e -> ven.close());

        guardar.setOnAction(e -> {
            try {
                int restId = Integer.parseInt(fRestId.getText().trim());
                String desc = fDesc.getText().trim();
                if (desc.isBlank()) {
                    UI.setError(err, "La descripción es obligatoria.");
                    return;
                }

                Session s = Session.getInstance();
                Integer estId = s.getEstacionamientoActualId();
                if (estId == null) {
                    UI.setError(err, "Debe seleccionar un estacionamiento.");
                    return;
                }

                ConvenioRestaurante conv = editar != null ? editar : new ConvenioRestaurante();
                conv.setRestauranteId(restId);
                conv.setDescripcion(desc);
                conv.setFechaInicio(dpInicio.getValue().atStartOfDay());
                conv.setFechaFin(dpFin.getValue().atTime(23, 59));
                conv.setEstado(comboEstado.getValue());
                conv.setEstacionamientoId(estId);

                if (editar == null) {
                    java.lang.reflect.Method m = convenioCtrl.getClass()
                            .getMethod("crearConvenio", ConvenioRestaurante.class);
                    boolean ok = (boolean) m.invoke(convenioCtrl, conv);
                    if (ok) {
                        refrescar(datos);
                        ven.close();
                    } else
                        UI.setError(err, "Error al guardar.");
                } else {
                    llamarActualizar(conv);
                    refrescar(datos);
                    ven.close();
                }
            } catch (NumberFormatException ex) {
                UI.setError(err, "El ID del restaurante debe ser un número.");
            } catch (Exception ex) {
                UI.setError(err, "Error: " + ex.getMessage());
            }
        });

        HBox bRow = new HBox(10, cancelar, guardar);
        bRow.setAlignment(Pos.CENTER_RIGHT);
        cont.getChildren().addAll(titulo, UI.separador(), grid, err, UI.separador(), bRow);
        ven.setScene(new Scene(cont));
        ven.showAndWait();
    }
}

// ─────────────────────────────────────────────────────────────
// CONVENIOS MODULE – Punto de entrada público
// ─────────────────────────────────────────────────────────────
class ConveniosModule extends ConveniosImpl {
    public ConveniosModule() {
        super();
    }
}
