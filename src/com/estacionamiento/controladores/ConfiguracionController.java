package com.estacionamiento.controladores;

import com.estacionamiento.dao.ConfiguracionDAO;
import com.estacionamiento.modelos.Configuracion;
import java.util.List;

/**
 * Controlador para gestión de Configuración del Sistema
 */
public class ConfiguracionController {
    private ConfiguracionDAO configuracionDAO;

    public ConfiguracionController() {
        this.configuracionDAO = new ConfiguracionDAO();
    }

    public boolean crearConfiguracion(Configuracion configuracion) throws Exception {
        if (configuracion == null || configuracion.getClave() == null || configuracion.getClave().trim().isEmpty()) {
            throw new Exception("Datos de la configuración inválidos");
        }
        return configuracionDAO.crear(configuracion);
    }

    public Configuracion obtenerConfiguracion(int id) throws Exception {
        return configuracionDAO.obtenerPorId(id);
    }

    public Configuracion obtenerPorClave(String clave) throws Exception {
        return configuracionDAO.obtenerPorClave(clave);
    }

    public Configuracion obtenerPorClaveYEstacionamiento(String clave, Integer estacionamientoId) throws Exception {
        return configuracionDAO.obtenerPorClaveYEstacionamiento(clave, estacionamientoId);
    }

    public List<Configuracion> obtenerTodasLasConfiguraciones() throws Exception {
        return configuracionDAO.obtenerTodos();
    }

    public List<Configuracion> obtenerPorEstacionamiento(Integer estacionamientoId) throws Exception {
        return configuracionDAO.obtenerPorEstacionamiento(estacionamientoId);
    }

    public boolean actualizarConfiguracion(Configuracion configuracion) throws Exception {
        if (configuracion == null || configuracion.getId() <= 0) {
            throw new Exception("Configuración inválida");
        }
        return configuracionDAO.actualizar(configuracion);
    }

    public boolean eliminarConfiguracion(int id) throws Exception {
        return configuracionDAO.eliminar(id);
    }

    // Métodos de utilidad

    public String obtenerValorConfiguracion(String clave) throws Exception {
        Configuracion config = obtenerPorClave(clave);
        return config != null ? config.getValor() : null;
    }

    public String obtenerValorConfiguracion(String clave, Integer estacionamientoId) throws Exception {
        Configuracion config = obtenerPorClaveYEstacionamiento(clave, estacionamientoId);
        return config != null ? config.getValor() : null;
    }

    public boolean actualizarValorConfiguracion(String clave, String nuevoValor) throws Exception {
        Configuracion config = obtenerPorClave(clave);
        if (config != null) {
            config.setValor(nuevoValor);
            return actualizarConfiguracion(config);
        }
        return false;
    }

    public boolean actualizarValorConfiguracion(String clave, String nuevoValor, Integer estacionamientoId) throws Exception {
        Configuracion config = obtenerPorClaveYEstacionamiento(clave, estacionamientoId);
        if (config != null) {
            config.setValor(nuevoValor);
            return actualizarConfiguracion(config);
        }
        return false;
    }

    // Validaciones

    public String validarConfiguracion(Configuracion configuracion) {
        if (configuracion.getClave() == null || configuracion.getClave().trim().isEmpty()) {
            return "La clave es requerida";
        }
        if (configuracion.getValor() == null) {
            return "El valor no puede ser nulo";
        }
        return null; // Válido
    }
}