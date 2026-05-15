package com.estacionamiento.dao;

import com.estacionamiento.modelos.Restaurante;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RestauranteDAO {
    private final Connection conexion;

    public RestauranteDAO() {
        this.conexion = ConexionDB.getInstancia().getConexion();
    }

    public boolean crear(Restaurante restaurante) {
        String sql = "INSERT INTO restaurantes (nombre, descripcion, telefono, email, " +
                "comision_porcentaje, estacionamiento_id, activo) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, restaurante.getNombre());
            ps.setString(2, restaurante.getDescripcion());
            ps.setString(3, restaurante.getTelefono());
            ps.setString(4, restaurante.getEmail());
            ps.setDouble(5, restaurante.getComisionPorcentaje());
            ps.setInt(6, restaurante.getEstacionamientoId());
            ps.setBoolean(7, restaurante.isActivo());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) restaurante.setId(keys.getInt(1));
            }
            return true;
        } catch (SQLException e) {
            System.err.println("Error al crear restaurante: " + e.getMessage());
            return false;
        }
    }

    public Restaurante obtenerPorId(int id) {
        String sql = "SELECT * FROM restaurantes WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener restaurante: " + e.getMessage());
        }
        return null;
    }

    public List<Restaurante> obtenerTodos() {
        List<Restaurante> lista = new ArrayList<>();
        String sql = "SELECT * FROM restaurantes ORDER BY nombre";
        try (Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            System.err.println("Error al obtener restaurantes: " + e.getMessage());
        }
        return lista;
    }

    public List<Restaurante> obtenerPorEstacionamiento(int estacionamientoId) {
        List<Restaurante> lista = new ArrayList<>();
        String sql = "SELECT * FROM restaurantes WHERE estacionamiento_id = ? AND activo = true ORDER BY nombre";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, estacionamientoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener restaurantes por estacionamiento: " + e.getMessage());
        }
        return lista;
    }

    public boolean actualizar(Restaurante restaurante) {
        String sql = "UPDATE restaurantes SET nombre = ?, descripcion = ?, telefono = ?, email = ?, " +
                "comision_porcentaje = ?, estacionamiento_id = ?, activo = ? WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, restaurante.getNombre());
            ps.setString(2, restaurante.getDescripcion());
            ps.setString(3, restaurante.getTelefono());
            ps.setString(4, restaurante.getEmail());
            ps.setDouble(5, restaurante.getComisionPorcentaje());
            ps.setInt(6, restaurante.getEstacionamientoId());
            ps.setBoolean(7, restaurante.isActivo());
            ps.setInt(8, restaurante.getId());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al actualizar restaurante: " + e.getMessage());
            return false;
        }
    }

    public boolean desactivar(int id) {
        String sql = "UPDATE restaurantes SET activo = false WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al desactivar restaurante: " + e.getMessage());
            return false;
        }
    }

    private Restaurante mapear(ResultSet rs) throws SQLException {
        Restaurante r = new Restaurante();
        r.setId(rs.getInt("id"));
        r.setNombre(rs.getString("nombre"));
        r.setDescripcion(rs.getString("descripcion"));
        r.setTelefono(rs.getString("telefono"));
        r.setEmail(rs.getString("email"));
        r.setComisionPorcentaje(rs.getDouble("comision_porcentaje"));
        r.setEstacionamientoId(rs.getInt("estacionamiento_id"));
        r.setActivo(rs.getBoolean("activo"));
        return r;
    }
}
