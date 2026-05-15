package com.estacionamiento.dao;

import com.estacionamiento.modelos.Pago;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PagoDAO {

    private final Connection conexion;

    public PagoDAO() {
        this.conexion = ConexionDB.getInstancia().getConexion();
    }

    public int crear(Pago pago) {
        String sql = "INSERT INTO pagos (registro_id, estacionamiento_id, cajero_id, cajero_nombre, " +
                "monto, monto_pagado, cambio, metodo_pago, numero_ticket, fecha_pago, notas, anulado, " +
                "restaurante_id, convenio_id, liquidacion_restaurante_id, estado_liquidacion) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            String metodo = pago.getMetodoPago() != null ? pago.getMetodoPago().name() : Pago.MetodoPago.EFECTIVO.name();
            String estadoLiquidacion = pago.getEstadoLiquidacion();
            if (estadoLiquidacion == null || estadoLiquidacion.isBlank()) {
                estadoLiquidacion = Pago.MetodoPago.CONVENIO.name().equals(metodo) ? "PENDIENTE" : "NO_APLICA";
            }

            ps.setInt(1, pago.getRegistroId());
            ps.setInt(2, pago.getEstacionamientoId());
            ps.setInt(3, pago.getCajeroId());
            ps.setString(4, pago.getCajeroNombre());
            ps.setDouble(5, pago.getMonto());
            ps.setDouble(6, pago.getMontoPagado());
            ps.setDouble(7, pago.getCambio());
            ps.setString(8, metodo);
            ps.setString(9, pago.getNumeroTicket());
            ps.setTimestamp(10, Timestamp.valueOf(pago.getFechaPago() != null ? pago.getFechaPago() : LocalDateTime.now()));
            ps.setString(11, pago.getNotas() != null ? pago.getNotas() : "");
            ps.setBoolean(12, pago.isAnulado());
            setNullableInt(ps, 13, pago.getRestauranteId());
            setNullableInt(ps, 14, pago.getConvenioId());
            setNullableInt(ps, 15, pago.getLiquidacionRestauranteId());
            ps.setString(16, estadoLiquidacion);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error al crear pago: " + e.getMessage());
        }
        return -1;
    }

    public List<Pago> obtenerTodos() {
        List<Pago> lista = new ArrayList<>();
        String sql = "SELECT * FROM pagos ORDER BY fecha_pago DESC";
        try (Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.err.println("Error al obtener pagos: " + e.getMessage());
        }
        return lista;
    }

    public List<Pago> obtenerPorEstacionamiento(int estacionamientoId) {
        List<Pago> lista = new ArrayList<>();
        String sql = "SELECT * FROM pagos WHERE estacionamiento_id = ? ORDER BY fecha_pago DESC";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, estacionamientoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener pagos: " + e.getMessage());
        }
        return lista;
    }

    public List<Pago> obtenerPorRestaurante(int restauranteId) {
        List<Pago> lista = new ArrayList<>();
        String sql = "SELECT * FROM pagos WHERE restaurante_id = ? ORDER BY fecha_pago DESC";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, restauranteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener pagos por restaurante: " + e.getMessage());
        }
        return lista;
    }

    public List<Pago> obtenerPendientesPorRestaurante(int restauranteId, LocalDateTime desde, LocalDateTime hasta) {
        List<Pago> lista = new ArrayList<>();
        String sql = "SELECT * FROM pagos WHERE restaurante_id = ? " +
                "AND metodo_pago = 'CONVENIO' AND estado_liquidacion = 'PENDIENTE' " +
                "AND anulado = false AND fecha_pago BETWEEN ? AND ? ORDER BY fecha_pago ASC";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, restauranteId);
            ps.setTimestamp(2, Timestamp.valueOf(desde));
            ps.setTimestamp(3, Timestamp.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener pagos pendientes por restaurante: " + e.getMessage());
        }
        return lista;
    }

    public List<Pago> obtenerDelDia(int estacionamientoId, LocalDateTime fecha) {
        List<Pago> lista = new ArrayList<>();
        String sql = "SELECT * FROM pagos WHERE estacionamiento_id = ? " +
                "AND DATE(fecha_pago) = DATE(?) AND anulado = false ORDER BY fecha_pago DESC";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, estacionamientoId);
            ps.setTimestamp(2, Timestamp.valueOf(fecha));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener pagos del dia: " + e.getMessage());
        }
        return lista;
    }

    public List<Pago> obtenerEntreFechas(int estacionamientoId, LocalDateTime desde, LocalDateTime hasta) {
        List<Pago> lista = new ArrayList<>();
        String sql = "SELECT * FROM pagos WHERE estacionamiento_id = ? " +
                "AND fecha_pago BETWEEN ? AND ? AND anulado = false ORDER BY fecha_pago DESC";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, estacionamientoId);
            ps.setTimestamp(2, Timestamp.valueOf(desde));
            ps.setTimestamp(3, Timestamp.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener pagos: " + e.getMessage());
        }
        return lista;
    }

    public double obtenerTotalPorMetodo(int estacionamientoId, String metodoPago,
                                        LocalDateTime desde, LocalDateTime hasta) {
        String sql = "SELECT COALESCE(SUM(monto), 0) as total FROM pagos " +
                "WHERE estacionamiento_id = ? AND metodo_pago = ? " +
                "AND fecha_pago BETWEEN ? AND ? AND anulado = false";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, estacionamientoId);
            ps.setString(2, metodoPago);
            ps.setTimestamp(3, Timestamp.valueOf(desde));
            ps.setTimestamp(4, Timestamp.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener total: " + e.getMessage());
        }
        return 0;
    }

    public int contarTransacciones(int estacionamientoId, LocalDateTime desde, LocalDateTime hasta) {
        String sql = "SELECT COUNT(*) as total FROM pagos " +
                "WHERE estacionamiento_id = ? AND fecha_pago BETWEEN ? AND ? AND anulado = false";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, estacionamientoId);
            ps.setTimestamp(2, Timestamp.valueOf(desde));
            ps.setTimestamp(3, Timestamp.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("Error al contar transacciones: " + e.getMessage());
        }
        return 0;
    }

    public boolean existePagoActivoPorRegistro(int registroId) {
        String sql = "SELECT COUNT(*) AS total FROM pagos WHERE registro_id = ? AND anulado = false";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, registroId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt("total") > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al validar pago activo: " + e.getMessage());
            return false;
        }
    }

    public Pago obtenerPorTicket(String numeroTicket) {
        String sql = "SELECT * FROM pagos WHERE numero_ticket = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, numeroTicket);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener pago por ticket: " + e.getMessage());
        }
        return null;
    }

    public boolean anular(int id) {
        String sql = "UPDATE pagos SET anulado = true, " +
                "estado_liquidacion = CASE WHEN metodo_pago = 'CONVENIO' THEN 'CANCELADO' ELSE estado_liquidacion END " +
                "WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al anular pago: " + e.getMessage());
            return false;
        }
    }

    public boolean marcarLiquidacion(List<Integer> pagoIds, int liquidacionId) {
        String sql = "UPDATE pagos SET liquidacion_restaurante_id = ?, estado_liquidacion = 'LIQUIDADO' " +
                "WHERE id = ? AND metodo_pago = 'CONVENIO' AND anulado = false";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            for (Integer pagoId : pagoIds) {
                ps.setInt(1, liquidacionId);
                ps.setInt(2, pagoId);
                ps.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Error al marcar pagos liquidados: " + e.getMessage());
            return false;
        }
    }

    private Pago mapear(ResultSet rs) throws SQLException {
        Pago p = new Pago();
        p.setId(rs.getInt("id"));
        p.setRegistroId(rs.getInt("registro_id"));
        p.setEstacionamientoId(rs.getInt("estacionamiento_id"));
        p.setCajeroId(rs.getInt("cajero_id"));
        p.setCajeroNombre(rs.getString("cajero_nombre"));
        p.setMonto(rs.getDouble("monto"));
        p.setMontoPagado(rs.getDouble("monto_pagado"));
        p.setCambio(rs.getDouble("cambio"));
        try {
            p.setMetodoPago(Pago.MetodoPago.valueOf(rs.getString("metodo_pago")));
        } catch (Exception e) {
            p.setMetodoPago(Pago.MetodoPago.EFECTIVO);
        }
        p.setNumeroTicket(rs.getString("numero_ticket"));
        Timestamp ts = rs.getTimestamp("fecha_pago");
        if (ts != null) p.setFechaPago(ts.toLocalDateTime());
        p.setNotas(rs.getString("notas"));
        p.setAnulado(rs.getBoolean("anulado"));
        p.setRestauranteId(getNullableInt(rs, "restaurante_id"));
        p.setConvenioId(getNullableInt(rs, "convenio_id"));
        p.setLiquidacionRestauranteId(getNullableInt(rs, "liquidacion_restaurante_id"));
        p.setEstadoLiquidacion(rs.getString("estado_liquidacion"));
        return p;
    }

    private void setNullableInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }

    private Integer getNullableInt(ResultSet rs, String column) throws SQLException {
        Object value = rs.getObject(column);
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).intValue();
        String text = String.valueOf(value);
        return text.isBlank() ? null : Integer.parseInt(text);
    }
}
