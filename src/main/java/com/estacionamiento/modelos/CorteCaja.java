package com.estacionamiento.modelos;

import java.time.LocalDateTime;

/**
 * Representa un corte de caja realizado por un cajero o encargado.
 * Registra el resumen financiero de un turno.
 */
public class CorteCaja {

    public enum TipoCorte {
        PARCIAL("Corte parcial"),
        FINAL("Corte final del día");

        private final String label;
        TipoCorte(String label) { this.label = label; }
        public String getLabel() { return label; }

        @Override
        public String toString() { return label; }
    }

    private int id;
    private int estacionamientoId;
    private int cajeroId;
    private String cajeroNombre;
    private LocalDateTime fechaInicio;     // Inicio del turno / período
    private LocalDateTime fechaCorte;      // Momento del corte
    private double totalEfectivo;
    private double totalTarjeta;
    private double totalTransferencia;
    private double totalConvenio;
    private double totalGeneral;           // Suma de todos los métodos
    private int totalTransacciones;        // Número de pagos en el período
    private String observaciones;
    private TipoCorte tipoCorte;
    private String folioCorte;            // Número único del corte

    public CorteCaja() {}

    public CorteCaja(int estacionamientoId, int cajeroId, String cajeroNombre,
                     LocalDateTime fechaInicio, TipoCorte tipoCorte) {
        this.estacionamientoId = estacionamientoId;
        this.cajeroId          = cajeroId;
        this.cajeroNombre      = cajeroNombre;
        this.fechaInicio       = fechaInicio;
        this.fechaCorte        = LocalDateTime.now();
        this.tipoCorte         = tipoCorte;
        this.observaciones     = "";
    }

    /** Calcula el total sumando todos los métodos de pago */
    public void calcularTotal() {
        this.totalGeneral = totalEfectivo + totalTarjeta
                + totalTransferencia + totalConvenio;
    }

    // ---- Getters y Setters ----

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEstacionamientoId() { return estacionamientoId; }
    public void setEstacionamientoId(int estacionamientoId) { this.estacionamientoId = estacionamientoId; }

    public int getCajeroId() { return cajeroId; }
    public void setCajeroId(int cajeroId) { this.cajeroId = cajeroId; }

    public String getCajeroNombre() { return cajeroNombre; }
    public void setCajeroNombre(String cajeroNombre) { this.cajeroNombre = cajeroNombre; }

    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDateTime getFechaCorte() { return fechaCorte; }
    public void setFechaCorte(LocalDateTime fechaCorte) { this.fechaCorte = fechaCorte; }

    public double getTotalEfectivo() { return totalEfectivo; }
    public void setTotalEfectivo(double totalEfectivo) { this.totalEfectivo = totalEfectivo; }

    public double getTotalTarjeta() { return totalTarjeta; }
    public void setTotalTarjeta(double totalTarjeta) { this.totalTarjeta = totalTarjeta; }

    public double getTotalTransferencia() { return totalTransferencia; }
    public void setTotalTransferencia(double totalTransferencia) { this.totalTransferencia = totalTransferencia; }

    public double getTotalConvenio() { return totalConvenio; }
    public void setTotalConvenio(double totalConvenio) { this.totalConvenio = totalConvenio; }

    public double getTotalGeneral() { return totalGeneral; }
    public void setTotalGeneral(double totalGeneral) { this.totalGeneral = totalGeneral; }

    public int getTotalTransacciones() { return totalTransacciones; }
    public void setTotalTransacciones(int totalTransacciones) { this.totalTransacciones = totalTransacciones; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public TipoCorte getTipoCorte() { return tipoCorte; }
    public void setTipoCorte(TipoCorte tipoCorte) { this.tipoCorte = tipoCorte; }

    public String getFolioCorte() { return folioCorte; }
    public void setFolioCorte(String folioCorte) { this.folioCorte = folioCorte; }

    @Override
    public String toString() {
        return "Corte #" + folioCorte + " - " + cajeroNombre
                + " - $" + String.format("%.2f", totalGeneral);
    }
}
