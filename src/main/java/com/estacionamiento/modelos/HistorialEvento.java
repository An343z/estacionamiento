package com.estacionamiento.modelos;

import java.time.LocalDateTime;

/**
 * Modelo de evento de historial para clientes/vehículos.
 */
public class HistorialEvento {
    private int id;
    private int clienteId;
    private int vehiculoId;
    private Integer registroId;
    private Integer cajonId;
    private String tipo; // Entrada, Salida, Pago, Cambio de cajón, Incidente
    private String descripcion;
    private double monto;
    private LocalDateTime fecha;
    private int estacionamientoId;

    public HistorialEvento() {
    }

    public HistorialEvento(int clienteId, int vehiculoId, Integer registroId, Integer cajonId, String tipo, String descripcion, double monto, LocalDateTime fecha, int estacionamientoId) {
        this.clienteId = clienteId;
        this.vehiculoId = vehiculoId;
        this.registroId = registroId;
        this.cajonId = cajonId;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.monto = monto;
        this.fecha = fecha;
        this.estacionamientoId = estacionamientoId;
    }

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

    public int getVehiculoId() {
        return vehiculoId;
    }

    public void setVehiculoId(int vehiculoId) {
        this.vehiculoId = vehiculoId;
    }

    public Integer getRegistroId() {
        return registroId;
    }

    public void setRegistroId(Integer registroId) {
        this.registroId = registroId;
    }

    public Integer getCajonId() {
        return cajonId;
    }

    public void setCajonId(Integer cajonId) {
        this.cajonId = cajonId;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
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

    public int getEstacionamientoId() {
        return estacionamientoId;
    }

    public void setEstacionamientoId(int estacionamientoId) {
        this.estacionamientoId = estacionamientoId;
    }
}
