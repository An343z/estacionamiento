package com.estacionamiento.modelos;

import java.time.LocalDateTime;

/**
 * Representa un pago realizado en caja.
 * Vincula un registro de entrada/salida con su método de pago y ticket.
 *
 * Métodos de pago soportados:
 *   EFECTIVO, TARJETA_CREDITO, TARJETA_DEBITO, TRANSFERENCIA, CONVENIO
 */
public class Pago {

    public enum MetodoPago {
        EFECTIVO("Efectivo"),
        TARJETA_CREDITO("Tarjeta crédito"),
        TARJETA_DEBITO("Tarjeta débito"),
        TRANSFERENCIA("Transferencia"),
        CONVENIO("Convenio");

        private final String label;
        MetodoPago(String label) { this.label = label; }
        public String getLabel() { return label; }

        @Override
        public String toString() { return label; }
    }

    private int id;
    private int registroId;          // FK registros_entrada_salida
    private int estacionamientoId;
    private int cajeroId;            // FK usuarios (quien cobró)
    private String cajeroNombre;     // Para mostrar en tickets e historial
    private double monto;
    private double montoPagado;      // Lo que dio el cliente (para calcular cambio)
    private double cambio;
    private MetodoPago metodoPago;
    private String numeroTicket;     // Folio único
    private LocalDateTime fechaPago;
    private String notas;
    private boolean anulado;         // Si el pago fue cancelado

    public Pago() {}

    public Pago(int registroId, int estacionamientoId, int cajeroId,
                String cajeroNombre, double monto, double montoPagado,
                MetodoPago metodoPago) {
        this.registroId        = registroId;
        this.estacionamientoId = estacionamientoId;
        this.cajeroId          = cajeroId;
        this.cajeroNombre      = cajeroNombre;
        this.monto             = monto;
        this.montoPagado       = montoPagado;
        this.cambio            = Math.max(0, montoPagado - monto);
        this.metodoPago        = metodoPago;
        this.fechaPago         = LocalDateTime.now();
        this.anulado           = false;
        this.notas             = "";
    }

    // ---- Getters y Setters ----

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getRegistroId() { return registroId; }
    public void setRegistroId(int registroId) { this.registroId = registroId; }

    public int getEstacionamientoId() { return estacionamientoId; }
    public void setEstacionamientoId(int estacionamientoId) { this.estacionamientoId = estacionamientoId; }

    public int getCajeroId() { return cajeroId; }
    public void setCajeroId(int cajeroId) { this.cajeroId = cajeroId; }

    public String getCajeroNombre() { return cajeroNombre; }
    public void setCajeroNombre(String cajeroNombre) { this.cajeroNombre = cajeroNombre; }

    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }

    public double getMontoPagado() { return montoPagado; }
    public void setMontoPagado(double montoPagado) {
        this.montoPagado = montoPagado;
        this.cambio = Math.max(0, montoPagado - monto);
    }

    public double getCambio() { return cambio; }
    public void setCambio(double cambio) { this.cambio = cambio; }

    public MetodoPago getMetodoPago() { return metodoPago; }
    public void setMetodoPago(MetodoPago metodoPago) { this.metodoPago = metodoPago; }

    public String getNumeroTicket() { return numeroTicket; }
    public void setNumeroTicket(String numeroTicket) { this.numeroTicket = numeroTicket; }

    public LocalDateTime getFechaPago() { return fechaPago; }
    public void setFechaPago(LocalDateTime fechaPago) { this.fechaPago = fechaPago; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public boolean isAnulado() { return anulado; }
    public void setAnulado(boolean anulado) { this.anulado = anulado; }

    @Override
    public String toString() {
        return "Ticket #" + numeroTicket + " - $" + String.format("%.2f", monto)
                + " (" + (metodoPago != null ? metodoPago.getLabel() : "?") + ")";
    }
}
