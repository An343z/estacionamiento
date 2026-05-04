package com.estacionamiento.utilidades;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Clase para cargar y gestionar la configuración de la aplicación
 */
public class ConfigManager {
    private static ConfigManager instancia;
    private Properties propiedades;
    private static final String CONFIG_FILE = "src/recursos/config.properties";

    private ConfigManager() {
        propiedades = new Properties();
        cargarConfiguracion();
    }

    /**
     * Obtiene la instancia única (Singleton)
     * @return instancia de ConfigManager
     */
    public static ConfigManager getInstancia() {
        if (instancia == null) {
            instancia = new ConfigManager();
        }
        return instancia;
    }

    /**
     * Carga el archivo de configuración
     */
    private void cargarConfiguracion() {
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            propiedades.load(fis);
            System.out.println("✓ Configuración cargada correctamente");
        } catch (IOException e) {
            System.err.println("✗ No se pudo cargar el archivo de configuración: " + CONFIG_FILE);
            e.printStackTrace();
        }
    }

    /**
     * Obtiene un valor de configuración
     * @param clave clave de la configuración
     * @return valor de la configuración, null si no existe
     */
    public String obtener(String clave) {
        return propiedades.getProperty(clave);
    }

    /**
     * Obtiene un valor de configuración con valor por defecto
     * @param clave clave de la configuración
     * @param valorPorDefecto valor por defecto si no existe
     * @return valor de la configuración o valorPorDefecto
     */
    public String obtener(String clave, String valorPorDefecto) {
        return propiedades.getProperty(clave, valorPorDefecto);
    }

    /**
     * Obtiene un valor numérico
     * @param clave clave de la configuración
     * @return valor numérico
     */
    public double obtenerDouble(String clave) {
        String valor = propiedades.getProperty(clave);
        if (valor != null) {
            try {
                return Double.parseDouble(valor);
            } catch (NumberFormatException e) {
                System.err.println("Error al convertir a double: " + clave);
            }
        }
        return 0.0;
    }

    /**
     * Obtiene un valor entero
     * @param clave clave de la configuración
     * @return valor entero
     */
    public int obtenerInt(String clave) {
        String valor = propiedades.getProperty(clave);
        if (valor != null) {
            try {
                return Integer.parseInt(valor);
            } catch (NumberFormatException e) {
                System.err.println("Error al convertir a int: " + clave);
            }
        }
        return 0;
    }

    /**
     * Obtiene un valor booleano
     * @param clave clave de la configuración
     * @return valor booleano
     */
    public boolean obtenerBoolean(String clave) {
        String valor = propiedades.getProperty(clave);
        return valor != null && valor.equalsIgnoreCase("true");
    }
}
