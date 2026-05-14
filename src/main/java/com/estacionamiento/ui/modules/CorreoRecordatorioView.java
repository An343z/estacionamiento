package com.estacionamiento.ui.modules;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Vista simple para envío de correos automáticos a usuarios premium/pensión.
 */
public class CorreoRecordatorioView extends ScrollPane {
    public CorreoRecordatorioView() {
        VBox content = new VBox(18);
        content.setAlignment(Pos.TOP_CENTER);
        content.setStyle("-fx-background-color: white; -fx-border-radius: 16; -fx-background-radius: 16; -fx-padding: 30 60 40 60; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 16, 0.2, 0, 2);");
        content.setFillWidth(true);
        setContent(content);
        setFitToWidth(true);
        setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        setStyle("-fx-background-color: transparent;");

        Label titulo = new Label("✉️ Recordatorio automático de expiración");
        titulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-padding: 10 0 0 0;");

        Label subtitulo = new Label("Indica cuántos días antes de que expire la suscripción se enviará el correo automático a usuarios premium/pensión:");
        subtitulo.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-padding: 0 0 10 0;");

        javafx.scene.control.TextField diasField = new javafx.scene.control.TextField("3");
        diasField.setPromptText("Ejemplo: 3");
        diasField.setMaxWidth(90);
        diasField.setStyle("-fx-font-size: 15px; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #cbd5e1; -fx-padding: 7 10;");

        Label ayuda = new Label("Solo números enteros positivos. Haz clic en Guardar para aplicar el cambio.");
        ayuda.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8; -fx-padding: 6 0 0 0;");

        Button btnGuardar = new Button("Guardar");
        btnGuardar.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 7 24;");
        btnGuardar.setDisable(false);

        diasField.textProperty().addListener((obs, oldVal, newVal) -> {
            boolean valido = newVal.matches("[1-9][0-9]*");
            if (!valido) {
                diasField.setStyle("-fx-background-color: #fee2e2; -fx-border-color: #ef4444; -fx-font-size: 15px; -fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 7 10;");
            } else {
                diasField.setStyle("-fx-font-size: 15px; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #cbd5e1; -fx-padding: 7 10;");
            }
            btnGuardar.setDisable(!valido);
        });

        btnGuardar.setOnAction(e -> {
            int dias = Integer.parseInt(diasField.getText());
            com.estacionamiento.utilidades.ConfigManager config = com.estacionamiento.utilidades.ConfigManager.getInstancia();
            config.setProperty("recordatorio.pension.dias", String.valueOf(dias));
            config.guardar();
            ayuda.setText("¡Guardado! Se enviarán recordatorios " + dias + " días antes de la expiración.");
            ayuda.setStyle("-fx-font-size: 11px; -fx-text-fill: #22c55e; -fx-padding: 6 0 0 0;");
        });

        Separator separador = new Separator();
        separador.setPrefWidth(440);

        Label manualTitulo = new Label("📧 Enviar correo manualmente");
        manualTitulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-padding: 24 0 0 0;");

        Label manualSubtitulo = new Label("Escribe el correo del usuario para enviar un recordatorio de renovación ahora:");
        manualSubtitulo.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-padding: 0 0 10 0;");

        TextField emailField = new TextField();
        emailField.setPromptText("correo@ejemplo.com");
        emailField.setMaxWidth(260);
        emailField.setStyle("-fx-font-size: 15px; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #cbd5e1; -fx-padding: 7 10;");

        Label mensajeLabel = new Label("Mensaje personalizado:");
        mensajeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #1e293b; -fx-padding: 10 0 4 0;");

        TextArea mensajeArea = new TextArea("Hola,\n\nEste es un recordatorio manual de renovación. Si tu suscripción está por expirar, por favor revisa tu estado en el sistema.\n\nSaludos.");
        mensajeArea.setWrapText(true);
        mensajeArea.setPromptText("Escribe aquí el mensaje personalizado...");
        mensajeArea.setPrefRowCount(6);
        mensajeArea.setMaxWidth(Double.MAX_VALUE);
        mensajeArea.setStyle("-fx-font-size: 14px; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #cbd5e1; -fx-padding: 8 10;");

        VBox mensajeBox = new VBox(10, mensajeLabel, mensajeArea);
        mensajeBox.setMaxWidth(420);
        HBox.setHgrow(mensajeBox, Priority.ALWAYS);

        Label manualAyuda = new Label("Introduce un email válido y un mensaje antes de enviar.");
        manualAyuda.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8; -fx-padding: 6 0 0 0;");

        Button btnEnviar = new Button("Enviar ahora");
        btnEnviar.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10 30; ");
        btnEnviar.setPrefWidth(180);
        btnEnviar.setDisable(true);

        VBox emailBox = new VBox(10, emailField, btnEnviar, manualAyuda);
        emailBox.setAlignment(Pos.TOP_CENTER);
        emailBox.setMaxWidth(260);

        emailField.textProperty().addListener((obs, oldVal, newVal) -> {
            boolean valido = com.estacionamiento.utilidades.Validaciones.validarEmail(newVal);
            if (!valido) {
                emailField.setStyle("-fx-background-color: #fee2e2; -fx-border-color: #ef4444; -fx-font-size: 15px; -fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 7 10;");
                manualAyuda.setText("Email inválido. Usa un formato como usuario@dominio.com.");
            } else {
                emailField.setStyle("-fx-font-size: 15px; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #cbd5e1; -fx-padding: 7 10;");
                manualAyuda.setText("Introduce un email válido y un mensaje antes de enviar.");
            }
            btnEnviar.setDisable(!valido || mensajeArea.getText().trim().isEmpty());
        });

        mensajeArea.textProperty().addListener((obs, oldVal, newVal) -> {
            btnEnviar.setDisable(!com.estacionamiento.utilidades.Validaciones.validarEmail(emailField.getText()) || newVal.trim().isEmpty());
        });

        btnEnviar.setOnAction(e -> {
            String destinatario = emailField.getText().trim();
            String cuerpo = mensajeArea.getText().trim();
            com.estacionamiento.utilidades.ConfigManager config = com.estacionamiento.utilidades.ConfigManager.getInstancia();
            String host = config.obtener("smtp.host", "smtp.tuservidor.com");
            String port = config.obtener("smtp.port", "587");
            String user = config.obtener("smtp.user", "usuario@tuservidor.com");
            String pass = config.obtener("smtp.pass", "password");
            boolean tls = config.obtenerBoolean("smtp.tls");
            com.estacionamiento.utilidades.EmailSender sender = new com.estacionamiento.utilidades.EmailSender(host, port, user, pass, tls);
            try {
                sender.enviarCorreo(destinatario,
                        "Recordatorio de renovación de pensión",
                        cuerpo);
                manualAyuda.setText("¡Correo enviado a " + destinatario + "!");
                manualAyuda.setStyle("-fx-font-size: 11px; -fx-text-fill: #22c55e; -fx-padding: 6 0 0 0;");
            } catch (Exception ex) {
                manualAyuda.setText("Error al enviar: " + ex.getMessage());
                manualAyuda.setStyle("-fx-font-size: 11px; -fx-text-fill: #ef4444; -fx-padding: 6 0 0 0;");
            }
        });

        HBox manualColumns = new HBox(20, mensajeBox, emailBox);
        manualColumns.setAlignment(Pos.TOP_CENTER);
        manualColumns.setMaxWidth(760);
        HBox.setHgrow(emailBox, Priority.NEVER);

        content.getChildren().addAll(titulo, subtitulo, diasField, ayuda, btnGuardar,
                separador, manualTitulo, manualSubtitulo, manualColumns);
    }
}
