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
        setAlignment(Pos.CENTER);
        Label titulo = new Label("Enviar recordatorio a usuarios premium/pensión");
        Button btnEnviar = new Button("Enviar correos ahora");
        btnEnviar.setOnAction(e -> enviarCorreos());
        getChildren().addAll(titulo, btnEnviar);
    }

    private void enviarCorreos() {
        // Aquí iría la lógica real de envío
        // Por ahora solo muestra un mensaje
        System.out.println("Correos enviados a usuarios premium/pensión");
    }
}
