package com.estacionamiento.dao;

import com.estacionamiento.modelos.Pago;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla 'pagos'.
 * Maneja registro de pagos, historial y consultas por método de pago.
 *
 * SQL para crear la tabla (agregar al script crear_base_datos.sql):
 *
 * CREATE TABLE IF NOT EXISTS pagos (
 *   id                  INT PRIMARY KEY AUTO_INCREMENT,
 *   registro_id         INT NOT NULL,
 *   estacionamiento_id  INT NOT NULL,
 *   cajero_id           INT NOT NULL,
 *   cajero_nombre       VARCHAR(200),
 *   monto               DOUBLE NOT NULL DEFAULT 0,
 *   monto_pagado        DOUBLE NOT NULL DEFAULT 0,
 *   cambio              DOUBLE NOT NULL DEFAULT 0,
 *   metodo_pago         VARCHAR(30) NOT NULL,
 *   numero_ticket       VARCHAR(30) UNIQUE,
 *   fecha_pago          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 *   notas               TEXT,
 *   anulado             BOOLEAN DEFAULT FALSE,
 *   FOREIGN KEY (registro_id) REFERENCES registros_entrada_salida(id),
 *   FOREIGN KEY (estacionamiento_id) REFERENCES estacionamientos(id),
 *   FOREIGN KEY (cajero_id) REFERENCES usuarios(id)
 * );
 */
public class PagoDAO {

    private Connection conexion;

    public PagoDAO() {
        this.conexion = ConexionDB.getInstancia().getConexion();
    }

    /** Guarda un nuevo pago y retorna el ID generado, -1 si falla */
    public int crear(Pago pago) {
        String sql = "INSERT INTO pagos (registro_id, estacionamiento_id, cajero_id, cajero_nombre, " +
                     "monto, monto_pagado, cambio, metodo_pago, numero_ticket, fecha_pago, notas, anulado) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, pago.getRegistroId());
            ps.setInt(2, pago.getEstacionamientoId());
            ps.setInt(3, pago.getCajeroId());
            ps.setString(4, pago.getCajeroNombre());
            ps.setDouble(5, pago.getMonto());
            ps.setDouble(6, pago.getMontoPagado());
            ps.setDouble(7, pago.getCambio());
            ps.setString(8, pago.getMetodoPago().name());
            ps.setString(9, pago.getNumeroTicket());
            ps.setTimestamp(10, Timestamp.valueOf(pago.getFechaPago()));
            ps.setString(11, pago.getNotas() != null ? pago.getNotas() : "");
            ps.setBoolean(12, pago.isAnulado());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error al crear pago: " + e.getMessage());
        }
        return -1;
    }

    /** Obtiene pagos por estacionamiento, ordenados del más reciente al más antiguo */
    public List<Pago> obtenerPorEstacionamiento(int estacionamientoId) {
        List<Pago> lista = new ArrayList<>();
        String sql = "SELECT * FROM pagos WHERE estacionamiento_id = ? " +
                     "ORDER BY fecha_pago DESC";
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

    /** Pagos del día actual para un estacionamiento */
    public List<Pago> obtenerDelDia(int estacionamientoId, LocalDateTime fecha) {
        List<Pago> lista = new ArrayList<>();
        String sql = "SELECT * FROM pagos WHERE estacionamiento_id = ? " +
                     "AND DATE(fecha_pago) = DATE(?) AND anulado = false " +
                     "ORDER BY fecha_pago DESC";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, estacionamientoId);
            ps.setTimestamp(2, Timestamp.valueOf(fecha));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener pagos del día: " + e.getMessage());
        }
        return lista;
    }

    /** Pagos entre dos fechas (para corte de caja) */
    public List<Pago> obtenerEntreFechas(int estacionamientoId,
                                         LocalDateTime desde, LocalDateTime hasta) {
        List<Pago> lista = new ArrayList<>();
        String sql = "SELECT * FROM pagos WHERE estacionamiento_id = ? " +
                     "AND fecha_pago BETWEEN ? AND ? AND anulado = false " +
                     "ORDER BY fecha_pago DESC";
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

    /** Total cobrado por método de pago en un rango de fechas */
    public double obtenerTotalPorMetodo(int estacionamientoId,
                                        String metodoPago,
                                        LocalDateTime desde,
                                        LocalDateTime hasta) {
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

    /** Cuenta transacciones en un rango de fechas */
    public int contarTransacciones(int estacionamientoId,
                                   LocalDateTime desde, LocalDateTime hasta) {
        String sql = "SELECT COUNT(*) as total FROM pagos " +
                     "WHERE estacionamiento_id = ? AND fecha_pago BETWEEN ? AND ? " +
                     "AND anulado = false";
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

    /** Obtiene un pago por su ticket */
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

    /** Anula un pago (no lo elimina físicamente) */
    public boolean anular(int id) {
        String sql = "UPDATE pagos SET anulado = true WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al anular pago: " + e.getMessage());
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
        return p;
    }
}
