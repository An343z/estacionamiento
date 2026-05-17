package com.estacionamiento.ui.main;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.estacionamiento.controladores.EstacionamientoController;
import com.estacionamiento.controladores.NotificacionController;
import com.estacionamiento.modelos.Estacionamiento;
import com.estacionamiento.ui.Session;
import com.estacionamiento.ui.UI;
import com.estacionamiento.ui.modules.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Ventana principal post-login.
 * Sidebar + TopBar + área de contenido.
 */
public class MainView extends BorderPane {

    private final Runnable onLogout;

    private final Sidebar sidebar;

    private final Label titleLabel;

    private final Label clockLabel;

    private Label estacionamientoLabel;

    private final StackPane contentArea;

    private final NotificacionController notifCtrl = new NotificacionController();

    private final EstacionamientoController estCtrl = new EstacionamientoController();

    private ComboBox<Estacionamiento> comboEstacionamiento;

    private ObservableList<Estacionamiento> estacionamientos;

    private Sidebar.Modulo moduloActual;

    public MainView(Runnable onLogout) {

        this.onLogout = onLogout;

        sidebar = new Sidebar(this::navegar);

        titleLabel = new Label("Dashboard");

        clockLabel = new Label();

        contentArea = new StackPane();

        contentArea.setStyle(
                "-fx-background-color:" + UI.BG + ";");

        comboEstacionamiento = new ComboBox<>();

        estacionamientos = FXCollections.observableArrayList();

        moduloActual = Sidebar.Modulo.DASHBOARD;

        VBox centro = new VBox();

        centro.getChildren().addAll(
                crearTopBar(),
                contentArea);

        VBox.setVgrow(contentArea, Priority.ALWAYS);

        setLeft(sidebar);

        setCenter(centro);

        cargarEstacionamientos();

        navegar(Sidebar.Modulo.DASHBOARD);

        sidebar.setActivo(Sidebar.Modulo.DASHBOARD);

        iniciarReloj();

        actualizarBadge();
    }

    private HBox crearTopBar() {

        HBox bar = new HBox();

        bar.setAlignment(Pos.CENTER_LEFT);

        bar.setPadding(
                new Insets(14, 28, 14, 28));

        bar.setStyle(
                "-fx-background-color:white;" +
                        "-fx-border-color:transparent transparent "
                        + UI.BORDER + " transparent;" +
                        "-fx-border-width:1;");

        titleLabel.setFont(
                Font.font("System", FontWeight.BLACK, 17));

        titleLabel.setStyle(
                "-fx-text-fill:" + UI.TEXT + ";");

        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        clockLabel.setStyle(
                "-fx-text-fill:" + UI.MUTED + ";" +
                        "-fx-font-size:13px;" +
                        "-fx-font-weight:bold;");

        estacionamientoLabel = new Label("Estacionamiento:");

        estacionamientoLabel.setStyle(
                "-fx-text-fill:" + UI.MUTED + ";" +
                        "-fx-font-size:12px;");

        comboEstacionamiento.setPrefWidth(200);

        comboEstacionamiento.setStyle(
                "-fx-font-size:12px;");

        Session s = Session.getInstance();

        Label userInfo = new Label(
                s.getNombreCompleto()
                        + " · "
                        + s.getRolNombre());

        userInfo.setStyle(
                "-fx-text-fill:" + UI.MUTED + ";" +
                        "-fx-font-size:12px;");

        HBox derecha = new HBox(16);

        derecha.setAlignment(Pos.CENTER_RIGHT);

        derecha.getChildren().addAll(
                estacionamientoLabel,
                comboEstacionamiento,
                userInfo,
                clockLabel);

        bar.getChildren().addAll(
                titleLabel,
                derecha);

        return bar;
    }

    private void iniciarReloj() {

        actualizarReloj();

        javafx.animation.Timeline tl = new javafx.animation.Timeline(

                new javafx.animation.KeyFrame(
                        javafx.util.Duration.seconds(1),

                        e -> actualizarReloj()));

        tl.setCycleCount(
                javafx.animation.Animation.INDEFINITE);

        tl.play();
    }

    private void actualizarReloj() {

        clockLabel.setText(

                LocalDateTime.now().format(

                        DateTimeFormatter.ofPattern(
                                "HH:mm:ss  dd/MM/yyyy")));
    }

    // ─────────────────────────────────────────────
    // VALIDACIÓN DE PERMISOS
    // ─────────────────────────────────────────────

    private boolean tienePermiso(Sidebar.Modulo modulo) {

        Session s = Session.getInstance();

        return switch (modulo) {

            // SOLO ADMIN
            case USUARIOS,
                    ESTACIONAMIENTOS,
                    CONFIGURACION ->

                s.isAdmin();

            // ADMIN Y ENCARGADO
            case REPORTES,
                    PRECIOS,
                    PROMOCIONES ->

                s.isAdmin() || s.isEncargado();

            // NO CAJERO
            case CLIENTES,
                    CORREO_RECORDATORIO ->

                !s.isCajero();

            // TODOS
            default -> true;
        };
    }

    private void navegar(Sidebar.Modulo modulo) {

        if (modulo == null) {

            Session.getInstance().cerrar();

            onLogout.run();

            return;
        }

        // ───────── VALIDAR PERMISOS ─────────

        if (!tienePermiso(modulo)) {

            UI.mostrarError(
                    "Acceso denegado",
                    "No tienes permisos para acceder a este módulo.");

            return;
        }

        moduloActual = modulo;

        titleLabel.setText(modulo.etiqueta);
        actualizarVisibilidadSelectorEstacionamiento(modulo);

        javafx.scene.Node vista = switch (modulo) {
            case DASHBOARD -> new DashboardModule();
            case ESTACIONAMIENTOS -> new EstacionamientosModule();
            case CAJONES -> new CajonesModule();
            case REGISTROS -> new RegistrosModule();
            case CAJA -> new CajaModule();
            case CLIENTES -> new ClientesModule();
            case VEHICULOS -> new VehiculosModule();
            case PENSIONES -> new PensionesModule();
            case PRECIOS -> new PreciosModule();
            case PROMOCIONES -> new PromocionesModule();
            case CORREO_RECORDATORIO -> new CorreoRecordatorioView();
            case CONVENIOS -> new ConveniosModule();
            case USUARIOS -> new UsuariosModule();
            case NOTIFICACIONES -> {
                actualizarBadge();
                yield new NotificacionesModule();
            }
            case REPORTES -> new ReportesModule();
            case CONFIGURACION -> new ConfiguracionModule();
        };

        contentArea.getChildren().setAll(vista);
    }

    private void actualizarVisibilidadSelectorEstacionamiento(Sidebar.Modulo modulo) {
        boolean visible = switch (modulo) {
            case CORREO_RECORDATORIO, USUARIOS, NOTIFICACIONES, CONFIGURACION -> false;
            default -> true;
        };

        estacionamientoLabel.setVisible(visible);
        estacionamientoLabel.setManaged(visible);
        comboEstacionamiento.setVisible(visible);
        comboEstacionamiento.setManaged(visible);
    }

    /**
     * Refresca la vista actualmente activa.
     * Útil cuando cambia el estacionamiento seleccionado.
     */
    private void refrescarVistaActual() {
        if (moduloActual != null) {
            navegar(moduloActual);
        }
    }

    private void actualizarBadge() {

        try {

            Session s = Session.getInstance();

            if (s.getUsuario() != null) {

                int noLeidas = notifCtrl.contarNotificacionesNoLeidas(
                        s.getUsuario().getId());

                sidebar.setBadgeNotif(noLeidas);
            }

        } catch (Exception e) {

            sidebar.setBadgeNotif(0);
        }
    }

    /**
     * Carga la lista de estacionamientos y configura el ComboBox
     * según el rol del usuario:
     * - Admin: puede cambiar, selecciona el primero por defecto
     * - Empleado: bloqueado, muestra su estacionamiento asignado
     */
    private void cargarEstacionamientos() {

        try {

            estacionamientos.setAll(
                    estCtrl.obtenerTodosLosEstacionamientos());

            comboEstacionamiento.setItems(
                    estacionamientos);

            comboEstacionamiento.setConverter(

                    new javafx.util.StringConverter<Estacionamiento>() {

                        @Override
                        public String toString(Estacionamiento e) {

                            return e != null
                                    ? e.getNombre()
                                    : "";
                        }

                        @Override
                        public Estacionamiento fromString(String s) {

                            return null;
                        }
                    });

            Session s = Session.getInstance();

            // ───────── ADMIN (puede cambiar de estacionamiento) ─────────

            if (s.isAdmin()) {

                comboEstacionamiento.setDisable(false);

                if (!estacionamientos.isEmpty()) {

                    comboEstacionamiento.setValue(
                            estacionamientos.get(0));

                    s.setEstacionamientoActualId(
                            estacionamientos.get(0).getId());
                    
                    s.setEstacionamientoActualNombre(
                            estacionamientos.get(0).getNombre());
                }

            } else {

                // ───────── EMPLEADOS (ENCARGADO O CAJERO) ─────────
                // BLOQUEAN EL CAMBIO DE ESTACIONAMIENTO

                Integer estId = s.getEstacionamientoId();

                if (estId != null) {

                    Estacionamiento asignado = estacionamientos.stream()

                            .filter(e -> e.getId() == estId)

                            .findFirst()

                            .orElse(null);

                    comboEstacionamiento.setValue(
                            asignado);

                    s.setEstacionamientoActualId(
                            estId);
                    
                    if (asignado != null) {
                        s.setEstacionamientoActualNombre(
                                asignado.getNombre());
                    }
                }

                // 🔒 BLOQUEAR CAMBIO - EMPLEADO NO PUEDE CAMBIAR
                comboEstacionamiento.setDisable(true);
            }

            // ───────── EVENTO AL CAMBIAR ESTACIONAMIENTO ─────────
            comboEstacionamiento.setOnAction(e -> {

                Estacionamiento sel = comboEstacionamiento.getValue();

                if (sel != null) {

                    Session.getInstance()
                            .setEstacionamientoActualId(
                                    sel.getId());
                    
                    Session.getInstance()
                            .setEstacionamientoActualNombre(
                                    sel.getNombre());

                    // Refrescar la vista actual con los datos del nuevo estacionamiento
                    refrescarVistaActual();
                }
            });

        } catch (Exception e) {

            UI.mostrarError(
                    "Error",
                    "No se pudieron cargar estacionamientos: "
                            + e.getMessage());
        }
    }
}
