package com.estacionamiento.modelos;

import java.time.LocalDateTime;

/**
 * Modelo de Factura Restaurante - Facturas de servicios de restaurante
 */
public class FacturaRestaurante {
    private int id;
    private int restauranteId;
    private LocalDateTime fecha;
    private double montoTotal;
    private double comision;
    private double montoNeto;
    private String estado; // "Pendiente", "Pagada", "Cancelada"

    public FacturaRestaurante() {
    }

    public FacturaRestaurante(int restauranteId, LocalDateTime fecha, double montoTotal, double comision) {
        this.restauranteId = restauranteId;
        this.fecha = fecha;
        this.montoTotal = montoTotal;
        this.comision = comision;
        this.montoNeto = montoTotal - comision;
        this.estado = "Pendiente";
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

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public double getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(double montoTotal) {
        this.montoTotal = montoTotal;
    }

    public double getComision() {
        return comision;
    }

    public void setComision(double comision) {
        this.comision = comision;
    }

    public double getMontoNeto() {
        return montoNeto;
    }

    public void setMontoNeto(double montoNeto) {
        this.montoNeto = montoNeto;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Factura #" + id + " - $" + montoNeto + " (" + estado + ")";
    }
}
