package com.estacionamiento.modelos;

import java.time.LocalDateTime;

/**
 * Modelo de Registro de Uso Restaurante - Registro de consumos en restaurantes
 */
public class RegistroUsoRestaurante {
    private int id;
    private int clienteId;
    private int restauranteId;
    private double monto;
    private LocalDateTime fecha;
    private String descripcion;

    public RegistroUsoRestaurante() {
    }

    public RegistroUsoRestaurante(int clienteId, int restauranteId, double monto, LocalDateTime fecha) {
        this.clienteId = clienteId;
        this.restauranteId = restauranteId;
        this.monto = monto;
        this.fecha = fecha;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClienteId() {
        return clienteId;
    }

    public void setClienteId(int clienteId) {
        this.clienteId = clienteId;
    }

    public int getRestauranteId() {
        return restauranteId;
    }

    public void setRestauranteId(int restauranteId) {
        this.restauranteId = restauranteId;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return "Registro #" + id + " - $" + monto;
    }
}
