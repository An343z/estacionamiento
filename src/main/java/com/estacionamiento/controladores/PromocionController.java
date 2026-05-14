package com.estacionamiento.controladores;

import com.estacionamiento.dao.PromocionDAO;
import com.estacionamiento.modelos.Promocion;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador para gestión de Promociones
 */
public class PromocionController {
    private final PromocionDAO promocionDAO;

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
                        (p.getFechaInicio() == null || !p.getFechaInicio().isAfter(ahora)) &&
                        (p.getFechaFin() == null || p.getFechaFin().isAfter(ahora)))
            .toList();
    }

    public Promocion obtenerMejorPromocion(int estacionamientoId, String tipoVehiculo, double montoBase, long horasConsumidas) throws Exception {
        return obtenerPromocionesVigentes(estacionamientoId).stream()
            .filter(p -> "Todos".equalsIgnoreCase(p.getTipoVehiculo()) || p.getTipoVehiculo().equalsIgnoreCase(tipoVehiculo))
            .max((a, b) -> Double.compare(calcularBeneficio(b, montoBase, horasConsumidas), calcularBeneficio(a, montoBase, horasConsumidas)))
            .orElse(null);
    }

    public double aplicarPromocion(Promocion promocion, double montoBase, long horasConsumidas) {
        if (promocion == null) {
            return montoBase;
        }
        if (promocion.getHorasGratis() > 0 && horasConsumidas <= promocion.getHorasGratis()) {
            return 0.0;
        }
        double montoConDescuento = montoBase;
        if (promocion.getDescuentoPorcentaje() > 0) {
            montoConDescuento = montoConDescuento * (1.0 - promocion.getDescuentoPorcentaje() / 100.0);
        }
        if (promocion.getDescuentoFijo() > 0) {
            montoConDescuento -= promocion.getDescuentoFijo();
        }
        return Math.max(montoConDescuento, 0.0);
    }

    public double calcularBeneficio(Promocion promocion, double montoBase, long horasConsumidas) {
        if (promocion == null) {
            return 0.0;
        }
        if (promocion.getHorasGratis() > 0 && horasConsumidas <= promocion.getHorasGratis()) {
            return montoBase;
        }
        double montoSinPromocion = montoBase;
        double montoConPromocion = aplicarPromocion(promocion, montoBase, horasConsumidas);
        return montoSinPromocion - montoConPromocion;
    }

    // Validaciones

    public String validarPromocion(Promocion promocion) {
        if (promocion.getNombre() == null || promocion.getNombre().trim().isEmpty()) {
            return "El nombre de la promoción es requerido";
        }
        if (promocion.getDescuentoPorcentaje() < 0 || promocion.getDescuentoPorcentaje() > 100) {
            return "El porcentaje de descuento debe estar entre 0 y 100";
        }
        if (promocion.getDescuentoFijo() < 0) {
            return "El descuento fijo no puede ser negativo";
        }
        if (promocion.getHorasGratis() < 0) {
            return "Las horas gratis no pueden ser negativas";
        }
        if (promocion.getFechaInicio() != null && promocion.getFechaFin() != null &&
            promocion.getFechaInicio().isAfter(promocion.getFechaFin())) {
            return "La fecha de inicio no puede ser posterior a la fecha de fin";
        }
        if (promocion.getDescuentoPorcentaje() == 0 && promocion.getDescuentoFijo() == 0 && promocion.getHorasGratis() == 0) {
            return "Debe definir al menos un tipo de beneficio para la promoción";
        }
        return null; // Válido
    }
}