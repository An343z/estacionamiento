package com.estacionamiento.dao;

import com.estacionamiento.modelos.Estacionamiento;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla de Estacionamientos
 */
public class EstacionamientoDAO {
    private Connection conexion;

    public EstacionamientoDAO() {
        this.conexion = ConexionDB.getInstancia().getConexion();
    }

    public boolean crear(Estacionamiento estacionamiento) {
        String sql = "INSERT INTO estacionamientos (nombre, direccion, telefono, email, total_cajones, cajones_disponibles, ciudad, provincia, codigo_postal) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setString(1, estacionamiento.getNombre());
            pstmt.setString(2, estacionamiento.getDireccion());
            pstmt.setString(3, estacionamiento.getTelefono());
            pstmt.setString(4, estacionamiento.getEmail());
            pstmt.setInt(5, estacionamiento.getTotalCajones());
            pstmt.setInt(6, estacionamiento.getCajonesDisponibles());
            pstmt.setString(7, estacionamiento.getCiudad());
            pstmt.setString(8, estacionamiento.getProvincia());
            pstmt.setString(9, estacionamiento.getCodigoPostal());
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al crear estacionamiento: " + e.getMessage());
            return false;
        }
    }

    public Estacionamiento obtenerPorId(int id) {
        String sql = "SELECT * FROM estacionamientos WHERE id = ?";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener estacionamiento: " + e.getMessage());
        }
        return null;
    }

    public List<Estacionamiento> obtenerTodos() {
        List<Estacionamiento> estacionamientos = new ArrayList<>();
        String sql = "SELECT * FROM estacionamientos";
        
        try (Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                estacionamientos.add(mapearResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener estacionamientos: " + e.getMessage());
        }
        return estacionamientos;
    }

    public boolean actualizar(Estacionamiento estacionamiento) {
        String sql = "UPDATE estacionamientos SET nombre = ?, direccion = ?, telefono = ?, email = ?, " +
                     "ciudad = ?, provincia = ?, codigo_postal = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setString(1, estacionamiento.getNombre());
            pstmt.setString(2, estacionamiento.getDireccion());
            pstmt.setString(3, estacionamiento.getTelefono());
            pstmt.setString(4, estacionamiento.getEmail());
            pstmt.setString(5, estacionamiento.getCiudad());
            pstmt.setString(6, estacionamiento.getProvincia());
            pstmt.setString(7, estacionamiento.getCodigoPostal());
            pstmt.setInt(8, estacionamiento.getId());
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al actualizar estacionamiento: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizarCajonesDisponibles(int estacionamientoId, int cantidad) {
        String sql = "UPDATE estacionamientos SET cajones_disponibles = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, cantidad);
            pstmt.setInt(2, estacionamientoId);
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al actualizar cajones disponibles: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM estacionamientos WHERE id = ?";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al eliminar estacionamiento: " + e.getMessage());
            return false;
        }
    }

    private Estacionamiento mapearResultSet(ResultSet rs) throws SQLException {
        Estacionamiento estacionamiento = new Estacionamiento();
        estacionamiento.setId(rs.getInt("id"));
        estacionamiento.setNombre(rs.getString("nombre"));
        estacionamiento.setDireccion(rs.getString("direccion"));
        estacionamiento.setTelefono(rs.getString("telefono"));
        estacionamiento.setEmail(rs.getString("email"));
        estacionamiento.setTotalCajones(rs.getInt("total_cajones"));
        estacionamiento.setCajonesDisponibles(rs.getInt("cajones_disponibles"));
        estacionamiento.setCiudad(rs.getString("ciudad"));
        estacionamiento.setProvincia(rs.getString("provincia"));
        estacionamiento.setCodigoPostal(rs.getString("codigo_postal"));
        return estacionamiento;
    }
}
