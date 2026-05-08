package com.estacionamiento.modelos;

import java.time.LocalDateTime;

/**
 * Modelo de Registro de Entrada/Salida - Historial de entradas y salidas
 */
public class RegistroEntradaSalida {
    private int id;
    private int vehiculoId;
    private int cajonId;
    private LocalDateTime fechaEntrada;
    private LocalDateTime fechaSalida;
    private double monto;
    private String promocionAplicada;
    private String estado; // "Activo", "Finalizado"
    private int estacionamientoId;

    public RegistroEntradaSalida() {
    }

    public RegistroEntradaSalida(int vehiculoId, int cajonId, LocalDateTime fechaEntrada, int estacionamientoId) {
        this.vehiculoId = vehiculoId;
        this.cajonId = cajonId;
        this.fechaEntrada = fechaEntrada;
        this.estacionamientoId = estacionamientoId;
        this.estado = "Activo";
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVehiculoId() {
        return vehiculoId;
    }

    public void setVehiculoId(int vehiculoId) {
        this.vehiculoId = vehiculoId;
    }

    public int getCajonId() {
        return cajonId;
    }

    public void setCajonId(int cajonId) {
        this.cajonId = cajonId;
    }

    public LocalDateTime getFechaEntrada() {
        return fechaEntrada;
    }

    public void setFechaEntrada(LocalDateTime fechaEntrada) {
        this.fechaEntrada = fechaEntrada;
    }

    public LocalDateTime getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(LocalDateTime fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public String getPromocionAplicada() {
        return promocionAplicada;
    }

    public void setPromocionAplicada(String promocionAplicada) {
        this.promocionAplicada = promocionAplicada;
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
        return "Registro #" + id + " - Vehículo " + vehiculoId + " (" + estado + ")";
    }
}
