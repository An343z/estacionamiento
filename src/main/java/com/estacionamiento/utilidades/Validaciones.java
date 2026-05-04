package com.estacionamiento.utilidades;

import javax.swing.*;
import java.util.regex.Pattern;

/**
 * Clase de utilidades para validaciones
 */
public class Validaciones {
    
    /**
     * Valida un email
     * @param email email a validar
     * @return true si el email es válido, false en caso contrario
     */
    public static boolean validarEmail(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email != null && Pattern.matches(regex, email);
    }

    /**
     * Valida que un campo no esté vacío
     * @param texto texto a validar
     * @return true si no está vacío, false en caso contrario
     */
    public static boolean validarNoVacio(String texto) {
        return texto != null && !texto.trim().isEmpty();
    }

    /**
     * Valida que un número sea positivo
     * @param numero número a validar
     * @return true si es positivo, false en caso contrario
     */
    public static boolean validarNumeroPositivo(double numero) {
        return numero > 0;
    }

    /**
     * Valida que una patente tenga formato válido (simplificado)
     * @param patente patente a validar
     * @return true si la patente es válida, false en caso contrario
     */
    public static boolean validarPatente(String patente) {
        // Acepta patentes con letras y números (simplificado)
        return validarNoVacio(patente) && patente.length() >= 5 && patente.length() <= 8;
    }

    /**
     * Valida que un teléfono tenga un formato válido
     * @param telefono teléfono a validar
     * @return true si el teléfono es válido, false en caso contrario
     */
    public static boolean validarTelefono(String telefono) {
        String regex = "^[0-9\\-\\+\\s]{7,15}$";
        return telefono != null && Pattern.matches(regex, telefono);
    }

    /**
     * Valida que un número de documento sea válido
     * @param documento documento a validar
     * @return true si el documento es válido, false en caso contrario
     */
    public static boolean validarDocumento(String documento) {
        return validarNoVacio(documento) && documento.length() >= 5 && documento.length() <= 15;
    }

    /**
     * Muestra un mensaje de error en un diálogo
     * @param titulo título del diálogo
     * @param mensaje mensaje a mostrar
     */
    public static void mostrarError(String titulo, String mensaje) {
        JOptionPane.showMessageDialog(null, mensaje, titulo, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Muestra un mensaje de éxito en un diálogo
     * @param titulo título del diálogo
     * @param mensaje mensaje a mostrar
     */
    public static void mostrarExito(String titulo, String mensaje) {
        JOptionPane.showMessageDialog(null, mensaje, titulo, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Muestra un mensaje de información en un diálogo
     * @param titulo título del diálogo
     * @param mensaje mensaje a mostrar
     */
    public static void mostrarInfo(String titulo, String mensaje) {
        JOptionPane.showMessageDialog(null, mensaje, titulo, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Muestra un diálogo de confirmación
     * @param titulo título del diálogo
     * @param mensaje mensaje a mostrar
     * @return true si el usuario confirma, false en caso contrario
     */
    public static boolean confirmar(String titulo, String mensaje) {
        int opcion = JOptionPane.showConfirmDialog(null, mensaje, titulo, JOptionPane.YES_NO_OPTION);
        return opcion == JOptionPane.YES_OPTION;
    }
}
