package com.estacionamiento.ui.modules;

import com.estacionamiento.controladores.CajaController;
import com.estacionamiento.controladores.ConvenioController;
import com.estacionamiento.dao.LiquidacionRestauranteDAO;
import com.estacionamiento.dao.PagoDAO;
import com.estacionamiento.modelos.ConvenioRestaurante;
import com.estacionamiento.modelos.LiquidacionRestaurante;
import com.estacionamiento.modelos.Pago;
import com.estacionamiento.modelos.Restaurante;
import com.estacionamiento.ui.Session;
import com.estacionamiento.ui.UI;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Módulo de Convenios con Restaurantes.
 *
 * Pestaña 1 — Restaurantes : CRUD (sin campo comisión)
 * Pestaña 2 — Convenios    : CRUD con ComboBox de restaurantes
 * Pestaña 3 — Cuentas      : Deuda pendiente por restaurante + liquidar
 */
class ConveniosImpl extends VBox {

    private final ConvenioController ctrl      = new ConvenioController();
    private final PagoDAO            pagoDAO   = new PagoDAO();
    private final LiquidacionRestauranteDAO liqDAO = new LiquidacionRestauranteDAO();

    private static final DateTimeFormatter FMT  = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMTD = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ObservableList<Restaurante>        datosRest = FXCollections.observableArrayList();
    private final ObservableList<ConvenioRestaurante> datosConv = FXCollections.observableArrayList();

    ConveniosImpl() {
        setPadding(new Insets(24, 28, 24, 28));
        setSpacing(16);
        setStyle("-fx-background-color:" + UI.BG + ";");
        construir();
        cargarTodo();
    }

    // ─────────────────────────────────────────────────────────
    //  CONSTRUCCIÓN PRINCIPAL
    // ─────────────────────────────────────────────────────────
    private void construir() {
        Label titulo = new Label("🤝 Convenios con Restaurantes");
        titulo.setFont(Font.font("System", FontWeight.BLACK, 18));
        titulo.setStyle("-fx-text-fill:" + UI.TEXT + ";");
        Label sub = new Label("Registra restaurantes, sus convenios y consulta lo que deben al estacionamiento");
        sub.setStyle("-fx-text-fill:" + UI.MUTED + ";-fx-font-size:12px;");

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabs, Priority.ALWAYS);
        tabs.getTabs().addAll(tabRestaurantes(), tabConvenios(), tabCuentas());
        getChildren().addAll(new VBox(4, titulo, sub), tabs);
    }

    // ─────────────────────────────────────────────────────────
    //  PESTAÑA 1 — RESTAURANTES (sin campo comisión)
    // ─────────────────────────────────────────────────────────
    private Tab tabRestaurantes() {
        Tab tab = new Tab("🏪 Restaurantes");

        TableView<Restaurante> tabla = new TableView<>();
        UI.estilizarTabla(tabla);
        tabla.setItems(datosRest);
        VBox.setVgrow(tabla, Priority.ALWAYS);

        TableColumn<Restaurante, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(55);

        TableColumn<Restaurante, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colNombre.setPrefWidth(200);

        TableColumn<Restaurante, String> colTel = new TableColumn<>("Teléfono");
        colTel.setCellValueFactory(new PropertyValueFactory<>("telefono"));

        TableColumn<Restaurante, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Restaurante, String> colDesc = new TableColumn<>("Descripción");
        colDesc.setCellValueFactory(new PropertyValueFactory<>("descripcion"));

        TableColumn<Restaurante, Boolean> colAct = new TableColumn<>("Estado");
        colAct.setCellValueFactory(new PropertyValueFactory<>("activo"));
        colAct.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Boolean v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); return; }
                setGraphic(UI.badge(v ? "Activo" : "Inactivo",
                        v ? UI.badgeGreen() : UI.badgeGray()));
            }
        });

        TableColumn<Restaurante, Void> colAcc = new TableColumn<>("Acciones");
        colAcc.setCellFactory(col -> new TableCell<>() {
            final Button edit = UI.btnSecundario("✏️ Editar");
            final Button del  = UI.btnPeligro("Desactivar");
            {
                edit.setStyle(edit.getStyle() + UI.BTN_SMALL);
                del.setStyle(del.getStyle() + UI.BTN_SMALL);
                edit.setOnAction(e -> formRestaurante(getTableView().getItems().get(getIndex())));
                del.setOnAction(e -> {
                    Restaurante r = getTableView().getItems().get(getIndex());
                    if (UI.confirmar("Desactivar", "¿Desactivar \"" + r.getNombre() + "\"?")) {
                        try { ctrl.desactivarRestaurante(r.getId()); cargarTodo(); }
                        catch (Exception ex) { UI.mostrarError("Error", ex.getMessage()); }
                    }
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                HBox h = new HBox(6, edit, del); h.setAlignment(Pos.CENTER_LEFT); setGraphic(h);
            }
        });
        colAcc.setPrefWidth(160);

        tabla.getColumns().addAll(colId, colNombre, colTel, colEmail, colDesc, colAct, colAcc);
        tabla.setPlaceholder(UI.panelVacio("🏪",
                "No hay restaurantes.\nCrea uno con el botón de arriba."));

        Button btnNuevo = UI.btnPrimario("+ Nuevo restaurante");
        btnNuevo.setOnAction(e -> formRestaurante(null));
        HBox barra = new HBox(btnNuevo); barra.setPadding(new Insets(12, 0, 8, 0));

        VBox c = new VBox(0, barra, tabla);
        c.setPadding(new Insets(12, 16, 16, 16));
        VBox.setVgrow(tabla, Priority.ALWAYS);
        tab.setContent(c);
        return tab;
    }

    // ─────────────────────────────────────────────────────────
    //  PESTAÑA 2 — CONVENIOS
    // ─────────────────────────────────────────────────────────
    private Tab tabConvenios() {
        Tab tab = new Tab("🤝 Convenios");

        TableView<ConvenioRestaurante> tabla = new TableView<>();
        UI.estilizarTabla(tabla);
        tabla.setItems(datosConv);
        VBox.setVgrow(tabla, Priority.ALWAYS);

        TableColumn<ConvenioRestaurante, String> colRest = new TableColumn<>("Restaurante");
        colRest.setCellValueFactory(c -> {
            int id = c.getValue().getRestauranteId();
            return new javafx.beans.property.SimpleStringProperty(
                    datosRest.stream().filter(r -> r.getId() == id)
                            .map(Restaurante::getNombre).findFirst().orElse("ID #" + id));
        });
        colRest.setPrefWidth(180);

        TableColumn<ConvenioRestaurante, String> colDesc = new TableColumn<>("Descripción");
        colDesc.setCellValueFactory(new PropertyValueFactory<>("descripcion"));

        TableColumn<ConvenioRestaurante, String> colTipo = new TableColumn<>("Cobertura");
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipoCobertura"));
        colTipo.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); return; }
                String est = switch (v) {
                    case "TOTAL" -> UI.badgeGreen(); case "PORCENTAJE" -> UI.badgeBlue();
                    case "MONTO_FIJO" -> UI.badgeAmber(); default -> UI.badgeGray();
                };
                setGraphic(UI.badge(v, est));
            }
        });

        TableColumn<ConvenioRestaurante, String> colVig = new TableColumn<>("Vigencia");
        colVig.setCellValueFactory(c -> {
            String d = c.getValue().getFechaInicio() != null ? c.getValue().getFechaInicio().toLocalDate().format(FMT) : "--";
            String h = c.getValue().getFechaFin()    != null ? c.getValue().getFechaFin().toLocalDate().format(FMT)    : "--";
            return new javafx.beans.property.SimpleStringProperty(d + " → " + h);
        });
        colVig.setPrefWidth(170);

        TableColumn<ConvenioRestaurante, String> colEst = new TableColumn<>("Estado");
        colEst.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colEst.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); return; }
                String est = switch (v) {
                    case "Vigente" -> UI.badgeGreen(); case "Vencido" -> UI.badgeAmber();
                    case "Cancelado" -> UI.badgeRed(); default -> UI.badgeGray();
                };
                setGraphic(UI.badge(v, est));
            }
        });

        TableColumn<ConvenioRestaurante, Void> colAcc = new TableColumn<>("Acciones");
        colAcc.setCellFactory(col -> new TableCell<>() {
            final Button edit = UI.btnSecundario("✏️ Editar");
            final Button can  = UI.btnPeligro("✖ Cancelar");
            {
                edit.setStyle(edit.getStyle() + UI.BTN_SMALL);
                can.setStyle(can.getStyle() + UI.BTN_SMALL);
                edit.setOnAction(e -> formConvenio(getTableView().getItems().get(getIndex())));
                can.setOnAction(e -> {
                    ConvenioRestaurante cv = getTableView().getItems().get(getIndex());
                    if (UI.confirmar("Cancelar convenio", "¿Cancelar convenio #" + cv.getId() + "?")) {
                        try { ctrl.cancelarConvenio(cv.getId()); cargarTodo(); }
                        catch (Exception ex) { UI.mostrarError("Error", ex.getMessage()); }
                    }
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                ConvenioRestaurante cv = getTableView().getItems().get(getIndex());
                HBox h = new HBox(6, edit);
                if ("Vigente".equals(cv.getEstado())) h.getChildren().add(can);
                h.setAlignment(Pos.CENTER_LEFT); setGraphic(h);
            }
        });
        colAcc.setPrefWidth(150);

        tabla.getColumns().addAll(colRest, colDesc, colTipo, colVig, colEst, colAcc);
        tabla.setPlaceholder(UI.panelVacio("🤝",
                "No hay convenios.\nPrimero crea un restaurante, luego crea el convenio aquí."));

        Button btnNuevo = UI.btnPrimario("+ Nuevo convenio");
        btnNuevo.setOnAction(e -> {
            List<Restaurante> activos = datosRest.stream().filter(Restaurante::isActivo).toList();
            if (activos.isEmpty()) {
                UI.mostrarInfo("Sin restaurantes",
                        "Registra primero un restaurante en la pestaña 'Restaurantes'.");
                return;
            }
            formConvenio(null);
        });

        HBox barra = new HBox(btnNuevo); barra.setPadding(new Insets(12, 0, 8, 0));
        VBox c = new VBox(0, barra, tabla);
        c.setPadding(new Insets(12, 16, 16, 16));
        VBox.setVgrow(tabla, Priority.ALWAYS);
        tab.setContent(c);
        return tab;
    }

    // ─────────────────────────────────────────────────────────
    //  PESTAÑA 3 — CUENTAS POR COBRAR
    // ─────────────────────────────────────────────────────────
    private Tab tabCuentas() {
        Tab tab = new Tab("💳 Cuentas por cobrar");

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:white;-fx-background-color:white;");

        VBox contenido = new VBox(16);
        contenido.setPadding(new Insets(16));

        Label tit = new Label("Deuda pendiente por restaurante");
        tit.setFont(Font.font("System", FontWeight.BOLD, 14));

        // Filtros de fecha
        DatePicker dpDesde = new DatePicker(LocalDate.now().withDayOfMonth(1));
        DatePicker dpHasta = new DatePicker(LocalDate.now());
        dpDesde.setStyle(UI.FIELD); dpHasta.setStyle(UI.FIELD);
        dpDesde.setMaxWidth(160);   dpHasta.setMaxWidth(160);

        Button btnActualizar = UI.btnSecundario("🔄 Actualizar");
        HBox filtros = new HBox(12,
                UI.grupoCampo("Desde", dpDesde),
                UI.grupoCampo("Hasta", dpHasta),
                btnActualizar);
        filtros.setAlignment(Pos.BOTTOM_LEFT);

        // Tabla de cuentas por restaurante
        TableView<ResumenRestaurante> tablaCuentas = new TableView<>();
        UI.estilizarTabla(tablaCuentas);
        ObservableList<ResumenRestaurante> datosCuentas = FXCollections.observableArrayList();
        tablaCuentas.setItems(datosCuentas);
        tablaCuentas.setPrefHeight(280);

        TableColumn<ResumenRestaurante, String> colNombre = new TableColumn<>("Restaurante");
        colNombre.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().nombre));
        colNombre.setPrefWidth(200);

        TableColumn<ResumenRestaurante, String> colPagos = new TableColumn<>("Pagos cubiertos");
        colPagos.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().totalPagos)));
        colPagos.setStyle("-fx-alignment:CENTER;");

        TableColumn<ResumenRestaurante, String> colDeuda = new TableColumn<>("Total a cobrar");
        colDeuda.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        String.format("$%.2f", c.getValue().totalDeuda)));
        colDeuda.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); return; }
                setText(v);
                setStyle("-fx-font-weight:bold;-fx-text-fill:" + UI.RED
                        + ";-fx-font-size:13px;");
            }
        });

        TableColumn<ResumenRestaurante, String> colLiquidado = new TableColumn<>("Ya liquidado");
        colLiquidado.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        String.format("$%.2f", c.getValue().totalLiquidado)));
        colLiquidado.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); return; }
                setText(v);
                setStyle("-fx-font-weight:bold;-fx-text-fill:" + UI.GREEN
                        + ";-fx-font-size:13px;");
            }
        });

        TableColumn<ResumenRestaurante, Void> colAccCuenta = new TableColumn<>("Acciones");
        colAccCuenta.setCellFactory(col -> new TableCell<>() {
            final Button btnVer = UI.btnSecundario("🔍 Ver detalle");
            final Button btnLiq = UI.btnPrimario("✅ Liquidar");
            {
                btnVer.setStyle(btnVer.getStyle() + UI.BTN_SMALL);
                btnLiq.setStyle(btnLiq.getStyle() + UI.BTN_SMALL);

                btnVer.setOnAction(e -> {
                    ResumenRestaurante r = getTableView().getItems().get(getIndex());
                    verDetallePagos(r, dpDesde.getValue(), dpHasta.getValue());
                });

                btnLiq.setOnAction(e -> {
                    ResumenRestaurante r = getTableView().getItems().get(getIndex());
                    if (r.totalDeuda <= 0) {
                        UI.mostrarInfo("Sin deuda", "Este restaurante no tiene deuda pendiente.");
                        return;
                    }
                    if (UI.confirmar("Liquidar cuenta",
                            "¿Marcar como cobrado $" + String.format("%.2f", r.totalDeuda)
                            + " de " + r.nombre + "?\n\nEsto registrará la liquidación.")) {
                        liquidarRestaurante(r, dpDesde.getValue(), dpHasta.getValue(), datosCuentas,
                                dpDesde, dpHasta);
                    }
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                ResumenRestaurante r = getTableView().getItems().get(getIndex());
                HBox h = new HBox(6, btnVer);
                if (r.totalDeuda > 0) h.getChildren().add(btnLiq);
                h.setAlignment(Pos.CENTER_LEFT);
                setGraphic(h);
            }
        });
        colAccCuenta.setPrefWidth(200);

        tablaCuentas.getColumns().addAll(colNombre, colPagos, colDeuda, colLiquidado, colAccCuenta);
        tablaCuentas.setPlaceholder(UI.panelVacio("💳", "Selecciona un rango de fechas y presiona Actualizar."));

        // Totales generales
        Label lblTotalDeuda = new Label("Total pendiente: $0.00");
        lblTotalDeuda.setFont(Font.font("System", FontWeight.BOLD, 14));
        lblTotalDeuda.setStyle("-fx-text-fill:" + UI.RED + ";");

        Label lblTotalLiq = new Label("Total liquidado: $0.00");
        lblTotalLiq.setFont(Font.font("System", FontWeight.BOLD, 14));
        lblTotalLiq.setStyle("-fx-text-fill:" + UI.GREEN + ";");

        HBox totales = new HBox(30, lblTotalDeuda, lblTotalLiq);
        totales.setPadding(new Insets(10, 0, 0, 0));

        // Historial de liquidaciones
        Label titHist = new Label("Historial de liquidaciones");
        titHist.setFont(Font.font("System", FontWeight.BOLD, 14));
        titHist.setPadding(new Insets(16, 0, 0, 0));

        TableView<LiquidacionRestaurante> tablaLiq = new TableView<>();
        UI.estilizarTabla(tablaLiq);
        ObservableList<LiquidacionRestaurante> datosLiq = FXCollections.observableArrayList();
        tablaLiq.setItems(datosLiq);
        tablaLiq.setPrefHeight(200);

        TableColumn<LiquidacionRestaurante, String> colLFolio = new TableColumn<>("Folio");
        colLFolio.setCellValueFactory(new PropertyValueFactory<>("folioLiquidacion"));
        TableColumn<LiquidacionRestaurante, String> colLRest = new TableColumn<>("Restaurante ID");
        colLRest.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                nombreRestaurante(c.getValue().getRestauranteId())));
        colLRest.setPrefWidth(150);
        TableColumn<LiquidacionRestaurante, String> colLFecha = new TableColumn<>("Fecha");
        colLFecha.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getFechaLiquidacion() != null
                        ? c.getValue().getFechaLiquidacion().format(FMTD) : "--"));
        colLFecha.setPrefWidth(130);
        TableColumn<LiquidacionRestaurante, String> colLTotal = new TableColumn<>("Total");
        colLTotal.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                String.format("$%.2f", c.getValue().getTotal())));
        TableColumn<LiquidacionRestaurante, String> colLEst = new TableColumn<>("Estado");
        colLEst.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colLEst.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); return; }
                String est = switch (v) {
                    case "COBRADA" -> UI.badgeGreen();
                    case "PENDIENTE" -> UI.badgeAmber();
                    case "CANCELADA" -> UI.badgeRed();
                    default -> UI.badgeGray();
                };
                setGraphic(UI.badge(v, est));
            }
        });
        tablaLiq.getColumns().addAll(colLFolio, colLRest, colLFecha, colLTotal, colLEst);
        tablaLiq.setPlaceholder(UI.panelVacio("📋", "Sin liquidaciones registradas."));

        // Cargar datos al presionar Actualizar
        Runnable cargarCuentas = () -> {
            datosCuentas.clear();
            datosLiq.clear();
            try {
                int estId = resolverEstId();
                LocalDateTime desde = dpDesde.getValue().atStartOfDay();
                LocalDateTime hasta = dpHasta.getValue().atTime(23, 59, 59);

                double totalDeudaGral = 0, totalLiqGral = 0;

                for (Restaurante r : datosRest) {
                    List<Pago> pagos = pagoDAO.obtenerPendientesPorRestaurante(r.getId(), desde, hasta);
                    double deuda = pagos.stream().mapToDouble(Pago::getMonto).sum();

                    // Calcular liquidado en el período
                    List<LiquidacionRestaurante> liqs = liqDAO.obtenerPorRestaurante(r.getId());
                    double liquidado = liqs.stream()
                            .filter(l -> "COBRADA".equals(l.getEstado())
                                    && l.getFechaLiquidacion() != null
                                    && !l.getFechaLiquidacion().isBefore(desde)
                                    && !l.getFechaLiquidacion().isAfter(hasta))
                            .mapToDouble(LiquidacionRestaurante::getTotal).sum();

                    datosCuentas.add(new ResumenRestaurante(
                            r.getId(), r.getNombre(), pagos.size(), deuda, liquidado));
                    totalDeudaGral += deuda;
                    totalLiqGral   += liquidado;
                }

                lblTotalDeuda.setText(String.format("Total pendiente: $%.2f", totalDeudaGral));
                lblTotalLiq.setText(String.format("Total liquidado: $%.2f", totalLiqGral));

                // Historial de liquidaciones del estacionamiento
                datosLiq.setAll(liqDAO.obtenerPorEstacionamiento(estId));

            } catch (Exception ex) {
                UI.mostrarError("Error al cargar cuentas", ex.getMessage());
            }
        };

        btnActualizar.setOnAction(e -> cargarCuentas.run());

        contenido.getChildren().addAll(tit, filtros, tablaCuentas, totales,
                UI.separador(), titHist, tablaLiq);
        scroll.setContent(contenido);
        tab.setContent(scroll);
        return tab;
    }

    // ─────────────────────────────────────────────────────────
    //  VER DETALLE DE PAGOS DE UN RESTAURANTE
    // ─────────────────────────────────────────────────────────
    private void verDetallePagos(ResumenRestaurante resumen,
                                  LocalDate desde, LocalDate hasta) {
        Stage v = new Stage();
        v.initModality(Modality.APPLICATION_MODAL);
        v.setTitle("Detalle — " + resumen.nombre);
        v.setResizable(true);

        VBox cont = new VBox(14);
        cont.setPadding(new Insets(24));
        cont.setPrefWidth(600);
        cont.setPrefHeight(480);

        Label titulo = new Label("🔍 Pagos pendientes de " + resumen.nombre);
        titulo.setFont(Font.font("System", FontWeight.BOLD, 15));

        Label periodo = new Label("Período: " + desde.format(FMT) + " al " + hasta.format(FMT));
        periodo.setStyle("-fx-text-fill:" + UI.MUTED + ";-fx-font-size:12px;");

        TableView<Pago> tabla = new TableView<>();
        UI.estilizarTabla(tabla);
        VBox.setVgrow(tabla, Priority.ALWAYS);

        TableColumn<Pago, String> colTicket = new TableColumn<>("Ticket");
        colTicket.setCellValueFactory(new PropertyValueFactory<>("numeroTicket"));
        TableColumn<Pago, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getFechaPago() != null ? c.getValue().getFechaPago().format(FMTD) : "--"));
        colFecha.setPrefWidth(140);
        TableColumn<Pago, String> colMonto = new TableColumn<>("Monto cubierto");
        colMonto.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                String.format("$%.2f", c.getValue().getMonto())));
        colMonto.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setText(null); return; }
                setText(val);
                setStyle("-fx-font-weight:bold;-fx-text-fill:" + UI.RED + ";");
            }
        });
        TableColumn<Pago, String> colEst = new TableColumn<>("Estado");
        colEst.setCellValueFactory(new PropertyValueFactory<>("estadoLiquidacion"));
        colEst.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); return; }
                setGraphic(UI.badge(v, "PENDIENTE".equals(v) ? UI.badgeAmber() : UI.badgeGreen()));
            }
        });
        tabla.getColumns().addAll(colTicket, colFecha, colMonto, colEst);

        try {
            List<Pago> pagos = pagoDAO.obtenerPendientesPorRestaurante(
                    resumen.restauranteId,
                    desde.atStartOfDay(),
                    hasta.atTime(23, 59, 59));
            tabla.setItems(FXCollections.observableArrayList(pagos));
        } catch (Exception ex) {
            tabla.setPlaceholder(UI.alertaError("Error: " + ex.getMessage()));
        }

        Label lblTotal = new Label(String.format("Total a cobrar: $%.2f", resumen.totalDeuda));
        lblTotal.setFont(Font.font("System", FontWeight.BLACK, 16));
        lblTotal.setStyle("-fx-text-fill:" + UI.RED + ";");

        Button cerrar = UI.btnSecundario("Cerrar");
        cerrar.setOnAction(e -> v.close());
        HBox bRow = new HBox(cerrar); bRow.setAlignment(Pos.CENTER_RIGHT);

        cont.getChildren().addAll(titulo, periodo, UI.separador(),
                tabla, lblTotal, UI.separador(), bRow);
        v.setScene(new Scene(cont));
        v.showAndWait();
    }

    // ─────────────────────────────────────────────────────────
    //  LIQUIDAR RESTAURANTE
    // ─────────────────────────────────────────────────────────
    private void liquidarRestaurante(ResumenRestaurante resumen,
                                      LocalDate desde, LocalDate hasta,
                                      ObservableList<ResumenRestaurante> datosCuentas,
                                      DatePicker dpDesde, DatePicker dpHasta) {
        try {
            CajaController cajaCtrl = new CajaController();
            int estId = resolverEstId();

            // Buscar convenio vigente del restaurante
            List<ConvenioRestaurante> convs = ctrl.obtenerPorEstacionamiento(estId);
            Integer convenioId = convs.stream()
                    .filter(cv -> cv.getRestauranteId() == resumen.restauranteId
                            && "Vigente".equals(cv.getEstado()))
                    .map(ConvenioRestaurante::getId)
                    .findFirst().orElse(null);

            LiquidacionRestaurante liq = cajaCtrl.liquidarPagosRestaurante(
                    resumen.restauranteId,
                    estId,
                    convenioId,
                    desde.atStartOfDay(),
                    hasta.atTime(23, 59, 59),
                    "Liquidación registrada desde módulo de Convenios"
            );

            if (liq != null) {
                UI.mostrarInfo("Liquidación registrada",
                        "✅ Folio: " + liq.getFolioLiquidacion()
                        + "\nRestaurante: " + resumen.nombre
                        + "\nTotal cobrado: $" + String.format("%.2f", liq.getTotal()));
                // Recargar
                dpDesde.getOnAction().handle(null);
            } else {
                UI.mostrarError("Error", "No se pudo registrar la liquidación.");
            }
        } catch (Exception ex) {
            UI.mostrarError("Error al liquidar", ex.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────────
    //  FORMULARIO — RESTAURANTE (sin comisión)
    // ─────────────────────────────────────────────────────────
    private void formRestaurante(Restaurante editar) {
        Stage v = new Stage();
        v.initModality(Modality.APPLICATION_MODAL);
        v.setTitle(editar == null ? "Nuevo restaurante" : "Editar restaurante");
        v.setResizable(false);

        VBox cont = new VBox(14);
        cont.setPadding(new Insets(24));
        cont.setPrefWidth(460);

        Label t = new Label(editar == null ? "🏪 Nuevo restaurante" : "✏️ Editar restaurante");
        t.setFont(Font.font("System", FontWeight.BOLD, 15));

        TextField fN   = UI.campo("Nombre del restaurante *");
        TextField fTel = UI.campo("Teléfono");
        TextField fE   = UI.campo("Email");
        TextField fD   = UI.campo("Descripción breve");

        if (editar != null) {
            fN.setText(editar.getNombre());
            fTel.setText(editar.getTelefono() != null ? editar.getTelefono() : "");
            fE.setText(editar.getEmail() != null ? editar.getEmail() : "");
            fD.setText(editar.getDescripcion() != null ? editar.getDescripcion() : "");
        }

        GridPane g = new GridPane(); g.setHgap(12); g.setVgap(12);
        g.add(UI.grupoCampo("Nombre *", fN),      0, 0, 2, 1);
        g.add(UI.grupoCampo("Teléfono", fTel),    0, 1);
        g.add(UI.grupoCampo("Email", fE),          1, 1);
        g.add(UI.grupoCampo("Descripción", fD),   0, 2, 2, 1);
        ColumnConstraints cc = new ColumnConstraints(); cc.setPercentWidth(50);
        g.getColumnConstraints().addAll(cc, new ColumnConstraints() {{ setPercentWidth(50); }});

        Label err = UI.errorLabel();
        Button guardar  = UI.btnPrimario(editar == null ? "Guardar restaurante" : "Actualizar");
        Button cancelar = UI.btnSecundario("Cancelar");
        cancelar.setOnAction(e -> v.close());

        guardar.setOnAction(e -> {
            if (fN.getText().isBlank()) { UI.setError(err, "El nombre es obligatorio."); return; }
            try {
                Restaurante r = editar != null ? editar : new Restaurante();
                r.setNombre(fN.getText().trim());
                r.setTelefono(fTel.getText().trim());
                r.setEmail(fE.getText().trim());
                r.setDescripcion(fD.getText().trim());
                r.setEstacionamientoId(resolverEstId());
                r.setActivo(true);
                r.setComisionPorcentaje(0); // sin campo visible, se guarda 0

                boolean ok = editar == null ? ctrl.crearRestaurante(r) : ctrl.actualizarRestaurante(r);
                if (ok) {
                    cargarTodo(); v.close();
                    if (editar == null)
                        UI.mostrarInfo("Restaurante creado",
                                "\"" + r.getNombre() + "\" registrado.\n" +
                                "Ahora puedes crear un convenio en la pestaña 'Convenios'.");
                } else UI.setError(err, "No se pudo guardar.");
            } catch (Exception ex) { UI.setError(err, ex.getMessage()); }
        });

        HBox br = new HBox(10, cancelar, guardar); br.setAlignment(Pos.CENTER_RIGHT);
        cont.getChildren().addAll(t, UI.separador(), g, err, UI.separador(), br);
        v.setScene(new Scene(cont)); v.showAndWait();
    }

    // ─────────────────────────────────────────────────────────
    //  FORMULARIO — CONVENIO
    // ─────────────────────────────────────────────────────────
    private void formConvenio(ConvenioRestaurante editar) {
        List<Restaurante> activos = datosRest.stream().filter(Restaurante::isActivo).toList();

        Stage v = new Stage();
        v.initModality(Modality.APPLICATION_MODAL);
        v.setTitle(editar == null ? "Nuevo convenio" : "Editar convenio");
        v.setResizable(false);

        VBox cont = new VBox(14); cont.setPadding(new Insets(24)); cont.setPrefWidth(500);
        Label t = new Label(editar == null ? "🤝 Nuevo convenio" : "✏️ Editar convenio");
        t.setFont(Font.font("System", FontWeight.BOLD, 15));

        ComboBox<Restaurante> comboRest = UI.combo();
        comboRest.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Restaurante r) { return r == null ? "" : "#" + r.getId() + " — " + r.getNombre(); }
            @Override public Restaurante fromString(String s) { return null; }
        });
        comboRest.getItems().addAll(activos);
        if (!activos.isEmpty()) comboRest.setValue(activos.get(0));

        ComboBox<String> comboTipo = UI.combo();
        comboTipo.getItems().addAll("TOTAL", "PORCENTAJE", "MONTO_FIJO", "HORAS_GRATIS");
        comboTipo.setValue("TOTAL");

        TextField fPct = UI.campo("Ej: 50"); fPct.setDisable(true);
        TextField fMax = UI.campo("Ej: 100.00"); fMax.setDisable(true);
        TextField fHrs = UI.campo("Ej: 2"); fHrs.setDisable(true);

        comboTipo.setOnAction(e -> {
            String tp = comboTipo.getValue();
            fPct.setDisable(!"PORCENTAJE".equals(tp));
            fMax.setDisable(!"MONTO_FIJO".equals(tp) && !"PORCENTAJE".equals(tp));
            fHrs.setDisable(!"HORAS_GRATIS".equals(tp));
        });

        TextArea fDesc = UI.areaTexto("Descripción del convenio...", 3);
        DatePicker dpI = new DatePicker(LocalDate.now());
        DatePicker dpF = new DatePicker(LocalDate.now().plusYears(1));
        dpI.setMaxWidth(Double.MAX_VALUE); dpF.setMaxWidth(Double.MAX_VALUE);
        dpI.setStyle(UI.FIELD); dpF.setStyle(UI.FIELD);
        ComboBox<String> comboEst = UI.combo();
        comboEst.getItems().addAll("Vigente", "Vencido", "Cancelado");
        comboEst.setValue("Vigente");

        if (editar != null) {
            activos.stream().filter(r -> r.getId() == editar.getRestauranteId())
                    .findFirst().ifPresent(comboRest::setValue);
            if (editar.getTipoCobertura() != null) comboTipo.setValue(editar.getTipoCobertura());
            fPct.setText(String.valueOf(editar.getPorcentajeCobertura()));
            if (editar.getMontoMaximo() != null) fMax.setText(String.valueOf(editar.getMontoMaximo()));
            fHrs.setText(String.valueOf(editar.getHorasGratis()));
            fDesc.setText(editar.getDescripcion() != null ? editar.getDescripcion() : "");
            if (editar.getFechaInicio() != null) dpI.setValue(editar.getFechaInicio().toLocalDate());
            if (editar.getFechaFin()    != null) dpF.setValue(editar.getFechaFin().toLocalDate());
            if (editar.getEstado()      != null) comboEst.setValue(editar.getEstado());
            comboTipo.getOnAction().handle(null);
        }

        GridPane g = new GridPane(); g.setHgap(12); g.setVgap(12);
        g.add(UI.grupoCampo("Restaurante *", comboRest),      0, 0, 2, 1);
        g.add(UI.grupoCampo("Tipo cobertura *", comboTipo),   0, 1);
        g.add(UI.grupoCampo("Estado", comboEst),               1, 1);
        g.add(UI.grupoCampo("Porcentaje %", fPct),             0, 2);
        g.add(UI.grupoCampo("Monto máximo ($)", fMax),         1, 2);
        g.add(UI.grupoCampo("Horas gratuitas", fHrs),          0, 3);
        g.add(UI.grupoCampo("Fecha inicio *", dpI),            0, 4);
        g.add(UI.grupoCampo("Fecha fin *", dpF),               1, 4);
        g.add(UI.grupoCampo("Descripción", fDesc),             0, 5, 2, 1);
        ColumnConstraints cc = new ColumnConstraints(); cc.setPercentWidth(50);
        g.getColumnConstraints().addAll(cc, new ColumnConstraints() {{ setPercentWidth(50); }});

        Label err = UI.errorLabel();
        Button guardar  = UI.btnPrimario(editar == null ? "Guardar convenio" : "Actualizar");
        Button cancelar = UI.btnSecundario("Cancelar"); cancelar.setOnAction(e -> v.close());

        guardar.setOnAction(e -> {
            if (comboRest.getValue() == null) { UI.setError(err, "Selecciona un restaurante."); return; }
            if (dpF.getValue() != null && dpI.getValue() != null
                    && dpF.getValue().isBefore(dpI.getValue())) {
                UI.setError(err, "La fecha fin debe ser posterior al inicio."); return;
            }
            try {
                int estId = resolverEstId();
                ConvenioRestaurante cv = editar != null ? editar : new ConvenioRestaurante();
                cv.setRestauranteId(comboRest.getValue().getId());
                cv.setDescripcion(fDesc.getText().trim());
                cv.setTipoCobertura(comboTipo.getValue());
                cv.setEstacionamientoId(estId);
                cv.setEstado(comboEst.getValue());
                cv.setFechaInicio(dpI.getValue().atStartOfDay());
                cv.setFechaFin(dpF.getValue().atTime(23, 59));
                try { cv.setPorcentajeCobertura(Double.parseDouble(fPct.getText().trim())); }
                catch (NumberFormatException x) { cv.setPorcentajeCobertura(100); }
                if (!fMax.getText().isBlank()) try {
                    cv.setMontoMaximo(Double.parseDouble(fMax.getText().trim())); }
                catch (NumberFormatException x) {}
                try { cv.setHorasGratis(Integer.parseInt(fHrs.getText().trim())); }
                catch (NumberFormatException x) { cv.setHorasGratis(0); }

                boolean ok = editar == null ? ctrl.crearConvenio(cv) : ctrl.actualizarConvenio(cv);
                if (ok) { cargarTodo(); v.close(); }
                else UI.setError(err, "No se pudo guardar.");
            } catch (Exception ex) { UI.setError(err, "Error: " + ex.getMessage()); }
        });

        HBox br = new HBox(10, cancelar, guardar); br.setAlignment(Pos.CENTER_RIGHT);
        cont.getChildren().addAll(t, UI.separador(), g, err, UI.separador(), br);
        v.setScene(new Scene(cont)); v.showAndWait();
    }

    // ─────────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────────
    private void cargarTodo() {
        try {
            int estId = resolverEstId();
            datosRest.setAll(ctrl.obtenerRestaurantesPorEstacionamiento(estId));
            datosConv.setAll(ctrl.obtenerPorEstacionamiento(estId));
        } catch (Exception e) { UI.mostrarError("Error al cargar", e.getMessage()); }
    }

    private int resolverEstId() {
        Session s = Session.getInstance();
        Integer id = s.getEstacionamientoActualId();
        if (id == null) id = s.getEstacionamientoId();
        return id != null ? id : 1;
    }

    private String nombreRestaurante(int id) {
        return datosRest.stream().filter(r -> r.getId() == id)
                .map(Restaurante::getNombre).findFirst().orElse("ID #" + id);
    }

    /** Clase interna para el resumen por restaurante en la pestaña Cuentas */
    private static class ResumenRestaurante {
        final int restauranteId;
        final String nombre;
        final int totalPagos;
        final double totalDeuda;
        final double totalLiquidado;

        ResumenRestaurante(int id, String nombre, int pagos, double deuda, double liquidado) {
            this.restauranteId  = id;
            this.nombre         = nombre;
            this.totalPagos     = pagos;
            this.totalDeuda     = deuda;
            this.totalLiquidado = liquidado;
        }
    }
}

public class ConveniosModule extends ConveniosImpl {
    public ConveniosModule() { super(); }
}
