package com.estacionamiento.dao;

import com.estacionamiento.modelos.Cliente;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla de Clientes
 */
public class ClienteDAO {
    private Connection conexion;

    public ClienteDAO() {
        this.conexion = ConexionDB.getInstancia().getConexion();
    }

    public boolean insertar(Cliente cliente) {
        String sql = "INSERT INTO clientes (nombre, apellido, email, telefono, numero_documento, tipo_documento, ciudad, fecha_registro, activo) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setString(1, cliente.getNombre());
            pstmt.setString(2, cliente.getApellido());
            pstmt.setString(3, cliente.getEmail());
            pstmt.setString(4, cliente.getTelefono());
            pstmt.setString(5, cliente.getNumeroDocumento());
            pstmt.setString(6, cliente.getTipoDocumento());
            pstmt.setString(7, cliente.getCiudad());
            pstmt.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setBoolean(9, cliente.isActivo());
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al insertar cliente: " + e.getMessage());
            return false;
        }
    }

    public Cliente obtenerPorId(int id) {
        String sql = "SELECT * FROM clientes WHERE id = ?";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener cliente: " + e.getMessage());
        }
        return null;
    }

    public Cliente obtenerPorDocumento(String numeroDocumento) {
        String sql = "SELECT * FROM clientes WHERE numero_documento = ?";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setString(1, numeroDocumento);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener cliente: " + e.getMessage());
        }
        return null;
    }

    public List<Cliente> obtenerTodos() {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT * FROM clientes WHERE activo = true ORDER BY nombre";
        
        try (Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                clientes.add(mapearResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener clientes: " + e.getMessage());
        }
        return clientes;
    }

    public boolean actualizar(Cliente cliente) {
        String sql = "UPDATE clientes SET nombre = ?, apellido = ?, email = ?, telefono = ?, " +
                     "numero_documento = ?, tipo_documento = ?, ciudad = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setString(1, cliente.getNombre());
            pstmt.setString(2, cliente.getApellido());
            pstmt.setString(3, cliente.getEmail());
            pstmt.setString(4, cliente.getTelefono());
            pstmt.setString(5, cliente.getNumeroDocumento());
            pstmt.setString(6, cliente.getTipoDocumento());
            pstmt.setString(7, cliente.getCiudad());
            pstmt.setInt(8, cliente.getId());
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al actualizar cliente: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(int id) {
        String sql = "UPDATE clientes SET activo = false WHERE id = ?";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al eliminar cliente: " + e.getMessage());
            return false;
        }
    }

    private Cliente mapearResultSet(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente();
        cliente.setId(rs.getInt("id"));
        cliente.setNombre(rs.getString("nombre"));
        cliente.setApellido(rs.getString("apellido"));
        cliente.setEmail(rs.getString("email"));
        cliente.setTelefono(rs.getString("telefono"));
        cliente.setNumeroDocumento(rs.getString("numero_documento"));
        cliente.setTipoDocumento(rs.getString("tipo_documento"));
        cliente.setCiudad(rs.getString("ciudad"));
        
        Timestamp fechaRegistro = rs.getTimestamp("fecha_registro");
        if (fechaRegistro != null) {
            cliente.setFechaRegistro(fechaRegistro.toLocalDateTime());
        }
        
        cliente.setActivo(rs.getBoolean("activo"));
        return cliente;
    }
}
