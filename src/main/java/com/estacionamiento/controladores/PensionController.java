package com.estacionamiento.controladores;

import java.util.List;

import com.estacionamiento.dao.CajonDAO;
import com.estacionamiento.dao.ClienteDAO;
import com.estacionamiento.dao.EstacionamientoDAO;
import com.estacionamiento.dao.PensionDAO;
import com.estacionamiento.dao.RegistroEntradaSalidaDAO;
import com.estacionamiento.dao.VehiculoDAO;
import com.estacionamiento.modelos.Cajon;
import com.estacionamiento.modelos.Cliente;
import com.estacionamiento.modelos.Pago;
import com.estacionamiento.modelos.Pension;
import com.estacionamiento.modelos.Usuario;
import com.estacionamiento.modelos.Vehiculo;
import com.estacionamiento.utilidades.ConfigManager;

/**
 * Controlador para gestión de Pensiones
 */
public class PensionController {
    private PensionDAO pensionDAO;
    private CajonDAO cajonDAO;
    private EstacionamientoDAO estacionamientoDAO;
    private ClienteDAO clienteDAO;
    private VehiculoDAO vehiculoDAO;
    private RegistroEntradaSalidaDAO registroDAO;
    private CajaController cajaController;

    public PensionController() {
        this.pensionDAO = new PensionDAO();
        this.cajonDAO = new CajonDAO();
        this.estacionamientoDAO = new EstacionamientoDAO();
        this.clienteDAO = new ClienteDAO();
        this.vehiculoDAO = new VehiculoDAO();
        this.registroDAO = new RegistroEntradaSalidaDAO();
        this.cajaController = new CajaController();
    }

    public boolean crearPension(Pension pension) throws Exception {
        if (pension == null) {
            throw new Exception("Pensión no puede ser nula");
        }
        boolean ok = pensionDAO.crear(pension);
        if (ok) {
            cajonDAO.cambiarEstado(pension.getCajonId(), "pensionado");
            actualizarDisponibles(pension.getEstacionamientoId());
        }
        return ok;
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
        Pension anterior = pensionDAO.obtenerPorId(pension.getId());
        boolean ok = pensionDAO.actualizar(pension);
        if (ok) {
            if (anterior != null && anterior.getCajonId() != pension.getCajonId()) {
                cajonDAO.cambiarEstado(anterior.getCajonId(), "libre");
            }
            cajonDAO.cambiarEstado(pension.getCajonId(), "pensionado");
            actualizarDisponibles(pension.getEstacionamientoId());
        }
        return ok;
    }

    public Pago crearPensionPorPlacaConCobro(String placa,
                                             String tipoVehiculo,
                                             String marca,
                                             String modelo,
                                             String color,
                                             int cajonId,
                                             int estacionamientoId,
                                             java.time.LocalDateTime fechaInicio,
                                             java.time.LocalDateTime fechaFin,
                                             double monto,
                                             Usuario usuario) throws Exception {
        Vehiculo vehiculo = resolverVehiculoPorPlaca(placa, tipoVehiculo, marca, modelo, color);
        validarPensionNueva(vehiculo, cajonId, estacionamientoId, fechaInicio, fechaFin, monto);

        Pension pension = new Pension(
                vehiculo.getClienteId(),
                vehiculo.getId(),
                cajonId,
                fechaInicio,
                fechaFin,
                monto,
                estacionamientoId
        );
        pension.setEstado("Activa");

        boolean ok = crearPension(pension);
        if (!ok || pension.getId() <= 0) {
            throw new Exception("No se pudo crear la pension.");
        }

        try {
            return cajaController.registrarPagoPension(pension, usuario);
        } catch (Exception ex) {
            cancelarPension(pension.getId());
            throw ex;
        }
    }

    public boolean cancelarPension(int pensionId) throws Exception {
        Pension pension = pensionDAO.obtenerPorId(pensionId);
        boolean ok = pensionDAO.cancelarPension(pensionId);
        if (ok && pension != null) {
            cajonDAO.cambiarEstado(pension.getCajonId(), "libre");
            actualizarDisponibles(pension.getEstacionamientoId());
        }
        return ok;
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

    private void actualizarDisponibles(int estacionamientoId) {
        int disponibles = cajonDAO.contarDisponibles(estacionamientoId);
        estacionamientoDAO.actualizarCajonesDisponibles(estacionamientoId, disponibles);
    }

    private Vehiculo resolverVehiculoPorPlaca(String placa,
                                              String tipoVehiculo,
                                              String marca,
                                              String modelo,
                                              String color) throws Exception {
        String placaNormalizada = placa == null ? "" : placa.trim().toUpperCase();
        if (placaNormalizada.isBlank()) {
            throw new Exception("La placa es obligatoria.");
        }

        Vehiculo vehiculo = vehiculoDAO.obtenerPorPatente(placaNormalizada);
        if (vehiculo != null) {
            return vehiculo;
        }

        Cliente cliente = new Cliente();
        cliente.setNombre("Cliente");
        cliente.setApellido("Pension " + placaNormalizada);
        cliente.setEmail("");
        cliente.setTelefono("");
        cliente.setNumeroDocumento("AUTO-" + placaNormalizada);
        cliente.setTipoDocumento("PLACA");
        cliente.setCiudad("");
        cliente.setActivo(true);

        if (!clienteDAO.crear(cliente) || cliente.getId() <= 0) {
            throw new Exception("No se pudo crear el cliente automatico para la placa.");
        }

        vehiculo = new Vehiculo(
                placaNormalizada,
                normalizarTexto(marca),
                normalizarTexto(modelo),
                normalizarTexto(color),
                cliente.getId(),
                normalizarTipo(tipoVehiculo)
        );
        vehiculo.setActivo(true);

        if (!vehiculoDAO.crear(vehiculo) || vehiculo.getId() <= 0) {
            throw new Exception("No se pudo crear el vehiculo automatico para la placa.");
        }

        return vehiculo;
    }

    private void validarPensionNueva(Vehiculo vehiculo,
                                     int cajonId,
                                     int estacionamientoId,
                                     java.time.LocalDateTime fechaInicio,
                                     java.time.LocalDateTime fechaFin,
                                     double monto) throws Exception {
        if (vehiculo == null || vehiculo.getId() <= 0) {
            throw new Exception("Vehiculo invalido.");
        }
        if (fechaInicio == null || fechaFin == null) {
            throw new Exception("Las fechas son obligatorias.");
        }
        if (!fechaFin.isAfter(fechaInicio)) {
            throw new Exception("La fecha fin debe ser posterior a la fecha inicio.");
        }
        if (monto <= 0) {
            throw new Exception("El monto debe ser mayor a cero.");
        }

        Pension existente = obtenerPensionPorVehiculo(vehiculo.getId());
        if (existente != null && existente.getEstacionamientoId() == estacionamientoId && !esPensionVencida(existente)) {
            throw new Exception("Esta placa ya tiene una pension activa.");
        }

        Cajon cajon = cajonDAO.obtenerPorId(cajonId);
        if (cajon == null || cajon.getEstacionamientoId() != estacionamientoId) {
            throw new Exception("Seleccione un cajon valido del estacionamiento.");
        }
        String estado = cajon.getEstado() == null ? "" : cajon.getEstado().trim().toLowerCase();
        if (!estado.equals("libre") && !estado.equals("disponible")) {
            throw new Exception("El cajon seleccionado no esta disponible.");
        }

        if (registroDAO.obtenerActivoDelVehiculo(vehiculo.getId()) != null) {
            throw new Exception("Esta placa ya tiene una entrada activa. Registre la salida antes de crear la pension.");
        }
    }

    private String normalizarTexto(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private String normalizarTipo(String tipoVehiculo) {
        String valor = normalizarTexto(tipoVehiculo);
        return valor.isBlank() ? "Auto" : valor;
    }
}
