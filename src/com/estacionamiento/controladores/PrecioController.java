package com.estacionamiento.controladores;

import com.estacionamiento.dao.PrecioDAO;
import com.estacionamiento.modelos.Precio;
import java.util.List;

/**
 * Controlador para gestión de Precios/Tarifas
 */
public class PrecioController {
    private PrecioDAO precioDAO;

    public PrecioController() {
        this.precioDAO = new PrecioDAO();
    }

    public boolean crearPrecio(Precio precio) throws Exception {
        if (precio == null || precio.getTipoVehiculo() == null || precio.getTipoVehiculo().trim().isEmpty()) {
            throw new Exception("Datos del precio inválidos");
        }
        return precioDAO.crear(precio);
    }

    public Precio obtenerPrecio(int id) throws Exception {
        return precioDAO.obtenerPorId(id);
    }

    public List<Precio> obtenerPreciosPorEstacionamiento(int estacionamientoId) throws Exception {
        return precioDAO.obtenerPorEstacionamiento(estacionamientoId);
    }

    public Precio obtenerPrecioPorTipoVehiculo(String tipoVehiculo, int estacionamientoId) throws Exception {
        return precioDAO.obtenerPorTipoVehiculo(tipoVehiculo, estacionamientoId);
    }

    public List<Precio> obtenerTodosLosPrecios() throws Exception {
        return precioDAO.obtenerTodos();
    }

    public boolean actualizarPrecio(Precio precio) throws Exception {
        if (precio == null || precio.getId() <= 0) {
            throw new Exception("Precio inválido");
        }
        return precioDAO.actualizar(precio);
    }

    public boolean eliminarPrecio(int id) throws Exception {
        return precioDAO.eliminar(id);
    }

    // Validaciones

    public String validarPrecio(Precio precio) {
        if (precio.getTipoVehiculo() == null || precio.getTipoVehiculo().trim().isEmpty()) {
            return "El tipo de vehículo es requerido";
        }
        if (precio.getPrecioHora() < 0) {
            return "El precio por hora no puede ser negativo";
        }
        if (precio.getPrecioMedia() < 0) {
            return "El precio por media hora no puede ser negativo";
        }
        if (precio.getPrecioDia() < 0) {
            return "El precio por día no puede ser negativo";
        }
        return null; // Válido
    }
}