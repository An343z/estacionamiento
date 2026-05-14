package com.estacionamiento.controladores;

import java.util.List;

import com.estacionamiento.dao.PensionDAO;
import com.estacionamiento.modelos.Pension;
import com.estacionamiento.utilidades.ConfigManager;

/**
 * Controlador para gestión de Pensiones
 */
public class PensionController {
    private PensionDAO pensionDAO;

    public PensionController() {
        this.pensionDAO = new PensionDAO();
    }

    public boolean crearPension(Pension pension) throws Exception {
        if (pension == null) {
            throw new Exception("Pensión no puede ser nula");
        }
        return pensionDAO.crear(pension);
    }

    public Pension obtenerPensionPorId(int id) throws Exception {
        return pensionDAO.obtenerPorId(id);
    }

    public Pension obtenerPensionPorVehiculo(int vehiculoId) throws Exception {
        return pensionDAO.obtenerPorVehiculo(vehiculoId);
    }

    public boolean tienePensionVencida(int vehiculoId) throws Exception {
        Pension pension = obtenerPensionPorVehiculo(vehiculoId);
        return pension != null && esPensionVencida(pension);
    }

    public boolean tienePensionActiva(int vehiculoId) throws Exception {
        Pension pension = obtenerPensionPorVehiculo(vehiculoId);
        return pension != null && !esPensionVencida(pension);
    }

    public List<Pension> obtenerTodasPensiones() throws Exception {
        return pensionDAO.obtenerTodos();
    }

    public List<Pension> obtenerPensionesPorCliente(int clienteId) throws Exception {
        return pensionDAO.obtenerPorCliente(clienteId);
    }

    public List<Pension> obtenerPensionesActivas(int estacionamientoId) throws Exception {
        return pensionDAO.obtenerActivas(estacionamientoId);
    }

    public boolean actualizarPension(Pension pension) throws Exception {
        if (pension == null || pension.getId() <= 0) {
            throw new Exception("Pensión inválida");
        }
        return pensionDAO.actualizar(pension);
    }

    public boolean cancelarPension(int pensionId) throws Exception {
        return pensionDAO.cancelarPension(pensionId);
    }

    public boolean eliminarPension(int id) throws Exception {
        return pensionDAO.eliminar(id);
    }

    public int obtenerTotalPensionesActivas(int estacionamientoId) throws Exception {
        List<Pension> pensiones = obtenerPensionesActivas(estacionamientoId);
        return pensiones.size();
    }

    public double obtenerIngresoMensualEstimado(int estacionamientoId) throws Exception {
        List<Pension> pensiones = obtenerPensionesActivas(estacionamientoId);
        return pensiones.stream()
            .mapToDouble(Pension::getMonto)
            .sum();
    }

    public String calcularEstado(Pension pension) {
        int diasAntes = ConfigManager.getInstancia().obtenerInt("recordatorio.pension.dias");
        if (diasAntes <= 0) {
            diasAntes = 3;
        }
        return pension.getEstadoCalculado(diasAntes);
    }

    public boolean esPensionVencida(Pension pension) {
        return "Vencida".equals(calcularEstado(pension));
    }
}
