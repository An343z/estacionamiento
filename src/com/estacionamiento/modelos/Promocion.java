package com.estacionamiento.modelos;

import java.time.LocalDateTime;

/**
 * Modelo de Promoción - Ofertas y descuentos
 */
public class Promocion {
    private int id;
    private String nombre;
    private String descripcion;
    private double descuentoPorcentaje;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String tipoVehiculo; // "Todos", "Auto", "Moto", "Camioneta"
    private int estacionamientoId;
    private boolean activa;

    public Promocion() {
    }

    public Promocion(String nombre, String descripcion, double descuentoPorcentaje, LocalDateTime fechaInicio, LocalDateTime fechaFin, String tipoVehiculo, int estacionamientoId) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.descuentoPorcentaje = descuentoPorcentaje;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.tipoVehiculo = tipoVehiculo;
        this.estacionamientoId = estacionamientoId;
        this.activa = true;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getDescuentoPorcentaje() {
        return descuentoPorcentaje;
    }

    public void setDescuentoPorcentaje(double descuentoPorcentaje) {
        this.descuentoPorcentaje = descuentoPorcentaje;
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

    public String getTipoVehiculo() {
        return tipoVehiculo;
    }

    public void setTipoVehiculo(String tipoVehiculo) {
        this.tipoVehiculo = tipoVehiculo;
    }

    public int getEstacionamientoId() {
        return estacionamientoId;
    }

    public void setEstacionamientoId(int estacionamientoId) {
        this.estacionamientoId = estacionamientoId;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    @Override
    public String toString() {
        return nombre + " - " + descuentoPorcentaje + "% descuento";
    }
}
