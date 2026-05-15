package com.estacionamiento.dao;

import com.estacionamiento.modelos.ClienteRestaurante;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ClienteRestauranteDAO {
    private final Connection conexion;

    public ClienteRestauranteDAO() {
        this.conexion = ConexionDB.getInstancia().getConexion();
    }

    public boolean asociar(ClienteRestaurante relacion) {
        String sql = "INSERT INTO clientes_restaurante (cliente_id, restaurante_id, convenio_id, " +
                "estacionamiento_id, fecha_registro, fecha_fin, observaciones, activo) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, relacion.getClienteId());
            ps.setInt(2, relacion.getRestauranteId());
            setNullableInt(ps, 3, relacion.getConvenioId());
            setNullableInt(ps, 4, relacion.getEstacionamientoId());
            ps.setTimestamp(5, Timestamp.valueOf(
                    relacion.getFechaRegistro() != null ? relacion.getFechaRegistro() : LocalDateTime.now()));
            setNullableTimestamp(ps, 6, relacion.getFechaFin());
            ps.setString(7, relacion.getObservaciones());
            ps.setBoolean(8, relacion.isActivo());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) relacion.setId(keys.getInt(1));
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Error al asociar cliente con restaurante: " + e.getMessage());
            return false;
        }
    }

    public ClienteRestaurante obtenerPorId(int id) {
        String sql = "SELECT * FROM clientes_restaurante WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener relacion cliente-restaurante: " + e.getMessage());
        }
        return null;
    }

    public List<ClienteRestaurante> obtenerPorRestaurante(int restauranteId) {
        List<ClienteRestaurante> lista = new ArrayList<>();
        String sql = "SELECT * FROM clientes_restaurante WHERE restaurante_id = ? AND activo = true " +
                "ORDER BY fecha_registro DESC";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, restauranteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener clientes por restaurante: " + e.getMessage());
        }
        return lista;
    }

    public List<ClienteRestaurante> obtenerPorCliente(int clienteId) {
        List<ClienteRestaurante> lista = new ArrayList<>();
        String sql = "SELECT * FROM clientes_restaurante WHERE cliente_id = ? AND activo = true " +
                "ORDER BY fecha_registro DESC";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener restaurantes por cliente: " + e.getMessage());
        }
        return lista;
    }

    public ClienteRestaurante obtenerActivo(int clienteId, int restauranteId) {
        String sql = "SELECT * FROM clientes_restaurante WHERE cliente_id = ? AND restaurante_id = ? " +
                "AND activo = true AND (fecha_fin IS NULL OR fecha_fin >= CURRENT_TIMESTAMP) " +
                "ORDER BY fecha_registro DESC LIMIT 1";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, clienteId);
            ps.setInt(2, restauranteId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener relacion activa: " + e.getMessage());
        }
        return null;
    }

    public boolean actualizar(ClienteRestaurante relacion) {
        String sql = "UPDATE clientes_restaurante SET cliente_id = ?, restaurante_id = ?, convenio_id = ?, " +
                "estacionamiento_id = ?, fecha_fin = ?, observaciones = ?, activo = ? WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, relacion.getClienteId());
            ps.setInt(2, relacion.getRestauranteId());
            setNullableInt(ps, 3, relacion.getConvenioId());
            setNullableInt(ps, 4, relacion.getEstacionamientoId());
            setNullableTimestamp(ps, 5, relacion.getFechaFin());
            ps.setString(6, relacion.getObservaciones());
            ps.setBoolean(7, relacion.isActivo());
            ps.setInt(8, relacion.getId());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al actualizar relacion cliente-restaurante: " + e.getMessage());
            return false;
        }
    }

    public boolean desactivar(int id) {
        String sql = "UPDATE clientes_restaurante SET activo = false, fecha_fin = CURRENT_TIMESTAMP WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al desactivar relacion cliente-restaurante: " + e.getMessage());
            return false;
        }
    }

    private ClienteRestaurante mapear(ResultSet rs) throws SQLException {
        ClienteRestaurante cr = new ClienteRestaurante();
        cr.setId(rs.getInt("id"));
        cr.setClienteId(rs.getInt("cliente_id"));
        cr.setRestauranteId(rs.getInt("restaurante_id"));
        cr.setConvenioId(getNullableInt(rs, "convenio_id"));
        cr.setEstacionamientoId(getNullableInt(rs, "estacionamiento_id"));
        Timestamp fechaRegistro = rs.getTimestamp("fecha_registro");
        if (fechaRegistro != null) cr.setFechaRegistro(fechaRegistro.toLocalDateTime());
        Timestamp fechaFin = rs.getTimestamp("fecha_fin");
        if (fechaFin != null) cr.setFechaFin(fechaFin.toLocalDateTime());
        cr.setObservaciones(rs.getString("observaciones"));
        cr.setActivo(rs.getBoolean("activo"));
        return cr;
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
