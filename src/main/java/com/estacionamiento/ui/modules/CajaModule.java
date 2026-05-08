package com.estacionamiento.ui.modules;

import com.estacionamiento.controladores.CajaController;
import com.estacionamiento.controladores.RegistroController;
import com.estacionamiento.modelos.*;
import com.estacionamiento.ui.Session;
import com.estacionamiento.ui.UI;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Módulo de Caja.
 *
 * Pestañas:
 *   1. Cobrar — registrar pago de un vehículo que salió
 *   2. Corte de Caja — resumen financiero del turno
 *   3. Historial — todos los pagos del estacionamiento
 *   4. Métodos de Pago — resumen por método
 *
 * Restricciones por rol (aplicadas también en CajaController):
 *   - Cajero  (rol 3): cobra y ve historial, NO puede hacer cortes
 *   - Encargado (rol 2): cobra + hace cortes + ve todo
 *   - Admin (rol 1): acceso total + ve todos los estacionamientos
 */
public class CajaModule extends VBox {

    private final CajaController     cajaCtrl   = new CajaController();
    private final RegistroController  regCtrl    = new RegistroController();
    private final DateTimeFormatter   fmtDT      = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final DateTimeFormatter   fmtD       = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public CajaModule() {
        setPadding(new Insets(24, 28, 24, 28));
        setSpacing(16);
        setStyle("-fx-background-color:" + UI.BG + ";");
        construirUI();
    }

    private void construirUI() {
        // Encabezado
        Label titulo = new Label("💰 Caja");
        titulo.setFont(Font.font("System", FontWeight.BLACK, 20));
        titulo.setStyle("-fx-text-fill:" + UI.TEXT + ";");

        Session s = Session.getInstance();
        Label subTitulo = new Label("Estacionamiento: " + resolverNombreEst()
                + "  ·  Cajero: " + s.getNombreCompleto()
                + "  ·  " + LocalDateTime.now().format(fmtD));
        subTitulo.setStyle("-fx-text-fill:" + UI.MUTED + ";-fx-font-size:12px;");

        // TabPane principal
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setStyle("-fx-background-color:white;-fx-background-radius:12;");
        VBox.setVgrow(tabs, Priority.ALWAYS);

        tabs.getTabs().add(crearTabCobrar());
        tabs.getTabs().add(crearTabHistorial());
        tabs.getTabs().add(crearTabMetodosPago());

        // Corte de caja: solo Encargado y Admin
        if (s.getUsuario() != null && s.getUsuario().getRol() != 3) {
            tabs.getTabs().add(crearTabCorte());
        }

        getChildren().addAll(new VBox(4, titulo, subTitulo), tabs);
    }

    // ══════════════════════════════════════════════════════════
    //  TAB 1 — COBRAR
    // ══════════════════════════════════════════════════════════

    private Tab crearTabCobrar() {
        Tab tab = new Tab("💳 Cobrar");

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:white;-fx-background-color:white;");

        VBox contenido = new VBox(16);
        contenido.setPadding(new Insets(20));

        // Panel de búsqueda del registro
        VBox panelBusqueda = crearPanelBusqueda(contenido);
        contenido.getChildren().add(panelBusqueda);

        scroll.setContent(contenido);
        tab.setContent(scroll);
        return tab;
    }

    private VBox crearPanelBusqueda(VBox contenidoPadre) {
        VBox card = new VBox(14);
        card.setStyle(UI.CARD);
        card.setPadding(new Insets(20));

        Label tit = new Label("Buscar registro del vehículo");
        tit.setFont(Font.font("System", FontWeight.BOLD, 13));

        // Campos de búsqueda
        TextField fVehId = UI.campo("ID del vehículo");
        fVehId.setPrefWidth(180);

        Button btnBuscar = UI.btnPrimario("🔍 Buscar");
        btnBuscar.setPrefWidth(130);
        btnBuscar.setMaxWidth(130);

        HBox filaBusq = new HBox(10, UI.grupoCampo("ID Vehículo *", fVehId), btnBuscar);
        filaBusq.setAlignment(Pos.BOTTOM_LEFT);

        // Panel de resultado (se llena al buscar)
        VBox panelResultado = new VBox(14);

        Label errBusq = UI.errorLabel();

        btnBuscar.setOnAction(e -> {
            panelResultado.getChildren().clear();
            UI.setError(errBusq, null);
            try {
                int vehId = Integer.parseInt(fVehId.getText().trim());
                RegistroEntradaSalida reg = regCtrl.obtenerRegistroActivoDelVehiculo(vehId);

                if (reg == null) {
                    // Buscar el último registro finalizado
                    List<RegistroEntradaSalida> lista = regCtrl.obtenerRegistrosPorEstacionamiento(
                            resolverEstId());
                    reg = lista.stream()
                            .filter(r -> r.getVehiculoId() == vehId && "Finalizado".equals(r.getEstado()))
                            .findFirst()
                            .orElse(null);
                }

                if (reg == null) {
                    UI.setError(errBusq, "No se encontró registro para el vehículo #" + vehId
                            + ". Asegúrate de registrar su salida primero.");
                } else {
                    panelResultado.getChildren().add(crearPanelPago(reg));
                }
            } catch (NumberFormatException ex) {
                UI.setError(errBusq, "El ID del vehículo debe ser un número entero.");
            }
        });

        card.getChildren().addAll(tit, filaBusq, errBusq, panelResultado);
        return card;
    }

    private VBox crearPanelPago(RegistroEntradaSalida reg) {
        VBox card = new VBox(14);
        card.setStyle(UI.CARD);
        card.setPadding(new Insets(20));

        Label tit = new Label("Detalle del cobro");
        tit.setFont(Font.font("System", FontWeight.BOLD, 13));

        // Resumen del registro
        GridPane info = new GridPane();
        info.setHgap(20); info.setVgap(8);
        fila(info, 0, "Registro ID:", "#" + reg.getId());
        fila(info, 1, "Vehículo ID:", "#" + reg.getVehiculoId());
        fila(info, 2, "Cajón:", "#" + reg.getCajonId());
        fila(info, 3, "Entrada:",
                reg.getFechaEntrada() != null ? reg.getFechaEntrada().format(fmtDT) : "-");
        fila(info, 4, "Salida:",
                reg.getFechaSalida() != null ? reg.getFechaSalida().format(fmtDT) : "En curso");
        fila(info, 5, "Estado:", reg.getEstado());

        // Monto a cobrar
        Label lblMonto = new Label(String.format("$%.2f", reg.getMonto()));
        lblMonto.setFont(Font.font("System", FontWeight.BLACK, 32));
        lblMonto.setStyle("-fx-text-fill:" + UI.GREEN + ";");

        VBox boxMonto = new VBox(4,
                new Label("Monto a cobrar:") {{ setStyle("-fx-text-fill:" + UI.MUTED + ";-fx-font-size:11px;-fx-font-weight:bold;"); }},
                lblMonto
        );

        // Método de pago
        ComboBox<Pago.MetodoPago> comboMetodo = UI.combo();
        comboMetodo.getItems().addAll(Pago.MetodoPago.values());
        comboMetodo.setValue(Pago.MetodoPago.EFECTIVO);

        TextField fMontoPagado = UI.campo(String.format("%.2f", reg.getMonto()));
        fMontoPagado.setText(String.format("%.2f", reg.getMonto()));

        Label lblCambio = new Label("Cambio: $0.00");
        lblCambio.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:" + UI.BLUE + ";");

        // Calcular cambio en tiempo real
        fMontoPagado.textProperty().addListener((o, a, n) -> {
            try {
                double pagado = Double.parseDouble(n.trim());
                double cambio = Math.max(0, pagado - reg.getMonto());
                lblCambio.setText(String.format("Cambio: $%.2f", cambio));
                lblCambio.setStyle("-fx-font-size:13px;-fx-font-weight:bold;-fx-text-fill:"
                        + (cambio >= 0 ? UI.GREEN : UI.RED) + ";");
            } catch (NumberFormatException ignored) {
                lblCambio.setText("Cambio: --");
            }
        });

        GridPane formPago = new GridPane();
        formPago.setHgap(12); formPago.setVgap(12);
        formPago.add(UI.grupoCampo("Método de pago *", comboMetodo), 0, 0);
        formPago.add(UI.grupoCampo("Monto recibido ($) *", fMontoPagado), 1, 0);
        formPago.add(lblCambio, 0, 1, 2, 1);
        ColumnConstraints cc = new ColumnConstraints(); cc.setPercentWidth(50);
        formPago.getColumnConstraints().addAll(cc, new ColumnConstraints() {{ setPercentWidth(50); }});

        Label errPago = UI.errorLabel();
        // Área de ticket
        TextArea areaTicket = new TextArea();
        areaTicket.setStyle("-fx-font-family:monospace;-fx-font-size:11px;" + UI.FIELD);
        areaTicket.setPrefRowCount(18);
        areaTicket.setEditable(false);
        areaTicket.setVisible(false);
        areaTicket.setManaged(false);

        Button btnCobrar = UI.btnPrimario("💳 Registrar pago y generar ticket");
        btnCobrar.setMaxWidth(Double.MAX_VALUE);

        btnCobrar.setOnAction(e -> {
            UI.setError(errPago, null);
            try {
                double montoPagado = Double.parseDouble(fMontoPagado.getText().trim());
                Usuario cajero = Session.getInstance().getUsuario();

                Pago pago = cajaCtrl.registrarPago(reg, montoPagado,
                        comboMetodo.getValue(), cajero);

                if (pago != null) {
                    // Mostrar ticket
                    String ticket = cajaCtrl.generarTextoTicket(pago, reg, resolverNombreEst());
                    areaTicket.setText(ticket);
                    areaTicket.setVisible(true);
                    areaTicket.setManaged(true);
                    btnCobrar.setDisable(true);
                    btnCobrar.setText("✅ Pago registrado");
                    UI.mostrarInfo("Pago registrado",
                            "Ticket #" + pago.getNumeroTicket()
                                    + "\nCambio: $" + String.format("%.2f", pago.getCambio()));
                } else {
                    UI.setError(errPago, "No se pudo registrar el pago. Intenta de nuevo.");
                }
            } catch (NumberFormatException ex) {
                UI.setError(errPago, "El monto recibido debe ser un número válido.");
            } catch (SecurityException | IllegalArgumentException ex) {
                UI.setError(errPago, ex.getMessage());
            }
        });

        card.getChildren().addAll(tit, UI.separador(), boxMonto, info,
                UI.separador(), formPago, errPago, btnCobrar, areaTicket);
        return card;
    }

    // ══════════════════════════════════════════════════════════
    //  TAB 2 — HISTORIAL DE PAGOS
    // ══════════════════════════════════════════════════════════

    private Tab crearTabHistorial() {
        Tab tab = new Tab("📋 Historial");

        VBox contenido = new VBox(14);
        contenido.setPadding(new Insets(20));

        // Filtros de fecha
        HBox filtros = new HBox(12);
        filtros.setAlignment(Pos.CENTER_LEFT);

        DatePicker dpDesde = new DatePicker(java.time.LocalDate.now());
        DatePicker dpHasta = new DatePicker(java.time.LocalDate.now());
        dpDesde.setStyle(UI.FIELD);
        dpHasta.setStyle(UI.FIELD);

        Button btnFiltrar = UI.btnSecundario("🔍 Filtrar");
        Button btnHoy = UI.btnSecundario("Hoy");

        filtros.getChildren().addAll(
                UI.grupoCampo("Desde", dpDesde),
                UI.grupoCampo("Hasta", dpHasta),
                btnFiltrar, btnHoy);

        // Tabla
        TableView<Pago> tabla = new TableView<>();
        UI.estilizarTabla(tabla);
        ObservableList<Pago> datos = FXCollections.observableArrayList();
        tabla.setItems(datos);
        VBox.setVgrow(tabla, Priority.ALWAYS);

        colTabla(tabla, "Ticket", "numeroTicket", 140);
        TableColumn<Pago, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getFechaPago() != null
                        ? c.getValue().getFechaPago().format(fmtDT) : ""));
        colFecha.setPrefWidth(140);
        tabla.getColumns().add(colFecha);

        TableColumn<Pago, String> colMetodo = new TableColumn<>("Método");
        colMetodo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getMetodoPago() != null
                        ? c.getValue().getMetodoPago().getLabel() : ""));
        colMetodo.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); return; }
                String estilo = switch (v) {
                    case "Efectivo"         -> UI.badgeGreen();
                    case "Tarjeta crédito",
                         "Tarjeta débito"   -> UI.badgeBlue();
                    case "Transferencia"    -> UI.badgeAmber();
                    case "Convenio"         -> UI.badgeGray();
                    default                 -> UI.badgeGray();
                };
                setGraphic(UI.badge(v, estilo));
            }
        });
        tabla.getColumns().add(colMetodo);

        TableColumn<Pago, String> colMonto = new TableColumn<>("Monto");
        colMonto.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                String.format("$%.2f", c.getValue().getMonto())));
        colMonto.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); return; }
                setText(v);
                setStyle("-fx-font-weight:bold;-fx-text-fill:" + UI.GREEN + ";");
            }
        });
        tabla.getColumns().add(colMonto);

        colTabla(tabla, "Cajero", "cajeroNombre", 160);

        TableColumn<Pago, String> colAnulado = new TableColumn<>("Estado");
        colAnulado.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().isAnulado() ? "Anulado" : "Válido"));
        colAnulado.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); return; }
                setGraphic(UI.badge(v, "Anulado".equals(v) ? UI.badgeRed() : UI.badgeGreen()));
            }
        });
        tabla.getColumns().add(colAnulado);

        tabla.setPlaceholder(UI.panelVacio("📋", "No hay pagos en el período seleccionado"));

        // Resumen total abajo
        Label lblTotal = new Label("Total: $0.00");
        lblTotal.setFont(Font.font("System", FontWeight.BOLD, 14));
        lblTotal.setStyle("-fx-text-fill:" + UI.GREEN + ";");

        // Acción filtrar
        Runnable cargar = () -> {
            try {
                LocalDateTime desde = dpDesde.getValue().atStartOfDay();
                LocalDateTime hasta = dpHasta.getValue().atTime(23, 59, 59);
                List<Pago> lista = cajaCtrl.listarPagosEntreFechas(
                        Session.getInstance().getUsuario(), desde, hasta);
                datos.setAll(lista);
                double total = lista.stream()
                        .filter(p -> !p.isAnulado())
                        .mapToDouble(Pago::getMonto).sum();
                lblTotal.setText(String.format("Total del período: $%.2f  (%d transacciones)",
                        total, lista.size()));
            } catch (Exception ex) {
                UI.mostrarError("Error", ex.getMessage());
            }
        };

        btnFiltrar.setOnAction(e -> cargar.run());
        btnHoy.setOnAction(e -> {
            dpDesde.setValue(java.time.LocalDate.now());
            dpHasta.setValue(java.time.LocalDate.now());
            cargar.run();
        });

        cargar.run(); // Carga inicial con el día de hoy

        contenido.getChildren().addAll(filtros, tabla, lblTotal);
        tab.setContent(contenido);
        return tab;
    }

    // ══════════════════════════════════════════════════════════
    //  TAB 3 — MÉTODOS DE PAGO
    // ══════════════════════════════════════════════════════════

    private Tab crearTabMetodosPago() {
        Tab tab = new Tab("📊 Métodos de pago");

        VBox contenido = new VBox(16);
        contenido.setPadding(new Insets(20));

        Label tit = new Label("Resumen por método de pago — Hoy");
        tit.setFont(Font.font("System", FontWeight.BOLD, 14));

        // Cargar datos del día
        VBox cards = new VBox(10);
        try {
            LocalDateTime desde = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            LocalDateTime hasta = LocalDateTime.now();
            List<Pago> pagosHoy = cajaCtrl.listarPagosEntreFechas(
                    Session.getInstance().getUsuario(), desde, hasta);

            double totalEfec  = pagosHoy.stream().filter(p -> !p.isAnulado() && p.getMetodoPago() == Pago.MetodoPago.EFECTIVO).mapToDouble(Pago::getMonto).sum();
            double totalTC    = pagosHoy.stream().filter(p -> !p.isAnulado() && p.getMetodoPago() == Pago.MetodoPago.TARJETA_CREDITO).mapToDouble(Pago::getMonto).sum();
            double totalTD    = pagosHoy.stream().filter(p -> !p.isAnulado() && p.getMetodoPago() == Pago.MetodoPago.TARJETA_DEBITO).mapToDouble(Pago::getMonto).sum();
            double totalTrans = pagosHoy.stream().filter(p -> !p.isAnulado() && p.getMetodoPago() == Pago.MetodoPago.TRANSFERENCIA).mapToDouble(Pago::getMonto).sum();
            double totalConv  = pagosHoy.stream().filter(p -> !p.isAnulado() && p.getMetodoPago() == Pago.MetodoPago.CONVENIO).mapToDouble(Pago::getMonto).sum();
            double totalGral  = totalEfec + totalTC + totalTD + totalTrans + totalConv;

            cards.getChildren().addAll(
                    cardMetodo("💵 Efectivo",        totalEfec,  UI.GREEN,        totalGral),
                    cardMetodo("💳 Tarjeta Crédito", totalTC,    UI.BLUE,         totalGral),
                    cardMetodo("💳 Tarjeta Débito",  totalTD,    UI.BLUE,         totalGral),
                    cardMetodo("🔄 Transferencia",   totalTrans, UI.AMBER,        totalGral),
                    cardMetodo("🤝 Convenio",        totalConv,  UI.MUTED,        totalGral)
            );

            // Total general
            HBox totalBox = new HBox();
            totalBox.setPadding(new Insets(16));
            totalBox.setStyle("-fx-background-color:" + UI.SIDEBAR
                    + ";-fx-background-radius:12;");
            Label lTotal = new Label(String.format("TOTAL DEL DÍA:  $%.2f", totalGral));
            lTotal.setFont(Font.font("System", FontWeight.BLACK, 18));
            lTotal.setStyle("-fx-text-fill:" + UI.GOLD + ";");
            totalBox.setAlignment(Pos.CENTER);
            totalBox.getChildren().add(lTotal);
            cards.getChildren().add(totalBox);

        } catch (Exception ex) {
            cards.getChildren().add(UI.alertaError("Error al cargar: " + ex.getMessage()));
        }

        contenido.getChildren().addAll(tit, cards);

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:white;-fx-background-color:white;");
        tab.setContent(scroll);
        return tab;
    }

    private HBox cardMetodo(String nombre, double monto, String color, double total) {
        HBox card = new HBox(16);
        card.setPadding(new Insets(16, 20, 16, 20));
        card.setStyle(UI.CARD);
        card.setAlignment(Pos.CENTER_LEFT);

        Label lNombre = new Label(nombre);
        lNombre.setFont(Font.font("System", FontWeight.BOLD, 13));
        lNombre.setPrefWidth(180);

        // Barra de progreso visual
        double pct = total > 0 ? monto / total : 0;
        Region barBg = new Region();
        barBg.setPrefHeight(8);
        barBg.setPrefWidth(200);
        barBg.setStyle("-fx-background-color:#f1f5f9;-fx-background-radius:4;");

        Region barFill = new Region();
        barFill.setPrefHeight(8);
        barFill.setPrefWidth(200 * pct);
        barFill.setStyle("-fx-background-color:" + color + ";-fx-background-radius:4;");

        StackPane barra = new StackPane();
        barra.setAlignment(Pos.CENTER_LEFT);
        barra.getChildren().addAll(barBg, barFill);

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label lMonto = new Label(String.format("$%.2f  (%.0f%%)", monto, pct * 100));
        lMonto.setFont(Font.font("System", FontWeight.BOLD, 13));
        lMonto.setStyle("-fx-text-fill:" + color + ";");

        card.getChildren().addAll(lNombre, barra, sp, lMonto);
        return card;
    }

    // ══════════════════════════════════════════════════════════
    //  TAB 4 — CORTE DE CAJA (solo Encargado y Admin)
    // ══════════════════════════════════════════════════════════

    private Tab crearTabCorte() {
        Tab tab = new Tab("🧾 Corte de Caja");

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background:white;-fx-background-color:white;");

        VBox contenido = new VBox(16);
        contenido.setPadding(new Insets(20));

        // Panel para hacer el corte
        VBox cardNuevo = new VBox(14);
        cardNuevo.setStyle(UI.CARD);
        cardNuevo.setPadding(new Insets(20));

        Label tit = new Label("Realizar nuevo corte de caja");
        tit.setFont(Font.font("System", FontWeight.BOLD, 13));

        ComboBox<CorteCaja.TipoCorte> comboTipo = UI.combo();
        comboTipo.getItems().addAll(CorteCaja.TipoCorte.values());
        comboTipo.setValue(CorteCaja.TipoCorte.PARCIAL);

        TextArea fObs = UI.areaTexto("Observaciones del corte (opcional)...", 3);

        TextArea areaCorte = new TextArea();
        areaCorte.setStyle("-fx-font-family:monospace;-fx-font-size:11px;" + UI.FIELD);
        areaCorte.setPrefRowCount(20);
        areaCorte.setEditable(false);
        areaCorte.setVisible(false);
        areaCorte.setManaged(false);

        Label errCorte = UI.errorLabel();

        Button btnCorte = UI.btnPrimario("🧾 Realizar corte de caja");
        btnCorte.setMaxWidth(Double.MAX_VALUE);

        btnCorte.setOnAction(e -> {
            UI.setError(errCorte, null);
            boolean ok = UI.confirmar("Confirmar corte",
                    "¿Deseas realizar un " + comboTipo.getValue().getLabel()
                            + "?\nEsto registrará los totales del período actual.");
            if (!ok) return;
            try {
                CorteCaja corte = cajaCtrl.realizarCorte(
                        Session.getInstance().getUsuario(),
                        comboTipo.getValue(),
                        fObs.getText());
                if (corte != null) {
                    String texto = cajaCtrl.generarTextoCorte(corte, resolverNombreEst());
                    areaCorte.setText(texto);
                    areaCorte.setVisible(true);
                    areaCorte.setManaged(true);
                    UI.mostrarInfo("Corte realizado",
                            "Folio: " + corte.getFolioCorte()
                                    + "\nTotal: $" + String.format("%.2f", corte.getTotalGeneral()));
                } else {
                    UI.setError(errCorte, "No se pudo guardar el corte. Verifica la conexión.");
                }
            } catch (SecurityException ex) {
                UI.setError(errCorte, ex.getMessage());
            } catch (Exception ex) {
                UI.setError(errCorte, "Error: " + ex.getMessage());
            }
        });

        cardNuevo.getChildren().addAll(tit,
                UI.grupoCampo("Tipo de corte", comboTipo),
                UI.grupoCampo("Observaciones", fObs),
                errCorte, btnCorte, areaCorte);

        // Historial de cortes
        VBox cardHist = new VBox(12);
        cardHist.setStyle(UI.CARD);
        cardHist.setPadding(new Insets(20));

        Label titHist = new Label("Historial de cortes");
        titHist.setFont(Font.font("System", FontWeight.BOLD, 13));

        TableView<CorteCaja> tablaCortes = new TableView<>();
        UI.estilizarTabla(tablaCortes);
        tablaCortes.setPrefHeight(250);

        ObservableList<CorteCaja> datosCortes = FXCollections.observableArrayList();
        tablaCortes.setItems(datosCortes);

        TableColumn<CorteCaja, String> colFolioC = new TableColumn<>("Folio");
        colFolioC.setCellValueFactory(new PropertyValueFactory<>("folioCorte"));

        TableColumn<CorteCaja, String> colFechaC = new TableColumn<>("Fecha corte");
        colFechaC.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getFechaCorte() != null
                        ? c.getValue().getFechaCorte().format(fmtDT) : ""));
        colFechaC.setPrefWidth(140);

        TableColumn<CorteCaja, String> colCajeroC = new TableColumn<>("Cajero");
        colCajeroC.setCellValueFactory(new PropertyValueFactory<>("cajeroNombre"));

        TableColumn<CorteCaja, String> colTipoC = new TableColumn<>("Tipo");
        colTipoC.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getTipoCorte() != null
                        ? c.getValue().getTipoCorte().getLabel() : ""));

        TableColumn<CorteCaja, String> colTotalC = new TableColumn<>("Total");
        colTotalC.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                String.format("$%.2f", c.getValue().getTotalGeneral())));
        colTotalC.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setText(null); return; }
                setText(v);
                setStyle("-fx-font-weight:bold;-fx-text-fill:" + UI.GREEN + ";");
            }
        });

        TableColumn<CorteCaja, String> colTransC = new TableColumn<>("Transacciones");
        colTransC.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(c.getValue().getTotalTransacciones())));
        colTransC.setStyle("-fx-alignment:CENTER;");

        tablaCortes.getColumns().addAll(colFolioC, colFechaC, colCajeroC,
                colTipoC, colTotalC, colTransC);
        tablaCortes.setPlaceholder(UI.panelVacio("🧾", "No hay cortes registrados"));

        try {
            datosCortes.setAll(cajaCtrl.listarCortes(Session.getInstance().getUsuario()));
        } catch (Exception ex) {
            tablaCortes.setPlaceholder(UI.alertaError("Error: " + ex.getMessage()));
        }

        cardHist.getChildren().addAll(titHist, tablaCortes);
        contenido.getChildren().addAll(cardNuevo, cardHist);
        scroll.setContent(contenido);
        tab.setContent(scroll);
        return tab;
    }

    // ══════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════

    private void fila(GridPane grid, int row, String key, String val) {
        Label k = new Label(key);
        k.setStyle("-fx-text-fill:" + UI.MUTED + ";-fx-font-size:11px;-fx-font-weight:bold;");
        Label v = new Label(val);
        v.setStyle("-fx-font-size:12px;");
        grid.add(k, 0, row);
        grid.add(v, 1, row);
    }

    private <T> void colTabla(TableView<T> tv, String nombre, String prop, double w) {
        TableColumn<T, ?> col = new TableColumn<>(nombre);
        col.setCellValueFactory(new PropertyValueFactory<>(prop));
        col.setPrefWidth(w);
        tv.getColumns().add(col);
    }

    private int resolverEstId() {
        Usuario u = Session.getInstance().getUsuario();
        if (u == null || u.getEstacionamientoId() == null) return 1;
        return u.getEstacionamientoId();
    }

    private String resolverNombreEst() {
        Usuario u = Session.getInstance().getUsuario();
        if (u == null) return "—";
        if (u.getNombreEstacionamiento() != null) return u.getNombreEstacionamiento();
        if (u.getEstacionamientoId() != null) return "Est. #" + u.getEstacionamientoId();
        return "Admin Global";
    }
}

// ── Clase pública para el Sidebar ──────────────────────────────────────────
class CajaImpl extends CajaModule {
    CajaImpl() { super(); }
}
