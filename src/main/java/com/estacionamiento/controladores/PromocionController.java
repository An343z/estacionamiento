package com.estacionamiento.controladores;

import com.estacionamiento.dao.PromocionDAO;
import com.estacionamiento.modelos.Promocion;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador para gestión de Promociones
 */
public class PromocionController {
    private PromocionDAO promocionDAO;

    public PromocionController() {
        this.promocionDAO = new PromocionDAO();
    }

    public boolean crearPromocion(Promocion promocion) throws Exception {
        if (promocion == null || promocion.getNombre() == null || promocion.getNombre().trim().isEmpty()) {
            throw new Exception("Datos de la promoción inválidos");
        }
        return promocionDAO.crear(promocion);
    }

    public Promocion obtenerPromocion(int id) throws Exception {
        return promocionDAO.obtenerPorId(id);
    }

    public List<Promocion> obtenerPromocionesActivas() throws Exception {
        return promocionDAO.obtenerActivas();
    }

    public List<Promocion> obtenerPromocionesPorEstacionamiento(int estacionamientoId) throws Exception {
        return promocionDAO.obtenerPorEstacionamiento(estacionamientoId);
    }

    public List<Promocion> obtenerTodasLasPromociones() throws Exception {
        return promocionDAO.obtenerTodos();
    }

    public boolean actualizarPromocion(Promocion promocion) throws Exception {
        if (promocion == null || promocion.getId() <= 0) {
            throw new Exception("Promoción inválida");
        }
        return promocionDAO.actualizar(promocion);
    }

    public boolean eliminarPromocion(int id) throws Exception {
        return promocionDAO.eliminar(id);
    }

    // Métodos específicos

    public List<Promocion> obtenerPromocionesVigentes(int estacionamientoId) throws Exception {
        List<Promocion> promociones = obtenerPromocionesPorEstacionamiento(estacionamientoId);
        LocalDateTime ahora = LocalDateTime.now();
        return promociones.stream()
            .filter(p -> p.isActiva() &&
                        (p.getFechaInicio() == null || p.getFechaInicio().isBefore(ahora) || p.getFechaInicio().isEqual(ahora)) &&
                        (p.getFechaFin() == null || p.getFechaFin().isAfter(ahora)))
            .toList();
    }

    // Validaciones

    public String validarPromocion(Promocion promocion) {
        if (promocion.getNombre() == null || promocion.getNombre().trim().isEmpty()) {
            return "El nombre de la promoción es requerido";
        }
        if (promocion.getDescuentoPorcentaje() < 0 || promocion.getDescuentoPorcentaje() > 100) {
            return "El porcentaje de descuento debe estar entre 0 y 100";
        }
        if (promocion.getFechaInicio() != null && promocion.getFechaFin() != null &&
            promocion.getFechaInicio().isAfter(promocion.getFechaFin())) {
            return "La fecha de inicio no puede ser posterior a la fecha de fin";
        }
        return null; // Válido
    }
}