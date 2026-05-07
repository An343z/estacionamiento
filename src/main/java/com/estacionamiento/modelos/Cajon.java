package com.estacionamiento.modelos;

/**
 * Modelo de Cajón - Representa un lugar de estacionamiento
 */
public class Cajon {
    private int id;
    private int numero;
    private String tipo; // "Normal", "Minusválido", "Preferente"
    private String estado; // "libre", "ocupado", "reservado", "pensionado", "fuera de servicio"
    private int estacionamientoId;
    private boolean activo;

    public Cajon() {
    }

    public Cajon(int numero, String tipo, String estado, int estacionamientoId) {
        this.numero = numero;
        this.tipo = tipo;
        this.estado = estado;
        this.estacionamientoId = estacionamientoId;
        this.activo = true;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
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

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return "Cajón " + numero + " (" + tipo + ") - " + estado;
    }
}
