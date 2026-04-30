package com.estacionamiento.dao;

import com.estacionamiento.modelos.Usuario;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO (Data Access Object) para la tabla de Usuarios
 * Realiza operaciones CRUD sobre la tabla usuarios
 */
public class UsuarioDAO {
    private Connection conexion;

    public UsuarioDAO() {
        this.conexion = ConexionDB.getInstancia().getConexion();
    }

    /**
     * Crear un nuevo usuario
     * @param usuario objeto Usuario con los datos a crear
     * @return true si se creó correctamente, false en caso contrario
     */
    public boolean crear(Usuario usuario) {
        String sql = "INSERT INTO usuarios (nombre, apellido, email, usuario,contrasena, rol, estacionamiento_id, activo, fecha_creacion) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setString(1, usuario.getNombre());
            pstmt.setString(2, usuario.getApellido());
            pstmt.setString(3, usuario.getEmail());
            pstmt.setString(4, usuario.getUsuario());
            pstmt.setString(5, usuario.getContrasena());
            pstmt.setInt(6, usuario.getRol());
            
            // Manejar estacionamientoId (puede ser NULL)
            if (usuario.getEstacionamientoId() != null) {
                pstmt.setInt(7, usuario.getEstacionamientoId());
            } else {
                pstmt.setNull(7, java.sql.Types.INTEGER);
            }
            
            pstmt.setBoolean(8, usuario.isActivo());
            pstmt.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al insertar usuario: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtener un usuario por ID
     * @param id ID del usuario
     * @return objeto Usuario si existe, null en caso contrario
     */
    public Usuario obtenerPorId(int id) {
        String sql = "SELECT * FROM usuarios WHERE id = ?";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener usuario: " + e.getMessage());
        }
        return null;
    }

    /**
     * Autenticar usuario (login)
     * @param usuario nombre de usuario
     * @param contrasena contrasena del usuario
     * @return objeto Usuario si la autenticación es exitosa, null en caso contrario
     */
    public Usuario autenticar(String usuario, String contrasena) {
        String sql = "SELECT * FROM usuarios WHERE usuario = ? AND contrasena = ? AND activo = true";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setString(1, usuario);
            pstmt.setString(2, contrasena);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en autenticación: " + e.getMessage());
        }
        return null;
    }

    /**
     * Obtener todos los usuarios
     * @return Lista de todos los usuarios
     */
    public List<Usuario> obtenerTodos() {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT u.*, e.nombre as est_nombre FROM usuarios u " +
                     "LEFT JOIN estacionamientos e ON u.estacionamiento_id = e.id " +
                     "ORDER BY u.nombre";
        
        try (Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                usuarios.add(mapearResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener usuarios: " + e.getMessage());
        }
        return usuarios;
    }

    /**
     * Obtener usuarios por estacionamiento
     * @param estacionamientoId ID del estacionamiento
     * @return Lista de usuarios del estacionamiento
     */
    public List<Usuario> obtenerPorEstacionamiento(int estacionamientoId) {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT u.*, e.nombre as est_nombre FROM usuarios u " +
                     "LEFT JOIN estacionamientos e ON u.estacionamiento_id = e.id " +
                     "WHERE u.estacionamiento_id = ? AND u.activo = true " +
                     "ORDER BY u.nombre";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, estacionamientoId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    usuarios.add(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener usuarios: " + e.getMessage());
        }
        return usuarios;
    }

    /**
     * Actualizar un usuario existente
     * @param usuario objeto Usuario con los datos actualizados
     * @return true si se actualizó correctamente, false en caso contrario
     */
    public boolean actualizar(Usuario usuario) {
        String sql = "UPDATE usuarios SET nombre = ?, apellido = ?, email = ?, usuario = ?, " +
                     "rol = ?, estacionamiento_id = ?, activo = ?, fecha_modificacion = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setString(1, usuario.getNombre());
            pstmt.setString(2, usuario.getApellido());
            pstmt.setString(3, usuario.getEmail());
            pstmt.setString(4, usuario.getUsuario());
            pstmt.setInt(5, usuario.getRol());
            
            // Manejar estacionamientoId (puede ser NULL)
            if (usuario.getEstacionamientoId() != null) {
                pstmt.setInt(6, usuario.getEstacionamientoId());
            } else {
                pstmt.setNull(6, java.sql.Types.INTEGER);
            }
            
            pstmt.setBoolean(7, usuario.isActivo());
            pstmt.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setInt(9, usuario.getId());
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al actualizar usuario: " + e.getMessage());
            return false;
        }
    }

    /**
     * Eliminar un usuario (borrado lógico)
     * @param id ID del usuario a eliminar
     * @return true si se eliminó correctamente, false en caso contrario
     */
    public boolean eliminar(int id) {
        String sql = "UPDATE usuarios SET activo = false WHERE id = ?";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al eliminar usuario: " + e.getMessage());
            return false;
        }
    }

    /**
     * Mapea un ResultSet a un objeto Usuario
     * @param rs ResultSet de la consulta
     * @return objeto Usuario
     * @throws SQLException
     */
    private Usuario mapearResultSet(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getInt("id"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setApellido(rs.getString("apellido"));
        usuario.setEmail(rs.getString("email"));
        usuario.setUsuario(rs.getString("usuario"));
        usuario.setContrasena(rs.getString("contrasena"));
        usuario.setRol(rs.getInt("rol"));
        usuario.setActivo(rs.getBoolean("activo"));
        
        // Manejar estacionamiento_id que puede ser NULL
        int estacionamientoId = rs.getInt("estacionamiento_id");
        if (!rs.wasNull()) {
            usuario.setEstacionamientoId(estacionamientoId);
        }
        
        // Obtener nombre del estacionamiento si existe
        try {
            String nombreEst = rs.getString("est_nombre");
            if (nombreEst != null) {
                usuario.setNombreEstacionamiento(nombreEst);
            }
        } catch (SQLException e) {
            // Si no existe la columna, ignorar
        }
        
        Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) {
            usuario.setFechaCreacion(fechaCreacion.toLocalDateTime());
        }
        
        Timestamp fechaModificacion = rs.getTimestamp("fecha_modificacion");
        if (fechaModificacion != null) {
            usuario.setFechaModificacion(fechaModificacion.toLocalDateTime());
        }
        
        return usuario;
    }
}
