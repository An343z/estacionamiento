package com.estacionamiento.controladores;

import com.estacionamiento.dao.ClienteDAO;
import com.estacionamiento.dao.VehiculoDAO;
import com.estacionamiento.modelos.Cliente;
import com.estacionamiento.modelos.Vehiculo;
import java.util.List;

/**
 * Controlador para gestionar clientes y sus vehículos
 */
public class ClienteController {
    private ClienteDAO clienteDAO;
    private VehiculoDAO vehiculoDAO;

    public ClienteController() {
        this.clienteDAO = new ClienteDAO();
        this.vehiculoDAO = new VehiculoDAO();
    }

    // Métodos para Cliente

    public boolean crearCliente(Cliente cliente) {
        return clienteDAO.crear(cliente);
    }

    public Cliente obtenerCliente(int id) {
        return clienteDAO.obtenerPorId(id);
    }

    public Cliente obtenerClientePorDocumento(String numeroDocumento) {
        return clienteDAO.obtenerPorDocumento(numeroDocumento);
    }

    public List<Cliente> obtenerTodosLosClientes() {
        return clienteDAO.obtenerTodos();
    }

    public boolean actualizarCliente(Cliente cliente) {
        return clienteDAO.actualizar(cliente);
    }

    public boolean eliminarCliente(int id) {
        return clienteDAO.eliminar(id);
    }

    // Métodos para Vehículo

    public boolean crearVehiculo(Vehiculo vehiculo) {
        return vehiculoDAO.crear(vehiculo);
    }

    public Vehiculo obtenerVehiculo(int id) {
        return vehiculoDAO.obtenerPorId(id);
    }

    public Vehiculo obtenerVehiculoPorPatente(String patente) {
        return vehiculoDAO.obtenerPorPatente(patente);
    }

    public List<Vehiculo> obtenerVehiculosPorCliente(int clienteId) {
        return vehiculoDAO.obtenerPorCliente(clienteId);
    }

    public List<Vehiculo> obtenerTodosLosVehiculos() {
        return vehiculoDAO.obtenerTodos();
    }

    public boolean actualizarVehiculo(Vehiculo vehiculo) {
        return vehiculoDAO.actualizar(vehiculo);
    }

    public boolean eliminarVehiculo(int id) {
        return vehiculoDAO.eliminar(id);
    }

    // Validaciones

    public String validarCliente(Cliente cliente) {
        if (cliente.getNombre() == null || cliente.getNombre().trim().isEmpty()) {
            return "El nombre es requerido";
        }
        if (cliente.getApellido() == null || cliente.getApellido().trim().isEmpty()) {
            return "El apellido es requerido";
        }
        if (cliente.getNumeroDocumento() == null || cliente.getNumeroDocumento().trim().isEmpty()) {
            return "El número de documento es requerido";
        }
        return null; // Válido
    }

    public String validarVehiculo(Vehiculo vehiculo) {
        if (vehiculo.getPatente() == null || vehiculo.getPatente().trim().isEmpty()) {
            return "La patente es requerida";
        }
        if (vehiculo.getMarca() == null || vehiculo.getMarca().trim().isEmpty()) {
            return "La marca es requerida";
        }
        if (vehiculo.getTipo() == null || vehiculo.getTipo().trim().isEmpty()) {
            return "El tipo de vehículo es requerido";
        }
        return null; // Válido
    }
}
