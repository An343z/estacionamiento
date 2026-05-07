package com.estacionamiento.dao;

import com.estacionamiento.modelos.Cajon;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla de Cajones
 */
public class CajonDAO {
    private Connection conexion;

    public CajonDAO() {
        this.conexion = ConexionDB.getInstancia().getConexion();
    }

    public boolean crear(Cajon cajon) {
        String sql = "INSERT INTO cajones (numero, tipo, estado, estacionamiento_id, activo) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, cajon.getNumero());
            pstmt.setString(2, cajon.getTipo());
            pstmt.setString(3, cajon.getEstado());
            pstmt.setInt(4, cajon.getEstacionamientoId());
            pstmt.setBoolean(5, cajon.isActivo());
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al crear cajón: " + e.getMessage());
            return false;
        }
    }

    public Cajon obtenerPorId(int id) {
        String sql = "SELECT * FROM cajones WHERE id = ?";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener cajón: " + e.getMessage());
        }
        return null;
    }

    public List<Cajon> obtenerPorEstacionamiento(int estacionamientoId) {
        List<Cajon> cajones = new ArrayList<>();
        String sql = "SELECT * FROM cajones WHERE estacionamiento_id = ? AND activo = true ORDER BY numero";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, estacionamientoId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    cajones.add(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener cajones: " + e.getMessage());
        }
        return cajones;
    }

    public List<Cajon> obtenerDisponibles(int estacionamientoId) {
        List<Cajon> cajones = new ArrayList<>();
        String sql = "SELECT * FROM cajones WHERE estacionamiento_id = ? AND estado = 'libre' AND activo = true";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, estacionamientoId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    cajones.add(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener cajones disponibles: " + e.getMessage());
        }
        return cajones;
    }

    public int contarDisponibles(int estacionamientoId) {
        String sql = "SELECT COUNT(*) as total FROM cajones WHERE estacionamiento_id = ? AND estado = 'libre'";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, estacionamientoId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al contar cajones: " + e.getMessage());
        }
        return 0;
    }

    public List<Cajon> obtenerTodos() {
        List<Cajon> cajones = new ArrayList<>();
        String sql = "SELECT * FROM cajones WHERE activo = true ORDER BY numero";
        
        try (Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                cajones.add(mapearResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener cajones: " + e.getMessage());
        }
        return cajones;
    }

    public boolean actualizar(Cajon cajon) {
        String sql = "UPDATE cajones SET numero = ?, tipo = ?, estado = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, cajon.getNumero());
            pstmt.setString(2, cajon.getTipo());
            pstmt.setString(3, cajon.getEstado());
            pstmt.setInt(4, cajon.getId());
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al actualizar cajón: " + e.getMessage());
            return false;
        }
    }

    public boolean cambiarEstado(int cajonId, String nuevoEstado) {
        String sql = "UPDATE cajones SET estado = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setString(1, nuevoEstado);
            pstmt.setInt(2, cajonId);
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al cambiar estado del cajón: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(int id) {
        String sql = "UPDATE cajones SET activo = false WHERE id = ?";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al eliminar cajón: " + e.getMessage());
            return false;
        }
    }

    private Cajon mapearResultSet(ResultSet rs) throws SQLException {
        Cajon cajon = new Cajon();
        cajon.setId(rs.getInt("id"));
        cajon.setNumero(rs.getInt("numero"));
        cajon.setTipo(rs.getString("tipo"));
        cajon.setEstado(rs.getString("estado"));
        cajon.setEstacionamientoId(rs.getInt("estacionamiento_id"));
        cajon.setActivo(rs.getBoolean("activo"));
        return cajon;
    }

    public Cajon obtenerPorNumeroYEstacionamiento(int numero, int estacionamientoId) {
    String sql = "SELECT * FROM cajones WHERE numero = ? AND estacionamiento_id = ? AND activo = true";

    try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
        pstmt.setInt(1, numero);
        pstmt.setInt(2, estacionamientoId);

        try (ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return mapearResultSet(rs);
            }
        }
    } catch (SQLException e) {
        System.err.println("Error al obtener cajón por número: " + e.getMessage());
    }
    return null;

    
}

public List<Cajon> obtenerPorEstado(int estacionamientoId, String estado) {
    List<Cajon> cajones = new ArrayList<>();
    String sql = "SELECT * FROM cajones WHERE estacionamiento_id = ? AND estado = ? AND activo = true";

    try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
        pstmt.setInt(1, estacionamientoId);
        pstmt.setString(2, estado);

        try (ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                cajones.add(mapearResultSet(rs));
            }
        }
    } catch (SQLException e) {
        System.err.println("Error al obtener cajones por estado: " + e.getMessage());
    }

    return cajones;
}


}
