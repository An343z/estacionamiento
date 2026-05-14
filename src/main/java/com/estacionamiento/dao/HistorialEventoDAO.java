package com.estacionamiento.dao;

import com.estacionamiento.modelos.HistorialEvento;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class HistorialEventoDAO {

    public boolean guardarEvento(HistorialEvento evento) throws Exception {
        String sql = "INSERT INTO historial_eventos(cliente_id, vehiculo_id, registro_id, cajon_id, tipo, descripcion, monto, fecha, estacionamiento_id) VALUES(?,?,?,?,?,?,?,?,?)";
        Connection cn = ConexionDB.getInstancia().getConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, evento.getClienteId());
            ps.setInt(2, evento.getVehiculoId());
            if (evento.getRegistroId() != null) ps.setInt(3, evento.getRegistroId()); else ps.setNull(3, java.sql.Types.INTEGER);
            if (evento.getCajonId() != null) ps.setInt(4, evento.getCajonId()); else ps.setNull(4, java.sql.Types.INTEGER);
            ps.setString(5, evento.getTipo());
            ps.setString(6, evento.getDescripcion());
            ps.setDouble(7, evento.getMonto());
            ps.setTimestamp(8, java.sql.Timestamp.valueOf(evento.getFecha()));
            ps.setInt(9, evento.getEstacionamientoId());
            return ps.executeUpdate() > 0;
        }
    }

    public List<HistorialEvento> obtenerEventosPorCliente(int clienteId) throws Exception {
        String sql = "SELECT * FROM historial_eventos WHERE cliente_id = ? ORDER BY fecha DESC";
        Connection cn = ConexionDB.getInstancia().getConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                List<HistorialEvento> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
                return lista;
            }
        }
    }

    public List<HistorialEvento> obtenerEventosPorRegistro(int registroId) throws Exception {
        String sql = "SELECT * FROM historial_eventos WHERE registro_id = ? ORDER BY fecha DESC";
        Connection cn = ConexionDB.getInstancia().getConexion();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, registroId);
            try (ResultSet rs = ps.executeQuery()) {
                List<HistorialEvento> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
                return lista;
            }
        }
    }

    private HistorialEvento mapear(ResultSet rs) throws Exception {
        HistorialEvento evento = new HistorialEvento();
        evento.setId(rs.getInt("id"));
        evento.setClienteId(rs.getInt("cliente_id"));
        evento.setVehiculoId(rs.getInt("vehiculo_id"));
        int registroId = rs.getInt("registro_id");
        if (!rs.wasNull()) evento.setRegistroId(registroId);
        int cajonId = rs.getInt("cajon_id");
        if (!rs.wasNull()) evento.setCajonId(cajonId);
        evento.setTipo(rs.getString("tipo"));
        evento.setDescripcion(rs.getString("descripcion"));
        evento.setMonto(rs.getDouble("monto"));
        evento.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
        evento.setEstacionamientoId(rs.getInt("estacionamiento_id"));
        return evento;
    }
}
