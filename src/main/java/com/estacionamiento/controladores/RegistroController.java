package com.estacionamiento.controladores;

import java.time.LocalDateTime;
import java.util.List;

import com.estacionamiento.dao.CajonDAO;
import com.estacionamiento.dao.EstacionamientoDAO;
import com.estacionamiento.dao.PrecioDAO;
import com.estacionamiento.dao.RegistroEntradaSalidaDAO;
import com.estacionamiento.dao.VehiculoDAO;
import com.estacionamiento.modelos.HistorialEvento;
import com.estacionamiento.modelos.Pension;
import com.estacionamiento.modelos.Precio;
import com.estacionamiento.modelos.Promocion;
import com.estacionamiento.modelos.RegistroEntradaSalida;
import com.estacionamiento.modelos.Vehiculo;
import com.estacionamiento.utilidades.DateUtils;

/**
 * Controlador para gestionar registros de entrada y salida
 */
public class RegistroController {
    private final RegistroEntradaSalidaDAO registroDAO;
    private final PrecioDAO precioDAO;
    private final CajonDAO cajonDAO;
    private final EstacionamientoDAO estacionamientoDAO;
    private final VehiculoDAO vehiculoDAO;
    private final PromocionController promocionController;
    private final HistorialController historialController;
    private final PensionController pensionController;

    public RegistroController() {
        this.registroDAO = new RegistroEntradaSalidaDAO();
        this.precioDAO = new PrecioDAO();
        this.cajonDAO = new CajonDAO();
        this.estacionamientoDAO = new EstacionamientoDAO();
        this.vehiculoDAO = new VehiculoDAO();
        this.promocionController = new PromocionController();
        this.historialController = new HistorialController();
        this.pensionController = new PensionController();
    }

    /**
     * Registra la entrada de un vehículo
     * @param vehiculoId ID del vehículo
     * @param cajonId ID del cajón asignado
     * @param estacionamientoId ID del estacionamiento
     * @return true si se registró correctamente
     */
    public boolean registrarEntrada(int vehiculoId, int cajonId, int estacionamientoId) throws Exception {
        Pension pension = pensionController.obtenerPensionPorVehiculo(vehiculoId);
        if (pension != null && pensionController.esPensionVencida(pension)) {
            throw new Exception("La pensión asignada al vehículo está vencida. Renueve la pensión antes de registrar una nueva entrada.");
        }

        RegistroEntradaSalida registro = new RegistroEntradaSalida(vehiculoId, cajonId, LocalDateTime.now(), estacionamientoId);
        
        if (registroDAO.crear(registro)) {
            // Cambiar estado del cajón a Ocupado
            cajonDAO.cambiarEstado(cajonId, "Ocupado");
            
            // Actualizar cajones disponibles del estacionamiento
            int disponibles = cajonDAO.contarDisponibles(estacionamientoId);
            estacionamientoDAO.actualizarCajonesDisponibles(estacionamientoId, disponibles);
            
            try {
                Vehiculo vehiculo = vehiculoDAO.obtenerPorId(vehiculoId);
                int clienteId = vehiculo != null ? vehiculo.getClienteId() : 0;
                historialController.registrarEvento(new HistorialEvento(clienteId, vehiculoId, null, cajonId, "Entrada", "Entrada registrada en cajón " + cajonId, 0.0, LocalDateTime.now(), estacionamientoId));
            } catch (Exception e) {
                System.err.println("No se pudo registrar evento de historial: " + e.getMessage());
            }

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

        LocalDateTime ahora = LocalDateTime.now();
        long minutosTranscurridos = DateUtils.calcularMinutos(registro.getFechaEntrada(), ahora);
        long horas = DateUtils.calcularHoras(registro.getFechaEntrada(), ahora);

        double monto;
        String promocionAplicada = null;
        String descripcionEvento;

        if (minutosTranscurridos <= 5) {
            monto = 0.0;
            descripcionEvento = "Salida dentro de la tolerancia de 5 minutos";
        } else {
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

            try {
                Promocion mejor = promocionController.obtenerMejorPromocion(estacionamientoId, tipoVehiculo, monto, horas);
                if (mejor != null) {
                    double montoConPromocion = promocionController.aplicarPromocion(mejor, monto, horas);
                    if (montoConPromocion < monto) {
                        promocionAplicada = mejor.getNombre();
                        monto = montoConPromocion;
                    }
                }
            } catch (Exception e) {
                System.err.println("No se pudo aplicar promoción: " + e.getMessage());
            }

            descripcionEvento = "Salida registrada" + (promocionAplicada != null ? " con promoción: " + promocionAplicada : "");
        }

        if (registroDAO.finalizarRegistro(registro.getId(), ahora, monto, promocionAplicada)) {
            // Cambiar estado del cajón a Disponible
            cajonDAO.cambiarEstado(registro.getCajonId(), "Disponible");
            
            // Actualizar cajones disponibles del estacionamiento
            int disponibles = cajonDAO.contarDisponibles(estacionamientoId);
            estacionamientoDAO.actualizarCajonesDisponibles(estacionamientoId, disponibles);
            
            registro.setFechaSalida(ahora);
            registro.setMonto(monto);
            registro.setPromocionAplicada(promocionAplicada);
            registro.setEstado("Finalizado");

            try {
                Vehiculo vehiculo = vehiculoDAO.obtenerPorId(vehiculoId);
                int clienteId = vehiculo != null ? vehiculo.getClienteId() : 0;
                historialController.registrarEvento(new HistorialEvento(clienteId, vehiculoId, registro.getId(), registro.getCajonId(), "Salida", descripcionEvento, monto, ahora, estacionamientoId));
            } catch (Exception e) {
                System.err.println("No se pudo registrar evento de historial: " + e.getMessage());
            }
            
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

    public boolean registrarCambioCajon(int clienteId, int vehiculoId, int registroId, int cajonAnterior, int cajonNuevo, int estacionamientoId) {
        String descripcion = String.format("Cambio de cajón de %d a %d", cajonAnterior, cajonNuevo);
        try {
            return historialController.registrarEvento(new HistorialEvento(clienteId, vehiculoId, registroId, cajonNuevo, "Cambio de cajón", descripcion, 0.0, LocalDateTime.now(), estacionamientoId));
        } catch (Exception e) {
            System.err.println("No se pudo registrar el cambio de cajón: " + e.getMessage());
            return false;
        }
    }

    public boolean registrarIncidente(int clienteId, int vehiculoId, String descripcion, int estacionamientoId) {
        try {
            return historialController.registrarEvento(new HistorialEvento(clienteId, vehiculoId, null, null, "Incidente", descripcion, 0.0, LocalDateTime.now(), estacionamientoId));
        } catch (Exception e) {
            System.err.println("No se pudo registrar el incidente: " + e.getMessage());
            return false;
        }
    }
}
