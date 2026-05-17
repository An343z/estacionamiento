package com.estacionamiento.controladores;

import com.estacionamiento.dao.NotificacionDAO;
import com.estacionamiento.dao.UsuarioDAO;
import com.estacionamiento.modelos.Notificacion;
import com.estacionamiento.modelos.Usuario;
import java.util.List;

/**
 * Controlador para gestión de Notificaciones
 */
public class NotificacionController {
    private NotificacionDAO notificacionDAO;
    private UsuarioDAO usuarioDAO;

    public NotificacionController() {
        this.notificacionDAO = new NotificacionDAO();
        this.usuarioDAO = new UsuarioDAO();
    }

    public boolean crearNotificacion(Notificacion notificacion) throws Exception {
        if (notificacion == null || notificacion.getUsuarioId() <= 0) {
            throw new Exception("Datos de la notificación inválidos");
        }
        return notificacionDAO.crear(notificacion);
    }

    public Notificacion obtenerNotificacion(int id) throws Exception {
        return notificacionDAO.obtenerPorId(id);
    }

    public List<Notificacion> obtenerTodasLasNotificaciones() throws Exception {
        return notificacionDAO.obtenerTodos();
    }

    public List<Notificacion> obtenerNotificacionesPorUsuario(int usuarioId) throws Exception {
        return notificacionDAO.obtenerPorUsuario(usuarioId);
    }

    public List<Notificacion> obtenerNotificacionesNoLeidas(int usuarioId) throws Exception {
        return notificacionDAO.obtenerNoLeidas(usuarioId);
    }

    public boolean actualizarNotificacion(Notificacion notificacion) throws Exception {
        if (notificacion == null || notificacion.getId() <= 0) {
            throw new Exception("Notificación inválida");
        }
        return notificacionDAO.actualizar(notificacion);
    }

    public boolean marcarComoLeida(int id) throws Exception {
        return notificacionDAO.marcarComoLeida(id);
    }

    public boolean eliminarNotificacion(int id) throws Exception {
        return notificacionDAO.eliminar(id);
    }

    // Métodos de utilidad

    public int contarNotificacionesNoLeidas(int usuarioId) throws Exception {
        List<Notificacion> noLeidas = obtenerNotificacionesNoLeidas(usuarioId);
        return noLeidas.size();
    }

    public boolean enviarNotificacion(int usuarioId, String titulo, String mensaje, String tipo) throws Exception {
        Notificacion notificacion = new Notificacion(usuarioId, titulo, mensaje, tipo);
        return crearNotificacion(notificacion);
    }

    public int enviarAEstacionamiento(int estacionamientoId, String titulo, String mensaje, String tipo) throws Exception {
        int enviados = 0;
        for (Usuario usuario : usuarioDAO.obtenerPorEstacionamiento(estacionamientoId)) {
            if (usuario.isActivo() && enviarNotificacion(usuario.getId(), titulo, mensaje, tipo)) {
                enviados++;
            }
        }
        return enviados;
    }

    public int enviarAAdministradores(String titulo, String mensaje, String tipo) throws Exception {
        int enviados = 0;
        for (Usuario usuario : usuarioDAO.obtenerTodos()) {
            if (usuario.isActivo() && usuario.getRol() == 1 &&
                    enviarNotificacion(usuario.getId(), titulo, mensaje, tipo)) {
                enviados++;
            }
        }
        return enviados;
    }

    // Validaciones

    public String validarNotificacion(Notificacion notificacion) {
        if (notificacion.getUsuarioId() <= 0) {
            return "El ID de usuario es requerido";
        }
        if (notificacion.getTitulo() == null || notificacion.getTitulo().trim().isEmpty()) {
            return "El título es requerido";
        }
        if (notificacion.getMensaje() == null || notificacion.getMensaje().trim().isEmpty()) {
            return "El mensaje es requerido";
        }
        if (notificacion.getTipo() == null || notificacion.getTipo().trim().isEmpty()) {
            return "El tipo es requerido";
        }
        return null; // Válido
    }
}
