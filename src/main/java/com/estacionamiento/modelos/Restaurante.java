package com.estacionamiento.modelos;

/**
 * Modelo de Restaurante - Información de restaurantes asociados
 */
public class Restaurante {
    private int id;
    private String nombre;
    private String descripcion;
    private String telefono;
    private String email;
    private double comisionPorcentaje;
    private int estacionamientoId;
    private boolean activo;

    public Restaurante() {
    }

    public Restaurante(String nombre, String descripcion, String telefono, String email, double comisionPorcentaje, int estacionamientoId) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.telefono = telefono;
        this.email = email;
        this.comisionPorcentaje = comisionPorcentaje;
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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getComisionPorcentaje() {
        return comisionPorcentaje;
    }

    public void setComisionPorcentaje(double comisionPorcentaje) {
        this.comisionPorcentaje = comisionPorcentaje;
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
        return nombre + " - " + comisionPorcentaje + "% comisión";
    }
}
