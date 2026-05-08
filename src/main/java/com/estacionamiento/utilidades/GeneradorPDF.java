package com.estacionamiento.utilidades;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import java.io.File;
import java.sql.*;
import java.time.LocalDate;

public class GeneradorPDF {

    private String rutaSalida;
    private Connection connection;

    public GeneradorPDF(
            String rutaSalida,
            Connection connection
    ) {

        this.rutaSalida = rutaSalida;
        this.connection = connection;
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
}