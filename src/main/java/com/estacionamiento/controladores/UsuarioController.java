package com.estacionamiento.controladores;

import com.estacionamiento.dao.UsuarioDAO;
import com.estacionamiento.modelos.Usuario;

/**
 * Controlador para gestionar usuarios
 * 
 * Soporta:
 * - Autenticación de usuarios (admin global, encargado, cajero)
 * - Gestión de usuarios por estacionamiento (solo para admin)
 */
public class UsuarioController {
    private UsuarioDAO usuarioDAO;

    public UsuarioController() {
        this.usuarioDAO = new UsuarioDAO();
    }

    /**
     * Autenticar un usuario
     * @param usuario nombre de usuario
     * @param contrasena contrasena del usuario
     * @return objeto Usuario si la autenticación es exitosa, null en caso contrario
     */
    public Usuario autenticar(String usuario, String contrasena) {
        return usuarioDAO.autenticar(usuario, contrasena);
    }

    /**
     * Crear un nuevo usuario (solo para admin)
     * @param usuario objeto Usuario con los datos
     * @return true si se creó correctamente, false en caso contrario
     */
    public boolean crearUsuario(Usuario usuario) {
        return usuarioDAO.crear(usuario);
    }

    /**
     * Obtener un usuario por ID
     * @param id ID del usuario
     * @return objeto Usuario si existe, null en caso contrario
     */
    public Usuario obtenerUsuario(int id) {
        return usuarioDAO.obtenerPorId(id);
    }

    /**
     * Actualizar un usuario (solo para admin)
     * @param usuario objeto Usuario con los datos actualizados
     * @return true si se actualizó correctamente, false en caso contrario
     */
    public boolean actualizarUsuario(Usuario usuario) {
        return usuarioDAO.actualizar(usuario);
    }

    /**
     * Eliminar un usuario (solo para admin)
     * @param id ID del usuario a eliminar
     * @return true si se eliminó correctamente, false en caso contrario
     */
    public boolean eliminarUsuario(int id) {
        return usuarioDAO.eliminar(id);
    }

    /**
     * Obtener todos los usuarios (solo para admin global)
     * @return lista de usuarios
     */
    public java.util.List<Usuario> obtenerTodos() {
        return usuarioDAO.obtenerTodos();
    }

    /**
     * Obtener usuarios de un estacionamiento específico
     * @param estacionamientoId ID del estacionamiento
     * @return lista de usuarios del estacionamiento
     */
    public java.util.List<Usuario> obtenerUsuariosPorEstacionamiento(int estacionamientoId) {
        return usuarioDAO.obtenerPorEstacionamiento(estacionamientoId);
    }

    /**
     * Valida los datos de un usuario
     * @param usuario objeto Usuario a validar
     * @return mensaje de error, null si es válido
     */
    public String validarUsuario(Usuario usuario) {
        if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty()) {
            return "El nombre es requerido";
        }
        if (usuario.getApellido() == null || usuario.getApellido().trim().isEmpty()) {
            return "El apellido es requerido";
        }
        if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
            return "El email es requerido";
        }
        if (usuario.getUsuario() == null || usuario.getUsuario().trim().isEmpty()) {
            return "El usuario es requerido";
        }
        if (usuario.getContrasena() == null || usuario.getContrasena().trim().isEmpty()) {
            return "La contrasena es requerida";
        }
        if (usuario.getRol() <= 0) {
            return "Debe seleccionar un rol válido";
        }
        
        // Si no es admin global, debe tener estacionamiento asignado
        if (usuario.getRol() != 1 && usuario.getEstacionamientoId() == null) {
            return "Debe asignar un estacionamiento al usuario";
        }
        
        return null; // Válido
    }

    /**
     * Obtiene la descripción de un rol
     * @param rol número del rol
     * @return descripción del rol
     */
    public String obtenerDescripcionRol(int rol) {
        switch (rol) {
            case 1: return "Administrador Global";
            case 2: return "Encargado de Estacionamiento";
            case 3: return "Cajero";
            default: return "Desconocido";
        }
    }
}
