package com.estacionamiento.modelos;

import java.time.LocalDateTime;

/**
 * Modelo de Precio - Tarifas por tipo de estacionamiento
 */
public class Precio {
    private int id;
    private String tipoVehiculo; // "Auto", "Moto", "Camioneta"
    private double precioHora;
    private double precioMedia;
    private double precioDia;
    private int estacionamientoId;
    private LocalDateTime fechaActualizacion;
    private boolean activo;

    public Precio() {
    }

    public Precio(String tipoVehiculo, double precioHora, double precioMedia, double precioDia, int estacionamientoId) {
        this.tipoVehiculo = tipoVehiculo;
        this.precioHora = precioHora;
        this.precioMedia = precioMedia;
        this.precioDia = precioDia;
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

    public String getTipoVehiculo() {
        return tipoVehiculo;
    }

    public void setTipoVehiculo(String tipoVehiculo) {
        this.tipoVehiculo = tipoVehiculo;
    }

    public double getPrecioHora() {
        return precioHora;
    }

    public void setPrecioHora(double precioHora) {
        this.precioHora = precioHora;
    }

    public double getPrecioMedia() {
        return precioMedia;
    }

    public void setPrecioMedia(double precioMedia) {
        this.precioMedia = precioMedia;
    }

    public double getPrecioDia() {
        return precioDia;
    }

    public void setPrecioDia(double precioDia) {
        this.precioDia = precioDia;
    }

    public int getEstacionamientoId() {
        return estacionamientoId;
    }

    public void setEstacionamientoId(int estacionamientoId) {
        this.estacionamientoId = estacionamientoId;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return tipoVehiculo + " - $" + precioHora + "/hora";
    }
}
