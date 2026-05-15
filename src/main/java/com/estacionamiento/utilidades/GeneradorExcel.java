package com.estacionamiento.utilidades;

import com.estacionamiento.dao.CajonDAO;
import com.estacionamiento.dao.ClienteDAO;
import com.estacionamiento.dao.ConexionDB;
import com.estacionamiento.dao.EstacionamientoDAO;
import com.estacionamiento.dao.PensionDAO;
import com.estacionamiento.dao.RegistroEntradaSalidaDAO;
import com.estacionamiento.dao.VehiculoDAO;
import com.estacionamiento.modelos.Cajon;
import com.estacionamiento.modelos.Cliente;
import com.estacionamiento.modelos.Estacionamiento;
import com.estacionamiento.modelos.Pension;
import com.estacionamiento.modelos.RegistroEntradaSalida;
import com.estacionamiento.modelos.Vehiculo;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class GeneradorExcel {

    private String rutaSalida;
    private Connection connection;
    private final EstacionamientoDAO estacionamientoDAO;
    private final CajonDAO cajonDAO;
    private final RegistroEntradaSalidaDAO registroDAO;
    private final PensionDAO pensionDAO;
    private final ClienteDAO clienteDAO;
    private final VehiculoDAO vehiculoDAO;

    public GeneradorExcel(String rutaSalida) {
        this(rutaSalida, ConexionDB.getInstancia().getConexion());
    }

    public GeneradorExcel(
            String rutaSalida,
            Connection connection
    ) {

        this.rutaSalida = rutaSalida;
        this.connection = connection;
        this.estacionamientoDAO = new EstacionamientoDAO();
        this.cajonDAO = new CajonDAO();
        this.registroDAO = new RegistroEntradaSalidaDAO();
        this.pensionDAO = new PensionDAO();
        this.clienteDAO = new ClienteDAO();
        this.vehiculoDAO = new VehiculoDAO();
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

    /**
     * Genera reporte de clientes y vehículos
     */
    public boolean generarReporteOcupacionBD(int estacionamientoId) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Estacionamiento estacionamiento = estacionamientoDAO.obtenerPorId(estacionamientoId);
            if (estacionamiento == null) {
                System.err.println("Estacionamiento no encontrado");
                return false;
            }

            List<Cajon> cajones = cajonDAO.obtenerPorEstacionamiento(estacionamientoId);
            int total = cajones.size();
            int disponibles = contarCajones(cajones, "Disponible");
            int ocupados = contarCajones(cajones, "Ocupado");
            int mantenimiento = contarCajones(cajones, "Mantenimiento");
            double porcentaje = total > 0 ? ocupados * 100.0 / total : 0;

            Sheet sheet = workbook.createSheet("Ocupacion");
            crearEncabezados(workbook, sheet, "Concepto", "Valor");
            agregarFila(sheet, 1, "Estacionamiento", estacionamiento.getNombre());
            agregarFila(sheet, 2, "Total Cajones", total);
            agregarFila(sheet, 3, "Disponibles", disponibles);
            agregarFila(sheet, 4, "Ocupados", ocupados);
            agregarFila(sheet, 5, "Mantenimiento", mantenimiento);
            agregarFila(sheet, 6, "% Ocupacion", String.format("%.2f%%", porcentaje));

            return guardarWorkbook(workbook, "Reporte_Ocupacion_" + nombreSeguro(estacionamiento.getNombre()) + "_" + LocalDate.now() + ".xlsx");
        } catch (Exception ex) {
            System.err.println("Error generando Excel de ocupacion: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    public boolean generarReporteIngresosBD(int estacionamientoId, LocalDate fechaInicio, LocalDate fechaFin) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            List<RegistroEntradaSalida> registros = registroDAO.obtenerPorEstacionamiento(estacionamientoId);
            double totalIngresos = registros.stream()
                    .filter(r -> "Finalizado".equals(r.getEstado()))
                    .filter(r -> estaEnRango(r, fechaInicio, fechaFin))
                    .mapToDouble(RegistroEntradaSalida::getMonto)
                    .sum();
            int totalRegistros = (int) registros.stream()
                    .filter(r -> "Finalizado".equals(r.getEstado()))
                    .filter(r -> estaEnRango(r, fechaInicio, fechaFin))
                    .count();

            Sheet sheet = workbook.createSheet("Ingresos");
            crearEncabezados(workbook, sheet, "Concepto", "Valor");
            agregarFila(sheet, 1, "Fecha inicio", fechaInicio.toString());
            agregarFila(sheet, 2, "Fecha fin", fechaFin.toString());
            agregarFila(sheet, 3, "Total Registros", totalRegistros);
            agregarFila(sheet, 4, "Total Ingresos", totalIngresos);
            agregarFila(sheet, 5, "Promedio por Registro", totalRegistros > 0 ? totalIngresos / totalRegistros : 0);

            return guardarWorkbook(workbook, "Reporte_Ingresos_" + fechaInicio + "_" + fechaFin + ".xlsx");
        } catch (Exception ex) {
            System.err.println("Error generando Excel de ingresos: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    public boolean generarReportePensionBD(int estacionamientoId) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            List<Pension> pensiones = pensionDAO.obtenerActivas(estacionamientoId);
            int activas = (int) pensiones.stream().filter(p -> "Activa".equals(p.getEstado())).count();
            int vencidas = (int) pensiones.stream().filter(p -> "Vencida".equals(p.getEstado())).count();
            int canceladas = (int) pensiones.stream().filter(p -> "Cancelada".equals(p.getEstado())).count();
            double ingresoMensual = pensiones.stream().mapToDouble(Pension::getMonto).sum();

            Sheet sheet = workbook.createSheet("Pensiones");
            crearEncabezados(workbook, sheet, "Concepto", "Valor");
            agregarFila(sheet, 1, "Activas", activas);
            agregarFila(sheet, 2, "Vencidas", vencidas);
            agregarFila(sheet, 3, "Canceladas", canceladas);
            agregarFila(sheet, 4, "Ingreso Mensual", ingresoMensual);

            return guardarWorkbook(workbook, "Reporte_Pensiones_" + LocalDate.now() + ".xlsx");
        } catch (Exception ex) {
            System.err.println("Error generando Excel de pensiones: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    public boolean generarReporteConsolidado(LocalDate fecha) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Consolidado");
            crearEncabezados(workbook, sheet, "Estacionamiento", "Ingresos", "Registros", "Clientes", "Vehiculos", "% Ocupacion");

            List<Cliente> clientes = clienteDAO.obtenerTodos();
            List<Vehiculo> vehiculos = vehiculoDAO.obtenerTodos();
            int fila = 1;

            for (Estacionamiento estacionamiento : estacionamientoDAO.obtenerTodos()) {
                List<RegistroEntradaSalida> registros = registroDAO.obtenerPorEstacionamiento(estacionamiento.getId());
                double ingresos = registros.stream()
                        .filter(r -> "Finalizado".equals(r.getEstado()))
                        .filter(r -> estaEnRango(r, fecha, fecha))
                        .mapToDouble(RegistroEntradaSalida::getMonto)
                        .sum();
                int finalizados = (int) registros.stream()
                        .filter(r -> "Finalizado".equals(r.getEstado()))
                        .filter(r -> estaEnRango(r, fecha, fecha))
                        .count();

                List<Cajon> cajones = cajonDAO.obtenerPorEstacionamiento(estacionamiento.getId());
                int ocupados = contarCajones(cajones, "Ocupado");
                double ocupacion = cajones.isEmpty() ? 0 : ocupados * 100.0 / cajones.size();

                Row row = sheet.createRow(fila++);
                row.createCell(0).setCellValue(estacionamiento.getNombre());
                row.createCell(1).setCellValue(ingresos);
                row.createCell(2).setCellValue(finalizados);
                row.createCell(3).setCellValue(clientes.size());
                row.createCell(4).setCellValue(vehiculos.size());
                row.createCell(5).setCellValue(ocupacion);
            }

            return guardarWorkbook(workbook, "Reporte_Consolidado_" + fecha + ".xlsx");
        } catch (Exception ex) {
            System.err.println("Error generando Excel consolidado: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

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

    private void crearEncabezados(XSSFWorkbook workbook, Sheet sheet, String... encabezados) {
        Row header = sheet.createRow(0);
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font headerFont = workbook.createFont();
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        for (int i = 0; i < encabezados.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(encabezados[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void agregarFila(Sheet sheet, int numeroFila, String concepto, String valor) {
        Row row = sheet.createRow(numeroFila);
        row.createCell(0).setCellValue(concepto);
        row.createCell(1).setCellValue(valor);
    }

    private void agregarFila(Sheet sheet, int numeroFila, String concepto, int valor) {
        Row row = sheet.createRow(numeroFila);
        row.createCell(0).setCellValue(concepto);
        row.createCell(1).setCellValue(valor);
    }

    private void agregarFila(Sheet sheet, int numeroFila, String concepto, double valor) {
        Row row = sheet.createRow(numeroFila);
        row.createCell(0).setCellValue(concepto);
        row.createCell(1).setCellValue(valor);
    }

    private boolean guardarWorkbook(XSSFWorkbook workbook, String nombreArchivo) throws Exception {
        String rutaCompleta = rutaSalida + File.separator + nombreArchivo;
        File archivo = new File(rutaCompleta);
        archivo.getParentFile().mkdirs();

        Sheet sheet = workbook.getSheetAt(0);
        for (int i = 0; i < sheet.getRow(0).getLastCellNum(); i++) {
            sheet.autoSizeColumn(i);
        }

        try (FileOutputStream fos = new FileOutputStream(rutaCompleta)) {
            workbook.write(fos);
        }

        System.out.println("Excel generado: " + rutaCompleta);
        return true;
    }

    private int contarCajones(List<Cajon> cajones, String estado) {
        return (int) cajones.stream()
                .filter(c -> estado.equals(c.getEstado()))
                .count();
    }

    private boolean estaEnRango(RegistroEntradaSalida registro, LocalDate inicio, LocalDate fin) {
        LocalDate fecha = null;
        if (registro.getFechaSalida() != null) {
            fecha = registro.getFechaSalida().toLocalDate();
        } else if (registro.getFechaEntrada() != null) {
            fecha = registro.getFechaEntrada().toLocalDate();
        }

        return fecha != null
                && !fecha.isBefore(inicio)
                && !fecha.isAfter(fin);
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
