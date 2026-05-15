package com.estacionamiento.utilidades;

import com.estacionamiento.dao.*;
import com.estacionamiento.modelos.*;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.TextAlignment;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

/**
 * Utilidad para generar reportes en PDF
 * Requiere: iText 7.2.5
 */
public class GeneradorPDF {
    private String rutaSalida;
    private EstacionamientoDAO estacionamientoDAO;
    private CajonDAO cajonDAO;
    private RegistroEntradaSalidaDAO registroDAO;
    private PensionDAO pensionDAO;
    private ClienteDAO clienteDAO;
    private VehiculoDAO vehiculoDAO;

    public GeneradorPDF(String rutaSalida) {
        this.rutaSalida = rutaSalida;
        this.estacionamientoDAO = new EstacionamientoDAO();
        this.cajonDAO = new CajonDAO();
        this.registroDAO = new RegistroEntradaSalidaDAO();
        this.pensionDAO = new PensionDAO();
        this.clienteDAO = new ClienteDAO();
        this.vehiculoDAO = new VehiculoDAO();
    }

    /**
     * Genera reporte de ocupación de cajones
     */
    public boolean generarReporteOcupacion(int estacionamientoId) {
        try {
            Estacionamiento estacionamiento = estacionamientoDAO.obtenerPorId(estacionamientoId);
            if (estacionamiento == null) {
                System.err.println("Estacionamiento no encontrado");
                return false;
            }

            String nombre = "Reporte_Ocupacion_" + estacionamiento.getNombre() + "_" + LocalDate.now() + ".pdf";
            String rutaCompleta = rutaSalida + File.separator + nombre;

            File archivo = new File(rutaCompleta);
            archivo.getParentFile().mkdirs();

            PdfWriter writer = new PdfWriter(rutaCompleta);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Título
            document.add(new Paragraph("REPORTE DE OCUPACIÓN")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(16));

            document.add(new Paragraph("Estacionamiento: " + estacionamiento.getNombre())
                    .setBold());
            document.add(new Paragraph("Fecha: " + LocalDate.now()).setFontSize(10));
            document.add(new Paragraph("\n"));

            // Tabla de ocupación
            Table table = new Table(UnitValue.createPercentArray(5)).useAllAvailableWidth();
            table.addHeaderCell("Total Cajones");
            table.addHeaderCell("Disponibles");
            table.addHeaderCell("Ocupados");
            table.addHeaderCell("Mantenimiento");
            table.addHeaderCell("% Ocupación");

            List<Cajon> cajones = cajonDAO.obtenerPorEstacionamiento(estacionamientoId);
            int total = cajones.size();
            int disponibles = (int) cajones.stream().filter(c -> "Disponible".equals(c.getEstado())).count();
            int ocupados = (int) cajones.stream().filter(c -> "Ocupado".equals(c.getEstado())).count();
            int mantenimiento = (int) cajones.stream().filter(c -> "Mantenimiento".equals(c.getEstado())).count();
            double porcentajeOcupacion = total > 0 ? (ocupados * 100.0 / total) : 0;

            table.addCell(String.valueOf(total));
            table.addCell(String.valueOf(disponibles));
            table.addCell(String.valueOf(ocupados));
            table.addCell(String.valueOf(mantenimiento));
            table.addCell(String.format("%.2f%%", porcentajeOcupacion));

            document.add(table);
            document.close();

            System.out.println("PDF generado: " + rutaCompleta);
            return true;
        } catch (Exception ex) {
            System.err.println("Error generando PDF de ocupación: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Genera reporte de ingresos del día
     */
    public boolean generarReporteIngresos(int estacionamientoId, LocalDate fecha) {
        try {
            Estacionamiento estacionamiento = estacionamientoDAO.obtenerPorId(estacionamientoId);
            if (estacionamiento == null) {
                System.err.println("Estacionamiento no encontrado");
                return false;
            }

            String nombre = "Reporte_Ingresos_" + estacionamiento.getNombre() + "_" + fecha + ".pdf";
            String rutaCompleta = rutaSalida + File.separator + nombre;

            File archivo = new File(rutaCompleta);
            archivo.getParentFile().mkdirs();

            PdfWriter writer = new PdfWriter(rutaCompleta);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            document.add(new Paragraph("REPORTE DE INGRESOS")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(16));

            document.add(new Paragraph("Estacionamiento: " + estacionamiento.getNombre()).setBold());
            document.add(new Paragraph("Fecha: " + fecha).setFontSize(10));
            document.add(new Paragraph("\n"));

            List<RegistroEntradaSalida> registros = registroDAO.obtenerPorEstacionamiento(estacionamientoId);
            double totalIngresos = registros.stream()
                    .filter(r -> "Finalizado".equals(r.getEstado()))
                    .mapToDouble(RegistroEntradaSalida::getMonto)
                    .sum();
            int totalRegistros = (int) registros.stream()
                    .filter(r -> "Finalizado".equals(r.getEstado()))
                    .count();

            Table table = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();
            table.addHeaderCell("Concepto");
            table.addHeaderCell("Valor");
            
            table.addCell("Total Registros");
            table.addCell(String.valueOf(totalRegistros));
            table.addCell("Total Ingresos");
            table.addCell(String.format("$%.2f", totalIngresos));
            table.addCell("Promedio por Registro");
            table.addCell(String.format("$%.2f", totalRegistros > 0 ? totalIngresos / totalRegistros : 0));

            document.add(table);
            document.close();

            System.out.println("PDF generado: " + rutaCompleta);
            return true;
        } catch (Exception ex) {
            System.err.println("Error generando PDF de ingresos: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Genera reporte de pensiones activas
     */
    public boolean generarReportePensiones(int estacionamientoId) {
        try {
            Estacionamiento estacionamiento = estacionamientoDAO.obtenerPorId(estacionamientoId);
            if (estacionamiento == null) {
                System.err.println("Estacionamiento no encontrado");
                return false;
            }

            String nombre = "Reporte_Pensiones_" + estacionamiento.getNombre() + "_" + LocalDate.now() + ".pdf";
            String rutaCompleta = rutaSalida + File.separator + nombre;

            File archivo = new File(rutaCompleta);
            archivo.getParentFile().mkdirs();

            PdfWriter writer = new PdfWriter(rutaCompleta);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            document.add(new Paragraph("REPORTE DE PENSIONES")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(16));

            document.add(new Paragraph("Estacionamiento: " + estacionamiento.getNombre()).setBold());
            document.add(new Paragraph("Fecha: " + LocalDate.now()).setFontSize(10));
            document.add(new Paragraph("\n"));

            List<Pension> pensiones = pensionDAO.obtenerActivas(estacionamientoId);
            double ingresoMensual = pensiones.stream().mapToDouble(Pension::getMonto).sum();

            Table table = new Table(UnitValue.createPercentArray(5)).useAllAvailableWidth();
            table.addHeaderCell("Cliente");
            table.addHeaderCell("Vehículo");
            table.addHeaderCell("Cajón");
            table.addHeaderCell("Estado");
            table.addHeaderCell("Monto Mensual");

            for (Pension pension : pensiones) {
                Cliente cliente = clienteDAO.obtenerPorId(pension.getClienteId());
                Vehiculo vehiculo = vehiculoDAO.obtenerPorId(pension.getVehiculoId());
                Cajon cajon = cajonDAO.obtenerPorId(pension.getCajonId());

                table.addCell(cliente != null ? cliente.getNombre() + " " + cliente.getApellido() : "N/A");
                table.addCell(vehiculo != null ? vehiculo.getPatente() : "N/A");
                table.addCell(cajon != null ? String.valueOf(cajon.getNumero()) : "N/A");
                table.addCell(pension.getEstado());
                table.addCell(String.format("$%.2f", pension.getMonto()));
            }

            document.add(table);
            document.add(new Paragraph("\nTOTAL INGRESOS MENSUALES: $" + String.format("%.2f", ingresoMensual))
                    .setBold());

            document.close();

            System.out.println("PDF generado: " + rutaCompleta);
            return true;
        } catch (Exception ex) {
            System.err.println("Error generando PDF de pensiones: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Genera reporte consolidado de cadena completa
     */
    public boolean generarReporteConsolidado(LocalDate fecha) {
        try {
            String nombre = "Reporte_Consolidado_" + fecha + ".pdf";
            String rutaCompleta = rutaSalida + File.separator + nombre;

            File archivo = new File(rutaCompleta);
            archivo.getParentFile().mkdirs();

            PdfWriter writer = new PdfWriter(rutaCompleta);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            document.add(new Paragraph("REPORTE CONSOLIDADO DE CADENA")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(16));

            document.add(new Paragraph("Fecha: " + fecha).setFontSize(10));
            document.add(new Paragraph("\n"));

            List<Estacionamiento> estacionamientos = estacionamientoDAO.obtenerTodos();
            double totalIngresosCadena = 0;
            int totalRegistrosCadena = 0;
            int totalClientesCadena = 0;
            int totalVehiculosCadena = 0;

            Table table = new Table(UnitValue.createPercentArray(5)).useAllAvailableWidth();
            table.addHeaderCell("Estacionamiento");
            table.addHeaderCell("Ingresos");
            table.addHeaderCell("Registros");
            table.addHeaderCell("Clientes");
            table.addHeaderCell("% Ocupación");

            for (Estacionamiento est : estacionamientos) {
                List<RegistroEntradaSalida> registros = registroDAO.obtenerPorEstacionamiento(est.getId());
                double ingresos = registros.stream()
                        .filter(r -> "Finalizado".equals(r.getEstado()))
                        .mapToDouble(RegistroEntradaSalida::getMonto)
                        .sum();
                int registrosFinalizados = (int) registros.stream()
                        .filter(r -> "Finalizado".equals(r.getEstado()))
                        .count();
                int clientes = clienteDAO.obtenerTodos().size();
                
                List<Cajon> cajones = cajonDAO.obtenerPorEstacionamiento(est.getId());
                int ocupados = (int) cajones.stream().filter(c -> "Ocupado".equals(c.getEstado())).count();
                double porcentaje = cajones.size() > 0 ? (ocupados * 100.0 / cajones.size()) : 0;

                table.addCell(est.getNombre());
                table.addCell(String.format("$%.2f", ingresos));
                table.addCell(String.valueOf(registrosFinalizados));
                table.addCell(String.valueOf(clientes));
                table.addCell(String.format("%.2f%%", porcentaje));

                totalIngresosCadena += ingresos;
                totalRegistrosCadena += registrosFinalizados;
                totalClientesCadena = clientes;
            }

            document.add(table);
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("TOTALES CADENA").setBold().setFontSize(12));
            
            Table resumen = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();
            resumen.addCell("Total Ingresos");
            resumen.addCell(String.format("$%.2f", totalIngresosCadena));
            resumen.addCell("Total Registros");
            resumen.addCell(String.valueOf(totalRegistrosCadena));
            resumen.addCell("Total Clientes");
            resumen.addCell(String.valueOf(totalClientesCadena));

            document.add(resumen);
            document.close();

            System.out.println("PDF generado: " + rutaCompleta);
            return true;
        } catch (Exception ex) {
            System.err.println("Error generando PDF consolidado: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    public String getRutaSalida() {
        return rutaSalida;
    }

    public void setRutaSalida(String rutaSalida) {
        this.rutaSalida = rutaSalida;
    }

    /**
     * Sobrecarga que recibe parámetros simples para compatibilidad
     */
    public boolean generarReporteOcupacion(String estacionamiento, int totalCajones, 
                                           int disponibles, int ocupados, int mantenimiento) {
        // Obtener el estacionamiento por nombre para usar el método completo
        List<Estacionamiento> todos = estacionamientoDAO.obtenerTodos();
        for (Estacionamiento est : todos) {
            if (est.getNombre().equals(estacionamiento)) {
                return generarReporteOcupacion(est.getId());
            }
        }
        System.err.println("Estacionamiento no encontrado: " + estacionamiento);
        return false;
    }

    /**
     * Sobrecarga que recibe parámetros simples para compatibilidad
     */
    public boolean generarReporteIngresos(LocalDate fecha, double totalIngresos, int totalRegistros) {
        // Usar datos del reporte general
        List<Estacionamiento> estacionamientos = estacionamientoDAO.obtenerTodos();
        if (!estacionamientos.isEmpty()) {
            return generarReporteIngresos(estacionamientos.get(0).getId(), fecha);
        }
        System.err.println("No hay estacionamientos registrados");
        return false;
    }

    /**
     * Sobrecarga que recibe parámetros simples para compatibilidad
     */
    public boolean generarReportePensiones(int totalActivas, double ingresoMensual) {
        // Usar datos del reporte general
        List<Estacionamiento> estacionamientos = estacionamientoDAO.obtenerTodos();
        if (!estacionamientos.isEmpty()) {
            return generarReportePensiones(estacionamientos.get(0).getId());
        }
        System.err.println("No hay estacionamientos registrados");
        return false;
    }

    /**
     * Sobrecarga que recibe parámetros simples para compatibilidad
     */
    public boolean generarReporteConsolidado(LocalDate fecha, List<String> estacionamientos, 
                                             double totalIngresos, int totalRegistros) {
        return generarReporteConsolidado(fecha);
    }
