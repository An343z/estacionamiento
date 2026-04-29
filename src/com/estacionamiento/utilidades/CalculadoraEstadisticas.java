package com.estacionamiento.utilidades;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

/**
 * Utilidad para cálculos estadísticos y análisis
 */
public class CalculadoraEstadisticas {

    /**
     * Calcula el porcentaje de ocupación
     */
    public static double calcularOcupacion(int ocupados, int total) {
        if (total == 0) return 0;
        return (double) ocupados / total * 100;
    }

    /**
     * Calcula el promedio
     */
    public static double calcularPromedio(List<Double> valores) {
        if (valores == null || valores.isEmpty()) return 0;
        return valores.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    /**
     * Calcula el total
     */
    public static double calcularTotal(List<Double> valores) {
        if (valores == null || valores.isEmpty()) return 0;
        return valores.stream().mapToDouble(Double::doubleValue).sum();
    }

    /**
     * Obtiene el máximo
     */
    public static double obtenerMaximo(List<Double> valores) {
        if (valores == null || valores.isEmpty()) return 0;
        return valores.stream().mapToDouble(Double::doubleValue).max().orElse(0);
    }

    /**
     * Obtiene el mínimo
     */
    public static double obtenerMinimo(List<Double> valores) {
        if (valores == null || valores.isEmpty()) return 0;
        return valores.stream().mapToDouble(Double::doubleValue).min().orElse(0);
    }

    /**
     * Calcula la desviación estándar
     */
    public static double calcularDesviacionEstandar(List<Double> valores) {
        if (valores == null || valores.isEmpty()) return 0;
        
        double promedio = calcularPromedio(valores);
        double sumaCuadrados = 0;
        
        for (Double valor : valores) {
            sumaCuadrados += Math.pow(valor - promedio, 2);
        }
        
        return Math.sqrt(sumaCuadrados / valores.size());
    }

    /**
     * Calcula días transcurridos entre fechas
     */
    public static long calcularDiasTranscurridos(LocalDate fechaInicio, LocalDate fechaFin) {
        if (fechaInicio == null || fechaFin == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(fechaInicio, fechaFin);
    }

    /**
     * Calcula horas transcurridas entre DateTimes
     */
    public static long calcularHorasTranscurridas(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null || fin == null) return 0;
        return java.time.temporal.ChronoUnit.HOURS.between(inicio, fin);
    }

    /**
     * Obtiene el mes anterior
     */
    public static YearMonth obtenerMesAnterior(YearMonth mes) {
        return mes.minusMonths(1);
    }

    /**
     * Calcula tendencia (aumento/disminución porcentual)
     */
    public static double calcularTendencia(double valorActual, double valorAnterior) {
        if (valorAnterior == 0) return 0;
        return ((valorActual - valorAnterior) / valorAnterior) * 100;
    }

    /**
     * Agrupa datos por fecha
     */
    public static Map<LocalDate, List<Double>> agruparPorFecha(Map<LocalDateTime, Double> datos) {
        Map<LocalDate, List<Double>> agrupado = new TreeMap<>();
        
        for (Map.Entry<LocalDateTime, Double> entrada : datos.entrySet()) {
            LocalDate fecha = entrada.getKey().toLocalDate();
            agrupado.computeIfAbsent(fecha, k -> new ArrayList<>()).add(entrada.getValue());
        }
        
        return agrupado;
    }

    /**
     * Calcula percentil
     */
    public static double calcularPercentil(List<Double> valores, int percentil) {
        if (valores == null || valores.isEmpty()) return 0;
        
        List<Double> sorted = new ArrayList<>(valores);
        Collections.sort(sorted);
        
        int indice = (int) Math.ceil(percentil / 100.0 * sorted.size()) - 1;
        indice = Math.max(0, Math.min(indice, sorted.size() - 1));
        
        return sorted.get(indice);
    }
}
