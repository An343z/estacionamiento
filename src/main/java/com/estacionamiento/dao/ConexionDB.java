package com.estacionamiento.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import com.estacionamiento.utilidades.Logger;

/**
 * Clase para manejar la conexión a la base de datos MySQL
 * Utiliza patrón Singleton para asegurar una sola instancia de conexión
 */
public class ConexionDB {
    private static ConexionDB instancia;
    private Connection conexion;
    
    // Parámetros de conexión
    private static final String URL = "jdbc:mysql://localhost:3306/estacionamiento";
    private static final String USUARIO = "root";
    private static final String CONTRASENA = "";
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    /**
     * Constructor privado para evitar instanciación directa
     */
    private ConexionDB() {
    }

    /**
     * Obtiene la instancia única de ConexionDB (Singleton)
     * @return instancia de ConexionDB
     */
    public static ConexionDB getInstancia() {
        if (instancia == null) {
            instancia = new ConexionDB();
        }
        return instancia;
    }

    /**
     * Establece la conexión con la base de datos
     * @return true si la conexión fue exitosa, false en caso contrario
     */
    public boolean conectar() {
        try {
            // Cargar el driver de MySQL
            Class.forName(DRIVER);
            
            // Establecer conexión
            conexion = DriverManager.getConnection(URL, USUARIO, CONTRASENA);
            Logger.info("Conexion a la base de datos establecida correctamente");
            return true;
        } catch (ClassNotFoundException e) {
            Logger.error("Driver MySQL no encontrado", e);
            e.printStackTrace();
            return false;
        } catch (SQLException e) {
          Logger.error("No se pudo conectar a la base de datos", e);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene la conexión activa
     * @return objeto Connection si está conectado, null en caso contrario
     */
    public Connection getConexion() {
        if (conexion == null) {
            conectar();
        }
        try {
            if (conexion.isClosed()) {
                conectar();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return conexion;
    }

    /**
     * Cierra la conexión con la base de datos
     */
    public void desconectar() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
                System.out.println("✓ Conexión cerrada correctamente");
            }
        } catch (SQLException e) {
            System.err.println("✗ Error al cerrar la conexión");
            e.printStackTrace();
        }
    }

    /**
     * Verifica si la conexión está activa
     * @return true si la conexión está activa, false en caso contrario
     */
    public boolean estaConectado() {
        try {
            return conexion != null && !conexion.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Modifica los parámetros de conexión (usar antes de conectar)
     * @param url URL de la base de datos
     * @param usuario nombre de usuario
     * @param contrasena contrasena de acceso
     */
    public static void setParametros(String url, String usuario, String contrasena) {
        // Nota: Para cambiar dinámicamente, necesitarías refactorizar esta clase
        // usando variables no-final. Esta es una alternativa simple pero requiere reiniciar.
        System.out.println("Nota: Modifica las constantes en la clase para cambiar los parámetros de conexión");
    }
}
