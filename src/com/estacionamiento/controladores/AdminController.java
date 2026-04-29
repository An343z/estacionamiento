package com.estacionamiento.controladores;

import com.estacionamiento.dao.*;
import com.estacionamiento.modelos.*;
import java.util.List;

/**
 * Controlador para el Admin Global
 * 
 * Permite gestionar:
 * - Múltiples estacionamientos
 * - Usuarios de cada estacionamiento
 * - Reportes consolidados
 */
public class AdminController {
    private UsuarioDAO usuarioDAO;
    private EstacionamientoDAO estacionamientoDAO;
    private RegistroEntradaSalidaDAO registroDAO;

    public AdminController() {
        this.usuarioDAO = new UsuarioDAO();
        this.estacionamientoDAO = new EstacionamientoDAO();
        this.registroDAO = new RegistroEntradaSalidaDAO();
    }

    // ============ MÉTODOS PARA ESTACIONAMIENTOS ============

    /**
     * Obtiene todos los estacionamientos de la cadena
     * @return lista de estacionamientos
     */
    public List<Estacionamiento> obtenerTodosEstacionamientos() {
        return estacionamientoDAO.obtenerTodos();
    }

    /**
     * Obtiene información de un estacionamiento
     * @param id ID del estacionamiento
     * @return datos del estacionamiento
     */
    public Estacionamiento obtenerEstacionamiento(int id) {
        return estacionamientoDAO.obtenerPorId(id);
    }

    /**
     * Actualiza información de estacionamiento
     * @param estacionamiento datos actualizados
     * @return true si se actualizó correctamente
     */
    public boolean actualizarEstacionamiento(Estacionamiento estacionamiento) {
        return estacionamientoDAO.actualizar(estacionamiento);
    }

    // ============ MÉTODOS PARA USUARIOS ============

    /**
     * Obtiene todos los usuarios de la cadena
     * @return lista de usuarios
     */
    public List<Usuario> obtenerTodosUsuarios() {
        return usuarioDAO.obtenerTodos();
    }

    /**
     * Obtiene usuarios de un estacionamiento específico
     * @param estacionamientoId ID del estacionamiento
     * @return lista de usuarios del estacionamiento
     */
    public List<Usuario> obtenerUsuariosDelEstacionamiento(int estacionamientoId) {
        return usuarioDAO.obtenerPorEstacionamiento(estacionamientoId);
    }

    /**
     * Crea un nuevo usuario asignado a un estacionamiento
     * @param usuario datos del usuario
     * @return true si se creó correctamente
     */
    public boolean crearUsuario(Usuario usuario) {
        // Validar que no sea admin global (solo el sistema puede crearlos)
        if (usuario.getRol() == 1 && usuario.getEstacionamientoId() == null) {
            System.err.println("Error: No se puede crear admin global desde aquí");
            return false;
        }
        
        return usuarioDAO.insertar(usuario);
    }

    /**
     * Actualiza un usuario
     * @param usuario datos actualizados
     * @return true si se actualizó correctamente
     */
    public boolean actualizarUsuario(Usuario usuario) {
        return usuarioDAO.actualizar(usuario);
    }

    /**
     * Elimina un usuario (borrado lógico)
     * @param usuarioId ID del usuario
     * @return true si se eliminó correctamente
     */
    public boolean eliminarUsuario(int usuarioId) {
        return usuarioDAO.eliminar(usuarioId);
    }

    /**
     * Reasigna un usuario a otro estacionamiento
     * @param usuarioId ID del usuario
     * @param nuevoEstacionamientoId ID del nuevo estacionamiento
     * @return true si se reasignó correctamente
     */
    public boolean reasignarUsuario(int usuarioId, int nuevoEstacionamientoId) {
        Usuario usuario = usuarioDAO.obtenerPorId(usuarioId);
        if (usuario == null) {
            return false;
        }
        
        usuario.setEstacionamientoId(nuevoEstacionamientoId);
        return usuarioDAO.actualizar(usuario);
    }

    // ============ MÉTODOS PARA REPORTES CONSOLIDADOS ============

    /**
     * Obtiene ingreso total de todos los estacionamientos
     * @return ingreso total
     */
    public double obtenerIngresoTotalCadena(java.time.LocalDateTime fecha) {
        double total = 0;
        List<Estacionamiento> estacionamientos = obtenerTodosEstacionamientos();
        
        for (Estacionamiento est : estacionamientos) {
            total += registroDAO.obtenerIngresoDelDia(est.getId(), fecha);
        }
        
        return total;
    }

    /**
     * Obtiene ocupación promedio de la cadena
     * @return porcentaje de ocupación
     */
    public double obtenerOcupacionPromedioCadena() {
        List<Estacionamiento> estacionamientos = obtenerTodosEstacionamientos();
        
        if (estacionamientos.isEmpty()) {
            return 0;
        }
        
        double ocupacionTotal = 0;
        int totalCajones = 0;
        
        for (Estacionamiento est : estacionamientos) {
            int ocupados = est.getTotalCajones() - est.getCajonesDisponibles();
            ocupacionTotal += ocupados;
            totalCajones += est.getTotalCajones();
        }
        
        return totalCajones > 0 ? (ocupacionTotal * 100.0 / totalCajones) : 0;
    }

    /**
     * Obtiene capacidad total de la cadena
     * @return total de cajones
     */
    public int obtenerCapacidadTotalCadena() {
        int total = 0;
        List<Estacionamiento> estacionamientos = obtenerTodosEstacionamientos();
        
        for (Estacionamiento est : estacionamientos) {
            total += est.getTotalCajones();
        }
        
        return total;
    }

    /**
     * Obtiene disponibilidad total de la cadena
     * @return total de cajones disponibles
     */
    public int obtenerDisponibilidadTotalCadena() {
        int total = 0;
        List<Estacionamiento> estacionamientos = obtenerTodosEstacionamientos();
        
        for (Estacionamiento est : estacionamientos) {
            total += est.getCajonesDisponibles();
        }
        
        return total;
    }

    /**
     * Obtiene total de usuarios por rol
     * @param rol número del rol
     * @return cantidad de usuarios con ese rol
     */
    public int obtenerTotalUsuariosPorRol(int rol) {
        int total = 0;
        List<Usuario> usuarios = obtenerTodosUsuarios();
        
        for (Usuario usuario : usuarios) {
            if (usuario.getRol() == rol && usuario.isActivo()) {
                total++;
            }
        }
        
        return total;
    }

    /**
     * Obtiene información resumida de cada estacionamiento
     * @return array con información de cada estacionamiento
     */
    public String[] obtenerResumenTodos() {
        List<Estacionamiento> estacionamientos = obtenerTodosEstacionamientos();
        String[] resumen = new String[estacionamientos.size()];
        
        for (int i = 0; i < estacionamientos.size(); i++) {
            Estacionamiento est = estacionamientos.get(i);
            int usuarios = usuarioDAO.obtenerPorEstacionamiento(est.getId()).size();
            
            resumen[i] = String.format("%s - %d cajones - %d disponibles - %d usuarios",
                est.getNombre(),
                est.getTotalCajones(),
                est.getCajonesDisponibles(),
                usuarios
            );
        }
        
        return resumen;
    }

    /**
     * Obtiene estadísticas generales de la cadena
     * @return objeto con estadísticas
     */
    public EstadisticasCadena obtenerEstadisticasCadena() {
        EstadisticasCadena stats = new EstadisticasCadena();
        
        stats.totalEstacionamientos = obtenerTodosEstacionamientos().size();
        stats.totalCajones = obtenerCapacidadTotalCadena();
        stats.cajonesDisponibles = obtenerDisponibilidadTotalCadena();
        stats.cajoneOcupados = stats.totalCajones - stats.cajonesDisponibles;
        stats.ocupacionPorcentaje = obtenerOcupacionPromedioCadena();
        
        stats.totalUsuarios = obtenerTodosUsuarios().size();
        stats.totalAdmins = obtenerTotalUsuariosPorRol(1);
        stats.totalEncargados = obtenerTotalUsuariosPorRol(2);
        stats.totalCajeros = obtenerTotalUsuariosPorRol(3);
        
        return stats;
    }

    /**
     * Clase interna para estadísticas de la cadena
     */
    public static class EstadisticasCadena {
        public int totalEstacionamientos;
        public int totalCajones;
        public int cajonesDisponibles;
        public int cajoneOcupados;
        public double ocupacionPorcentaje;
        public int totalUsuarios;
        public int totalAdmins;
        public int totalEncargados;
        public int totalCajeros;

        @Override
        public String toString() {
            return String.format(
                "Estadísticas de la Cadena:\n" +
                "  Estacionamientos: %d\n" +
                "  Cajones totales: %d\n" +
                "  Disponibles: %d (%d ocupados)\n" +
                "  Ocupación: %.1f%%\n" +
                "  Usuarios: %d (Admin: %d, Encargados: %d, Cajeros: %d)",
                totalEstacionamientos, totalCajones, cajonesDisponibles, cajoneOcupados,
                ocupacionPorcentaje, totalUsuarios, totalAdmins, totalEncargados, totalCajeros
            );
        }
    }
}
