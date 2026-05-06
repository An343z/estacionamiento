package com.estacionamiento.ui.main;

import com.estacionamiento.controladores.NotificacionController;
import com.estacionamiento.ui.Session;
import com.estacionamiento.ui.UI;
import com.estacionamiento.ui.modules.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Ventana principal post-login.
 * Sidebar + TopBar + área de contenido.
 */
public class MainView extends BorderPane {

    private final Runnable onLogout;
    private final Sidebar sidebar;
    private final Label titleLabel;
    private final Label clockLabel;
    private final StackPane contentArea;
    private final NotificacionController notifCtrl = new NotificacionController();

    public MainView(Runnable onLogout) {
        this.onLogout = onLogout;

        sidebar = new Sidebar(this::navegar);
        titleLabel = new Label("Dashboard");
        clockLabel = new Label();
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color:" + UI.BG + ";");

        VBox centro = new VBox();
        centro.getChildren().addAll(crearTopBar(), contentArea);
        VBox.setVgrow(contentArea, Priority.ALWAYS);

        setLeft(sidebar);
        setCenter(centro);

        navegar(Sidebar.Modulo.DASHBOARD);
        sidebar.setActivo(Sidebar.Modulo.DASHBOARD);
        iniciarReloj();
        actualizarBadge();
    }

    private HBox crearTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(14, 28, 14, 28));
        bar.setStyle("-fx-background-color:white;-fx-border-color:transparent transparent " + UI.BORDER + " transparent;-fx-border-width:1;");

        titleLabel.setFont(Font.font("System", FontWeight.BLACK, 17));
        titleLabel.setStyle("-fx-text-fill:" + UI.TEXT + ";");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        clockLabel.setStyle("-fx-text-fill:" + UI.MUTED + ";-fx-font-size:13px;-fx-font-weight:bold;");

        // Info del usuario en la topbar
        Session s = Session.getInstance();
        Label userInfo = new Label(s.getNombreCompleto() + " · " + s.getRolNombre());
        userInfo.setStyle("-fx-text-fill:" + UI.MUTED + ";-fx-font-size:12px;");

        HBox derecha = new HBox(16);
        derecha.setAlignment(Pos.CENTER_RIGHT);
        derecha.getChildren().addAll(userInfo, clockLabel);

        bar.getChildren().addAll(titleLabel, derecha);
        return bar;
    }

    private void iniciarReloj() {
        actualizarReloj();
        javafx.animation.Timeline tl = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), e -> actualizarReloj())
        );
        tl.setCycleCount(javafx.animation.Animation.INDEFINITE);
        tl.play();
    }

    private void actualizarReloj() {
        clockLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss  dd/MM/yyyy")));
    }

    private void navegar(Sidebar.Modulo modulo) {
        if (modulo == null) { Session.getInstance().cerrar(); onLogout.run(); return; }

        titleLabel.setText(modulo.etiqueta);

        javafx.scene.Node vista = switch (modulo) {
            case DASHBOARD        -> new DashboardModule();
            case ESTACIONAMIENTOS -> new EstacionamientosModule();
            case CAJONES          -> new CajonesModule();
            case REGISTROS        -> new RegistrosModule();
            case CLIENTES         -> new ClientesModule();
            case VEHICULOS        -> new VehiculosModule();
            case PENSIONES        -> new PensionesModule();
            case PRECIOS          -> new PreciosModule();
            case PROMOCIONES      -> new PromocionesModule();
            case CORREO_RECORDATORIO -> new com.estacionamiento.ui.modules.CorreoRecordatorioView();
            case NOTIFICACIONES   -> { actualizarBadge(); yield new NotificacionesModule(); }
            case REPORTES         -> new ReportesModule();
            case CONFIGURACION    -> new ConfiguracionModule();
        };

        contentArea.getChildren().setAll(vista);
    }

    private void actualizarBadge() {
        try {
            Session s = Session.getInstance();
            if (s.getUsuario() != null) {
                int noLeidas = notifCtrl.contarNotificacionesNoLeidas(s.getUsuario().getId());
                sidebar.setBadgeNotif(noLeidas);
            }
        } catch (Exception e) {
            sidebar.setBadgeNotif(0);
        }
    }
}
