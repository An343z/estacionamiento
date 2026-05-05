package com.estacionamiento.utilidades;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

    private static final String ARCHIVO_LOG = "ppark.log";
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void info(String mensaje) {
        escribir("INFO", mensaje);
    }

    public static void error(String mensaje) {
        escribir("ERROR", mensaje);
    }

    public static void error(String mensaje, Exception e) {
        escribir("ERROR", mensaje + " - " + e.getMessage());
    }

    public static void advertencia(String mensaje) {
        escribir("ADVERTENCIA", mensaje);
    }

    private static void escribir(String nivel, String mensaje) {
        String linea = "[" + LocalDateTime.now().format(FORMATO) + "] [" + nivel + "] " + mensaje;
        System.out.println(linea);
        try (PrintWriter pw = new PrintWriter(new FileWriter(ARCHIVO_LOG, true))) {
            pw.println(linea);
        } catch (IOException e) {
            System.err.println("Error al escribir en el log: " + e.getMessage());
        }
    }
}