package com.estacionamiento.utilidades;

import com.estacionamiento.dao.ClienteDAO;
import com.estacionamiento.dao.PensionDAO;
import com.estacionamiento.modelos.Cliente;
import com.estacionamiento.modelos.Pension;

import java.time.LocalDate;
import java.util.List;

/**
 * Tarea utilitaria para enviar recordatorios de expiración de pensión por correo
 */
public class RecordatorioPensionTask {
    public static void enviarRecordatorios() {
        ConfigManager config = ConfigManager.getInstancia();
        int diasAntes = config.obtenerInt("recordatorio.pension.dias");
        if (diasAntes <= 0) return;

        // Configuración SMTP (debería estar en config.properties)
        String host = config.obtener("smtp.host", "smtp.tuservidor.com");
        String port = config.obtener("smtp.port", "587");
        String user = config.obtener("smtp.user", "usuario@tuservidor.com");
        String pass = config.obtener("smtp.pass", "password");
        boolean tls = config.obtenerBoolean("smtp.tls");
        EmailSender sender = new EmailSender(host, port, user, pass, tls);

        PensionDAO pensionDAO = new PensionDAO();
        ClienteDAO clienteDAO = new ClienteDAO();
        List<Pension> pensiones = pensionDAO.obtenerTodos();
        LocalDate hoy = LocalDate.now();

        for (Pension p : pensiones) {
            if (!"Activa".equalsIgnoreCase(p.getEstado()) || p.getFechaFin() == null) continue;
            LocalDate fechaFin = p.getFechaFin().toLocalDate();
            if (fechaFin.minusDays(diasAntes).isEqual(hoy)) {
                Cliente c = clienteDAO.obtenerPorId(p.getClienteId());
                if (c != null && c.getEmail() != null && !c.getEmail().isEmpty()) {
                    String asunto = "Recordatorio: Tu pensión está por expirar";
                    String mensaje = "Hola " + c.getNombre() + ",\n\nTu pensión en el estacionamiento expirará el " + fechaFin + ".\nPor favor, realiza la renovación si deseas continuar con el servicio.\n\nSaludos.";
                    try {
                        sender.enviarCorreo(c.getEmail(), asunto, mensaje);
                        System.out.println("Correo de recordatorio enviado a " + c.getEmail());
                    } catch (Exception ex) {
                        System.err.println("No se pudo enviar correo a " + c.getEmail() + ": " + ex.getMessage());
                    }
                }
            }
        }
    }
}
