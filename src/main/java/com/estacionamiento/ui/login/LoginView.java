package com.estacionamiento.ui.login;

import com.estacionamiento.controladores.UsuarioController;
import com.estacionamiento.modelos.Usuario;
import com.estacionamiento.ui.Session;
import com.estacionamiento.ui.UI;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

/**
 * Pantalla de inicio de sesión.
 * Autentica al usuario usando UsuarioController → UsuarioDAO → MySQL.
 */
public class LoginView extends BorderPane {

    private final Runnable onLoginSuccess;
    private final UsuarioController usuarioCtrl = new UsuarioController();

    private TextField campoUsuario;
    private PasswordField campoPassword;
    private Button btnLogin;
    private Label errorLabel;
    private Label loadingLabel;

    public LoginView(Runnable onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
        construirUI();
    }

    private void construirUI() {
        // Panel izquierdo de marca
        VBox panelMarca = crearPanelMarca();
        // Panel derecho del formulario
        VBox panelForm  = crearPanelFormulario();

        HBox contenido = new HBox();
        HBox.setHgrow(panelMarca, Priority.ALWAYS);
        HBox.setHgrow(panelForm,  Priority.ALWAYS);
        contenido.getChildren().addAll(panelMarca, panelForm);
        setCenter(contenido);
        setStyle("-fx-background-color:" + UI.SIDEBAR + ";");
    }

    private VBox crearPanelMarca() {
        VBox panel = new VBox(18);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(60));
        panel.setStyle("-fx-background-color:" + UI.SIDEBAR + ";");
        panel.setPrefWidth(420);

        Label logo = new Label("P·PARK");
        logo.setFont(Font.font("System", FontWeight.BLACK, 48));
        logo.setStyle("-fx-text-fill:" + UI.GOLD + ";-fx-letter-spacing:6;");

        Label tagline = new Label("Sistema de Gestión de Estacionamiento");
        tagline.setStyle("-fx-text-fill:rgba(255,255,255,0.5);-fx-font-size:14px;");
        tagline.setWrapText(true);
        tagline.setAlignment(Pos.CENTER);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color:rgba(255,255,255,0.1);");
        sep.setMaxWidth(200);

        VBox features = new VBox(14);
        features.setAlignment(Pos.CENTER_LEFT);
        String[][] feats = {
            {"🅿️", "Control de cajones en tiempo real"},
            {"👥", "Gestión de pensionados y clientes"},
            {"📊", "Reportes e indicadores financieros"},
            {"🔔", "Notificaciones y alertas del sistema"}
        };
        for (String[] f : feats) {
            HBox item = new HBox(12);
            item.setAlignment(Pos.CENTER_LEFT);
            Label ico = new Label(f[0]); ico.setFont(Font.font(20));
            Label txt = new Label(f[1]);
            txt.setStyle("-fx-text-fill:rgba(255,255,255,0.65);-fx-font-size:13px;");
            item.getChildren().addAll(ico, txt);
            features.getChildren().add(item);
        }
        panel.getChildren().addAll(logo, tagline, sep, features);
        return panel;
    }

    private VBox crearPanelFormulario() {
        VBox panel = new VBox(20);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(60, 70, 60, 70));
        panel.setStyle("-fx-background-color:" + UI.BG + ";");

        Label titulo = new Label("Iniciar sesión");
        titulo.setFont(Font.font("System", FontWeight.BLACK, 26));
        titulo.setStyle("-fx-text-fill:" + UI.TEXT + ";");

        Label subtitulo = new Label("Ingresa tu usuario y contraseña");
        subtitulo.setStyle("-fx-text-fill:" + UI.MUTED + ";-fx-font-size:13px;");

        // Campos
        campoUsuario  = UI.campo("Nombre de usuario");
        campoPassword = UI.campoPassword("Contraseña");
        campoPassword.setOnAction(e -> intentarLogin());

        VBox formCampos = new VBox(12);
        formCampos.getChildren().addAll(
            UI.grupoCampo("Usuario *", campoUsuario),
            UI.grupoCampo("Contraseña *", campoPassword)
        );

        // Error
        errorLabel = UI.errorLabel();

        // Estado de carga
        loadingLabel = new Label("⏳ Conectando con la base de datos...");
        loadingLabel.setStyle("-fx-text-fill:" + UI.MUTED + ";-fx-font-size:12px;");
        loadingLabel.setVisible(false);
        loadingLabel.setManaged(false);

        // Botón
        btnLogin = UI.btnPrimario("Ingresar al sistema");
        btnLogin.setFont(Font.font("System", FontWeight.BOLD, 14));
        btnLogin.setPrefHeight(44);
        btnLogin.setOnAction(e -> intentarLogin());

        // Info de roles
        Label infoRoles = UI.alertaInfo(
            "Roles disponibles: Administrador Global (rol 1), " +
            "Encargado (rol 2), Cajero (rol 3)");

        panel.getChildren().addAll(titulo, subtitulo, formCampos,
                errorLabel, loadingLabel, btnLogin, infoRoles);
        return panel;
    }

    private void intentarLogin() {
        String usuario  = campoUsuario.getText().trim();
        String password = campoPassword.getText();

        if (usuario.isBlank()) {
            UI.setError(errorLabel, "El usuario es obligatorio.");
            return;
        }
        if (password.isBlank()) {
            UI.setError(errorLabel, "La contraseña es obligatoria.");
            return;
        }

        // Deshabilitar UI mientras conecta
        btnLogin.setDisable(true);
        UI.setError(errorLabel, null);
        loadingLabel.setVisible(true);
        loadingLabel.setManaged(true);

        // Autenticar en hilo separado para no bloquear la UI
        new Thread(() -> {
            Usuario u = usuarioCtrl.autenticar(usuario, password);

            javafx.application.Platform.runLater(() -> {
                loadingLabel.setVisible(false);
                loadingLabel.setManaged(false);
                btnLogin.setDisable(false);

                if (u != null) {
                    Session.getInstance().iniciar(u);
                    onLoginSuccess.run();
                } else {
                    UI.setError(errorLabel, "Usuario o contraseña incorrectos.");
                    campoPassword.clear();
                    campoPassword.requestFocus();

                    FadeTransition ft = new FadeTransition(Duration.millis(200), errorLabel);
                    ft.setFromValue(0); ft.setToValue(1); ft.play();
                }
            });
        }).start();
    }
}
