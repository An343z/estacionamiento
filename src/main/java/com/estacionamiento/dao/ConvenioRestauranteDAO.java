package com.estacionamiento.dao;

import com.estacionamiento.modelos.ConvenioRestaurante;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ConvenioRestauranteDAO {
    private final Connection conexion;

    public ConvenioRestauranteDAO() {
        this.conexion = ConexionDB.getInstancia().getConexion();
    }

    public boolean crear(ConvenioRestaurante convenio) {
        String sql = "INSERT INTO convenios_restaurante (restaurante_id, descripcion, fecha_inicio, " +
                "fecha_fin, estado, estacionamiento_id, tipo_cobertura, porcentaje_cobertura, " +
                "monto_maximo, horas_gratis) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, convenio.getRestauranteId());
            ps.setString(2, convenio.getDescripcion());
            setNullableTimestamp(ps, 3, convenio.getFechaInicio());
            setNullableTimestamp(ps, 4, convenio.getFechaFin());
            ps.setString(5, convenio.getEstado() != null ? convenio.getEstado() : "Vigente");
            ps.setInt(6, convenio.getEstacionamientoId());
            ps.setString(7, convenio.getTipoCobertura() != null ? convenio.getTipoCobertura() : "TOTAL");
            ps.setDouble(8, convenio.getPorcentajeCobertura() > 0 ? convenio.getPorcentajeCobertura() : 100.0);
            setNullableDouble(ps, 9, convenio.getMontoMaximo());
            ps.setInt(10, convenio.getHorasGratis());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) convenio.setId(keys.getInt(1));
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Error al crear convenio: " + e.getMessage());
            return false;
        }
    }

    public ConvenioRestaurante obtenerPorId(int id) {
        String sql = "SELECT * FROM convenios_restaurante WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener convenio: " + e.getMessage());
        }
        return null;
    }

    public List<ConvenioRestaurante> obtenerPorEstacionamiento(int estacionamientoId) {
        List<ConvenioRestaurante> lista = new ArrayList<>();
        String sql = "SELECT * FROM convenios_restaurante WHERE estacionamiento_id = ? " +
                "ORDER BY fecha_inicio DESC, id DESC";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, estacionamientoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener convenios: " + e.getMessage());
        }
        return lista;
    }

    public List<ConvenioRestaurante> obtenerPorRestaurante(int restauranteId) {
        List<ConvenioRestaurante> lista = new ArrayList<>();
        String sql = "SELECT * FROM convenios_restaurante WHERE restaurante_id = ? " +
                "ORDER BY fecha_inicio DESC, id DESC";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, restauranteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener convenios por restaurante: " + e.getMessage());
        }
        return lista;
    }

    public ConvenioRestaurante obtenerVigentePorRestaurante(int restauranteId, int estacionamientoId) {
        String sql = "SELECT * FROM convenios_restaurante WHERE restaurante_id = ? " +
                "AND estacionamiento_id = ? AND estado = 'Vigente' " +
                "AND (fecha_inicio IS NULL OR fecha_inicio <= CURRENT_TIMESTAMP) " +
                "AND (fecha_fin IS NULL OR fecha_fin >= CURRENT_TIMESTAMP) " +
                "ORDER BY fecha_inicio DESC, id DESC LIMIT 1";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, restauranteId);
            ps.setInt(2, estacionamientoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener convenio vigente: " + e.getMessage());
        }
        return null;
    }

    public ConvenioRestaurante obtenerVigentePorCliente(int clienteId, int estacionamientoId) {
        String sql = "SELECT c.* FROM clientes_restaurante cr " +
                "JOIN convenios_restaurante c ON c.restaurante_id = cr.restaurante_id " +
                "AND (cr.convenio_id IS NULL OR cr.convenio_id = c.id) " +
                "WHERE cr.cliente_id = ? AND cr.activo = true " +
                "AND c.estacionamiento_id = ? AND c.estado = 'Vigente' " +
                "AND (cr.fecha_fin IS NULL OR cr.fecha_fin >= CURRENT_TIMESTAMP) " +
                "AND (c.fecha_inicio IS NULL OR c.fecha_inicio <= CURRENT_TIMESTAMP) " +
                "AND (c.fecha_fin IS NULL OR c.fecha_fin >= CURRENT_TIMESTAMP) " +
                "ORDER BY c.fecha_inicio DESC, c.id DESC LIMIT 1";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, clienteId);
            ps.setInt(2, estacionamientoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener convenio por cliente: " + e.getMessage());
        }
        return null;
    }

    public ConvenioRestaurante obtenerVigentePorVehiculo(int vehiculoId, int estacionamientoId) {
        String sql = "SELECT c.* FROM vehiculos v " +
                "JOIN clientes_restaurante cr ON cr.cliente_id = v.cliente_id AND cr.activo = true " +
                "JOIN convenios_restaurante c ON c.restaurante_id = cr.restaurante_id " +
                "AND (cr.convenio_id IS NULL OR cr.convenio_id = c.id) " +
                "WHERE v.id = ? AND v.activo = true " +
                "AND c.estacionamiento_id = ? AND c.estado = 'Vigente' " +
                "AND (cr.fecha_fin IS NULL OR cr.fecha_fin >= CURRENT_TIMESTAMP) " +
                "AND (c.fecha_inicio IS NULL OR c.fecha_inicio <= CURRENT_TIMESTAMP) " +
                "AND (c.fecha_fin IS NULL OR c.fecha_fin >= CURRENT_TIMESTAMP) " +
                "ORDER BY c.fecha_inicio DESC, c.id DESC LIMIT 1";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, vehiculoId);
            ps.setInt(2, estacionamientoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener convenio por vehiculo: " + e.getMessage());
        }
        return null;
    }

    public boolean actualizar(ConvenioRestaurante convenio) {
        String sql = "UPDATE convenios_restaurante SET restaurante_id = ?, descripcion = ?, fecha_inicio = ?, " +
                "fecha_fin = ?, estado = ?, estacionamiento_id = ?, tipo_cobertura = ?, " +
                "porcentaje_cobertura = ?, monto_maximo = ?, horas_gratis = ? WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, convenio.getRestauranteId());
            ps.setString(2, convenio.getDescripcion());
            setNullableTimestamp(ps, 3, convenio.getFechaInicio());
            setNullableTimestamp(ps, 4, convenio.getFechaFin());
            ps.setString(5, convenio.getEstado());
            ps.setInt(6, convenio.getEstacionamientoId());
            ps.setString(7, convenio.getTipoCobertura() != null ? convenio.getTipoCobertura() : "TOTAL");
            ps.setDouble(8, convenio.getPorcentajeCobertura() > 0 ? convenio.getPorcentajeCobertura() : 100.0);
            setNullableDouble(ps, 9, convenio.getMontoMaximo());
            ps.setInt(10, convenio.getHorasGratis());
            ps.setInt(11, convenio.getId());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al actualizar convenio: " + e.getMessage());
            return false;
        }
    }

    public boolean cancelar(int id) {
        String sql = "UPDATE convenios_restaurante SET estado = 'Cancelado' WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al cancelar convenio: " + e.getMessage());
            return false;
        }
    }

    private ConvenioRestaurante mapear(ResultSet rs) throws SQLException {
        ConvenioRestaurante c = new ConvenioRestaurante();
        c.setId(rs.getInt("id"));
        c.setRestauranteId(rs.getInt("restaurante_id"));
        c.setDescripcion(rs.getString("descripcion"));
        Timestamp inicio = rs.getTimestamp("fecha_inicio");
        if (inicio != null) c.setFechaInicio(inicio.toLocalDateTime());
        Timestamp fin = rs.getTimestamp("fecha_fin");
        if (fin != null) c.setFechaFin(fin.toLocalDateTime());
        c.setEstado(rs.getString("estado"));
        c.setEstacionamientoId(rs.getInt("estacionamiento_id"));
        c.setTipoCobertura(rs.getString("tipo_cobertura"));
        c.setPorcentajeCobertura(rs.getDouble("porcentaje_cobertura"));
        c.setMontoMaximo(getNullableDouble(rs, "monto_maximo"));
        c.setHorasGratis(rs.getInt("horas_gratis"));
        return c;
    }

    private void setNullableTimestamp(PreparedStatement ps, int index, LocalDateTime value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.TIMESTAMP);
        } else {
            ps.setTimestamp(index, Timestamp.valueOf(value));
        }
    }

    private void setNullableDouble(PreparedStatement ps, int index, Double value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.DECIMAL);
        } else {
            ps.setDouble(index, value);
        }
    }

    private Double getNullableDouble(ResultSet rs, String column) throws SQLException {
        Object value = rs.getObject(column);
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).doubleValue();
        String text = String.valueOf(value);
        return text.isBlank() ? null : Double.parseDouble(text);
    }
}
