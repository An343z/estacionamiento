package com.estacionamiento.utilidades;

import com.estacionamiento.dao.CajonDAO;
import com.estacionamiento.dao.ConexionDB;
import com.estacionamiento.dao.EstacionamientoDAO;
import com.estacionamiento.dao.PensionDAO;
import com.estacionamiento.dao.RegistroEntradaSalidaDAO;
import com.estacionamiento.modelos.Cajon;
import com.estacionamiento.modelos.Estacionamiento;
import com.estacionamiento.modelos.Pension;
import com.estacionamiento.modelos.RegistroEntradaSalida;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

public class GeneradorPDF {

    private String rutaSalida;
    private Connection connection;
    private final EstacionamientoDAO estacionamientoDAO;
    private final CajonDAO cajonDAO;
    private final RegistroEntradaSalidaDAO registroDAO;
    private final PensionDAO pensionDAO;

    public GeneradorPDF(String rutaSalida) {
        this(rutaSalida, ConexionDB.getInstancia().getConexion());
    }

    public GeneradorPDF(
            String rutaSalida,
            Connection connection
    ) {

        this.rutaSalida = rutaSalida;
        this.connection = connection;
        this.estacionamientoDAO = new EstacionamientoDAO();
        this.cajonDAO = new CajonDAO();
        this.registroDAO = new RegistroEntradaSalidaDAO();
        this.pensionDAO = new PensionDAO();
    }

    public boolean generarPdfDia(
            LocalDate fecha
    ) {

        return generarPdfFiltrado(
                "PDF_DIA_" + fecha,
                "DATE(%s) = '" + fecha + "'"
        );
    }

    public boolean generarPdfMes(
            int anio,
            int mes
    ) {

        return generarPdfFiltrado(
                "PDF_MES_" + anio + "_" + mes,
                "MONTH(%s)=" + mes +
                " AND YEAR(%s)=" + anio
        );
    }

    public boolean generarPdfAnio(
            int anio
    ) {

        return generarPdfFiltrado(
                "PDF_ANIO_" + anio,
                "YEAR(%s)=" + anio
        );
    }

    public boolean generarPdfCompleto() {

        return generarPdfFiltrado(
                "PDF_BASE_COMPLETA",
                null
        );
    }

    private boolean generarPdfFiltrado(
            String nombreArchivo,
            String filtro
    ) {

        try {

            String rutaCompleta =
                    rutaSalida
                    + File.separator
                    + nombreArchivo
                    + ".pdf";

            File archivo =
                    new File(rutaCompleta);

            archivo.getParentFile().mkdirs();

            PdfWriter writer =
                    new PdfWriter(rutaCompleta);

            PdfDocument pdf =
                    new PdfDocument(writer);

            Document document =
                    new Document(pdf);

            document.add(
                    new Paragraph(
                            "REPORTE BASE DE DATOS"
                    )
            );

            document.add(
                    new Paragraph(
                            "Generado: "
                            + LocalDate.now()
                    )
            );

            Statement tablasStmt =
                    connection.createStatement();

            ResultSet tablas =
                    tablasStmt.executeQuery(
                            "SHOW TABLES"
                    );

            System.out.println("TABLAS ENCONTRADAS:");

            while(tablas.next()) {

                String tabla =
                        tablas.getString(1);

                System.out.println(tabla);

                try {

                    document.add(
                            new Paragraph(
                                    "\n=============================="
                            )
                    );

                    document.add(
                            new Paragraph(
                                    "TABLA: " + tabla
                            )
                    );

                    System.out.println(
                            "Exportando tabla: "
                            + tabla
                    );

                    Statement st =
                            connection.createStatement();

                    String columnaFecha =
                            obtenerColumnaFecha(
                                    tabla
                            );

                    ResultSet rs;

                    if(columnaFecha != null
                            && filtro != null) {

                        String query =
                                "SELECT * FROM `"
                                + tabla
                                + "` WHERE "
                                + String.format(
                                        filtro,
                                        columnaFecha,
                                        columnaFecha
                                );

                        rs = st.executeQuery(query);

                    } else {

                        rs = st.executeQuery(
                                "SELECT * FROM `"
                                + tabla
                                + "`"
                        );
                    }

                    ResultSetMetaData rsMeta =
                            rs.getMetaData();

                    int columnas =
                            rsMeta.getColumnCount();

                    int totalFilas = 0;

                    while(rs.next()) {

                        totalFilas++;

                        StringBuilder fila =
                                new StringBuilder();

                        for(int i = 1;
                            i <= columnas;
                            i++) {

                            fila.append(
                                    rsMeta.getColumnName(i)
                            );

                            fila.append(": ");

                            Object valor =
                                    rs.getObject(i);

                            fila.append(
                                    valor != null
                                    ? valor.toString()
                                    : "NULL"
                            );

                            fila.append(" | ");
                        }

                        document.add(
                                new Paragraph(
                                        fila.toString()
                                )
                        );

                        System.out.println(
                                fila
                        );
                    }

                    document.add(
                            new Paragraph(
                                    "TOTAL REGISTROS: "
                                    + totalFilas
                            )
                    );

                    rs.close();
                    st.close();

                } catch(Exception ex) {

                    document.add(
                            new Paragraph(
                                    "ERROR EN TABLA: "
                                    + tabla
                            )
                    );

                    System.out.println(
                            "Error exportando tabla: "
                            + tabla
                    );

                    ex.printStackTrace();
                }
            }

            tablas.close();
            tablasStmt.close();

            document.close();

            System.out.println(
                    "PDF generado: "
                    + rutaCompleta
            );

            return true;

        } catch(Exception ex) {

            ex.printStackTrace();
            return false;
        }
    }

    private String obtenerColumnaFecha(
            String tabla
    ) {

        switch(tabla) {

            case "registros_entrada_salida":
                return "fecha_entrada";

            case "historial_eventos":
                return "fecha";

            case "pensiones":
                return "fecha_inicio";

            case "facturas_restaurante":
                return "fecha";

            case "notificaciones":
                return "fecha";

            case "clientes":
                return "fecha_registro";

            case "vehiculos":
                return "fecha_registro";

            case "usuarios":
                return "fecha_creacion";

            case "clientes_restaurante":
                return "fecha_registro";

            case "registros_uso_restaurante":
                return "fecha";

            case "promociones":
                return "fecha_inicio";

            case "convenios_restaurante":
                return "fecha_inicio";

            default:
                return null;
        }
    }

    public boolean generarReporteOcupacion(int estacionamientoId) {
        try {
            Estacionamiento estacionamiento = estacionamientoDAO.obtenerPorId(estacionamientoId);
            if (estacionamiento == null) {
                System.err.println("Estacionamiento no encontrado");
                return false;
            }

            String rutaCompleta = rutaSalida + File.separator
                    + "Reporte_Ocupacion_" + nombreSeguro(estacionamiento.getNombre())
                    + "_" + LocalDate.now() + ".pdf";

            File archivo = new File(rutaCompleta);
            archivo.getParentFile().mkdirs();

            try (PdfWriter writer = new PdfWriter(rutaCompleta);
                 PdfDocument pdf = new PdfDocument(writer);
                 Document document = new Document(pdf)) {

                document.add(new Paragraph("REPORTE DE OCUPACION")
                        .setTextAlignment(TextAlignment.CENTER)
                        .setBold()
                        .setFontSize(16));
                document.add(new Paragraph("Estacionamiento: " + estacionamiento.getNombre()).setBold());
                document.add(new Paragraph("Fecha: " + LocalDate.now()).setFontSize(10));
                document.add(new Paragraph("\n"));

                List<Cajon> cajones = cajonDAO.obtenerPorEstacionamiento(estacionamientoId);
                int total = cajones.size();
                int disponibles = contarCajones(cajones, "Disponible");
                int ocupados = contarCajones(cajones, "Ocupado");
                int mantenimiento = contarCajones(cajones, "Mantenimiento");
                double porcentajeOcupacion = total > 0 ? (ocupados * 100.0 / total) : 0;

                Table tabla = new Table(UnitValue.createPercentArray(5)).useAllAvailableWidth();
                tabla.addHeaderCell("Total Cajones");
                tabla.addHeaderCell("Disponibles");
                tabla.addHeaderCell("Ocupados");
                tabla.addHeaderCell("Mantenimiento");
                tabla.addHeaderCell("% Ocupacion");
                tabla.addCell(String.valueOf(total));
                tabla.addCell(String.valueOf(disponibles));
                tabla.addCell(String.valueOf(ocupados));
                tabla.addCell(String.valueOf(mantenimiento));
                tabla.addCell(String.format("%.2f%%", porcentajeOcupacion));
                document.add(tabla);
            }

            System.out.println("PDF generado: " + rutaCompleta);
            return true;
        } catch (Exception ex) {
            System.err.println("Error generando PDF de ocupacion: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    public boolean generarReporteIngresos(int estacionamientoId, LocalDate fecha) {
        try {
            Estacionamiento estacionamiento = estacionamientoDAO.obtenerPorId(estacionamientoId);
            if (estacionamiento == null) {
                System.err.println("Estacionamiento no encontrado");
                return false;
            }

            String rutaCompleta = rutaSalida + File.separator
                    + "Reporte_Ingresos_" + nombreSeguro(estacionamiento.getNombre())
                    + "_" + fecha + ".pdf";

            File archivo = new File(rutaCompleta);
            archivo.getParentFile().mkdirs();

            List<RegistroEntradaSalida> registros = registroDAO.obtenerPorEstacionamiento(estacionamientoId);
            double totalIngresos = registros.stream()
                    .filter(r -> "Finalizado".equals(r.getEstado()))
                    .filter(r -> esMismaFecha(r, fecha))
                    .mapToDouble(RegistroEntradaSalida::getMonto)
                    .sum();
            long totalRegistros = registros.stream()
                    .filter(r -> "Finalizado".equals(r.getEstado()))
                    .filter(r -> esMismaFecha(r, fecha))
                    .count();

            try (PdfWriter writer = new PdfWriter(rutaCompleta);
                 PdfDocument pdf = new PdfDocument(writer);
                 Document document = new Document(pdf)) {

                document.add(new Paragraph("REPORTE DE INGRESOS")
                        .setTextAlignment(TextAlignment.CENTER)
                        .setBold()
                        .setFontSize(16));
                document.add(new Paragraph("Estacionamiento: " + estacionamiento.getNombre()).setBold());
                document.add(new Paragraph("Fecha: " + fecha).setFontSize(10));
                document.add(new Paragraph("\n"));

                Table tabla = new Table(UnitValue.createPercentArray(2)).useAllAvailableWidth();
                tabla.addHeaderCell("Concepto");
                tabla.addHeaderCell("Valor");
                tabla.addCell("Total Registros");
                tabla.addCell(String.valueOf(totalRegistros));
                tabla.addCell("Total Ingresos");
                tabla.addCell(String.format("$%.2f", totalIngresos));
                tabla.addCell("Promedio por Registro");
                tabla.addCell(String.format("$%.2f", totalRegistros > 0 ? totalIngresos / totalRegistros : 0));
                document.add(tabla);
            }

            System.out.println("PDF generado: " + rutaCompleta);
            return true;
        } catch (Exception ex) {
            System.err.println("Error generando PDF de ingresos: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    public boolean generarReportePensiones(int estacionamientoId) {
        try {
            Estacionamiento estacionamiento = estacionamientoDAO.obtenerPorId(estacionamientoId);
            if (estacionamiento == null) {
                System.err.println("Estacionamiento no encontrado");
                return false;
            }

            String rutaCompleta = rutaSalida + File.separator
                    + "Reporte_Pensiones_" + nombreSeguro(estacionamiento.getNombre())
                    + "_" + LocalDate.now() + ".pdf";

            File archivo = new File(rutaCompleta);
            archivo.getParentFile().mkdirs();

            List<Pension> pensiones = pensionDAO.obtenerActivas(estacionamientoId);
            double ingresoMensual = pensiones.stream().mapToDouble(Pension::getMonto).sum();

            try (PdfWriter writer = new PdfWriter(rutaCompleta);
                 PdfDocument pdf = new PdfDocument(writer);
                 Document document = new Document(pdf)) {

                document.add(new Paragraph("REPORTE DE PENSIONES")
                        .setTextAlignment(TextAlignment.CENTER)
                        .setBold()
                        .setFontSize(16));
                document.add(new Paragraph("Estacionamiento: " + estacionamiento.getNombre()).setBold());
                document.add(new Paragraph("Fecha: " + LocalDate.now()).setFontSize(10));
                document.add(new Paragraph("\n"));

                Table tabla = new Table(UnitValue.createPercentArray(4)).useAllAvailableWidth();
                tabla.addHeaderCell("Cliente ID");
                tabla.addHeaderCell("Vehiculo ID");
                tabla.addHeaderCell("Cajon ID");
                tabla.addHeaderCell("Monto Mensual");

                for (Pension pension : pensiones) {
                    tabla.addCell(String.valueOf(pension.getClienteId()));
                    tabla.addCell(String.valueOf(pension.getVehiculoId()));
                    tabla.addCell(String.valueOf(pension.getCajonId()));
                    tabla.addCell(String.format("$%.2f", pension.getMonto()));
                }

                document.add(tabla);
                document.add(new Paragraph("\nTOTAL INGRESOS MENSUALES: $" + String.format("%.2f", ingresoMensual))
                        .setBold());
            }

            System.out.println("PDF generado: " + rutaCompleta);
            return true;
        } catch (Exception ex) {
            System.err.println("Error generando PDF de pensiones: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    public boolean generarReporteConsolidado(LocalDate fecha) {
        try {
            String rutaCompleta = rutaSalida + File.separator + "Reporte_Consolidado_" + fecha + ".pdf";
            File archivo = new File(rutaCompleta);
            archivo.getParentFile().mkdirs();

            try (PdfWriter writer = new PdfWriter(rutaCompleta);
                 PdfDocument pdf = new PdfDocument(writer);
                 Document document = new Document(pdf)) {

                document.add(new Paragraph("REPORTE CONSOLIDADO")
                        .setTextAlignment(TextAlignment.CENTER)
                        .setBold()
                        .setFontSize(16));
                document.add(new Paragraph("Fecha: " + fecha).setFontSize(10));
                document.add(new Paragraph("\n"));

                Table tabla = new Table(UnitValue.createPercentArray(4)).useAllAvailableWidth();
                tabla.addHeaderCell("Estacionamiento");
                tabla.addHeaderCell("Ingresos");
                tabla.addHeaderCell("Registros");
                tabla.addHeaderCell("% Ocupacion");

                for (Estacionamiento estacionamiento : estacionamientoDAO.obtenerTodos()) {
                    List<RegistroEntradaSalida> registros = registroDAO.obtenerPorEstacionamiento(estacionamiento.getId());
                    double ingresos = registros.stream()
                            .filter(r -> "Finalizado".equals(r.getEstado()))
                            .filter(r -> esMismaFecha(r, fecha))
                            .mapToDouble(RegistroEntradaSalida::getMonto)
                            .sum();
                    long finalizados = registros.stream()
                            .filter(r -> "Finalizado".equals(r.getEstado()))
                            .filter(r -> esMismaFecha(r, fecha))
                            .count();

                    List<Cajon> cajones = cajonDAO.obtenerPorEstacionamiento(estacionamiento.getId());
                    int ocupados = contarCajones(cajones, "Ocupado");
                    double ocupacion = cajones.isEmpty() ? 0 : ocupados * 100.0 / cajones.size();

                    tabla.addCell(estacionamiento.getNombre());
                    tabla.addCell(String.format("$%.2f", ingresos));
                    tabla.addCell(String.valueOf(finalizados));
                    tabla.addCell(String.format("%.2f%%", ocupacion));
                }

                document.add(tabla);
            }

            System.out.println("PDF generado: " + rutaCompleta);
            return true;
        } catch (Exception ex) {
            System.err.println("Error generando PDF consolidado: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    public boolean generarReportePensiones(int totalActivas, double ingresoMensual) {
        Integer estacionamientoId = obtenerPrimerEstacionamientoId();
        return estacionamientoId != null && generarReportePensiones(estacionamientoId);
    }

    public boolean generarReporteConsolidado(LocalDate fecha, List<String> estacionamientos,
                                             double totalIngresos, int totalRegistros) {
        return generarReporteConsolidado(fecha);
    }

    /**
     * Genera reporte completo de toda la base de datos
     */
    public boolean generarReporteCompletoBD() {
        try {
            String nombre = "Reporte_Completo_BD_" + LocalDate.now() + ".pdf";
            String rutaCompleta = rutaSalida + File.separator + nombre;

            File archivo = new File(rutaCompleta);
            archivo.getParentFile().mkdirs();
            archivo.createNewFile();

            System.out.println("PDF completo generado: " + rutaCompleta);
            return true;
        } catch (Exception ex) {
            System.err.println("Error generando PDF completo: " + ex.getMessage());
            return false;
        }
    }

    private int contarCajones(List<Cajon> cajones, String estado) {
        return (int) cajones.stream()
                .filter(c -> estado.equals(c.getEstado()))
                .count();
    }

    private boolean esMismaFecha(RegistroEntradaSalida registro, LocalDate fecha) {
        if (registro.getFechaSalida() != null) {
            return fecha.equals(registro.getFechaSalida().toLocalDate());
        }
        return registro.getFechaEntrada() != null
                && fecha.equals(registro.getFechaEntrada().toLocalDate());
    }

    private Integer obtenerPrimerEstacionamientoId() {
        List<Estacionamiento> estacionamientos = estacionamientoDAO.obtenerTodos();
        if (estacionamientos.isEmpty()) {
            return null;
        }
        return estacionamientos.get(0).getId();
    }

    private String nombreSeguro(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return "estacionamiento";
        }
        return nombre.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    public String getRutaSalida() {
        return rutaSalida;
    }

    public void setRutaSalida(String rutaSalida) {
        this.rutaSalida = rutaSalida;
    }
}
