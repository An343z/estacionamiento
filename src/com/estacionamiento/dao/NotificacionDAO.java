package com.estacionamiento.dao;

import com.estacionamiento.modelos.Notificacion;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla de Notificaciones
 */
public class NotificacionDAO {
    private Connection conexion;

    public NotificacionDAO() {
        this.conexion = ConexionDB.getInstancia().getConexion();
    }

    public boolean crear(Notificacion notificacion) {
        String sql = "INSERT INTO notificaciones (usuario_id, titulo, mensaje, tipo, fecha, leida) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, notificacion.getUsuarioId());
            pstmt.setString(2, notificacion.getTitulo());
            pstmt.setString(3, notificacion.getMensaje());
            pstmt.setString(4, notificacion.getTipo());
            pstmt.setTimestamp(5, Timestamp.valueOf(notificacion.getFecha()));
            pstmt.setBoolean(6, notificacion.isLeida());

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al crear notificación: " + e.getMessage());
            return false;
        }
    }

    public Notificacion obtenerPorId(int id) {
        String sql = "SELECT * FROM notificaciones WHERE id = ?";

        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener notificación: " + e.getMessage());
        }
        return null;
    }

    public List<Notificacion> obtenerTodos() {
        List<Notificacion> notificaciones = new ArrayList<>();
        String sql = "SELECT * FROM notificaciones ORDER BY fecha DESC";

        try (Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                notificaciones.add(mapearResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener notificaciones: " + e.getMessage());
        }
        return notificaciones;
    }

    public List<Notificacion> obtenerPorUsuario(int usuarioId) {
        List<Notificacion> notificaciones = new ArrayList<>();
        String sql = "SELECT * FROM notificaciones WHERE usuario_id = ? ORDER BY fecha DESC";

        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, usuarioId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    notificaciones.add(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener notificaciones por usuario: " + e.getMessage());
        }
        return notificaciones;
    }

    public List<Notificacion> obtenerNoLeidas(int usuarioId) {
        List<Notificacion> notificaciones = new ArrayList<>();
        String sql = "SELECT * FROM notificaciones WHERE usuario_id = ? AND leida = false ORDER BY fecha DESC";

        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, usuarioId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    notificaciones.add(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener notificaciones no leídas: " + e.getMessage());
        }
        return notificaciones;
    }

    public boolean actualizar(Notificacion notificacion) {
        String sql = "UPDATE notificaciones SET titulo = ?, mensaje = ?, tipo = ?, leida = ? WHERE id = ?";

        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setString(1, notificacion.getTitulo());
            pstmt.setString(2, notificacion.getMensaje());
            pstmt.setString(3, notificacion.getTipo());
            pstmt.setBoolean(4, notificacion.isLeida());
            pstmt.setInt(5, notificacion.getId());

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al actualizar notificación: " + e.getMessage());
            return false;
        }
    }

    public boolean marcarComoLeida(int id) {
        String sql = "UPDATE notificaciones SET leida = true WHERE id = ?";

        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al marcar notificación como leída: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM notificaciones WHERE id = ?";

        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al eliminar notificación: " + e.getMessage());
            return false;
        }
    }

    private Notificacion mapearResultSet(ResultSet rs) throws SQLException {
        Notificacion notificacion = new Notificacion();
        notificacion.setId(rs.getInt("id"));
        notificacion.setUsuarioId(rs.getInt("usuario_id"));
        notificacion.setTitulo(rs.getString("titulo"));
        notificacion.setMensaje(rs.getString("mensaje"));
        notificacion.setTipo(rs.getString("tipo"));
        notificacion.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
        notificacion.setLeida(rs.getBoolean("leida"));
        return notificacion;
    }
}