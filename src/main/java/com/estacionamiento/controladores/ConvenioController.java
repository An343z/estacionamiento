package com.estacionamiento.controladores;

import com.estacionamiento.dao.ClienteRestauranteDAO;
import com.estacionamiento.dao.ConvenioRestauranteDAO;
import com.estacionamiento.dao.RestauranteDAO;
import com.estacionamiento.modelos.ClienteRestaurante;
import com.estacionamiento.modelos.ConvenioRestaurante;
import com.estacionamiento.modelos.Restaurante;

import java.util.List;

public class ConvenioController {
    private final RestauranteDAO restauranteDAO = new RestauranteDAO();
    private final ConvenioRestauranteDAO convenioDAO = new ConvenioRestauranteDAO();
    private final ClienteRestauranteDAO clienteRestauranteDAO = new ClienteRestauranteDAO();

    public boolean crearRestaurante(Restaurante restaurante) {
        validarRestaurante(restaurante);
        return restauranteDAO.crear(restaurante);
    }

    public boolean actualizarRestaurante(Restaurante restaurante) {
        validarRestaurante(restaurante);
        return restauranteDAO.actualizar(restaurante);
    }

    public boolean desactivarRestaurante(int id) {
        return restauranteDAO.desactivar(id);
    }

    public Restaurante obtenerRestaurantePorId(int id) {
        return restauranteDAO.obtenerPorId(id);
    }

    public List<Restaurante> obtenerRestaurantesPorEstacionamiento(int estacionamientoId) {
        return restauranteDAO.obtenerPorEstacionamiento(estacionamientoId);
    }

    public boolean crearConvenio(ConvenioRestaurante convenio) {
        validarConvenio(convenio);
        return convenioDAO.crear(convenio);
    }

    public boolean actualizarConvenio(ConvenioRestaurante convenio) {
        validarConvenio(convenio);
        return convenioDAO.actualizar(convenio);
    }

    public boolean cancelarConvenio(int convenioId) {
        return convenioDAO.cancelar(convenioId);
    }

    public List<ConvenioRestaurante> obtenerPorEstacionamiento(int estacionamientoId) {
        return convenioDAO.obtenerPorEstacionamiento(estacionamientoId);
    }

    public List<ConvenioRestaurante> obtenerPorRestaurante(int restauranteId) {
        return convenioDAO.obtenerPorRestaurante(restauranteId);
    }

    public ConvenioRestaurante obtenerConvenioVigentePorCliente(int clienteId, int estacionamientoId) {
        return convenioDAO.obtenerVigentePorCliente(clienteId, estacionamientoId);
    }

    public ConvenioRestaurante obtenerConvenioVigentePorVehiculo(int vehiculoId, int estacionamientoId) {
        return convenioDAO.obtenerVigentePorVehiculo(vehiculoId, estacionamientoId);
    }

    public boolean asociarClienteRestaurante(ClienteRestaurante relacion) {
        if (relacion == null) throw new IllegalArgumentException("La relacion es obligatoria.");
        if (relacion.getClienteId() <= 0) throw new IllegalArgumentException("El cliente es obligatorio.");
        if (relacion.getRestauranteId() <= 0) throw new IllegalArgumentException("El restaurante es obligatorio.");
        return clienteRestauranteDAO.asociar(relacion);
    }

    public boolean desactivarClienteRestaurante(int relacionId) {
        return clienteRestauranteDAO.desactivar(relacionId);
    }

    public List<ClienteRestaurante> obtenerClientesPorRestaurante(int restauranteId) {
        return clienteRestauranteDAO.obtenerPorRestaurante(restauranteId);
    }

    public List<ClienteRestaurante> obtenerRestaurantesPorCliente(int clienteId) {
        return clienteRestauranteDAO.obtenerPorCliente(clienteId);
    }

    public double calcularMontoCubierto(ConvenioRestaurante convenio, double montoBase) {
        if (convenio == null) return 0;
        String tipo = convenio.getTipoCobertura() != null ? convenio.getTipoCobertura() : "TOTAL";
        double cubierto;
        switch (tipo) {
            case "PORCENTAJE" -> cubierto = montoBase * (convenio.getPorcentajeCobertura() / 100.0);
            case "MONTO_FIJO" -> cubierto = convenio.getMontoMaximo() != null ? convenio.getMontoMaximo() : montoBase;
            case "HORAS_GRATIS" -> cubierto = convenio.getMontoMaximo() != null ? convenio.getMontoMaximo() : montoBase;
            default -> cubierto = montoBase;
        }
        if (convenio.getMontoMaximo() != null && !"MONTO_FIJO".equals(tipo)) {
            cubierto = Math.min(cubierto, convenio.getMontoMaximo());
        }
        return Math.max(0, Math.min(montoBase, cubierto));
    }

    private void validarRestaurante(Restaurante restaurante) {
        if (restaurante == null) throw new IllegalArgumentException("El restaurante es obligatorio.");
        if (restaurante.getNombre() == null || restaurante.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del restaurante es obligatorio.");
        }
        if (restaurante.getEstacionamientoId() <= 0) {
            throw new IllegalArgumentException("El estacionamiento del restaurante es obligatorio.");
        }
    }

    private void validarConvenio(ConvenioRestaurante convenio) {
        if (convenio == null) throw new IllegalArgumentException("El convenio es obligatorio.");
        if (convenio.getRestauranteId() <= 0) throw new IllegalArgumentException("El restaurante es obligatorio.");
        if (convenio.getEstacionamientoId() <= 0) throw new IllegalArgumentException("El estacionamiento es obligatorio.");
        if (convenio.getFechaInicio() != null && convenio.getFechaFin() != null
                && convenio.getFechaFin().isBefore(convenio.getFechaInicio())) {
            throw new IllegalArgumentException("La fecha fin debe ser posterior a la fecha inicio.");
        }
    }
}
