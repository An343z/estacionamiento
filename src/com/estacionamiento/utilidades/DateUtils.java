package com.estacionamiento.utilidades;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Clase de utilidades para manejo de fechas y tiempos
 */
public class DateUtils {
    
    /**
     * Calcula la diferencia en horas entre dos fechas
     * @param fechaInicio fecha de inicio
     * @param fechaFin fecha de fin
     * @return diferencia en horas
     */
    public static long calcularHoras(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return ChronoUnit.HOURS.between(fechaInicio, fechaFin);
    }

    /**
     * Calcula la diferencia en minutos entre dos fechas
     * @param fechaInicio fecha de inicio
     * @param fechaFin fecha de fin
     * @return diferencia en minutos
     */
    public static long calcularMinutos(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return ChronoUnit.MINUTES.between(fechaInicio, fechaFin);
    }

    /**
     * Calcula la diferencia en días entre dos fechas
     * @param fechaInicio fecha de inicio
     * @param fechaFin fecha de fin
     * @return diferencia en días
     */
    public static long calcularDias(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return ChronoUnit.DAYS.between(fechaInicio, fechaFin);
    }

    /**
     * Obtiene la hora formateada como string
     * @param fechaHora fecha y hora
     * @return string con formato HH:mm:ss
     */
    public static String formatearHora(LocalDateTime fechaHora) {
        return String.format("%02d:%02d:%02d", 
            fechaHora.getHour(), 
            fechaHora.getMinute(), 
            fechaHora.getSecond());
    }

    /**
     * Obtiene la fecha formateada como string
     * @param fechaHora fecha y hora
     * @return string con formato dd/MM/yyyy
     */
    public static String formatearFecha(LocalDateTime fechaHora) {
        return String.format("%02d/%02d/%04d", 
            fechaHora.getDayOfMonth(), 
            fechaHora.getMonthValue(), 
            fechaHora.getYear());
    }

    /**
     * Obtiene la fecha y hora formateada como string
     * @param fechaHora fecha y hora
     * @return string con formato dd/MM/yyyy HH:mm:ss
     */
    public static String formatearFechaHora(LocalDateTime fechaHora) {
        return formatearFecha(fechaHora) + " " + formatearHora(fechaHora);
    }
}
