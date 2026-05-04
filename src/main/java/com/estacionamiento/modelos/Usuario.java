package com.estacionamiento.modelos;

import java.time.LocalDateTime;

/**
 * Modelo de Usuario para autenticación y gestión de empleados
 * 
 * Roles:
 *   - 1 = Admin Global (sin estacionamiento asignado, ve todos)
 *   - 2 = Encargado (asignado a un estacionamiento)
 *   - 3 = Cajero (asignado a un estacionamiento)
 */
public class Usuario {
    private int id;
    private String nombre;
    private String apellido;
    private String email;
    private String usuario;
    private String contrasena;
    private int rol; // 1=Admin Global, 2=Encargado, 3=Cajero
    private Integer estacionamientoId; // NULL si es admin global
    private String nombreEstacionamiento; // Para visualización
    private boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;

    public Usuario() {
    }

    public Usuario(String nombre, String apellido, String email, String usuario, String contrasena, int rol) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.usuario = usuario;
        this.contrasena = contrasena;
        this.rol = rol;
        this.estacionamientoId = null; // Por defecto sin asignación
        this.activo = true;
    }

    public Usuario(String nombre, String apellido, String email, String usuario, String contrasena, int rol, Integer estacionamientoId) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.usuario = usuario;
        this.contrasena = contrasena;
        this.rol = rol;
        this.estacionamientoId = estacionamientoId;
        this.activo = true;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public int getRol() {
        return rol;
    }

    public void setRol(int rol) {
        this.rol = rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public Integer getEstacionamientoId() {
        return estacionamientoId;
    }

    public void setEstacionamientoId(Integer estacionamientoId) {
        this.estacionamientoId = estacionamientoId;
    }

    public String getNombreEstacionamiento() {
        return nombreEstacionamiento;
    }

    public void setNombreEstacionamiento(String nombreEstacionamiento) {
        this.nombreEstacionamiento = nombreEstacionamiento;
    }

    /**
     * Verifica si es un admin global (sin estacionamiento asignado)
     * @return true si es admin global
     */
    public boolean esAdminGlobal() {
        return rol == 1 && estacionamientoId == null;
    }

    /**
     * Verifica si puede gestionar usuarios
     * @return true si es admin
     */
    public boolean puedeGestionarUsuarios() {
        return rol == 1;
    }

    /**
     * Verifica si puede acceder a un estacionamiento
     * @param estacionamientoId ID del estacionamiento
     * @return true si tiene acceso
     */
    public boolean tieneAccesoA(int estacionamientoId) {
        return esAdminGlobal() || this.estacionamientoId == null || this.estacionamientoId == estacionamientoId;
    }

    @Override
    public String toString() {
        String sufijo = nombreEstacionamiento != null ? " (" + nombreEstacionamiento + ")" : (estacionamientoId != null ? " (Est. #" + estacionamientoId + ")" : " (Admin Global)");
        return nombre + " " + apellido + sufijo;
    }
}
