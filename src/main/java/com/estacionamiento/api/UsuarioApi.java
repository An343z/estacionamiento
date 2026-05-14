package com.estacionamiento.api;

import com.estacionamiento.modelos.Usuario;

import java.time.LocalDateTime;

public class UsuarioApi {
    private final ApiClient apiClient = new ApiClient();

    public Usuario autenticar(String usuario, String contrasena) {
        String body = String.format(
                "{\"usuario\":\"%s\",\"contrasena\":\"%s\"}",
                JsonUtils.escape(usuario),
                JsonUtils.escape(contrasena)
        );

        try {
            ApiClient.ApiResponse response = apiClient.postJson("/auth/login.php", body);
            if (!response.isSuccess()) {
                return null;
            }
            return mapearUsuario(response.getBody(), contrasena);
        } catch (Exception e) {
            System.err.println("Error al autenticar por API: " + e.getMessage());
            return null;
        }
    }

    private Usuario mapearUsuario(String json, String contrasena) {
        Integer id = JsonUtils.getInteger(json, "id");
        if (id == null) {
            return null;
        }

        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNombre(JsonUtils.getString(json, "nombre"));
        usuario.setApellido(JsonUtils.getString(json, "apellido"));
        usuario.setEmail(JsonUtils.getString(json, "email"));
        usuario.setUsuario(JsonUtils.getString(json, "usuario"));
        usuario.setContrasena(contrasena);

        Integer rol = JsonUtils.getInteger(json, "rol");
        usuario.setRol(rol == null ? 3 : rol);
        usuario.setEstacionamientoId(JsonUtils.getInteger(json, "estacionamiento_id"));
        usuario.setNombreEstacionamiento(JsonUtils.getString(json, "nombre_estacionamiento"));

        Boolean activo = JsonUtils.getBoolean(json, "activo");
        usuario.setActivo(activo == null || activo);

        setFechaCreacion(usuario, JsonUtils.getString(json, "fecha_creacion"));
        setFechaModificacion(usuario, JsonUtils.getString(json, "fecha_modificacion"));
        return usuario;
    }

    private void setFechaCreacion(Usuario usuario, String value) {
        LocalDateTime fecha = parseFecha(value);
        if (fecha != null) {
            usuario.setFechaCreacion(fecha);
        }
    }

    private void setFechaModificacion(Usuario usuario, String value) {
        LocalDateTime fecha = parseFecha(value);
        if (fecha != null) {
            usuario.setFechaModificacion(fecha);
        }
    }

    private LocalDateTime parseFecha(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value.replace(' ', 'T'));
        } catch (Exception e) {
            return null;
        }
    }
}
