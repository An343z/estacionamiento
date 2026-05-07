package com.estacionamiento;

import com.estacionamiento.ui.login.LoginView;
import com.estacionamiento.ui.main.MainView;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Punto de entrada de la aplicación P·PARK Integrado.
 *
 * Ejecutar con:
 *   mvn javafx:run
 */
public class App extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("P·PARK — Sistema de Gestión de Estacionamiento");
        stage.setMinWidth(1100);
        stage.setMinHeight(650);
        stage.setWidth(1280);
        stage.setHeight(760);

        // Enviar recordatorios de pensión al iniciar la app
        try {
            com.estacionamiento.utilidades.RecordatorioPensionTask.enviarRecordatorios();
        } catch (Exception ex) {
            System.err.println("Error al enviar recordatorios de pensión: " + ex.getMessage());
        }

        mostrarLogin();
        stage.show();
    }

    private void mostrarLogin() {
        LoginView login = new LoginView(this::mostrarApp);
        primaryStage.setScene(new Scene(login,
                primaryStage.getWidth(), primaryStage.getHeight()));
    }

    private void mostrarApp() {
        MainView main = new MainView(this::mostrarLogin);
        primaryStage.setScene(new Scene(main,
                primaryStage.getWidth(), primaryStage.getHeight()));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
