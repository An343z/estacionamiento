package com.estacionamiento.utilidades;

import com.estacionamiento.dao.*;
import com.estacionamiento.modelos.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.List;

/**
 * Utilidad para generar reportes en Excel
 * Requiere: Apache POI 5.2.3
 */
public class GeneradorExcel {
    private String rutaSalida;
    private EstacionamientoDAO estacionamientoDAO;
    private CajonDAO cajonDAO;
    private RegistroEntradaSalidaDAO registroDAO;
    private PensionDAO pensionDAO;
    private ClienteDAO clienteDAO;
    private VehiculoDAO vehiculoDAO;

    public GeneradorExcel(String rutaSalida) {
        this.rutaSalida = rutaSalida;
        this.estacionamientoDAO = new EstacionamientoDAO();
        this.cajonDAO = new CajonDAO();
        this.registroDAO = new RegistroEntradaSalidaDAO();
        this.pensionDAO = new PensionDAO();
        this.clienteDAO = new ClienteDAO();
        this.vehiculoDAO = new VehiculoDAO();
    }

    /**
     * Genera reporte de datos con encabezados y filas
     */
    public boolean generarReporteConDatos(String nombreReporte, List<String> encabezados, List<List<String>> datos) {
        try {
            String nombre = nombreReporte + "_" + LocalDate.now() + ".xlsx";
            String rutaCompleta = rutaSalida + File.separator + nombre;

            File archivo = new File(rutaCompleta);
            archivo.getParentFile().mkdirs();

            XSSFWorkbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Reporte");

            // Crear encabezados
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < encabezados.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(encabezados.get(i));
                cell.setCellStyle(headerStyle);
            }

            // Agregar datos
            int rowNum = 1;
            for (List<String> fila : datos) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < fila.size(); i++) {
                    row.createCell(i).setCellValue(fila.get(i));
                }
            }

            // Ajustar ancho de columnas
            for (int i = 0; i < encabezados.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            // Guardar archivo
            try (FileOutputStream fos = new FileOutputStream(rutaCompleta)) {
                workbook.write(fos);
            }
            workbook.close();

            System.out.println("Excel generado: " + rutaCompleta);
            return true;
        } catch (Exception ex) {
            System.err.println("Error generando Excel: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Genera reporte de ocupación en Excel
     */
    public boolean generarReporteOcupacion(String estacionamiento, int totalCajones,
                                           int disponibles, int ocupados, int mantenimiento) {
        try {
            String nombre = "Reporte_Ocupacion_" + LocalDate.now() + ".xlsx";
            String rutaCompleta = rutaSalida + File.separator + nombre;

            File archivo = new File(rutaCompleta);
            archivo.getParentFile().mkdirs();

            XSSFWorkbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Ocupación");

            // Encabezados
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Estacionamiento", "Total Cajones", "Disponibles", "Ocupados", "Mantenimiento"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            // Datos
            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue(estacionamiento);
            dataRow.createCell(1).setCellValue(totalCajones);
            dataRow.createCell(2).setCellValue(disponibles);
            dataRow.createCell(3).setCellValue(ocupados);
            dataRow.createCell(4).setCellValue(mantenimiento);

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(rutaCompleta)) {
                workbook.write(fos);
            }
            workbook.close();

            System.out.println("Excel generado: " + rutaCompleta);
            return true;
        } catch (Exception ex) {
            System.err.println("Error generando Excel: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Genera reporte de ingresos en Excel
     */
    public boolean generarReporteIngresos(LocalDate fechaInicio, LocalDate fechaFin,
                                          double totalIngresos, int totalRegistros) {
        try {
            String nombre = "Reporte_Ingresos_" + fechaInicio + "_a_" + fechaFin + ".xlsx";
            String rutaCompleta = rutaSalida + File.separator + nombre;

            File archivo = new File(rutaCompleta);
            archivo.getParentFile().mkdirs();

            XSSFWorkbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Ingresos");

            Row headerRow = sheet.createRow(0);
            String[] headers = {"Concepto", "Valor"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            int rowNum = 1;
            Row row1 = sheet.createRow(rowNum++);
            row1.createCell(0).setCellValue("Período Inicio");
            row1.createCell(1).setCellValue(fechaInicio.toString());

            Row row2 = sheet.createRow(rowNum++);
            row2.createCell(0).setCellValue("Período Fin");
            row2.createCell(1).setCellValue(fechaFin.toString());

            Row row3 = sheet.createRow(rowNum++);
            row3.createCell(0).setCellValue("Total Registros");
            row3.createCell(1).setCellValue(totalRegistros);

            Row row4 = sheet.createRow(rowNum++);
            row4.createCell(0).setCellValue("Total Ingresos");
            row4.createCell(1).setCellValue(totalIngresos);

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(rutaCompleta)) {
                workbook.write(fos);
            }
            workbook.close();

            System.out.println("Excel generado: " + rutaCompleta);
            return true;
        } catch (Exception ex) {
            System.err.println("Error generando Excel: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Genera reporte de pensiones en Excel
     */
    public boolean generarReportePensiones(int activas, int vencidas, int canceladas,
                                          double ingresoMensual) {
        try {
            String nombre = "Reporte_Pensiones_" + LocalDate.now() + ".xlsx";
            String rutaCompleta = rutaSalida + File.separator + nombre;

            File archivo = new File(rutaCompleta);
            archivo.getParentFile().mkdirs();

            XSSFWorkbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Pensiones");

            Row headerRow = sheet.createRow(0);
            String[] headers = {"Estado", "Cantidad"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            int rowNum = 1;
            Row row1 = sheet.createRow(rowNum++);
            row1.createCell(0).setCellValue("Activas");
            row1.createCell(1).setCellValue(activas);

            Row row2 = sheet.createRow(rowNum++);
            row2.createCell(0).setCellValue("Vencidas");
            row2.createCell(1).setCellValue(vencidas);

            Row row3 = sheet.createRow(rowNum++);
            row3.createCell(0).setCellValue("Canceladas");
            row3.createCell(1).setCellValue(canceladas);

            Row row4 = sheet.createRow(rowNum++);
            row4.createCell(0).setCellValue("Ingreso Mensual Estimado");
            row4.createCell(1).setCellValue(ingresoMensual);

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(rutaCompleta)) {
                workbook.write(fos);
            }
            workbook.close();

            System.out.println("Excel generado: " + rutaCompleta);
            return true;
        } catch (Exception ex) {
            System.err.println("Error generando Excel: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Genera reporte de clientes y vehículos
     */
    public boolean generarReporteClientes(int totalClientes, int totalVehiculos) {
        try {
            String nombre = "Reporte_Clientes_" + LocalDate.now() + ".xlsx";
            String rutaCompleta = rutaSalida + File.separator + nombre;

            File archivo = new File(rutaCompleta);
            archivo.getParentFile().mkdirs();

            XSSFWorkbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Clientes");

            Row row1 = sheet.createRow(0);
            row1.createCell(0).setCellValue("Total Clientes");
            row1.createCell(1).setCellValue(totalClientes);

            Row row2 = sheet.createRow(1);
            row2.createCell(0).setCellValue("Total Vehículos");
            row2.createCell(1).setCellValue(totalVehiculos);

            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            try (FileOutputStream fos = new FileOutputStream(rutaCompleta)) {
                workbook.write(fos);
            }
            workbook.close();

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
    public boolean generarReporteConsolidado(LocalDate fecha) {
        try {
            String nombre = "Reporte_Consolidado_" + fecha + ".xlsx";
            String rutaCompleta = rutaSalida + File.separator + nombre;

            File archivo = new File(rutaCompleta);
            archivo.getParentFile().mkdirs();

            XSSFWorkbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Consolidado");

            // Encabezados principales
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            String[] headers = {"Estacionamiento", "Ingresos Totales", "Registros", "Clientes", "Vehículos", "% Ocupación"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Obtener todos los estacionamientos
            List<Estacionamiento> estacionamientos = estacionamientoDAO.obtenerTodos();
            double totalIngresosCadena = 0;
            int totalRegistrosCadena = 0;
            int totalClientesCadena = 0;
            int totalVehiculosCadena = 0;

            int rowNum = 1;
            for (Estacionamiento est : estacionamientos) {
                Row row = sheet.createRow(rowNum++);

                // Ingresos
                List<RegistroEntradaSalida> registros = registroDAO.obtenerPorEstacionamiento(est.getId());
                double ingresos = registros.stream()
                        .filter(r -> "Finalizado".equals(r.getEstado()))
                        .mapToDouble(RegistroEntradaSalida::getMonto)
                        .sum();
                int registrosFinalizados = (int) registros.stream()
                        .filter(r -> "Finalizado".equals(r.getEstado()))
                        .count();

                // Clientes y vehículos
                List<Cliente> clientes = clienteDAO.obtenerTodos();
                List<Vehiculo> vehiculos = vehiculoDAO.obtenerTodos();

                // Ocupación
                List<Cajon> cajones = cajonDAO.obtenerPorEstacionamiento(est.getId());
                int ocupados = (int) cajones.stream().filter(c -> "Ocupado".equals(c.getEstado())).count();
                double porcentaje = cajones.size() > 0 ? (ocupados * 100.0 / cajones.size()) : 0;

                // Llenar fila
                row.createCell(0).setCellValue(est.getNombre());
                row.createCell(1).setCellValue(ingresos);
                row.createCell(2).setCellValue(registrosFinalizados);
                row.createCell(3).setCellValue(clientes.size());
                row.createCell(4).setCellValue(vehiculos.size());
                row.createCell(5).setCellValue(porcentaje);

                totalIngresosCadena += ingresos;
                totalRegistrosCadena += registrosFinalizados;
                totalClientesCadena = clientes.size();
                totalVehiculosCadena = vehiculos.size();
            }

            // Fila de totales
            rowNum++;
            Row totalRow = sheet.createRow(rowNum);
            totalRow.createCell(0).setCellValue("TOTALES CADENA");
            totalRow.createCell(1).setCellValue(totalIngresosCadena);
            totalRow.createCell(2).setCellValue(totalRegistrosCadena);
            totalRow.createCell(3).setCellValue(totalClientesCadena);
            totalRow.createCell(4).setCellValue(totalVehiculosCadena);

            CellStyle totalStyle = workbook.createCellStyle();
            totalStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
            totalStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            totalStyle.setFont(boldFont);

            for (int i = 0; i < 5; i++) {
                totalRow.getCell(i).setCellStyle(totalStyle);
            }

            // Ajustar anchos
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(rutaCompleta)) {
                workbook.write(fos);
            }
            workbook.close();

            System.out.println("Excel generado: " + rutaCompleta);
            return true;
        } catch (Exception ex) {
            System.err.println("Error generando Excel consolidado: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * Genera reporte de ocupación consultando BD
     */
    public boolean generarReporteOcupacionBD(int estacionamientoId) {
        try {
            Estacionamiento estacionamiento = estacionamientoDAO.obtenerPorId(estacionamientoId);
            if (estacionamiento == null) {
                System.err.println("Estacionamiento no encontrado");
                return false;
            }

            List<Cajon> cajones = cajonDAO.obtenerPorEstacionamiento(estacionamientoId);
            int total = cajones.size();
            int disponibles = (int) cajones.stream().filter(c -> "Disponible".equals(c.getEstado())).count();
            int ocupados = (int) cajones.stream().filter(c -> "Ocupado".equals(c.getEstado())).count();
            int mantenimiento = (int) cajones.stream().filter(c -> "Mantenimiento".equals(c.getEstado())).count();

            return generarReporteOcupacion(estacionamiento.getNombre(), total, disponibles, ocupados, mantenimiento);
        } catch (Exception ex) {
            System.err.println("Error consultando ocupación: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Genera reporte de ingresos consultando BD
     */
    public boolean generarReporteIngresosBD(int estacionamientoId, LocalDate fechaInicio, LocalDate fechaFin) {
        try {
            List<RegistroEntradaSalida> registros = registroDAO.obtenerPorEstacionamiento(estacionamientoId);
            double totalIngresos = registros.stream()
                    .filter(r -> "Finalizado".equals(r.getEstado()))
                    .mapToDouble(RegistroEntradaSalida::getMonto)
                    .sum();
            int totalRegistros = (int) registros.stream()
                    .filter(r -> "Finalizado".equals(r.getEstado()))
                    .count();

            return generarReporteIngresos(fechaInicio, fechaFin, totalIngresos, totalRegistros);
        } catch (Exception ex) {
            System.err.println("Error consultando ingresos: " + ex.getMessage());
            return false;
        }
    }

    /**
     * Genera reporte de pensiones consultando BD
     */
    public boolean generarReportePensionBD(int estacionamientoId) {
        try {
            List<Pension> pensiones = pensionDAO.obtenerActivas(estacionamientoId);
            int activas = (int) pensiones.stream().filter(p -> "Activa".equals(p.getEstado())).count();
            int vencidas = (int) pensiones.stream().filter(p -> "Vencida".equals(p.getEstado())).count();
            int canceladas = (int) pensiones.stream().filter(p -> "Cancelada".equals(p.getEstado())).count();
            double ingresoMensual = pensiones.stream().mapToDouble(Pension::getMonto).sum();

            return generarReportePensiones(activas, vencidas, canceladas, ingresoMensual);
        } catch (Exception ex) {
            System.err.println("Error consultando pensiones: " + ex.getMessage());
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
