package com.estacionamiento.controladores;

import com.estacionamiento.dao.RegistroEntradaSalidaDAO;
import com.estacionamiento.dao.PrecioDAO;
import com.estacionamiento.dao.CajonDAO;
import com.estacionamiento.dao.EstacionamientoDAO;
import com.estacionamiento.modelos.RegistroEntradaSalida;
import com.estacionamiento.modelos.Precio;
import com.estacionamiento.modelos.Cajon;
import com.estacionamiento.modelos.Estacionamiento;
import com.estacionamiento.utilidades.DateUtils;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador para gestionar registros de entrada y salida
 */
public class RegistroController {
    private RegistroEntradaSalidaDAO registroDAO;
    private PrecioDAO precioDAO;
    private CajonDAO cajonDAO;
    private EstacionamientoDAO estacionamientoDAO;

    public RegistroController() {
        this.registroDAO = new RegistroEntradaSalidaDAO();
        this.precioDAO = new PrecioDAO();
        this.cajonDAO = new CajonDAO();
        this.estacionamientoDAO = new EstacionamientoDAO();
    }

    /**
     * Registra la entrada de un vehículo
     * @param vehiculoId ID del vehículo
     * @param cajonId ID del cajón asignado
     * @param estacionamientoId ID del estacionamiento
     * @return true si se registró correctamente
     */
    public boolean registrarEntrada(int vehiculoId, int cajonId, int estacionamientoId) {
        RegistroEntradaSalida registro = new RegistroEntradaSalida(vehiculoId, cajonId, LocalDateTime.now(), estacionamientoId);
        
        if (registroDAO.insertar(registro)) {
            // Cambiar estado del cajón a Ocupado
            cajonDAO.cambiarEstado(cajonId, "Ocupado");
            
            // Actualizar cajones disponibles del estacionamiento
            int disponibles = cajonDAO.contarDisponibles(estacionamientoId);
            estacionamientoDAO.actualizarCajonesDisponibles(estacionamientoId, disponibles);
            
            return true;
        }
        return false;
    }

    /**
     * Registra la salida de un vehículo y calcula el monto a pagar
     * @param vehiculoId ID del vehículo
     * @param tipoVehiculo tipo de vehículo (para consultar tarifa)
     * @param estacionamientoId ID del estacionamiento
     * @return objeto RegistroEntradaSalida con el monto calculado, null si no hay registro activo
     */
    public RegistroEntradaSalida registrarSalida(int vehiculoId, String tipoVehiculo, int estacionamientoId) {
        // Obtener el registro activo del vehículo
        RegistroEntradaSalida registro = registroDAO.obtenerActivoDelVehiculo(vehiculoId);
        
        if (registro == null) {
            return null;
        }

        // Obtener la tarifa
        Precio precio = precioDAO.obtenerPorTipoVehiculo(tipoVehiculo, estacionamientoId);
        if (precio == null) {
            return null;
        }

        // Calcular monto
        LocalDateTime ahora = LocalDateTime.now();
        long horas = DateUtils.calcularHoras(registro.getFechaEntrada(), ahora);
        
        double monto;
        if (horas < 1) {
            monto = precio.getPrecioHora();
        } else if (horas <= 4) {
            monto = precio.getPrecioMedia();
        } else {
            long dias = DateUtils.calcularDias(registro.getFechaEntrada(), ahora);
            monto = dias * precio.getPrecioDia();
            long horasRestantes = horas - (dias * 24);
            if (horasRestantes > 0) {
                monto += precio.getPrecioHora() * horasRestantes;
            }
        }

        // Finalizar el registro
        if (registroDAO.finalizarRegistro(registro.getId(), ahora, monto)) {
            // Cambiar estado del cajón a Disponible
            cajonDAO.cambiarEstado(registro.getCajonId(), "Disponible");
            
            // Actualizar cajones disponibles del estacionamiento
            int disponibles = cajonDAO.contarDisponibles(estacionamientoId);
            estacionamientoDAO.actualizarCajonesDisponibles(estacionamientoId, disponibles);
            
            registro.setFechaSalida(ahora);
            registro.setMonto(monto);
            registro.setEstado("Finalizado");
            
            return registro;
        }

        return null;
    }

    public RegistroEntradaSalida obtenerRegistro(int id) {
        return registroDAO.obtenerPorId(id);
    }

    public RegistroEntradaSalida obtenerRegistroActivoDelVehiculo(int vehiculoId) {
        return registroDAO.obtenerActivoDelVehiculo(vehiculoId);
    }

    public List<RegistroEntradaSalida> obtenerRegistrosPorEstacionamiento(int estacionamientoId) {
        return registroDAO.obtenerPorEstacionamiento(estacionamientoId);
    }

    public double obtenerIngresoDelDia(int estacionamientoId, LocalDateTime fecha) {
        return registroDAO.obtenerIngresoDelDia(estacionamientoId, fecha);
    }
}
