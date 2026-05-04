package com.estacionamiento.ui;

import com.estacionamiento.modelos.Usuario;

/**
 * Sesión activa del usuario logueado.
 * Singleton compartido entre todas las vistas.
 */
public class Session {

    private static Session instance;
    private Usuario usuario;

    private Session() {}

    public static Session getInstance() {
        if (instance == null) instance = new Session();
        return instance;
    }

    public void iniciar(Usuario u) { this.usuario = u; }
    public void cerrar()           { this.usuario = null; }

    public Usuario getUsuario()    { return usuario; }
    public boolean isLoggedIn()    { return usuario != null; }

    /** Rol 1 = Admin Global */
    public boolean isAdmin()       { return usuario != null && usuario.getRol() == 1; }
    /** Rol 2 = Encargado */
    public boolean isEncargado()   { return usuario != null && usuario.getRol() == 2; }
    /** Rol 3 = Cajero */
    public boolean isCajero()      { return usuario != null && usuario.getRol() == 3; }

    /** ID del estacionamiento asignado (null si es admin global) */
    public Integer getEstacionamientoId() {
        return usuario != null ? usuario.getEstacionamientoId() : null;
    }

    /** Nombre para mostrar */
    public String getNombreCompleto() {
        return usuario != null ? usuario.getNombre() + " " + usuario.getApellido() : "";
    }

    public String getRolNombre() {
        if (usuario == null) return "";
        return switch (usuario.getRol()) {
            case 1 -> "Administrador Global";
            case 2 -> "Encargado";
            case 3 -> "Cajero";
            default -> "Desconocido";
        };
    }
}
