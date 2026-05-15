package com.estacionamiento.controladores;

import com.estacionamiento.dao.CorteCajaDAO;
import com.estacionamiento.dao.LiquidacionRestauranteDAO;
import com.estacionamiento.dao.PagoDAO;
import com.estacionamiento.modelos.CorteCaja;
import com.estacionamiento.modelos.LiquidacionRestaurante;
import com.estacionamiento.modelos.Pago;
import com.estacionamiento.modelos.RegistroEntradaSalida;
import com.estacionamiento.modelos.Usuario;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador de Caja.
 *
 * Reglas de negocio:
 *   - Solo Cajeros y Encargados pueden registrar pagos.
 *   - Solo Encargados y Admin Global pueden hacer cortes de caja.
 *   - El cajero solo ve pagos de su estacionamiento.
 *   - El Admin Global ve todos los cortes de todos los estacionamientos.
 *   - No se puede registrar un pago si el registro ya está pagado.
 *   - El folio del ticket y del corte se generan automáticamente.
 */
public class CajaController {

    private final PagoDAO       pagoDAO       = new PagoDAO();
    private final CorteCajaDAO  corteCajaDAO  = new CorteCajaDAO();
    private final LiquidacionRestauranteDAO liquidacionDAO = new LiquidacionRestauranteDAO();

    // ════════════════════════════════════════════════════════
    //  PAGOS
    // ════════════════════════════════════════════════════════

    /**
     * Registra un pago para un registro de entrada/salida.
     *
     * @param registro     El registro que se va a cobrar (debe estar Finalizado)
     * @param montoPagado  Lo que entregó el cliente
     * @param metodo       Método de pago
     * @param cajero       Usuario que realiza el cobro
     * @return El Pago guardado con su número de ticket, null si falla
     * @throws SecurityException si el usuario no tiene permiso
     * @throws IllegalArgumentException si los datos son inválidos
     */
    public Pago registrarPago(RegistroEntradaSalida registro,
                              double montoPagado,
                              Pago.MetodoPago metodo,
                              Usuario cajero) {

        // ---- Validación de rol ----
        if (cajero.getRol() != 3 && cajero.getRol() != 2 && cajero.getRol() != 1) {
            throw new SecurityException("No tienes permiso para registrar pagos.");
        }

        // ---- Validación multi-estacionamiento ----
        if (!cajero.esAdminGlobal() && cajero.getEstacionamientoId() != null
                && cajero.getEstacionamientoId() != registro.getEstacionamientoId()) {
            throw new SecurityException(
                    "No puedes cobrar en un estacionamiento diferente al tuyo.");
        }

        // ---- Validaciones de datos ----
        if (registro == null) {
            throw new IllegalArgumentException("El registro de entrada/salida no puede ser nulo.");
        }
        if (!"Finalizado".equals(registro.getEstado())) {
            throw new IllegalArgumentException(
                    "El vehículo aún está en el estacionamiento. Registra primero su salida.");
        }
        if (pagoDAO.existePagoActivoPorRegistro(registro.getId())) {
            throw new IllegalArgumentException("Este registro ya tiene un pago activo.");
        }
        if (metodo == Pago.MetodoPago.CONVENIO) {
            throw new IllegalArgumentException("Para pagos por convenio usa registrarPagoConvenio con restaurante/convenio.");
        }
        if (montoPagado < registro.getMonto()) {
            throw new IllegalArgumentException(
                    String.format("El monto pagado ($%.2f) es menor al monto a cobrar ($%.2f).",
                            montoPagado, registro.getMonto()));
        }

        // ---- Crear pago ----
        Pago pago = new Pago(
                registro.getId(),
                registro.getEstacionamientoId(),
                cajero.getId(),
                cajero.getNombre() + " " + cajero.getApellido(),
                registro.getMonto(),
                montoPagado,
                metodo
        );
        pago.setNumeroTicket(generarFolioTicket(registro.getEstacionamientoId()));

        int id = pagoDAO.crear(pago);
        if (id > 0) {
            pago.setId(id);
            return pago;
        }
        return null;
    }

    public Pago registrarPagoConvenio(RegistroEntradaSalida registro,
                                      Usuario cajero,
                                      int restauranteId,
                                      Integer convenioId) {
        if (restauranteId <= 0) {
            throw new IllegalArgumentException("El restaurante es obligatorio para pagos por convenio.");
        }
        validarRegistroCobrable(registro, cajero);

        Pago pago = new Pago(
                registro.getId(),
                registro.getEstacionamientoId(),
                cajero.getId(),
                cajero.getNombre() + " " + cajero.getApellido(),
                registro.getMonto(),
                registro.getMonto(),
                Pago.MetodoPago.CONVENIO
        );
        pago.setRestauranteId(restauranteId);
        pago.setConvenioId(convenioId);
        pago.setEstadoLiquidacion("PENDIENTE");
        pago.setNotas("Pago cubierto por convenio de restaurante");
        pago.setNumeroTicket(generarFolioTicket(registro.getEstacionamientoId()));

        int id = pagoDAO.crear(pago);
        if (id > 0) {
            pago.setId(id);
            return pago;
        }
        return null;
    }

    /** Lista pagos del estacionamiento del cajero (o todos si es Admin Global) */
    public List<Pago> listarPagos(Usuario usuario) {
        if (usuario.esAdminGlobal()) {
            return pagoDAO.obtenerTodos();
        }
        int estId = usuario.getEstacionamientoId() != null ? usuario.getEstacionamientoId() : 0;
        return pagoDAO.obtenerPorEstacionamiento(estId);
    }

    /** Lista pagos del día para el estacionamiento del cajero */
    public List<Pago> listarPagosDelDia(Usuario usuario) {
        int estId = resolverEstacionamiento(usuario);
        return pagoDAO.obtenerDelDia(estId, LocalDateTime.now());
    }

    /** Lista pagos entre dos fechas */
    public List<Pago> listarPagosEntreFechas(Usuario usuario,
                                              LocalDateTime desde,
                                              LocalDateTime hasta) {
        validarFechas(desde, hasta);
        int estId = resolverEstacionamiento(usuario);
        return pagoDAO.obtenerEntreFechas(estId, desde, hasta);
    }

    public List<Pago> listarPagosConvenioPendientes(int restauranteId,
                                                     LocalDateTime desde,
                                                     LocalDateTime hasta) {
        validarFechas(desde, hasta);
        return pagoDAO.obtenerPendientesPorRestaurante(restauranteId, desde, hasta);
    }

    public LiquidacionRestaurante liquidarPagosRestaurante(int restauranteId,
                                                           int estacionamientoId,
                                                           Integer convenioId,
                                                           LocalDateTime desde,
                                                           LocalDateTime hasta,
                                                           String observaciones) {
        validarFechas(desde, hasta);
        List<Pago> pagos = pagoDAO.obtenerPendientesPorRestaurante(restauranteId, desde, hasta);
        if (pagos.isEmpty()) {
            throw new IllegalArgumentException("No hay pagos de convenio pendientes para liquidar.");
        }

        double total = pagos.stream().mapToDouble(Pago::getMonto).sum();
        LiquidacionRestaurante liquidacion = new LiquidacionRestaurante();
        liquidacion.setRestauranteId(restauranteId);
        liquidacion.setEstacionamientoId(estacionamientoId);
        liquidacion.setConvenioId(convenioId);
        liquidacion.setFechaInicio(desde);
        liquidacion.setFechaFin(hasta);
        liquidacion.setFechaLiquidacion(LocalDateTime.now());
        liquidacion.setTotal(total);
        liquidacion.setEstado("COBRADA");
        liquidacion.setFolioLiquidacion(generarFolioLiquidacion(restauranteId));
        liquidacion.setObservaciones(observaciones != null ? observaciones.trim() : "");

        int id = liquidacionDAO.crear(liquidacion);
        if (id <= 0) return null;

        liquidacion.setId(id);
        List<Integer> pagoIds = new ArrayList<>();
        for (Pago pago : pagos) {
            pagoIds.add(pago.getId());
        }
        pagoDAO.marcarLiquidacion(pagoIds, id);
        return liquidacion;
    }

    // ════════════════════════════════════════════════════════
    //  CORTE DE CAJA
    // ════════════════════════════════════════════════════════

    /**
     * Realiza un corte de caja calculando los totales desde el último corte.
     *
     * @param usuario    Encargado o Admin que hace el corte
     * @param tipoCorte  PARCIAL o FINAL
     * @param observaciones Notas opcionales
     * @return El CorteCaja guardado, null si falla
     * @throws SecurityException si el cajero intenta hacer un corte
     */
    public CorteCaja realizarCorte(Usuario usuario,
                                   CorteCaja.TipoCorte tipoCorte,
                                   String observaciones) {

        // ---- Solo Encargado (rol 2) o Admin (rol 1) ----
        if (usuario.getRol() == 3) {
            throw new SecurityException(
                    "Los cajeros no pueden realizar cortes de caja. " +
                    "Solicita al encargado que realice el corte.");
        }

        int estId = resolverEstacionamiento(usuario);

        // Determinar inicio del período (desde el último corte o inicio del día)
        CorteCaja ultimo = corteCajaDAO.obtenerUltimo(estId);
        LocalDateTime desde = ultimo != null
                ? ultimo.getFechaCorte()
                : LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime hasta = LocalDateTime.now();

        // Calcular totales por método de pago
        CorteCaja corte = new CorteCaja(estId, usuario.getId(),
                usuario.getNombre() + " " + usuario.getApellido(), desde, tipoCorte);

        corte.setTotalEfectivo(
                pagoDAO.obtenerTotalPorMetodo(estId, "EFECTIVO", desde, hasta));
        corte.setTotalTarjeta(
                pagoDAO.obtenerTotalPorMetodo(estId, "TARJETA_CREDITO", desde, hasta)
                + pagoDAO.obtenerTotalPorMetodo(estId, "TARJETA_DEBITO", desde, hasta));
        corte.setTotalTransferencia(
                pagoDAO.obtenerTotalPorMetodo(estId, "TRANSFERENCIA", desde, hasta));
        corte.setTotalConvenio(
                pagoDAO.obtenerTotalPorMetodo(estId, "CONVENIO", desde, hasta));
        corte.calcularTotal();
        corte.setTotalTransacciones(pagoDAO.contarTransacciones(estId, desde, hasta));
        corte.setFolioCorte(generarFolioCorte(estId));
        corte.setObservaciones(observaciones != null ? observaciones.trim() : "");

        int id = corteCajaDAO.crear(corte);
        if (id > 0) {
            corte.setId(id);
            return corte;
        }
        return null;
    }

    /** Historial de cortes para el estacionamiento del usuario */
    public List<CorteCaja> listarCortes(Usuario usuario) {
        if (usuario.esAdminGlobal()) {
            return corteCajaDAO.obtenerTodos();
        }
        int estId = resolverEstacionamiento(usuario);
        return corteCajaDAO.obtenerPorEstacionamiento(estId);
    }

    // ════════════════════════════════════════════════════════
    //  TICKET (generación de texto)
    // ════════════════════════════════════════════════════════

    /**
     * Genera el texto del ticket de pago listo para imprimir o mostrar.
     */
    public String generarTextoTicket(Pago pago, RegistroEntradaSalida registro,
                                     String nombreEstacionamiento) {
        DateTimeFormatter fmtFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter fmtHora  = DateTimeFormatter.ofPattern("HH:mm");

        StringBuilder sb = new StringBuilder();
        sb.append("============================================\n");
        sb.append("           P·PARK ESTACIONAMIENTO          \n");
        sb.append("     ").append(nombreEstacionamiento).append("\n");
        sb.append("============================================\n");
        sb.append("  TICKET DE PAGO\n\n");
        sb.append("  Folio:      ").append(pago.getNumeroTicket()).append("\n");
        sb.append("  Fecha:      ").append(pago.getFechaPago().format(fmtFecha)).append("\n");
        sb.append("  Hora:       ").append(pago.getFechaPago().format(fmtHora)).append("\n");
        sb.append("--------------------------------------------\n");
        if (registro != null) {
            sb.append("  Vehículo:   #").append(registro.getVehiculoId()).append("\n");
            sb.append("  Cajón:      #").append(registro.getCajonId()).append("\n");
            if (registro.getFechaEntrada() != null)
                sb.append("  Entrada:    ").append(registro.getFechaEntrada().format(fmtHora))
                  .append("  ").append(registro.getFechaEntrada().format(fmtFecha)).append("\n");
            if (registro.getFechaSalida() != null)
                sb.append("  Salida:     ").append(registro.getFechaSalida().format(fmtHora))
                  .append("  ").append(registro.getFechaSalida().format(fmtFecha)).append("\n");
        }
        sb.append("--------------------------------------------\n");
        sb.append(String.format("  TOTAL:      $%.2f%n", pago.getMonto()));
        sb.append(String.format("  PAGADO:     $%.2f%n", pago.getMontoPagado()));
        sb.append(String.format("  CAMBIO:     $%.2f%n", pago.getCambio()));
        sb.append("  Método:     ").append(pago.getMetodoPago().getLabel()).append("\n");
        sb.append("--------------------------------------------\n");
        sb.append("  Atendió:    ").append(pago.getCajeroNombre()).append("\n");
        sb.append("============================================\n");
        sb.append("       ¡GRACIAS POR SU PREFERENCIA!       \n");
        sb.append("============================================\n");
        return sb.toString();
    }

    /**
     * Genera el texto del resumen de corte de caja.
     */
    public String generarTextoCorte(CorteCaja corte, String nombreEstacionamiento) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        StringBuilder sb = new StringBuilder();
        sb.append("============================================\n");
        sb.append("           P·PARK ESTACIONAMIENTO          \n");
        sb.append("     ").append(nombreEstacionamiento).append("\n");
        sb.append("============================================\n");
        sb.append("  CORTE DE CAJA — ").append(corte.getTipoCorte().getLabel().toUpperCase()).append("\n\n");
        sb.append("  Folio:      ").append(corte.getFolioCorte()).append("\n");
        sb.append("  Cajero:     ").append(corte.getCajeroNombre()).append("\n");
        sb.append("  Período:    ").append(corte.getFechaInicio().format(fmt)).append("\n");
        sb.append("              hasta ").append(corte.getFechaCorte().format(fmt)).append("\n");
        sb.append("--------------------------------------------\n");
        sb.append("  DESGLOSE POR MÉTODO DE PAGO\n\n");
        sb.append(String.format("  Efectivo:          $%10.2f%n", corte.getTotalEfectivo()));
        sb.append(String.format("  Tarjeta:           $%10.2f%n", corte.getTotalTarjeta()));
        sb.append(String.format("  Transferencia:     $%10.2f%n", corte.getTotalTransferencia()));
        sb.append(String.format("  Convenio:          $%10.2f%n", corte.getTotalConvenio()));
        sb.append("--------------------------------------------\n");
        sb.append(String.format("  TOTAL GENERAL:     $%10.2f%n", corte.getTotalGeneral()));
        sb.append(String.format("  Transacciones:     %11d%n", corte.getTotalTransacciones()));
        if (corte.getObservaciones() != null && !corte.getObservaciones().isBlank()) {
            sb.append("--------------------------------------------\n");
            sb.append("  Observaciones: ").append(corte.getObservaciones()).append("\n");
        }
        sb.append("============================================\n");
        return sb.toString();
    }

    // ════════════════════════════════════════════════════════
    //  HELPERS PRIVADOS
    // ════════════════════════════════════════════════════════

    /** Resuelve el ID del estacionamiento según el rol del usuario */
    private void validarRegistroCobrable(RegistroEntradaSalida registro, Usuario cajero) {
        if (registro == null) {
            throw new IllegalArgumentException("El registro de entrada/salida no puede ser nulo.");
        }
        if (cajero == null) {
            throw new IllegalArgumentException("El cajero es obligatorio.");
        }
        if (cajero.getRol() != 3 && cajero.getRol() != 2 && cajero.getRol() != 1) {
            throw new SecurityException("No tienes permiso para registrar pagos.");
        }
        if (!cajero.esAdminGlobal() && cajero.getEstacionamientoId() != null
                && cajero.getEstacionamientoId() != registro.getEstacionamientoId()) {
            throw new SecurityException("No puedes cobrar en un estacionamiento diferente al tuyo.");
        }
        if (!"Finalizado".equals(registro.getEstado())) {
            throw new IllegalArgumentException("Registra primero la salida del vehiculo.");
        }
        if (pagoDAO.existePagoActivoPorRegistro(registro.getId())) {
            throw new IllegalArgumentException("Este registro ya tiene un pago activo.");
        }
    }

    private int resolverEstacionamiento(Usuario usuario) {
        if (usuario.esAdminGlobal() || usuario.getEstacionamientoId() == null) {
            return 1; // Admin sin asignación: retorna 1 como default
        }
        return usuario.getEstacionamientoId();
    }

    private void validarFechas(LocalDateTime desde, LocalDateTime hasta) {
        if (desde == null || hasta == null)
            throw new IllegalArgumentException("Las fechas son obligatorias.");
        if (hasta.isBefore(desde))
            throw new IllegalArgumentException("La fecha fin debe ser posterior a la de inicio.");
    }

    /** Genera un folio único para ticket: EST{estId}-{yyyyMMddHHmmss} */
    private String generarFolioTicket(int estacionamientoId) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return "T" + estacionamientoId + "-" + LocalDateTime.now().format(fmt);
    }

    /** Genera un folio único para corte: CUT{estId}-{yyyyMMddHHmmss} */
    private String generarFolioCorte(int estacionamientoId) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return "C" + estacionamientoId + "-" + LocalDateTime.now().format(fmt);
    }

    private String generarFolioLiquidacion(int restauranteId) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return "LR" + restauranteId + "-" + LocalDateTime.now().format(fmt);
    }
}
