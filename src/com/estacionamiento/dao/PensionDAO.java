package com.estacionamiento.dao;

import com.estacionamiento.modelos.Pension;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla de Pensiones
 */
public class PensionDAO {
    private Connection conexion;

    public PensionDAO() {
        this.conexion = ConexionDB.getInstancia().getConexion();
    }

    public boolean insertar(Pension pension) {
        String sql = "INSERT INTO pensiones (cliente_id, vehiculo_id, cajon_id, fecha_inicio, fecha_fin, monto, estado, estacionamiento_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, pension.getClienteId());
            pstmt.setInt(2, pension.getVehiculoId());
            pstmt.setInt(3, pension.getCajonId());
           pstmt.setTimestamp(4, Timestamp.valueOf(pension.getFechaInicio()));
            pstmt.setTimestamp(5, Timestamp.valueOf(pension.getFechaFin()));
            pstmt.setDouble(6, pension.getMonto());
            pstmt.setString(7, pension.getEstado());
            pstmt.setInt(8, pension.getEstacionamientoId());
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al insertar pensión: " + e.getMessage());
            return false;
        }
    }

    public Pension obtenerPorId(int id) {
        String sql = "SELECT * FROM pensiones WHERE id = ?";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener pensión: " + e.getMessage());
        }
        return null;
    }

    public List<Pension> obtenerPorCliente(int clienteId) {
        List<Pension> pensiones = new ArrayList<>();
        String sql = "SELECT * FROM pensiones WHERE cliente_id = ? ORDER BY fecha_inicio DESC";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, clienteId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    pensiones.add(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener pensiones: " + e.getMessage());
        }
        return pensiones;
    }

    public List<Pension> obtenerActivas(int estacionamientoId) {
        List<Pension> pensiones = new ArrayList<>();
        String sql = "SELECT * FROM pensiones WHERE estacionamiento_id = ? AND estado = 'Activa' " +
                     "AND fecha_fin > NOW() ORDER BY fecha_inicio DESC";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, estacionamientoId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    pensiones.add(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener pensiones: " + e.getMessage());
        }
        return pensiones;
    }

    public boolean actualizar(Pension pension) {
        String sql = "UPDATE pensiones SET fecha_fin = ?, monto = ?, estado = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(pension.getFechaFin()));
            pstmt.setDouble(2, pension.getMonto());
            pstmt.setString(3, pension.getEstado());
            pstmt.setInt(4, pension.getId());
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al actualizar pensión: " + e.getMessage());
            return false;
        }
    }

    public List<Pension> obtenerTodos() {
        List<Pension> pensiones = new ArrayList<>();
        String sql = "SELECT * FROM pensiones ORDER BY fecha_inicio DESC";
        
        try (Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                pensiones.add(mapearResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener pensiones: " + e.getMessage());
        }
        return pensiones;
    }

    public boolean cancelarPension(int pensionId) {
        String sql = "UPDATE pensiones SET estado = 'Cancelada' WHERE id = ?";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, pensionId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al cancelar pensión: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM pensiones WHERE id = ?";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al eliminar pensión: " + e.getMessage());
            return false;
        }
    }

    private Pension mapearResultSet(ResultSet rs) throws SQLException {
        Pension pension = new Pension();
        pension.setId(rs.getInt("id"));
        pension.setClienteId(rs.getInt("cliente_id"));
        pension.setVehiculoId(rs.getInt("vehiculo_id"));
        pension.setCajonId(rs.getInt("cajon_id"));
        
        Timestamp fechaInicio = rs.getTimestamp("fecha_inicio");
        if (fechaInicio != null) {
            pension.setFechaInicio(fechaInicio.toLocalDateTime());
        }
        
        Timestamp fechaFin = rs.getTimestamp("fecha_fin");
        if (fechaFin != null) {
            pension.setFechaFin(fechaFin.toLocalDateTime());
        }
        
        pension.setMonto(rs.getDouble("monto"));
        pension.setEstado(rs.getString("estado"));
        pension.setEstacionamientoId(rs.getInt("estacionamiento_id"));
        
        return pension;
    }
}
