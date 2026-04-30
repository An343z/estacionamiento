package com.estacionamiento.controladores;

import com.estacionamiento.dao.EstacionamientoDAO;
import com.estacionamiento.dao.CajonDAO;
import com.estacionamiento.modelos.Estacionamiento;
import com.estacionamiento.modelos.Cajon;
import java.util.List;

/**
 * Controlador para gestionar estacionamientos y cajones
 */
public class EstacionamientoController {
    private EstacionamientoDAO estacionamientoDAO;
    private CajonDAO cajonDAO;

    public EstacionamientoController() {
        this.estacionamientoDAO = new EstacionamientoDAO();
        this.cajonDAO = new CajonDAO();
    }

    // Métodos para Estacionamiento

    public boolean crearEstacionamiento(Estacionamiento estacionamiento) {
        return estacionamientoDAO.crear(estacionamiento);
    }

    public Estacionamiento obtenerEstacionamiento(int id) {
        return estacionamientoDAO.obtenerPorId(id);
    }

    public List<Estacionamiento> obtenerTodosLosEstacionamientos() {
        return estacionamientoDAO.obtenerTodos();
    }

    public boolean actualizarEstacionamiento(Estacionamiento estacionamiento) {
        return estacionamientoDAO.actualizar(estacionamiento);
    }

    // Métodos para Cajón

    public boolean crearCajon(Cajon cajon) {
        return cajonDAO.crear(cajon);
    }

    public Cajon obtenerCajon(int id) {
        return cajonDAO.obtenerPorId(id);
    }

    public List<Cajon> obtenerCajonesPorEstacionamiento(int estacionamientoId) {
        return cajonDAO.obtenerPorEstacionamiento(estacionamientoId);
    }

    public List<Cajon> obtenerCajonesDisponibles(int estacionamientoId) {
        return cajonDAO.obtenerDisponibles(estacionamientoId);
    }

    public int contarCajonesDisponibles(int estacionamientoId) {
        return cajonDAO.contarDisponibles(estacionamientoId);
    }

    public boolean actualizarCajon(Cajon cajon) {
        return cajonDAO.actualizar(cajon);
    }

    public boolean cambiarEstadoCajon(int cajonId, String nuevoEstado) {
        return cajonDAO.cambiarEstado(cajonId, nuevoEstado);
    }

    public boolean eliminarCajon(int id) {
        return cajonDAO.eliminar(id);
    }

    // Validaciones

    public String validarEstacionamiento(Estacionamiento estacionamiento) {
        if (estacionamiento.getNombre() == null || estacionamiento.getNombre().trim().isEmpty()) {
            return "El nombre es requerido";
        }
        if (estacionamiento.getDireccion() == null || estacionamiento.getDireccion().trim().isEmpty()) {
            return "La dirección es requerida";
        }
        if (estacionamiento.getTotalCajones() <= 0) {
            return "El número de cajones debe ser mayor a 0";
        }
        return null; // Válido
    }

    public String validarCajon(Cajon cajon) {
        if (cajon.getNumero() <= 0) {
            return "El número de cajón debe ser mayor a 0";
        }
        if (cajon.getTipo() == null || cajon.getTipo().trim().isEmpty()) {
            return "El tipo de cajón es requerido";
        }
        return null; // Válido
    }
}
