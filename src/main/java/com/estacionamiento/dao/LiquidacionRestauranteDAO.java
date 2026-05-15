package com.estacionamiento.dao;

import com.estacionamiento.modelos.LiquidacionRestaurante;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LiquidacionRestauranteDAO {
    private final Connection conexion;

    public LiquidacionRestauranteDAO() {
        this.conexion = ConexionDB.getInstancia().getConexion();
    }

    public int crear(LiquidacionRestaurante liquidacion) {
        String sql = "INSERT INTO liquidaciones_restaurante (restaurante_id, estacionamiento_id, convenio_id, " +
                "fecha_inicio, fecha_fin, fecha_liquidacion, total, estado, folio_liquidacion, observaciones) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, liquidacion.getRestauranteId());
            ps.setInt(2, liquidacion.getEstacionamientoId());
            setNullableInt(ps, 3, liquidacion.getConvenioId());
            setNullableTimestamp(ps, 4, liquidacion.getFechaInicio());
            setNullableTimestamp(ps, 5, liquidacion.getFechaFin());
            ps.setTimestamp(6, Timestamp.valueOf(
                    liquidacion.getFechaLiquidacion() != null ? liquidacion.getFechaLiquidacion() : LocalDateTime.now()));
            ps.setDouble(7, liquidacion.getTotal());
            ps.setString(8, liquidacion.getEstado() != null ? liquidacion.getEstado() : "PENDIENTE");
            ps.setString(9, liquidacion.getFolioLiquidacion());
            ps.setString(10, liquidacion.getObservaciones());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error al crear liquidacion de restaurante: " + e.getMessage());
        }
        return -1;
    }

    public LiquidacionRestaurante obtenerPorId(int id) {
        String sql = "SELECT * FROM liquidaciones_restaurante WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener liquidacion: " + e.getMessage());
        }
        return null;
    }

    public List<LiquidacionRestaurante> obtenerPorRestaurante(int restauranteId) {
        List<LiquidacionRestaurante> lista = new ArrayList<>();
        String sql = "SELECT * FROM liquidaciones_restaurante WHERE restaurante_id = ? " +
                "ORDER BY fecha_liquidacion DESC";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, restauranteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener liquidaciones por restaurante: " + e.getMessage());
        }
        return lista;
    }

    public List<LiquidacionRestaurante> obtenerPorEstacionamiento(int estacionamientoId) {
        List<LiquidacionRestaurante> lista = new ArrayList<>();
        String sql = "SELECT * FROM liquidaciones_restaurante WHERE estacionamiento_id = ? " +
                "ORDER BY fecha_liquidacion DESC";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, estacionamientoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener liquidaciones por estacionamiento: " + e.getMessage());
        }
        return lista;
    }

    public boolean cambiarEstado(int id, String estado) {
        String sql = "UPDATE liquidaciones_restaurante SET estado = ? WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setInt(2, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al actualizar liquidacion: " + e.getMessage());
            return false;
        }
    }

    private LiquidacionRestaurante mapear(ResultSet rs) throws SQLException {
        LiquidacionRestaurante l = new LiquidacionRestaurante();
        l.setId(rs.getInt("id"));
        l.setRestauranteId(rs.getInt("restaurante_id"));
        l.setEstacionamientoId(rs.getInt("estacionamiento_id"));
        l.setConvenioId(getNullableInt(rs, "convenio_id"));
        Timestamp inicio = rs.getTimestamp("fecha_inicio");
        if (inicio != null) l.setFechaInicio(inicio.toLocalDateTime());
        Timestamp fin = rs.getTimestamp("fecha_fin");
        if (fin != null) l.setFechaFin(fin.toLocalDateTime());
        Timestamp fechaLiquidacion = rs.getTimestamp("fecha_liquidacion");
        if (fechaLiquidacion != null) l.setFechaLiquidacion(fechaLiquidacion.toLocalDateTime());
        l.setTotal(rs.getDouble("total"));
        l.setEstado(rs.getString("estado"));
        l.setFolioLiquidacion(rs.getString("folio_liquidacion"));
        l.setObservaciones(rs.getString("observaciones"));
        return l;
    }

    private void setNullableInt(PreparedStatement ps, int index, Integer value) throws SQLException {
        if (value == null) ps.setNull(index, Types.INTEGER);
        else ps.setInt(index, value);
    }

    private void setNullableTimestamp(PreparedStatement ps, int index, LocalDateTime value) throws SQLException {
        if (value == null) ps.setNull(index, Types.TIMESTAMP);
        else ps.setTimestamp(index, Timestamp.valueOf(value));
    }

    private Integer getNullableInt(ResultSet rs, String column) throws SQLException {
        Object value = rs.getObject(column);
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).intValue();
        String text = String.valueOf(value);
        return text.isBlank() ? null : Integer.parseInt(text);
    }
}
