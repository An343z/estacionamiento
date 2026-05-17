package com.estacionamiento.controladores;

import java.time.LocalDateTime;
import java.util.List;

import com.estacionamiento.dao.CajonDAO;
import com.estacionamiento.dao.ClienteDAO;
import com.estacionamiento.dao.EstacionamientoDAO;
import com.estacionamiento.dao.PrecioDAO;
import com.estacionamiento.dao.RegistroEntradaSalidaDAO;
import com.estacionamiento.dao.VehiculoDAO;
import com.estacionamiento.modelos.Cliente;
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
    private final ClienteDAO clienteDAO;
    private final VehiculoDAO vehiculoDAO;
    private final PromocionController promocionController;
    private final HistorialController historialController;
    private final PensionController pensionController;

    public RegistroController() {

        this.registroDAO = new RegistroEntradaSalidaDAO();
        this.precioDAO = new PrecioDAO();
        this.cajonDAO = new CajonDAO();
        this.estacionamientoDAO = new EstacionamientoDAO();
        this.clienteDAO = new ClienteDAO();
        this.vehiculoDAO = new VehiculoDAO();
        this.promocionController = new PromocionController();
        this.historialController = new HistorialController();
        this.pensionController = new PensionController();
    }

    public Vehiculo registrarEntradaPorPlaca(
            String placa,
            String tipoVehiculo,
            String marca,
            String modelo,
            String color,
            int cajonId,
            int estacionamientoId
    ) throws Exception {

        String placaNormalizada = normalizarPlaca(placa);

        if (placaNormalizada.isBlank()) {
            throw new Exception("La placa es obligatoria.");
        }

        Vehiculo vehiculo = vehiculoDAO.obtenerPorPatente(placaNormalizada);

        if (vehiculo == null) {
            Cliente cliente = obtenerOCrearClienteAutomatico(placaNormalizada);

            vehiculo = new Vehiculo();
            vehiculo.setPatente(placaNormalizada);
            vehiculo.setTipo(valorONull(tipoVehiculo) != null ? tipoVehiculo.trim() : "Auto");
            vehiculo.setMarca(valorONull(marca));
            vehiculo.setModelo(valorONull(modelo));
            vehiculo.setColor(valorONull(color));
            vehiculo.setClienteId(cliente.getId());
            vehiculo.setActivo(true);

            if (!vehiculoDAO.crear(vehiculo) || vehiculo.getId() <= 0) {
                throw new Exception("No se pudo registrar el vehiculo.");
            }
        } else {
            boolean cambio = false;

            if (valorONull(marca) != null && valorONull(vehiculo.getMarca()) == null) {
                vehiculo.setMarca(marca.trim());
                cambio = true;
            }
            if (valorONull(modelo) != null && valorONull(vehiculo.getModelo()) == null) {
                vehiculo.setModelo(modelo.trim());
                cambio = true;
            }
            if (valorONull(color) != null && valorONull(vehiculo.getColor()) == null) {
                vehiculo.setColor(color.trim());
                cambio = true;
            }
            if (valorONull(tipoVehiculo) != null &&
                    (valorONull(vehiculo.getTipo()) == null ||
                     !vehiculo.getTipo().equalsIgnoreCase(tipoVehiculo.trim()))) {
                vehiculo.setTipo(tipoVehiculo.trim());
                cambio = true;
            }
            if (cambio) {
                vehiculoDAO.actualizar(vehiculo);
            }
        }

        Pension pension = pensionController.obtenerPensionPorVehiculo(vehiculo.getId());
        if (pension != null && pension.getEstacionamientoId() == estacionamientoId) {
            if (pensionController.esPensionVencida(pension)) {
                throw new Exception(
                    "La pensiÃ³n asignada al vehÃ­culo estÃ¡ vencida. " +
                    "Renueve la pensiÃ³n antes de registrar una nueva entrada."
                );
            }
            cajonId = pension.getCajonId();
        }

        if (cajonId <= 0) {
            throw new Exception("Debe seleccionar un cajon disponible o usar una placa con pension activa.");
        }

        registrarEntrada(vehiculo.getId(), cajonId, estacionamientoId);
        return vehiculo;
    }

    public RegistroEntradaSalida registrarSalidaPorPlaca(
            String placa,
            int estacionamientoId
    ) throws Exception {
        String placaNormalizada = normalizarPlaca(placa);
        if (placaNormalizada.isBlank()) {
            throw new Exception("La placa es obligatoria.");
        }

        Vehiculo vehiculo = vehiculoDAO.obtenerPorPatente(placaNormalizada);
        if (vehiculo == null) {
            throw new Exception("No existe un vehiculo registrado con esa placa.");
        }

        String tipo = vehiculo.getTipo() != null ? vehiculo.getTipo() : "Auto";
        return registrarSalida(vehiculo.getId(), tipo, estacionamientoId);
    }

    public RegistroEntradaSalida registrarSalidaPorCajon(
            int cajonId,
            int estacionamientoId
    ) {
        RegistroEntradaSalida activo =
                registroDAO.obtenerActivoPorCajon(cajonId, estacionamientoId);

        if (activo == null) {
            return null;
        }

        Vehiculo vehiculo = vehiculoDAO.obtenerPorId(activo.getVehiculoId());
        String tipo = vehiculo != null && vehiculo.getTipo() != null
                ? vehiculo.getTipo()
                : "Auto";

        return registrarSalida(activo.getVehiculoId(), tipo, estacionamientoId);
    }

    public RegistroEntradaSalida obtenerRegistroActivoPorCajon(
            int cajonId,
            int estacionamientoId
    ) {
        return registroDAO.obtenerActivoPorCajon(cajonId, estacionamientoId);
    }

    private Cliente obtenerOCrearClienteAutomatico(String placaNormalizada)
            throws Exception {

        String documento = "AUTO-" + placaNormalizada;
        Cliente cliente = clienteDAO.obtenerPorDocumento(documento);

        if (cliente != null) {
            return cliente;
        }

        cliente = new Cliente();
        cliente.setNombre("Cliente");
        cliente.setApellido(placaNormalizada);
        cliente.setNumeroDocumento(documento);
        cliente.setTipoDocumento("PLACA");
        cliente.setCiudad("");
        cliente.setEmail("");
        cliente.setTelefono("");
        cliente.setActivo(true);

        if (!clienteDAO.crear(cliente) || cliente.getId() <= 0) {
            throw new Exception("No se pudo registrar el cliente automatico.");
        }

        return cliente;
    }

    private String normalizarPlaca(String placa) {
        return placa == null ? "" : placa.trim().toUpperCase();
    }

    private String valorONull(String valor) {
        return valor == null || valor.trim().isBlank() ? null : valor.trim();
    }

    /**
     * Registra la entrada de un vehículo
     * @param vehiculoId ID del vehículo
     * @param cajonId ID del cajón asignado
     * @param estacionamientoId ID del estacionamiento
     * @return true si se registró correctamente
     */
    public boolean registrarEntrada(
            int vehiculoId,
            int cajonId,
            int estacionamientoId
    ) throws Exception {

        // ===== VALIDAR PENSION =====

        Pension pension =
                pensionController.obtenerPensionPorVehiculo(vehiculoId);

        if (pension != null &&
            pensionController.esPensionVencida(pension)) {

            throw new Exception(
                "La pensión asignada al vehículo está vencida. " +
                "Renueve la pensión antes de registrar una nueva entrada."
            );
        }

        // ===== VALIDAR ENTRADA ACTIVA =====

        RegistroEntradaSalida activo =
                registroDAO.obtenerActivoDelVehiculo(vehiculoId);

        if (activo != null) {
            throw new Exception(
                "El vehículo ya tiene una entrada activa."
            );
        }

        // ===== CREAR REGISTRO =====

        RegistroEntradaSalida registro =
                new RegistroEntradaSalida(
                        vehiculoId,
                        cajonId,
                        LocalDateTime.now(),
                        estacionamientoId
                );

        // ===== CORRECCION IMPORTANTE =====

        registro.setEstado("Activo");
        registro.setPromocionAplicada(null);

        // ===== LOGS =====

        System.out.println("=== REGISTRANDO ENTRADA ===");
        System.out.println("Vehiculo ID: " + vehiculoId);
        System.out.println("Cajon ID: " + cajonId);
        System.out.println("Estacionamiento ID: " + estacionamientoId);
        System.out.println("Estado: " + registro.getEstado());

        // ===== INSERTAR =====

        if (registroDAO.crear(registro)) {

            // Cambiar estado del cajón
            cajonDAO.cambiarEstado(cajonId, "ocupado");

            // Actualizar cajones disponibles
            int disponibles =
                    cajonDAO.contarDisponibles(estacionamientoId);

            estacionamientoDAO.actualizarCajonesDisponibles(
                    estacionamientoId,
                    disponibles
            );

            try {

                Vehiculo vehiculo =
                        vehiculoDAO.obtenerPorId(vehiculoId);

                int clienteId =
                        vehiculo != null
                                ? vehiculo.getClienteId()
                                : 0;

                historialController.registrarEvento(
                        new HistorialEvento(
                                clienteId,
                                vehiculoId,
                                null,
                                cajonId,
                                "Entrada",
                                "Entrada registrada en cajón " + cajonId,
                                0.0,
                                LocalDateTime.now(),
                                estacionamientoId
                        )
                );

            } catch (Exception e) {

                System.err.println(
                    "No se pudo registrar evento de historial: "
                    + e.getMessage()
                );
            }

            return true;
        }

        throw new Exception(
            "No se pudo registrar la entrada."
        );
    }

    /**
     * Registra la salida de un vehículo y calcula el monto a pagar
     * @param vehiculoId ID del vehículo
     * @param tipoVehiculo tipo de vehículo (para consultar tarifa)
     * @param estacionamientoId ID del estacionamiento
     * @return objeto RegistroEntradaSalida con el monto calculado
     */
    public RegistroEntradaSalida registrarSalida(
            int vehiculoId,
            String tipoVehiculo,
            int estacionamientoId
    ) {

        // Obtener registro activo
        RegistroEntradaSalida registro =
                registroDAO.obtenerActivoDelVehiculo(vehiculoId);

        if (registro == null) {
            return null;
        }

        // Obtener tarifa
        Precio precio =
                precioDAO.obtenerPorTipoVehiculo(
                        tipoVehiculo,
                        estacionamientoId
                );

        if (precio == null) {
            return null;
        }

        LocalDateTime ahora = LocalDateTime.now();

        long minutosTranscurridos =
                DateUtils.calcularMinutos(
                        registro.getFechaEntrada(),
                        ahora
                );

        long horas =
                DateUtils.calcularHoras(
                        registro.getFechaEntrada(),
                        ahora
                );

        double monto;
        String promocionAplicada = null;
        String descripcionEvento;

        if (minutosTranscurridos <= 5) {

            monto = 0.0;

            descripcionEvento =
                    "Salida dentro de la tolerancia de 5 minutos";

        } else {

            if (horas < 1) {

                monto = precio.getPrecioHora();

            } else if (horas <= 4) {

                monto = precio.getPrecioMedia();

            } else {

                long dias =
                        DateUtils.calcularDias(
                                registro.getFechaEntrada(),
                                ahora
                        );

                monto = dias * precio.getPrecioDia();

                long horasRestantes =
                        horas - (dias * 24);

                if (horasRestantes > 0) {

                    monto +=
                            precio.getPrecioHora()
                            * horasRestantes;
                }
            }

            try {

                Promocion mejor =
                        promocionController.obtenerMejorPromocion(
                                estacionamientoId,
                                tipoVehiculo,
                                monto,
                                horas
                        );

                if (mejor != null) {

                    double montoConPromocion =
                            promocionController.aplicarPromocion(
                                    mejor,
                                    monto,
                                    horas
                            );

                    if (montoConPromocion < monto) {

                        promocionAplicada =
                                mejor.getNombre();

                        monto = montoConPromocion;
                    }
                }

            } catch (Exception e) {

                System.err.println(
                    "No se pudo aplicar promoción: "
                    + e.getMessage()
                );
            }

            descripcionEvento =
                    "Salida registrada"
                    + (
                        promocionAplicada != null
                        ? " con promoción: "
                          + promocionAplicada
                        : ""
                    );
        }

        if (registroDAO.finalizarRegistro(
                registro.getId(),
                ahora,
                monto,
                promocionAplicada
        )) {

            // Liberar cajón
            String estadoCajon = "libre";
            try {
                Pension pension = pensionController.obtenerPensionPorVehiculo(vehiculoId);
                if (pension != null &&
                    pension.getCajonId() == registro.getCajonId() &&
                    pension.getEstacionamientoId() == estacionamientoId &&
                    !pensionController.esPensionVencida(pension)) {
                    estadoCajon = "pensionado";
                }
            } catch (Exception e) {
                System.err.println("No se pudo validar pension al liberar cajon: " + e.getMessage());
            }

            cajonDAO.cambiarEstado(
                    registro.getCajonId(),
                    estadoCajon
            );

            // Actualizar disponibles
            int disponibles =
                    cajonDAO.contarDisponibles(estacionamientoId);

            estacionamientoDAO.actualizarCajonesDisponibles(
                    estacionamientoId,
                    disponibles
            );

            registro.setFechaSalida(ahora);
            registro.setMonto(monto);
            registro.setPromocionAplicada(promocionAplicada);
            registro.setEstado("Finalizado");

            try {

                Vehiculo vehiculo =
                        vehiculoDAO.obtenerPorId(vehiculoId);

                int clienteId =
                        vehiculo != null
                                ? vehiculo.getClienteId()
                                : 0;

                historialController.registrarEvento(
                        new HistorialEvento(
                                clienteId,
                                vehiculoId,
                                registro.getId(),
                                registro.getCajonId(),
                                "Salida",
                                descripcionEvento,
                                monto,
                                ahora,
                                estacionamientoId
                        )
                );

            } catch (Exception e) {

                System.err.println(
                    "No se pudo registrar evento de historial: "
                    + e.getMessage()
                );
            }

            return registro;
        }

        return null;
    }

    public RegistroEntradaSalida obtenerRegistro(int id) {
        return registroDAO.obtenerPorId(id);
    }

    public RegistroEntradaSalida obtenerRegistroActivoDelVehiculo(
            int vehiculoId
    ) {
        return registroDAO.obtenerActivoDelVehiculo(vehiculoId);
    }

    public List<RegistroEntradaSalida>
    obtenerRegistrosPorEstacionamiento(
            int estacionamientoId
    ) {
        return registroDAO.obtenerPorEstacionamiento(
                estacionamientoId
        );
    }

    public double obtenerIngresoDelDia(
            int estacionamientoId,
            LocalDateTime fecha
    ) {
        return registroDAO.obtenerIngresoDelDia(
                estacionamientoId,
                fecha
        );
    }

    public boolean registrarCambioCajon(
            int clienteId,
            int vehiculoId,
            int registroId,
            int cajonAnterior,
            int cajonNuevo,
            int estacionamientoId
    ) {

        String descripcion =
                String.format(
                        "Cambio de cajón de %d a %d",
                        cajonAnterior,
                        cajonNuevo
                );

        try {

            return historialController.registrarEvento(
                    new HistorialEvento(
                            clienteId,
                            vehiculoId,
                            registroId,
                            cajonNuevo,
                            "Cambio de cajón",
                            descripcion,
                            0.0,
                            LocalDateTime.now(),
                            estacionamientoId
                    )
            );

        } catch (Exception e) {

            System.err.println(
                "No se pudo registrar el cambio de cajón: "
                + e.getMessage()
            );

            return false;
        }
    }

    public boolean registrarIncidente(
            int clienteId,
            int vehiculoId,
            String descripcion,
            int estacionamientoId
    ) {

        try {

            return historialController.registrarEvento(
                    new HistorialEvento(
                            clienteId,
                            vehiculoId,
                            null,
                            null,
                            "Incidente",
                            descripcion,
                            0.0,
                            LocalDateTime.now(),
                            estacionamientoId
                    )
            );

        } catch (Exception e) {

            System.err.println(
                "No se pudo registrar el incidente: "
                + e.getMessage()
            );

            return false;
        }
    }
}
