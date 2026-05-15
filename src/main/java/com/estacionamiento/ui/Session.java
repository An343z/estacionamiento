package com.estacionamiento.ui;

import com.estacionamiento.modelos.Usuario;

/**
 * Sesión activa del usuario logueado.
 * Singleton compartido entre todas las vistas.
 */
public class Session {

    private static Session instance;

    private Usuario usuario;

    private Integer estacionamientoActualId;

    private String estacionamientoActualNombre;

    private Session() {}

    public static Session getInstance() {

        if (instance == null) {
            instance = new Session();
        }

        return instance;
    }

    public void iniciar(Usuario u) {

        this.usuario = u;

        if (u != null) {
            // En lugar de solo asignar el ID, carga TODO el estacionamiento (ID y nombre)
            recargarEstacionamientoActual();
        }
    }

    public void cerrar() {

        this.usuario = null;
        this.estacionamientoActualId = null;
        this.estacionamientoActualNombre = null;
    }

    public void recargarEstacionamientoActual() {
        if (usuario != null && usuario.getEstacionamientoId() != null) {
            try {
                com.estacionamiento.controladores.EstacionamientoController estCtrl = 
                    new com.estacionamiento.controladores.EstacionamientoController();
                com.estacionamiento.modelos.Estacionamiento est = 
                    estCtrl.obtenerEstacionamiento(usuario.getEstacionamientoId());
                if (est != null) {
                    this.estacionamientoActualId = est.getId();
                    this.estacionamientoActualNombre = est.getNombre();
                } else {
                    // Si no se encuentra el estacionamiento, usar solo el ID
                    this.estacionamientoActualId = usuario.getEstacionamientoId();
                    this.estacionamientoActualNombre = "Estacionamiento #" + usuario.getEstacionamientoId();
                }
            } catch (Exception e) {
                System.err.println("Error al recargar estacionamiento: " + e.getMessage());
                // Fallback: usar solo el ID
                this.estacionamientoActualId = usuario.getEstacionamientoId();
                this.estacionamientoActualNombre = "Estacionamiento #" + usuario.getEstacionamientoId();
            }
        } else if (usuario != null && usuario.getEstacionamientoId() == null) {
            // Admin global: no tiene estacionamiento asignado
            this.estacionamientoActualId = null;
            this.estacionamientoActualNombre = null;
        }
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public boolean isLoggedIn() {
        return usuario != null;
    }

    // ───────── ROLES ─────────

    public boolean isAdmin() {
        return usuario != null && usuario.getRol() == 1;
    }

    public boolean isEncargado() {
        return usuario != null && usuario.getRol() == 2;
    }

    public boolean isCajero() {
        return usuario != null && usuario.getRol() == 3;
    }

    // ───────── ESTACIONAMIENTO ─────────

    public Integer getEstacionamientoId() {
        return usuario != null ? usuario.getEstacionamientoId() : null;
    }

    public Integer getEstacionamientoActualId() {
        return estacionamientoActualId;
    }

    public void setEstacionamientoActualId(Integer id) {
        this.estacionamientoActualId = id;
    }

    public String getEstacionamientoActualNombre() {
        return estacionamientoActualNombre;
    }

    public void setEstacionamientoActualNombre(String nombre) {
        this.estacionamientoActualNombre = nombre;
    }

    // ───────── DATOS VISUALES ─────────

    public String getNombreCompleto() {

        if (usuario == null) {
            return "";
        }

        return usuario.getNombre() + " " + usuario.getApellido();
    }

    public String getRolNombre() {

        if (usuario == null) {
            return "";
        }

        return switch (usuario.getRol()) {

            case 1 -> "Administrador Global";
            case 2 -> "Encargado";
            case 3 -> "Cajero";
            default -> "Desconocido";
        };
    }
}