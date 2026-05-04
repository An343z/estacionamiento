package com.estacionamiento.utilidades;

import javax.swing.table.DefaultTableModel;
import java.util.List;

/**
 * Clase para crear y personalizar tablas en Swing
 */
public class TablaPersonalizada {
    
    /**
     * Crea un DefaultTableModel personalizado (no editable)
     * @param columnas nombres de las columnas
     * @param filas datos a mostrar (array de arrays)
     * @return DefaultTableModel no editable
     */
    public static DefaultTableModel crearModelo(String[] columnas, Object[][] filas) {
        DefaultTableModel modelo = new DefaultTableModel(filas, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hacer no editable
            }
        };
        return modelo;
    }

    /**
     * Crea un DefaultTableModel con columnas específicas
     * @param columnas nombres de las columnas
     * @return DefaultTableModel vacío
     */
    public static DefaultTableModel crearModeloVacio(String[] columnas) {
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        return modelo;
    }

    /**
     * Agrega una fila al modelo
     * @param modelo DefaultTableModel
     * @param fila datos de la fila
     */
    public static void agregarFila(DefaultTableModel modelo, Object[] fila) {
        modelo.addRow(fila);
    }

    /**
     * Limpia todas las filas del modelo
     * @param modelo DefaultTableModel
     */
    public static void limpiar(DefaultTableModel modelo) {
        modelo.setRowCount(0);
    }

    /**
     * Obtiene el índice de la fila seleccionada
     * @param indiceSeleccionado índice retornado por getSelectedRow()
     * @return índice si es válido, -1 si no hay selección
     */
    public static int validarSeleccion(int indiceSeleccionado) {
        return indiceSeleccionado;
    }

    /**
     * Obtiene un valor específico de la tabla
     * @param modelo DefaultTableModel
     * @param fila número de fila
     * @param columna número de columna
     * @return valor de la celda
     */
    public static Object obtenerValor(DefaultTableModel modelo, int fila, int columna) {
        if (fila >= 0 && fila < modelo.getRowCount() && columna >= 0 && columna < modelo.getColumnCount()) {
            return modelo.getValueAt(fila, columna);
        }
        return null;
    }

    /**
     * Obtiene todos los valores de una fila
     * @param modelo DefaultTableModel
     * @param fila número de fila
     * @return array con todos los valores de la fila
     */
    public static Object[] obtenerFila(DefaultTableModel modelo, int fila) {
        if (fila >= 0 && fila < modelo.getRowCount()) {
            Object[] datos = new Object[modelo.getColumnCount()];
            for (int i = 0; i < modelo.getColumnCount(); i++) {
                datos[i] = modelo.getValueAt(fila, i);
            }
            return datos;
        }
        return null;
    }

    /**
     * Elimina una fila del modelo
     * @param modelo DefaultTableModel
     * @param fila número de fila
     */
    public static void eliminarFila(DefaultTableModel modelo, int fila) {
        if (fila >= 0 && fila < modelo.getRowCount()) {
            modelo.removeRow(fila);
        }
    }

    /**
     * Obtiene el número de filas
     * @param modelo DefaultTableModel
     * @return número de filas
     */
    public static int obtenerNumeroFilas(DefaultTableModel modelo) {
        return modelo.getRowCount();
    }
}
