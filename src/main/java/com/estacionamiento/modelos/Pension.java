package com.estacionamiento.modelos;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Modelo de Pensión - Estacionamientos de larga duración
 */
public class Pension {
    private int id;
    private int clienteId;
    private int vehiculoId;
    private int cajonId;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private double monto;
    private String estado; // "Activa", "Finalizada", "Cancelada"
    private int estacionamientoId;

    public Pension() {
    }

    public Pension(int clienteId, int vehiculoId, int cajonId, LocalDateTime fechaInicio, LocalDateTime fechaFin, double monto, int estacionamientoId) {
        this.clienteId = clienteId;
        this.vehiculoId = vehiculoId;
        this.cajonId = cajonId;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.monto = monto;
        this.estacionamientoId = estacionamientoId;
        this.estado = "Activa";
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

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
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

    public String getEstadoCalculado(int diasAntesVencimiento) {
        if (fechaFin == null) {
            return estado != null ? estado : "Activa";
        }
        LocalDate hoy = LocalDate.now();
        LocalDate fechaFinLocal = fechaFin.toLocalDate();
        if (fechaFinLocal.isBefore(hoy)) {
            return "Vencida";
        }
        if (!fechaFinLocal.isAfter(hoy.plusDays(diasAntesVencimiento))) {
            return "Próxima a vencer";
        }
        return estado != null ? estado : "Activa";
    }

    public boolean estaVencida(int diasAntesVencimiento) {
        return "Vencida".equals(getEstadoCalculado(diasAntesVencimiento));
    }

    @Override
    public String toString() {
        return "Pensión #" + id + " - Cliente " + clienteId + " (" + estado + ")";
    }
}
