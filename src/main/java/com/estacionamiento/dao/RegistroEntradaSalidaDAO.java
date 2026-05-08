package com.estacionamiento.dao;

import com.estacionamiento.modelos.RegistroEntradaSalida;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla de Registros de Entrada/Salida
 */
public class RegistroEntradaSalidaDAO {
    private Connection conexion;

    public RegistroEntradaSalidaDAO() {
        this.conexion = ConexionDB.getInstancia().getConexion();
    }

    public boolean crear(RegistroEntradaSalida registro) {
        String sql = "INSERT INTO registros_entrada_salida (vehiculo_id, cajon_id, fecha_entrada, estado, promocion_aplicada, estacionamiento_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, registro.getVehiculoId());
            pstmt.setInt(2, registro.getCajonId());
            pstmt.setTimestamp(3, Timestamp.valueOf(registro.getFechaEntrada()));
            pstmt.setString(4, registro.getEstado());
            pstmt.setString(5, registro.getPromocionAplicada());
            pstmt.setInt(6, registro.getEstacionamientoId());
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al crear registro: " + e.getMessage());
            return false;
        }
    }

    public RegistroEntradaSalida obtenerPorId(int id) {
        String sql = "SELECT * FROM registros_entrada_salida WHERE id = ?";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener registro: " + e.getMessage());
        }
        return null;
    }

    public RegistroEntradaSalida obtenerActivoDelVehiculo(int vehiculoId) {
        String sql = "SELECT * FROM registros_entrada_salida WHERE vehiculo_id = ? AND estado = 'Activo' LIMIT 1";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, vehiculoId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapearResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener registro: " + e.getMessage());
        }
        return null;
    }

    public boolean finalizarRegistro(int registroId, LocalDateTime fechaSalida, double monto, String promocionAplicada) {
        String sql = "UPDATE registros_entrada_salida SET fecha_salida = ?, monto = ?, promocion_aplicada = ?, estado = 'Finalizado' WHERE id = ?";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(fechaSalida));
            pstmt.setDouble(2, monto);
            pstmt.setString(3, promocionAplicada);
            pstmt.setInt(4, registroId);
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al finalizar registro: " + e.getMessage());
            return false;
        }
    }

    public List<RegistroEntradaSalida> obtenerPorEstacionamiento(int estacionamientoId) {
        List<RegistroEntradaSalida> registros = new ArrayList<>();
        String sql = "SELECT * FROM registros_entrada_salida WHERE estacionamiento_id = ? ORDER BY fecha_entrada DESC";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, estacionamientoId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    registros.add(mapearResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener registros: " + e.getMessage());
        }
        return registros;
    }

    public List<RegistroEntradaSalida> obtenerTodos() {
        List<RegistroEntradaSalida> registros = new ArrayList<>();
        String sql = "SELECT * FROM registros_entrada_salida ORDER BY fecha_entrada DESC";
        
        try (Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                registros.add(mapearResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener registros: " + e.getMessage());
        }
        return registros;
    }

    public boolean actualizar(RegistroEntradaSalida registro) {
        String sql = "UPDATE registros_entrada_salida SET vehiculo_id = ?, cajon_id = ?, fecha_entrada = ?, fecha_salida = ?, monto = ?, promocion_aplicada = ?, estado = ? WHERE id = ?";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, registro.getVehiculoId());
            pstmt.setInt(2, registro.getCajonId());
            pstmt.setTimestamp(3, Timestamp.valueOf(registro.getFechaEntrada()));
            if (registro.getFechaSalida() != null) {
                pstmt.setTimestamp(4, Timestamp.valueOf(registro.getFechaSalida()));
            } else {
                pstmt.setNull(4, java.sql.Types.TIMESTAMP);
            }
            pstmt.setDouble(5, registro.getMonto());
            pstmt.setString(6, registro.getPromocionAplicada());
            pstmt.setString(7, registro.getEstado());
            pstmt.setInt(8, registro.getId());
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al actualizar registro: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM registros_entrada_salida WHERE id = ?";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al eliminar registro: " + e.getMessage());
            return false;
        }
    }

    public double obtenerIngresoDelDia(int estacionamientoId, LocalDateTime fecha) {
        String sql = "SELECT SUM(monto) as total FROM registros_entrada_salida " +
                     "WHERE estacionamiento_id = ? AND DATE(fecha_salida) = DATE(?) AND estado = 'Finalizado'";
        
        try (PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, estacionamientoId);
            pstmt.setTimestamp(2, Timestamp.valueOf(fecha));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener ingreso: " + e.getMessage());
        }
        return 0;
    }

    private RegistroEntradaSalida mapearResultSet(ResultSet rs) throws SQLException {
        RegistroEntradaSalida registro = new RegistroEntradaSalida();
        registro.setId(rs.getInt("id"));
        registro.setVehiculoId(rs.getInt("vehiculo_id"));
        registro.setCajonId(rs.getInt("cajon_id"));
        
        Timestamp fechaEntrada = rs.getTimestamp("fecha_entrada");
        if (fechaEntrada != null) {
            registro.setFechaEntrada(fechaEntrada.toLocalDateTime());
        }
        
        Timestamp fechaSalida = rs.getTimestamp("fecha_salida");
        if (fechaSalida != null) {
            registro.setFechaSalida(fechaSalida.toLocalDateTime());
        }
        
        registro.setMonto(rs.getDouble("monto"));
        registro.setPromocionAplicada(rs.getString("promocion_aplicada"));
        registro.setEstado(rs.getString("estado"));
        registro.setEstacionamientoId(rs.getInt("estacionamiento_id"));
        
        return registro;
    }
}
