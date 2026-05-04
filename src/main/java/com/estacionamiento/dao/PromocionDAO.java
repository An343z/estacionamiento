package com.estacionamiento.dao;

import com.estacionamiento.modelos.Promocion;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla de Promociones
 */
public class PromocionDAO {
    private Connection conexion;

    public PromocionDAO() {
        this.conexion = ConexionDB.getInstancia().getConexion();
    }

    public boolean crear(Promocion promocion) {
        String sql = "INSERT INTO promociones (nombre, descripcion, descuento_porcentaje, fecha_inicio, fecha_fin, tipo_vehiculo, estacionamiento_id, activa) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setString(1, promocion.getNombre());
            pstmt.setString(2, promocion.getDescripcion());
            pstmt.setDouble(3, promocion.getDescuentoPorcentaje());
            pstmt.setTimestamp(4, Timestamp.valueOf(promocion.getFechaInicio()));
            pstmt.setTimestamp(5, Timestamp.valueOf(promocion.getFechaFin()));
            pstmt.setString(6, promocion.getTipoVehiculo());
            pstmt.setInt(7, promocion.getEstacionamientoId());
            pstmt.setBoolean(8, promocion.isActiva());
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al crear promoción: " + e.getMessage());
            return false;
        }
    }

    public Promocion obtenerPorId(int id) {
        String sql = "SELECT * FROM promociones WHERE id = ?";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener promoción: " + e.getMessage());
        }
        return null;
    }
    public List<Promocion> obtenerTodos() {
        List<Promocion> promociones = new ArrayList<>();
        String sql = "SELECT * FROM promociones ORDER BY fecha_inicio DESC";
        
        try (Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                promociones.add(mapearResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener promociones: " + e.getMessage());
        }
        return promociones;
    }

    public boolean actualizar(Promocion promocion) {
        String sql = "UPDATE promociones SET nombre = ?, descripcion = ?, descuento_porcentaje = ?, " +
                     "fecha_fin = ?, tipo_vehiculo = ?, activa = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setString(1, promocion.getNombre());
            pstmt.setString(2, promocion.getDescripcion());
            pstmt.setDouble(3, promocion.getDescuentoPorcentaje());
            pstmt.setTimestamp(4, Timestamp.valueOf(promocion.getFechaFin()));
            pstmt.setString(5, promocion.getTipoVehiculo());
            pstmt.setBoolean(6, promocion.isActiva());
            pstmt.setInt(7, promocion.getId());
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al actualizar promoción: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(int id) {
        String sql = "UPDATE promociones SET activa = false WHERE id = ?";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al eliminar promoción: " + e.getMessage());
            return false;
        }
    }

    public List<Promocion> obtenerActivas() {
        List<Promocion> promociones = new ArrayList<>();
        String sql = "SELECT * FROM promociones WHERE activa = true ORDER BY fecha_inicio DESC";
        try (Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) { promociones.add(mapearResultSet(rs)); }
        } catch (SQLException e) {
            System.err.println("Error al obtener promociones activas: " + e.getMessage());
        }
        return promociones;
    }

    public List<Promocion> obtenerPorEstacionamiento(int estacionamientoId) {
        List<Promocion> promociones = new ArrayList<>();
        String sql = "SELECT * FROM promociones WHERE estacionamiento_id = ? ORDER BY fecha_inicio DESC";
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, estacionamientoId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) { promociones.add(mapearResultSet(rs)); }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener promociones por estacionamiento: " + e.getMessage());
        }
        return promociones;
    }

    private Promocion mapearResultSet(ResultSet rs) throws SQLException {
        Promocion promocion = new Promocion();
        promocion.setId(rs.getInt("id"));
        promocion.setNombre(rs.getString("nombre"));
        promocion.setDescripcion(rs.getString("descripcion"));
        promocion.setDescuentoPorcentaje(rs.getDouble("descuento_porcentaje"));
        
        Timestamp fechaInicio = rs.getTimestamp("fecha_inicio");
        if (fechaInicio != null) {
            promocion.setFechaInicio(fechaInicio.toLocalDateTime());
        }
        
        Timestamp fechaFin = rs.getTimestamp("fecha_fin");
        if (fechaFin != null) {
            promocion.setFechaFin(fechaFin.toLocalDateTime());
        }
        
        promocion.setTipoVehiculo(rs.getString("tipo_vehiculo"));
        promocion.setEstacionamientoId(rs.getInt("estacionamiento_id"));
        promocion.setActiva(rs.getBoolean("activa"));
        
        return promocion;
    }
}
