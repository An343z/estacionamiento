package com.estacionamiento.utilidades;

public class EmailTest {
    public static void main(String[] args) {
        try {
            ConfigManager config = ConfigManager.getInstancia();
            String host = config.obtener("smtp.host");
            String port = config.obtener("smtp.port");
            String user = config.obtener("smtp.user");
            String pass = config.obtener("smtp.pass");
            boolean tls = config.obtenerBoolean("smtp.tls");

            EmailSender sender = new EmailSender(host, port, user, pass, tls);
            String destinatario = "setsuna01001@gmail.com";
            String asunto = "Prueba de correo - P·PARK";
            String mensaje = "Este es un correo de prueba enviado desde la aplicación P·PARK.";

            sender.enviarCorreo(destinatario, asunto, mensaje);
            System.out.println("ENVIO_OK: Correo enviado correctamente a " + destinatario);
        } catch (Exception ex) {
            System.err.println("ENVIO_ERROR: " + ex.getClass().getName() + " - " + ex.getMessage());
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
