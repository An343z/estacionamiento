package com.estacionamiento.ui.modules;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Vista simple para envío de correos automáticos a usuarios premium/pensión.
 */
public class CorreoRecordatorioView extends VBox {
    public CorreoRecordatorioView() {
        setSpacing(18);
        setAlignment(Pos.TOP_CENTER);
        setStyle("-fx-background-color: white; -fx-border-radius: 16; -fx-background-radius: 16; -fx-padding: 30 60 40 60; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 16, 0.2, 0, 2);");

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

        javafx.scene.control.Button btnGuardar = new javafx.scene.control.Button("Guardar");
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
            // Aquí se puede guardar el valor en la configuración
            System.out.println("Días seleccionados para recordatorio: " + diasField.getText());
        });

        getChildren().addAll(titulo, subtitulo, diasField, ayuda, btnGuardar);
    }
}
