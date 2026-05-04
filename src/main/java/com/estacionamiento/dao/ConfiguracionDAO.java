package com.estacionamiento.dao;

import com.estacionamiento.modelos.Configuracion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla de Configuracion
 */
public class ConfiguracionDAO {
    private Connection conexion;

    public ConfiguracionDAO() {
        this.conexion = ConexionDB.getInstancia().getConexion();
    }

    public boolean crear(Configuracion configuracion) {
        String sql = "INSERT INTO configuracion (clave, valor, descripcion, estacionamiento_id) " +
                     "VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setString(1, configuracion.getClave());
            pstmt.setString(2, configuracion.getValor());
            pstmt.setString(3, configuracion.getDescripcion());
            if (configuracion.getEstacionamientoId() > 0) {
                pstmt.setInt(4, configuracion.getEstacionamientoId());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al crear configuración: " + e.getMessage());
            return false;
        }
    }

    public Configuracion obtenerPorId(int id) {
        String sql = "SELECT * FROM configuracion WHERE id = ?";

        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener configuración: " + e.getMessage());
        }
        return null;
    }

    public Configuracion obtenerPorClave(String clave) {
        String sql = "SELECT * FROM configuracion WHERE clave = ?";

        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setString(1, clave);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener configuración por clave: " + e.getMessage());
        }
        return null;
    }

    public List<Configuracion> obtenerTodos() {
        List<Configuracion> configuraciones = new ArrayList<>();
        String sql = "SELECT * FROM configuracion ORDER BY clave";

        try (Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                configuraciones.add(mapearResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener configuraciones: " + e.getMessage());
        }
        return configuraciones;
    }

    public Configuracion obtenerPorClaveYEstacionamiento(String clave, Integer estacionamientoId) {
        String sql;
        if (estacionamientoId != null) {
            sql = "SELECT * FROM configuracion WHERE clave = ? AND (estacionamiento_id = ? OR estacionamiento_id IS NULL) ORDER BY estacionamiento_id DESC LIMIT 1";
        } else {
            sql = "SELECT * FROM configuracion WHERE clave = ? AND estacionamiento_id IS NULL";
        }

        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setString(1, clave);
            if (estacionamientoId != null) {
                pstmt.setInt(2, estacionamientoId);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener configuración por clave y estacionamiento: " + e.getMessage());
        }
        return null;
    }

    public List<Configuracion> obtenerPorEstacionamiento(Integer estacionamientoId) {
        List<Configuracion> configuraciones = new ArrayList<>();
        String sql = "SELECT * FROM configuracion WHERE estacionamiento_id = ? OR estacionamiento_id IS NULL ORDER BY clave";

        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, estacionamientoId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    configuraciones.add(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener configuraciones por estacionamiento: " + e.getMessage());
        }
        return configuraciones;
    }

    public boolean actualizar(Configuracion configuracion) {
        String sql = "UPDATE configuracion SET clave = ?, valor = ?, descripcion = ?, estacionamiento_id = ? WHERE id = ?";

        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setString(1, configuracion.getClave());
            pstmt.setString(2, configuracion.getValor());
            pstmt.setString(3, configuracion.getDescripcion());
            if (configuracion.getEstacionamientoId() > 0) {
                pstmt.setInt(4, configuracion.getEstacionamientoId());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }
            pstmt.setInt(5, configuracion.getId());

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al actualizar configuración: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizarValor(String clave, String nuevoValor) {
        String sql = "UPDATE configuracion SET valor = ? WHERE clave = ?";

        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setString(1, nuevoValor);
            pstmt.setString(2, clave);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al actualizar valor de configuración: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM configuracion WHERE id = ?";

        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al eliminar configuración: " + e.getMessage());
            return false;
        }
    }

    private Configuracion mapearResultSet(ResultSet rs) throws SQLException {
        Configuracion configuracion = new Configuracion();
        configuracion.setId(rs.getInt("id"));
        configuracion.setClave(rs.getString("clave"));
        configuracion.setValor(rs.getString("valor"));
        configuracion.setDescripcion(rs.getString("descripcion"));
        int estacionamientoId = rs.getInt("estacionamiento_id");
        if (!rs.wasNull()) {
            configuracion.setEstacionamientoId(estacionamientoId);
        }
        return configuracion;
    }
}