package com.estacionamiento.controladores;

import com.estacionamiento.dao.VehiculoDAO;
import com.estacionamiento.modelos.Vehiculo;
import java.util.List;

/**
 * Controlador para gestión de Vehículos
 */
public class VehiculoController {
    private VehiculoDAO vehiculoDAO;

    public VehiculoController() {
        this.vehiculoDAO = new VehiculoDAO();
    }

    public boolean crearVehiculo(Vehiculo vehiculo) throws Exception {
        if (vehiculo == null || vehiculo.getPatente() == null || vehiculo.getPatente().isEmpty()) {
            throw new Exception("Datos de vehículo inválidos");
        }
        return vehiculoDAO.crear(vehiculo);
    }

    public Vehiculo obtenerVehiculoPorId(int id) throws Exception {
        return vehiculoDAO.obtenerPorId(id);
    }

    public Vehiculo obtenerVehiculoPorPatente(String patente) throws Exception {
        return vehiculoDAO.obtenerPorPatente(patente);
    }

    public List<Vehiculo> obtenerTodosVehiculos() throws Exception {
        return vehiculoDAO.obtenerTodos();
    }

    public List<Vehiculo> obtenerVehiculosConHistorial() throws Exception {
        return vehiculoDAO.obtenerConHistorial();
    }

    public List<Vehiculo> obtenerVehiculosPorCliente(int clienteId) throws Exception {
        return vehiculoDAO.obtenerPorCliente(clienteId);
    }

    public boolean actualizarVehiculo(Vehiculo vehiculo) throws Exception {
        if (vehiculo == null || vehiculo.getId() <= 0) {
            throw new Exception("Vehículo inválido");
        }
        return vehiculoDAO.actualizar(vehiculo);
    }

    public boolean eliminarVehiculo(int id) throws Exception {
        return vehiculoDAO.eliminar(id);
    }

    public int obtenerTotalVehiculos() throws Exception {
        return obtenerTodosVehiculos().size();
    }
}
