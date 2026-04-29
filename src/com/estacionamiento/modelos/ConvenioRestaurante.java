package com.estacionamiento.modelos;

import java.time.LocalDateTime;

/**
 * Modelo de Convenio Restaurante - Acuerdos con restaurantes
 */
public class ConvenioRestaurante {
    private int id;
    private int restauranteId;
    private String descripcion;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String estado; // "Vigente", "Vencido", "Cancelado"
    private int estacionamientoId;

    public ConvenioRestaurante() {
    }

    public ConvenioRestaurante(int restauranteId, String descripcion, LocalDateTime fechaInicio, LocalDateTime fechaFin, int estacionamientoId) {
        this.restauranteId = restauranteId;
        this.descripcion = descripcion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estacionamientoId = estacionamientoId;
        this.estado = "Vigente";
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRestauranteId() {
        return restauranteId;
    }

    public void setRestauranteId(int restauranteId) {
        this.restauranteId = restauranteId;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getEstacionamientoId() {
        return estacionamientoId;
    }

    public void setEstacionamientoId(int estacionamientoId) {
        this.estacionamientoId = estacionamientoId;
    }

    @Override
    public String toString() {
        return "Convenio #" + id + " (" + estado + ")";
    }
}
