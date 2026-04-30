package com.estacionamiento.controladores;

import com.estacionamiento.dao.NotificacionDAO;
import com.estacionamiento.modelos.Notificacion;
import java.util.List;

/**
 * Controlador para gestión de Notificaciones
 */
public class NotificacionController {
    private NotificacionDAO notificacionDAO;

    public NotificacionController() {
        this.notificacionDAO = new NotificacionDAO();
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