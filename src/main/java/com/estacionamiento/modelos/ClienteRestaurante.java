package com.estacionamiento.modelos;

import java.time.LocalDateTime;

/**
 * Modelo de Cliente Restaurante - Relación cliente-restaurante
 */
public class ClienteRestaurante {
    private int id;
    private int clienteId;
    private int restauranteId;
    private LocalDateTime fechaRegistro;
    private boolean activo;

    public ClienteRestaurante() {
    }

    public ClienteRestaurante(int clienteId, int restauranteId) {
        this.clienteId = clienteId;
        this.restauranteId = restauranteId;
        this.activo = true;
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

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return "Cliente-Restaurante #" + id;
    }
}
