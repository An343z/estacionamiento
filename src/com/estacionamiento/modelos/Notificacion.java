package com.estacionamiento.modelos;

import java.time.LocalDateTime;

/**
 * Modelo de Notificación - Alertas y mensajes del sistema
 */
public class Notificacion {
    private int id;
    private int usuarioId;
    private String titulo;
    private String mensaje;
    private String tipo; // "Info", "Advertencia", "Error"
    private LocalDateTime fecha;
    private boolean leida;

    public Notificacion() {
    }

    public Notificacion(int usuarioId, String titulo, String mensaje, String tipo) {
        this.usuarioId = usuarioId;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.tipo = tipo;
        this.fecha = LocalDateTime.now();
        this.leida = false;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public boolean isLeida() {
        return leida;
    }

    public void setLeida(boolean leida) {
        this.leida = leida;
    }

    @Override
    public String toString() {
        return titulo + " (" + tipo + ")";
    }
}
