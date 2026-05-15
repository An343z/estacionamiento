package com.estacionamiento.modelos;

import java.time.LocalDateTime;

public class LiquidacionRestaurante {
    private int id;
    private int restauranteId;
    private int estacionamientoId;
    private Integer convenioId;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private LocalDateTime fechaLiquidacion;
    private double total;
    private String estado; // PENDIENTE, COBRADA, CANCELADA
    private String folioLiquidacion;
    private String observaciones;

    public LiquidacionRestaurante() {
        this.estado = "PENDIENTE";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getRestauranteId() { return restauranteId; }
    public void setRestauranteId(int restauranteId) { this.restauranteId = restauranteId; }

    public int getEstacionamientoId() { return estacionamientoId; }
    public void setEstacionamientoId(int estacionamientoId) { this.estacionamientoId = estacionamientoId; }

    public Integer getConvenioId() { return convenioId; }
    public void setConvenioId(Integer convenioId) { this.convenioId = convenioId; }

    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }

    public LocalDateTime getFechaLiquidacion() { return fechaLiquidacion; }
    public void setFechaLiquidacion(LocalDateTime fechaLiquidacion) { this.fechaLiquidacion = fechaLiquidacion; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getFolioLiquidacion() { return folioLiquidacion; }
    public void setFolioLiquidacion(String folioLiquidacion) { this.folioLiquidacion = folioLiquidacion; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    @Override
    public String toString() {
        return "Liquidacion #" + folioLiquidacion + " - $" + String.format("%.2f", total)
                + " (" + estado + ")";
    }
}
