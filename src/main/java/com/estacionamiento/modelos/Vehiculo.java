package com.estacionamiento.modelos;

import java.time.LocalDateTime;

/**
 * Modelo de Vehículo - Vehículos asociados a clientes
 */
public class Vehiculo {
    private int id;
    private String patente;
    private String marca;
    private String modelo;
    private String color;
    private int clienteId;
    private String tipo; // "Auto", "Moto", "Camioneta"
    private LocalDateTime fechaRegistro;
    private boolean activo;

    public Vehiculo() {
    }

    public Vehiculo(String patente, String marca, String modelo, String color, int clienteId, String tipo) {
        this.patente = patente;
        this.marca = marca;
        this.modelo = modelo;
        this.color = color;
        this.clienteId = clienteId;
        this.tipo = tipo;
        this.activo = true;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPatente() {
        return patente;
    }

    public void setPatente(String patente) {
        this.patente = patente;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getClienteId() {
        return clienteId;
    }

    public void setClienteId(int clienteId) {
        this.clienteId = clienteId;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
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
        return patente + " - " + marca + " " + modelo;
    }
}
