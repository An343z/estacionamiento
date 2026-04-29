package com.estacionamiento.controladores;

import com.estacionamiento.dao.CajonDAO;
import com.estacionamiento.modelos.Cajon;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador para gestión de Cajones
 */
public class CajonController {
    private CajonDAO cajonDAO;

    public CajonController() {
        this.cajonDAO = new CajonDAO();
    }

    public boolean crearCajon(Cajon cajon) throws Exception {
        if (cajon == null || cajon.getEstacionamientoId() <= 0) {
            throw new Exception("Datos del cajón inválidos");
        }
        return cajonDAO.insertar(cajon);
    }

    public Cajon obtenerCajonPorId(int id) throws Exception {
        return cajonDAO.obtenerPorId(id);
    }

    public List<Cajon> obtenerCajonesPorEstacionamiento(int estacionamientoId) throws Exception {
        return cajonDAO.obtenerPorEstacionamiento(estacionamientoId);
    }

    public Cajon obtenerCajonPorNumeroYEstacionamiento(int numero, int estacionamientoId) throws Exception {
        List<Cajon> cajones = cajonDAO.obtenerPorEstacionamiento(estacionamientoId);
        return cajones.stream()
            .filter(c -> c.getNumero() == numero)
            .findFirst()
            .orElse(null);
    }

    public List<Cajon> obtenerCajonesPorEstado(int estacionamientoId, String estado) throws Exception {
        List<Cajon> cajones = cajonDAO.obtenerPorEstacionamiento(estacionamientoId);
        return cajones.stream()
            .filter(c -> estado.equals(c.getEstado()))
            .collect(Collectors.toList());
    }

    public boolean actualizarCajon(Cajon cajon) throws Exception {
        if (cajon == null || cajon.getId() <= 0) {
            throw new Exception("Cajón inválido");
        }
        return cajonDAO.actualizar(cajon);
    }

    public boolean cambiarEstadoCajon(int cajonId, String nuevoEstado) throws Exception {
        return cajonDAO.cambiarEstado(cajonId, nuevoEstado);
    }

    public boolean eliminarCajon(int id) throws Exception {
        return cajonDAO.eliminar(id);
    }

    public int obtenerCajonesDisponibles(int estacionamientoId) throws Exception {
        return cajonDAO.contarDisponibles(estacionamientoId);
    }

    public int obtenerCajonesOcupados(int estacionamientoId) throws Exception {
        List<Cajon> cajones = obtenerCajonesPorEstado(estacionamientoId, "Ocupado");
        return cajones.size();
    }
}
