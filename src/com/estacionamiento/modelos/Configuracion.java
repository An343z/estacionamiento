package com.estacionamiento.modelos;

/**
 * Modelo de Configuración - Parámetros generales del sistema
 */
public class Configuracion {
    private int id;
    private String clave;
    private String valor;
    private String descripcion;
    private int estacionamientoId;

    public Configuracion() {
    }

    public Configuracion(String clave, String valor, String descripcion, int estacionamientoId) {
        this.clave = clave;
        this.valor = valor;
        this.descripcion = descripcion;
        this.estacionamientoId = estacionamientoId;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getEstacionamientoId() {
        return estacionamientoId;
    }

    public void setEstacionamientoId(int estacionamientoId) {
        this.estacionamientoId = estacionamientoId;
    }

    @Override
    public String toString() {
        return clave + " = " + valor;
    }
}
