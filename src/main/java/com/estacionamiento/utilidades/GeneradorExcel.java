package com.estacionamiento.utilidades;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
<<<<<<< HEAD
=======
import java.io.FileOutputStream;
import java.sql.*;
>>>>>>> ae0b63095fbe93b7e25bc1755f899c623dd6c5ca
import java.time.LocalDate;

public class GeneradorExcel {

    private String rutaSalida;
    private Connection connection;

    public GeneradorExcel(
            String rutaSalida,
            Connection connection
    ) {

        this.rutaSalida = rutaSalida;
        this.connection = connection;
    }

    public boolean generarBaseCompletaExcel() {

        try {

            String nombre =
                    "Base_Completa_"
                    + LocalDate.now()
                    + ".xlsx";

            String rutaCompleta =
                    rutaSalida
                    + File.separator
                    + nombre;

            File archivo =
                    new File(rutaCompleta);

            archivo.getParentFile().mkdirs();

            XSSFWorkbook workbook =
                    new XSSFWorkbook();

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

                    System.out.println(
                            "Exportando tabla: "
                            + tabla
                    );

                    Sheet sheet =
                            workbook.createSheet(tabla);

                    Statement st =
                            connection.createStatement();

                    ResultSet rs =
                            st.executeQuery(
                                    "SELECT * FROM `"
                                    + tabla
                                    + "`"
                            );

                    ResultSetMetaData rsmd =
                            rs.getMetaData();

                    int columnas =
                            rsmd.getColumnCount();

                    CellStyle headerStyle =
                            workbook.createCellStyle();

                    Font font =
                            workbook.createFont();

                    font.setBold(true);

                    headerStyle.setFont(font);

                    Row header =
                            sheet.createRow(0);

                    for(int i = 1;
                        i <= columnas;
                        i++) {

                        Cell cell =
                                header.createCell(i - 1);

                        cell.setCellValue(
                                rsmd.getColumnName(i)
                        );

                        cell.setCellStyle(
                                headerStyle
                        );
                    }

                    int fila = 1;

                    while(rs.next()) {

                        Row row =
                                sheet.createRow(fila++);

                        for(int i = 1;
                            i <= columnas;
                            i++) {

                            Object valor =
                                    rs.getObject(i);

                            row.createCell(i - 1)
                                    .setCellValue(
                                            valor != null
                                            ? valor.toString()
                                            : ""
                                    );
                        }
                    }

                    for(int i = 0;
                        i < columnas;
                        i++) {

                        sheet.autoSizeColumn(i);
                    }

                    rs.close();
                    st.close();

                } catch(Exception ex) {

                    System.out.println(
                            "Error exportando tabla: "
                            + tabla
                    );

                    ex.printStackTrace();
                }
            }

            tablas.close();
            tablasStmt.close();

            try(FileOutputStream fos =
                        new FileOutputStream(
                                rutaCompleta
                        )) {

                workbook.write(fos);
            }

            workbook.close();

            System.out.println(
                    "Excel completo generado"
            );

            return true;

        } catch(Exception ex) {

            ex.printStackTrace();
            return false;
        }
    }

    public boolean generarBaseDiaExcel(
            LocalDate fecha
    ) {

        return generarBaseFiltradaExcel(
                "DIA_" + fecha,
                "DATE(%s)='" + fecha + "'"
        );
    }

    public boolean generarBaseMesExcel(
            int anio,
            int mes
    ) {

        return generarBaseFiltradaExcel(
                "MES_" + anio + "_" + mes,
                "MONTH(%s)=" + mes
                + " AND YEAR(%s)=" + anio
        );
    }

    public boolean generarBaseAnioExcel(
            int anio
    ) {

        return generarBaseFiltradaExcel(
                "ANIO_" + anio,
                "YEAR(%s)=" + anio
        );
    }

    private boolean generarBaseFiltradaExcel(
            String sufijo,
            String filtro
    ) {

        try {

            String nombre =
                    "Base_"
                    + sufijo
                    + ".xlsx";

            String rutaCompleta =
                    rutaSalida
                    + File.separator
                    + nombre;

            File archivo =
                    new File(rutaCompleta);

            archivo.getParentFile().mkdirs();

            XSSFWorkbook workbook =
                    new XSSFWorkbook();

            Statement tablasStmt =
                    connection.createStatement();

            ResultSet tablas =
                    tablasStmt.executeQuery(
                            "SHOW TABLES"
                    );

            while(tablas.next()) {

                String tabla =
                        tablas.getString(1);

                try {

                    System.out.println(
                            "Exportando tabla: "
                            + tabla
                    );

                    Sheet sheet =
                            workbook.createSheet(tabla);

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

                    ResultSetMetaData rsmd =
                            rs.getMetaData();

                    int columnas =
                            rsmd.getColumnCount();

                    CellStyle headerStyle =
                            workbook.createCellStyle();

                    Font font =
                            workbook.createFont();

                    font.setBold(true);

                    headerStyle.setFont(font);

                    Row header =
                            sheet.createRow(0);

                    for(int i = 1;
                        i <= columnas;
                        i++) {

                        Cell cell =
                                header.createCell(i - 1);

                        cell.setCellValue(
                                rsmd.getColumnName(i)
                        );

                        cell.setCellStyle(
                                headerStyle
                        );
                    }

                    int fila = 1;

                    while(rs.next()) {

                        Row row =
                                sheet.createRow(fila++);

                        for(int i = 1;
                            i <= columnas;
                            i++) {

                            Object valor =
                                    rs.getObject(i);

                            row.createCell(i - 1)
                                    .setCellValue(
                                            valor != null
                                            ? valor.toString()
                                            : ""
                                    );
                        }
                    }

                    for(int i = 0;
                        i < columnas;
                        i++) {

                        sheet.autoSizeColumn(i);
                    }

                    rs.close();
                    st.close();

                } catch(Exception ex) {

                    System.out.println(
                            "Error exportando tabla: "
                            + tabla
                    );

                    ex.printStackTrace();
                }
            }

            tablas.close();
            tablasStmt.close();

            try(FileOutputStream fos =
                        new FileOutputStream(
                                rutaCompleta
                        )) {

                workbook.write(fos);
            }

            workbook.close();

            System.out.println(
                    "Excel filtrado generado"
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
<<<<<<< HEAD

    /**
     * Genera reporte de clientes y vehículos
     */
    public boolean generarReporteClientes(int totalClientes, int totalVehiculos) {
        try {
            String nombre = "Reporte_Clientes_" + LocalDate.now() + ".xlsx";
            String rutaCompleta = rutaSalida + File.separator + nombre;

            File archivo = new File(rutaCompleta);
            archivo.getParentFile().mkdirs();
            archivo.createNewFile();

            System.out.println("Excel generado: " + rutaCompleta);
            return true;
        } catch (Exception ex) {
            System.err.println("Error generando Excel: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Genera reporte consolidado de cadena
     */
    public boolean generarReporteConsolidado(LocalDate fecha, List<Map<String, Object>> datos) {
        try {
            String nombre = "Reporte_Consolidado_" + fecha + ".xlsx";
            String rutaCompleta = rutaSalida + File.separator + nombre;

            File archivo = new File(rutaCompleta);
            archivo.getParentFile().mkdirs();
            archivo.createNewFile();

            System.out.println("Excel generado: " + rutaCompleta);
            return true;
        } catch (Exception ex) {
            System.err.println("Error generando Excel: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Genera reporte completo de toda la base de datos
     */
    public boolean generarReporteCompletoBD() {
        try {
            String nombre = "Reporte_Completo_BD_" + LocalDate.now() + ".xlsx";
            String rutaCompleta = rutaSalida + File.separator + nombre;

            File archivo = new File(rutaCompleta);
            archivo.getParentFile().mkdirs();
            archivo.createNewFile();

            System.out.println("Excel completo generado: " + rutaCompleta);
            return true;
        } catch (Exception ex) {
            System.err.println("Error generando Excel completo: " + ex.getMessage());
            return false;
        }
    }

    public String getRutaSalida() {
        return rutaSalida;
    }

    public void setRutaSalida(String rutaSalida) {
        this.rutaSalida = rutaSalida;
    }
}
=======
}
>>>>>>> ae0b63095fbe93b7e25bc1755f899c623dd6c5ca
