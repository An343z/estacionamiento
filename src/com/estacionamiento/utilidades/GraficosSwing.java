package com.estacionamiento.utilidades;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Utilidad para crear gráficos simples en Swing
 */
public class GraficoPastel extends JPanel {
    private List<Double> datos;
    private List<String> etiquetas;
    private List<Color> colores;

    public GraficoPastel(List<Double> datos, List<String> etiquetas, List<Color> colores) {
        this.datos = datos;
        this.etiquetas = etiquetas;
        this.colores = colores;
        setPreferredSize(new Dimension(400, 400));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int ancho = getWidth();
        int alto = getHeight();
        int centroX = ancho / 2;
        int centroY = alto / 2;
        int radio = Math.min(ancho, alto) / 3;

        if (datos == null || datos.isEmpty()) {
            g2d.drawString("Sin datos", centroX - 25, centroY);
            return;
        }

        // Calcular total
        double total = datos.stream().mapToDouble(Double::doubleValue).sum();
        if (total == 0) total = 1; // Evitar división por cero

        // Dibujar pastel
        double anguloActual = -90;
        for (int i = 0; i < datos.size(); i++) {
            double valor = datos.get(i);
            double angulo = (valor / total) * 360;
            
            g2d.setColor(colores.get(i % colores.size()));
            g2d.fillArc(centroX - radio, centroY - radio, radio * 2, radio * 2,
                (int) anguloActual, (int) angulo);
            
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawArc(centroX - radio, centroY - radio, radio * 2, radio * 2,
                (int) anguloActual, (int) angulo);

            // Dibujar etiqueta
            if (i < etiquetas.size()) {
                double anguloMedio = anguloActual + angulo / 2;
                int labelX = (int) (centroX + (radio + 30) * Math.cos(Math.toRadians(anguloMedio)));
                int labelY = (int) (centroY + (radio + 30) * Math.sin(Math.toRadians(anguloMedio)));
                String texto = etiquetas.get(i) + ": " + String.format("%.1f%%", (valor / total) * 100);
                g2d.drawString(texto, labelX - 30, labelY);
            }

            anguloActual += angulo;
        }
    }
}

/**
 * Gráfico de barras horizontal
 */
class GraficoBarras extends JPanel {
    private List<Double> datos;
    private List<String> etiquetas;
    private List<Color> colores;

    public GraficoBarras(List<Double> datos, List<String> etiquetas, List<Color> colores) {
        this.datos = datos;
        this.etiquetas = etiquetas;
        this.colores = colores;
        setPreferredSize(new Dimension(500, 300));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (datos == null || datos.isEmpty()) {
            g2d.drawString("Sin datos", 20, 20);
            return;
        }

        int margenIzq = 100;
        int margenDer = 20;
        int margenArriba = 20;
        int margenAbajo = 30;

        int ancho = getWidth() - margenIzq - margenDer;
        int altoDisponible = getHeight() - margenArriba - margenAbajo;
        int altoFila = altoDisponible / datos.size();

        // Encontrar máximo
        double maximo = datos.stream().mapToDouble(Double::doubleValue).max().orElse(1);

        // Dibujar ejes
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(margenIzq, margenArriba, margenIzq, getHeight() - margenAbajo);
        g2d.drawLine(margenIzq, getHeight() - margenAbajo, getWidth() - margenDer, getHeight() - margenAbajo);

        // Dibujar barras
        for (int i = 0; i < datos.size(); i++) {
            int y = margenArriba + (i * altoFila) + altoFila / 2;
            double valor = datos.get(i);
            int anchoBarr = (int) ((valor / maximo) * ancho);

            // Dibujar etiqueta
            g2d.setColor(Color.BLACK);
            String etiqueta = etiquetas.get(i);
            g2d.drawString(etiqueta, 5, y + 5);

            // Dibujar barra
            g2d.setColor(colores.get(i % colores.size()));
            g2d.fillRect(margenIzq, y - altoFila / 4, anchoBarr, altoFila / 2);
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(1));
            g2d.drawRect(margenIzq, y - altoFila / 4, anchoBarr, altoFila / 2);

            // Dibujar valor
            g2d.drawString(String.format("%.0f", valor), margenIzq + anchoBarr + 5, y + 5);
        }
    }
}

/**
 * Gráfico de líneas
 */
class GraficoLineas extends JPanel {
    private List<Double> datos;
    private List<String> etiquetas;
    private Color colorLinea;

    public GraficoLineas(List<Double> datos, List<String> etiquetas, Color colorLinea) {
        this.datos = datos;
        this.etiquetas = etiquetas;
        this.colorLinea = colorLinea;
        setPreferredSize(new Dimension(500, 300));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (datos == null || datos.size() < 2) {
            g2d.drawString("Sin datos", 20, 20);
            return;
        }

        int margenIzq = 50;
        int margenDer = 20;
        int margenArriba = 20;
        int margenAbajo = 30;

        int ancho = getWidth() - margenIzq - margenDer;
        int alto = getHeight() - margenArriba - margenAbajo;

        // Encontrar máximo y mínimo
        double maximo = datos.stream().mapToDouble(Double::doubleValue).max().orElse(1);
        double minimo = datos.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double rango = maximo - minimo;
        if (rango == 0) rango = 1;

        // Dibujar ejes
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(margenIzq, margenArriba, margenIzq, getHeight() - margenAbajo);
        g2d.drawLine(margenIzq, getHeight() - margenAbajo, getWidth() - margenDer, getHeight() - margenAbajo);

        // Calcular puntos
        int[] x = new int[datos.size()];
        int[] y = new int[datos.size()];
        int anchoPunto = ancho / (datos.size() - 1);

        for (int i = 0; i < datos.size(); i++) {
            x[i] = margenIzq + (i * anchoPunto);
            y[i] = getHeight() - margenAbajo - 
                (int) (((datos.get(i) - minimo) / rango) * alto);
        }

        // Dibujar línea
        g2d.setColor(colorLinea);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawPolyline(x, y, datos.size());

        // Dibujar puntos
        for (int i = 0; i < x.length; i++) {
            g2d.fillOval(x[i] - 4, y[i] - 4, 8, 8);
        }
    }
}
