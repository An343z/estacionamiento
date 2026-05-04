package com.estacionamiento.modelos;

/**
 * Modelo de Estacionamiento - Información general del estacionamiento
 */
public class Estacionamiento {
    private int id;
    private String nombre;
    private String direccion;
    private String telefono;
    private String email;
    private int totalCajones;
    private int cajonesDisponibles;
    private String ciudad;
    private String provincia;
    private String codigoPostal;

    public Estacionamiento() {
    }

    public Estacionamiento(String nombre, String direccion, String telefono, String email, int totalCajones) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.telefono = telefono;
        this.email = email;
        this.totalCajones = totalCajones;
        this.cajonesDisponibles = totalCajones;
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

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
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

    public int getTotalCajones() {
        return totalCajones;
    }

    public void setTotalCajones(int totalCajones) {
        this.totalCajones = totalCajones;
    }

    public int getCajonesDisponibles() {
        return cajonesDisponibles;
    }

    public void setCajonesDisponibles(int cajonesDisponibles) {
        this.cajonesDisponibles = cajonesDisponibles;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    @Override
    public String toString() {
        return nombre + " - " + direccion;
    }
}
