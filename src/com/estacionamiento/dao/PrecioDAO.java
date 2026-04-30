package com.estacionamiento.dao;

import com.estacionamiento.modelos.Precio;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla de Precios
 */
public class PrecioDAO {
    private Connection conexion;

    public PrecioDAO() {
        this.conexion = ConexionDB.getInstancia().getConexion();
    }

    public boolean crear(Precio precio) {
        String sql = "INSERT INTO precios (tipo_vehiculo, precio_hora, precio_media, precio_dia, estacionamiento_id, fecha_actualizacion, activo) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setString(1, precio.getTipoVehiculo());
            pstmt.setDouble(2, precio.getPrecioHora());
            pstmt.setDouble(3, precio.getPrecioMedia());
            pstmt.setDouble(4, precio.getPrecioDia());
            pstmt.setInt(5, precio.getEstacionamientoId());
            pstmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setBoolean(7, precio.isActivo());
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al crear precio: " + e.getMessage());
            return false;
        }
    }

    public Precio obtenerPorId(int id) {
        String sql = "SELECT * FROM precios WHERE id = ?";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener precio: " + e.getMessage());
        }
        return null;
    }

    public Precio obtenerPorTipoVehiculo(String tipoVehiculo, int estacionamientoId) {
        String sql = "SELECT * FROM precios WHERE tipo_vehiculo = ? AND estacionamiento_id = ? AND activo = true";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setString(1, tipoVehiculo);
            pstmt.setInt(2, estacionamientoId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener precio: " + e.getMessage());
        }
        return null;
    }

    public List<Precio> obtenerPorEstacionamiento(int estacionamientoId) {
        List<Precio> precios = new ArrayList<>();
        String sql = "SELECT * FROM precios WHERE estacionamiento_id = ? AND activo = true";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, estacionamientoId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    precios.add(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener precios: " + e.getMessage());
        }
        return precios;
    }

    public boolean actualizar(Precio precio) {
        String sql = "UPDATE precios SET precio_hora = ?, precio_media = ?, precio_dia = ?, fecha_actualizacion = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setDouble(1, precio.getPrecioHora());
            pstmt.setDouble(2, precio.getPrecioMedia());
            pstmt.setDouble(3, precio.getPrecioDia());
            pstmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setInt(5, precio.getId());
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al actualizar precio: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(int id) {
        String sql = "UPDATE precios SET activo = false WHERE id = ?";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al eliminar precio: " + e.getMessage());
            return false;
        }
    }

    private Precio mapearResultSet(ResultSet rs) throws SQLException {
        Precio precio = new Precio();
        precio.setId(rs.getInt("id"));
        precio.setTipoVehiculo(rs.getString("tipo_vehiculo"));
        precio.setPrecioHora(rs.getDouble("precio_hora"));
        precio.setPrecioMedia(rs.getDouble("precio_media"));
        precio.setPrecioDia(rs.getDouble("precio_dia"));
        precio.setEstacionamientoId(rs.getInt("estacionamiento_id"));
        
        Timestamp fechaActualizacion = rs.getTimestamp("fecha_actualizacion");
        if (fechaActualizacion != null) {
            precio.setFechaActualizacion(fechaActualizacion.toLocalDateTime());
        }
        
        precio.setActivo(rs.getBoolean("activo"));
        return precio;
    }
}
