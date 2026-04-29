package com.estacionamiento.dao;

import com.estacionamiento.modelos.Vehiculo;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla de Vehículos
 */
public class VehiculoDAO {
    private Connection conexion;

    public VehiculoDAO() {
        this.conexion = ConexionDB.getInstancia().getConexion();
    }

    public boolean insertar(Vehiculo vehiculo) {
        String sql = "INSERT INTO vehiculos (patente, marca, modelo, color, cliente_id, tipo, fecha_registro, activo) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setString(1, vehiculo.getPatente());
            pstmt.setString(2, vehiculo.getMarca());
            pstmt.setString(3, vehiculo.getModelo());
            pstmt.setString(4, vehiculo.getColor());
            pstmt.setInt(5, vehiculo.getClienteId());
            pstmt.setString(6, vehiculo.getTipo());
            pstmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setBoolean(8, vehiculo.isActivo());
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al insertar vehículo: " + e.getMessage());
            return false;
        }
    }

    public Vehiculo obtenerPorId(int id) {
        String sql = "SELECT * FROM vehiculos WHERE id = ?";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener vehículo: " + e.getMessage());
        }
        return null;
    }

    public Vehiculo obtenerPorPatente(String patente) {
        String sql = "SELECT * FROM vehiculos WHERE patente = ?";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setString(1, patente);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener vehículo: " + e.getMessage());
        }
        return null;
    }

    public List<Vehiculo> obtenerPorCliente(int clienteId) {
        List<Vehiculo> vehiculos = new ArrayList<>();
        String sql = "SELECT * FROM vehiculos WHERE cliente_id = ? AND activo = true";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, clienteId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    vehiculos.add(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener vehículos: " + e.getMessage());
        }
        return vehiculos;
    }

    public List<Vehiculo> obtenerTodos() {
        List<Vehiculo> vehiculos = new ArrayList<>();
        String sql = "SELECT * FROM vehiculos WHERE activo = true ORDER BY patente";
        
        try (Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                vehiculos.add(mapearResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener vehículos: " + e.getMessage());
        }
        return vehiculos;
    }

    public boolean actualizar(Vehiculo vehiculo) {
        String sql = "UPDATE vehiculos SET patente = ?, marca = ?, modelo = ?, color = ?, tipo = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setString(1, vehiculo.getPatente());
            pstmt.setString(2, vehiculo.getMarca());
            pstmt.setString(3, vehiculo.getModelo());
            pstmt.setString(4, vehiculo.getColor());
            pstmt.setString(5, vehiculo.getTipo());
            pstmt.setInt(6, vehiculo.getId());
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al actualizar vehículo: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(int id) {
        String sql = "UPDATE vehiculos SET activo = false WHERE id = ?";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al eliminar vehículo: " + e.getMessage());
            return false;
        }
    }

    private Vehiculo mapearResultSet(ResultSet rs) throws SQLException {
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(rs.getInt("id"));
        vehiculo.setPatente(rs.getString("patente"));
        vehiculo.setMarca(rs.getString("marca"));
        vehiculo.setModelo(rs.getString("modelo"));
        vehiculo.setColor(rs.getString("color"));
        vehiculo.setClienteId(rs.getInt("cliente_id"));
        vehiculo.setTipo(rs.getString("tipo"));
        
        Timestamp fechaRegistro = rs.getTimestamp("fecha_registro");
        if (fechaRegistro != null) {
            vehiculo.setFechaRegistro(fechaRegistro.toLocalDateTime());
        }
        
        vehiculo.setActivo(rs.getBoolean("activo"));
        return vehiculo;
    }
}
