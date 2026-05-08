package com.estacionamiento.dao;

import com.estacionamiento.modelos.CorteCaja;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla 'cortes_caja'.
 *
 * SQL para crear la tabla (agregar al script crear_base_datos.sql):
 *
 * CREATE TABLE IF NOT EXISTS cortes_caja (
 *   id                  INT PRIMARY KEY AUTO_INCREMENT,
 *   estacionamiento_id  INT NOT NULL,
 *   cajero_id           INT NOT NULL,
 *   cajero_nombre       VARCHAR(200),
 *   fecha_inicio        TIMESTAMP NOT NULL,
 *   fecha_corte         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 *   total_efectivo      DOUBLE DEFAULT 0,
 *   total_tarjeta       DOUBLE DEFAULT 0,
 *   total_transferencia DOUBLE DEFAULT 0,
 *   total_convenio      DOUBLE DEFAULT 0,
 *   total_general       DOUBLE DEFAULT 0,
 *   total_transacciones INT DEFAULT 0,
 *   tipo_corte          VARCHAR(20) DEFAULT 'PARCIAL',
 *   folio_corte         VARCHAR(30) UNIQUE,
 *   observaciones       TEXT,
 *   FOREIGN KEY (estacionamiento_id) REFERENCES estacionamientos(id),
 *   FOREIGN KEY (cajero_id) REFERENCES usuarios(id)
 * );
 */
public class CorteCajaDAO {

    private Connection conexion;

    public CorteCajaDAO() {
        this.conexion = ConexionDB.getInstancia().getConexion();
    }

    /** Guarda un corte de caja y retorna el ID generado, -1 si falla */
    public int crear(CorteCaja corte) {
        String sql = "INSERT INTO cortes_caja (estacionamiento_id, cajero_id, cajero_nombre, " +
                     "fecha_inicio, fecha_corte, total_efectivo, total_tarjeta, " +
                     "total_transferencia, total_convenio, total_general, " +
                     "total_transacciones, tipo_corte, folio_corte, observaciones) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, corte.getEstacionamientoId());
            ps.setInt(2, corte.getCajeroId());
            ps.setString(3, corte.getCajeroNombre());
            ps.setTimestamp(4, Timestamp.valueOf(corte.getFechaInicio()));
            ps.setTimestamp(5, Timestamp.valueOf(corte.getFechaCorte()));
            ps.setDouble(6, corte.getTotalEfectivo());
            ps.setDouble(7, corte.getTotalTarjeta());
            ps.setDouble(8, corte.getTotalTransferencia());
            ps.setDouble(9, corte.getTotalConvenio());
            ps.setDouble(10, corte.getTotalGeneral());
            ps.setInt(11, corte.getTotalTransacciones());
            ps.setString(12, corte.getTipoCorte().name());
            ps.setString(13, corte.getFolioCorte());
            ps.setString(14, corte.getObservaciones() != null ? corte.getObservaciones() : "");
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error al guardar corte de caja: " + e.getMessage());
        }
        return -1;
    }

    /** Lista todos los cortes de un estacionamiento ordenados del más reciente */
    public List<CorteCaja> obtenerPorEstacionamiento(int estacionamientoId) {
        List<CorteCaja> lista = new ArrayList<>();
        String sql = "SELECT * FROM cortes_caja WHERE estacionamiento_id = ? " +
                     "ORDER BY fecha_corte DESC";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, estacionamientoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener cortes: " + e.getMessage());
        }
        return lista;
    }

    /** Obtiene el corte más reciente de un estacionamiento */
    public CorteCaja obtenerUltimo(int estacionamientoId) {
        String sql = "SELECT * FROM cortes_caja WHERE estacionamiento_id = ? " +
                     "ORDER BY fecha_corte DESC LIMIT 1";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, estacionamientoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener último corte: " + e.getMessage());
        }
        return null;
    }

    /** Obtiene todos los cortes del sistema (para Admin Global) */
    public List<CorteCaja> obtenerTodos() {
        List<CorteCaja> lista = new ArrayList<>();
        String sql = "SELECT * FROM cortes_caja ORDER BY fecha_corte DESC";
        try (Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.err.println("Error al obtener todos los cortes: " + e.getMessage());
        }
        return lista;
    }

    private CorteCaja mapear(ResultSet rs) throws SQLException {
        CorteCaja c = new CorteCaja();
        c.setId(rs.getInt("id"));
        c.setEstacionamientoId(rs.getInt("estacionamiento_id"));
        c.setCajeroId(rs.getInt("cajero_id"));
        c.setCajeroNombre(rs.getString("cajero_nombre"));
        Timestamp ti = rs.getTimestamp("fecha_inicio");
        if (ti != null) c.setFechaInicio(ti.toLocalDateTime());
        Timestamp tc = rs.getTimestamp("fecha_corte");
        if (tc != null) c.setFechaCorte(tc.toLocalDateTime());
        c.setTotalEfectivo(rs.getDouble("total_efectivo"));
        c.setTotalTarjeta(rs.getDouble("total_tarjeta"));
        c.setTotalTransferencia(rs.getDouble("total_transferencia"));
        c.setTotalConvenio(rs.getDouble("total_convenio"));
        c.setTotalGeneral(rs.getDouble("total_general"));
        c.setTotalTransacciones(rs.getInt("total_transacciones"));
        try {
            c.setTipoCorte(CorteCaja.TipoCorte.valueOf(rs.getString("tipo_corte")));
        } catch (Exception e) {
            c.setTipoCorte(CorteCaja.TipoCorte.PARCIAL);
        }
        c.setFolioCorte(rs.getString("folio_corte"));
        c.setObservaciones(rs.getString("observaciones"));
        return c;
    }
}
