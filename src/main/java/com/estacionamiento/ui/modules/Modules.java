package com.estacionamiento.ui.modules;

import com.estacionamiento.controladores.*;
import com.estacionamiento.modelos.*;
import com.estacionamiento.ui.Session;
import com.estacionamiento.ui.UI;
import com.estacionamiento.utilidades.GeneradorPDF;
import com.estacionamiento.utilidades.GeneradorExcel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.DirectoryChooser;
import com.estacionamiento.dao.ConexionDB;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

// ─────────────────────────────────────────────────────────────
//  DASHBOARD
// ─────────────────────────────────────────────────────────────
class DashboardImpl extends ScrollPane {

    private final EstacionamientoController estCtrl = new EstacionamientoController();
    private final RegistroController regCtrl = new RegistroController();
    private final PensionController penCtrl  = new PensionController();
    private final GeneradorPDF pdfGen = new GeneradorPDF(System.getProperty("user.dir") + "/reportes");
    private final GeneradorExcel excelGen = new GeneradorExcel(System.getProperty("user.dir") + "/reportes");

    DashboardImpl() {
        setFitToWidth(true);
        setStyle("-fx-background:" + UI.BG + ";-fx-background-color:" + UI.BG + ";");

        VBox contenido = new VBox(20);
        contenido.setPadding(new Insets(24, 28, 24, 28));
        contenido.setStyle("-fx-background-color:" + UI.BG + ";");

        // Bienvenida
        Session s = Session.getInstance();
        Label bienvenida = new Label("Bienvenido, " + s.getNombreCompleto() + " 👋");
        bienvenida.setFont(Font.font("System", FontWeight.BOLD, 18));

        // Stats de cajones
        HBox stats = crearStats();

        // Pensiones activas
        VBox secPensiones = crearSeccionPensiones();

        // Reportes
        VBox secReportes = crearSeccionReportes();

        contenido.getChildren().addAll(bienvenida, stats, secPensiones, secReportes);
        setContent(contenido);
    }

    private HBox crearStats() {
        HBox hb = new HBox(16);
        try {
            Session s = Session.getInstance();
            Integer estId = s.getEstacionamientoActualId();

            int disponibles = 0, ocupados = 0, totalCajones = 0;
            List<Estacionamiento> ests;

            if (estId != null) {
                ests = List.of(estCtrl.obtenerEstacionamiento(estId));
            } else {
                ests = estCtrl.obtenerTodosLosEstacionamientos();
            }

            for (Estacionamiento e : ests) {
                disponibles  += e.getCajonesDisponibles();
                totalCajones += e.getTotalCajones();
            }
            ocupados = totalCajones - disponibles;
            int pct = totalCajones > 0 ? (ocupados * 100 / totalCajones) : 0;

            double ingresoDia = estId != null
                ? regCtrl.obtenerIngresoDelDia(estId, LocalDateTime.now())
                : 0;

            hb.getChildren().addAll(
                stat("🚗 Ocupados",   String.valueOf(ocupados),    "cajones",          UI.RED),
                stat("✅ Libres",      String.valueOf(disponibles), "disponibles",      UI.GREEN),
                stat("📊 Ocupación",  pct + "%",                  "del total",        UI.BLUE),
                stat("💰 Ingreso hoy", String.format("$%.0f", ingresoDia), "del día", UI.AMBER)
            );
        } catch (Exception e) {
            hb.getChildren().add(UI.alertaError("Error al cargar estadísticas: " + e.getMessage()));
        }
        return hb;
    }

    private VBox stat(String label, String valor, String detalle, String color) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(16, 18, 16, 18));
        card.setStyle(UI.CARD);
        HBox.setHgrow(card, Priority.ALWAYS);
        Label lbl = new Label(label); lbl.setStyle("-fx-text-fill:" + UI.MUTED + ";-fx-font-size:10px;-fx-font-weight:bold;");
        Label val = new Label(valor); val.setFont(Font.font("System", FontWeight.BLACK, 24)); val.setStyle("-fx-text-fill:" + color + ";");
        Label det = new Label(detalle); det.setStyle("-fx-text-fill:" + UI.MUTED + ";-fx-font-size:10px;");
        card.getChildren().addAll(lbl, val, det);
        return card;
    }

    private VBox crearSeccionPensiones() {
        VBox card = new VBox(12);
        card.setStyle(UI.CARD); card.setPadding(new Insets(20));

        Label titulo = new Label("Pensiones Activas");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 13));

        try {
            Session s = Session.getInstance();
            Integer estId = s.getEstacionamientoActualId();
            if (estId == null) {
                card.getChildren().addAll(titulo, UI.panelVacio("👥", "Seleccione un estacionamiento para ver pensiones"));
                return card;
            }
            List<Pension> pensiones = penCtrl.obtenerPensionesActivas(estId);

            if (pensiones.isEmpty()) {
                card.getChildren().addAll(titulo, UI.panelVacio("👥", "No hay pensiones activas"));
                return card;
            }

            for (Pension p : pensiones) {
                HBox fila = new HBox(14);
                fila.setAlignment(Pos.CENTER_LEFT);
                fila.setPadding(new Insets(8, 0, 8, 0));
                fila.setStyle("-fx-border-color:transparent transparent #f1f5f9 transparent;-fx-border-width:1;");

                Label cliente = new Label("Cliente #" + p.getClienteId());
                cliente.setStyle("-fx-font-weight:bold;");
                Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
                Label monto = new Label(String.format("$%.2f", p.getMonto()));
                monto.setStyle("-fx-text-fill:" + UI.GREEN + ";-fx-font-weight:bold;");
                String estadoTexto = penCtrl.calcularEstado(p);
                Label estado = UI.badge(estadoTexto,
                    "Activa".equals(estadoTexto) ? UI.badgeGreen() :
                    "Próxima a vencer".equals(estadoTexto) ? UI.badgeBlue() : UI.badgeRed());

                fila.getChildren().addAll(cliente, sp, monto, estado);
                card.getChildren().add(fila);
            }
        } catch (Exception e) {
            card.getChildren().add(UI.alertaError("Error: " + e.getMessage()));
        }

        card.getChildren().add(0, titulo);
        return card;
    }

    private VBox crearSeccionReportes() {
        VBox card = new VBox(12);
        card.setStyle(UI.CARD);
        card.setPadding(new Insets(20));

        Label titulo = new Label("Reportes de Base de Datos");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 13));

        HBox botones = new HBox(12);
        botones.setAlignment(Pos.CENTER_LEFT);

        Button btnPDF = UI.btnPrimario("📄 Generar PDF");
        btnPDF.setOnAction(e -> {
            boolean exito = pdfGen.generarReporteCompletoBD();
            if (exito) {
                UI.mostrarInfo("Éxito", "PDF generado correctamente");
            } else {
                UI.mostrarError("Error", "No se pudo generar el PDF");
            }
        });

        Button btnExcel = UI.btnPrimario("📊 Generar Excel");
        btnPDF.setMaxWidth(Region.USE_PREF_SIZE);
        btnExcel.setMaxWidth(Region.USE_PREF_SIZE);
        btnExcel.setOnAction(e -> {
            boolean exito = excelGen.generarReporteCompletoBD();
            if (exito) {
                UI.mostrarInfo("Éxito", "Excel generado correctamente");
            } else {
                UI.mostrarError("Error", "No se pudo generar el Excel");
            }
        });

        botones.getChildren().addAll(btnPDF, btnExcel);
        card.getChildren().addAll(titulo, botones);
        return card;
    }
}

// ─────────────────────────────────────────────────────────────
//  CLIENTES
// ─────────────────────────────────────────────────────────────
class ClientesImpl extends VBox {

    private final ClienteController ctrl = new ClienteController();
    private ObservableList<Cliente> datos;
    private TableView<Cliente> tabla;
    private TextField busqueda;

    ClientesImpl() {
        setPadding(new Insets(24, 28, 24, 28));
        setSpacing(16);
        setStyle("-fx-background-color:" + UI.BG + ";");
        construir();
        cargar();
    }

    private void construir() {
        Button btnNuevo = UI.btnPrimario("+ Nuevo cliente");
        btnNuevo.setOnAction(e -> formulario(null));

        busqueda = UI.campo("🔍  Buscar por nombre, documento...");
        busqueda.textProperty().addListener((o, a, n) -> filtrar(n));

        tabla = new TableView<>();
        UI.estilizarTabla(tabla);
        datos = FXCollections.observableArrayList();
        tabla.setItems(datos);
        VBox.setVgrow(tabla, Priority.ALWAYS);

        col("ID", "id", 50);
        colStr("Nombre", c -> c.getNombre() + " " + c.getApellido(), 200);
        col("Teléfono", "telefono", 130);
        col("Email", "email", 180);
        col("Documento", "numeroDocumento", 130);
        colAcciones();
        tabla.setPlaceholder(UI.panelVacio("👤", "No hay clientes"));

        getChildren().addAll(UI.encabezado("Clientes", "Gestión de clientes registrados", btnNuevo),
                busqueda, tabla);
    }

    private void col(String name, String prop, double w) {
        TableColumn<Cliente, ?> c = new TableColumn<>(name);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        c.setPrefWidth(w);
        tabla.getColumns().add(c);
    }

    @SuppressWarnings("unchecked")
    private void colStr(String name, java.util.function.Function<Cliente, String> fn, double w) {
        TableColumn<Cliente, String> c = new TableColumn<>(name);
        c.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(fn.apply(data.getValue())));
        c.setPrefWidth(w);
        tabla.getColumns().add(c);
    }

    private void colAcciones() {
        TableColumn<Cliente, Void> c = new TableColumn<>("Acciones");
        c.setCellFactory(col -> new TableCell<>() {
            final Button historial = UI.btnPrimario("📜 Historial");
            final Button edit = UI.btnSecundario("✏️ Editar");
            final Button del  = UI.btnPeligro("🗑️");
            { historial.setStyle(historial.getStyle() + UI.BTN_SMALL);
              edit.setStyle(edit.getStyle() + UI.BTN_SMALL);
              del.setStyle(del.getStyle() + UI.BTN_SMALL);
              historial.setOnAction(e -> mostrarHistorial(getTableView().getItems().get(getIndex())));
              edit.setOnAction(e -> formulario(getTableView().getItems().get(getIndex())));
              del.setOnAction(e -> {
                  Cliente cl = getTableView().getItems().get(getIndex());
                  if (UI.confirmar("Eliminar", "¿Eliminar a " + cl.getNombre() + "?")) {
                      ctrl.eliminarCliente(cl.getId()); cargar();
                  }
              }); }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                HBox h = new HBox(6, historial, edit, del); h.setAlignment(Pos.CENTER_LEFT); setGraphic(h);
            }
        });
        c.setPrefWidth(250);
        tabla.getColumns().add(c);
    }

    private void cargar() {
        try { datos.setAll(ctrl.obtenerTodosLosClientes()); }
        catch (Exception e) { UI.mostrarError("Error", e.getMessage()); }
    }

    private void filtrar(String q) {
        try {
            if (q == null || q.isBlank()) { cargar(); return; }
            datos.setAll(ctrl.obtenerTodosLosClientes().stream()
                .filter(c -> (c.getNombre()+" "+c.getApellido()).toLowerCase().contains(q.toLowerCase())
                           || (c.getNumeroDocumento() != null && c.getNumeroDocumento().contains(q))
                           || (c.getEmail() != null && c.getEmail().toLowerCase().contains(q.toLowerCase())))
                .toList());
        } catch (Exception e) { cargar(); }
    }

    private void mostrarHistorial(Cliente cliente) {
        Stage v = new Stage(); v.initModality(Modality.APPLICATION_MODAL);
        v.setTitle("Historial de " + cliente.getNombre() + " " + cliente.getApellido()); v.setResizable(false);

        HistorialController historialCtrl = new HistorialController();
        TableView<HistorialEvento> tablaHistorial = new TableView<>();
        UI.estilizarTabla(tablaHistorial);
        ObservableList<HistorialEvento> datosHistorial = FXCollections.observableArrayList();
        tablaHistorial.setItems(datosHistorial);
        VBox.setVgrow(tablaHistorial, Priority.ALWAYS);

        TableColumn<HistorialEvento, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
            c.getValue().getFecha() != null ? c.getValue().getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : ""));
        colFecha.setPrefWidth(140);

        TableColumn<HistorialEvento, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colTipo.setPrefWidth(120);

        TableColumn<HistorialEvento, String> colDesc = new TableColumn<>("Descripción");
        colDesc.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colDesc.setPrefWidth(300);

        TableColumn<HistorialEvento, String> colMonto = new TableColumn<>("Monto");
        colMonto.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
            c.getValue().getMonto() > 0 ? String.format("$%.2f", c.getValue().getMonto()) : "-"));
        colMonto.setPrefWidth(100);

        tablaHistorial.getColumns().addAll(colFecha, colTipo, colDesc, colMonto);
        tablaHistorial.setPlaceholder(UI.panelVacio("🕒", "No hay eventos para este cliente"));

        try {
            datosHistorial.setAll(historialCtrl.obtenerEventosPorCliente(cliente.getId()));
        } catch (Exception e) {
            UI.mostrarError("Error", e.getMessage());
        }

        VBox cont = new VBox(14, UI.encabezado("Historial de cliente", "Eventos y movimientos registrados"), tablaHistorial);
        cont.setPadding(new Insets(24));
        cont.setPrefSize(700, 420);

        v.setScene(new Scene(cont));
        v.showAndWait();
    }

    private void formulario(Cliente editar) {
        Stage v = new Stage(); v.initModality(Modality.APPLICATION_MODAL);
        v.setTitle(editar == null ? "Nuevo cliente" : "Editar cliente"); v.setResizable(false);

        VBox cont = new VBox(14); cont.setPadding(new Insets(24)); cont.setPrefWidth(480);
        Label titulo = new Label(editar == null ? "➕ Nuevo cliente" : "✏️ Editar cliente");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 15));

        TextField fNombre   = UI.campo("Nombre");
        TextField fApellido = UI.campo("Apellido");
        TextField fTelefono = UI.campo("Teléfono");
        TextField fEmail    = UI.campo("Email");
        TextField fDoc      = UI.campo("Número de documento");
        TextField fCiudad   = UI.campo("Ciudad");

        if (editar != null) {
            fNombre.setText(editar.getNombre()); fApellido.setText(editar.getApellido());
            fTelefono.setText(editar.getTelefono()); fEmail.setText(editar.getEmail());
            fDoc.setText(editar.getNumeroDocumento()); fCiudad.setText(editar.getCiudad());
        }

        GridPane grid = new GridPane(); grid.setHgap(12); grid.setVgap(12);
        grid.add(UI.grupoCampo("Nombre *", fNombre),      0, 0);
        grid.add(UI.grupoCampo("Apellido *", fApellido),  1, 0);
        grid.add(UI.grupoCampo("Teléfono", fTelefono),    0, 1);
        grid.add(UI.grupoCampo("Email", fEmail),           1, 1);
        grid.add(UI.grupoCampo("Documento *", fDoc),      0, 2);
        grid.add(UI.grupoCampo("Ciudad", fCiudad),         1, 2);
        ColumnConstraints cc = new ColumnConstraints(); cc.setPercentWidth(50);
        grid.getColumnConstraints().addAll(cc, new ColumnConstraints() {{ setPercentWidth(50); }});

        Label err = UI.errorLabel();
        Button guardar = UI.btnPrimario(editar == null ? "Guardar" : "Actualizar");
        Button cancelar = UI.btnSecundario("Cancelar");
        cancelar.setOnAction(e -> v.close());

        guardar.setOnAction(e -> {
            Cliente c = editar != null ? editar : new Cliente();
            c.setNombre(fNombre.getText().trim()); c.setApellido(fApellido.getText().trim());
            c.setTelefono(fTelefono.getText().trim()); c.setEmail(fEmail.getText().trim());
            c.setNumeroDocumento(fDoc.getText().trim()); c.setCiudad(fCiudad.getText().trim());
            c.setActivo(true);

            String error = ctrl.validarCliente(c);
            if (error != null) { UI.setError(err, error); return; }

            boolean ok = editar == null ? ctrl.crearCliente(c) : ctrl.actualizarCliente(c);
            if (ok) { cargar(); v.close(); }
            else UI.setError(err, "Error al guardar. Intenta de nuevo.");
        });

        HBox bRow = new HBox(10, cancelar, guardar); bRow.setAlignment(Pos.CENTER_RIGHT);
        cont.getChildren().addAll(titulo, UI.separador(), grid, err, UI.separador(), bRow);
        v.setScene(new Scene(cont)); v.showAndWait();
    }
}

// ─────────────────────────────────────────────────────────────
//  VEHICULOS
// ─────────────────────────────────────────────────────────────
class VehiculosImpl extends VBox {

    private final VehiculoController ctrl = new VehiculoController();
    private ObservableList<Vehiculo> datos;
    private TableView<Vehiculo> tabla;

    VehiculosImpl() {
        setPadding(new Insets(24, 28, 24, 28));
        setSpacing(16);
        setStyle("-fx-background-color:" + UI.BG + ";");
        construir();
        cargar();
    }

    private void construir() {
        tabla = new TableView<>();
        UI.estilizarTabla(tabla);
        datos = FXCollections.observableArrayList();
        tabla.setItems(datos);
        VBox.setVgrow(tabla, Priority.ALWAYS);

        col("ID", "id", 70);
        col("Placa", "patente", 130);
        col("Tipo", "tipo", 110);
        col("Color", "color", 120);
        col("Marca", "marca", 130);
        col("Modelo", "modelo", 130);
        col("Cliente ID", "clienteId", 100);

        tabla.setPlaceholder(UI.panelVacio("Auto", "No hay vehiculos con historial"));
        getChildren().addAll(UI.encabezado("Vehiculos", "Historial de vehiculos que han entrado"), tabla);
    }

    private void col(String name, String prop, double w) {
        TableColumn<Vehiculo, ?> c = new TableColumn<>(name);
        c.setCellValueFactory(new PropertyValueFactory<>(prop));
        c.setPrefWidth(w);
        tabla.getColumns().add(c);
    }

    private void cargar() {
        try {
            datos.setAll(ctrl.obtenerVehiculosConHistorial());
        } catch (Exception e) {
            UI.mostrarError("Error", e.getMessage());
        }
    }
}

// -----------------------------------------------------------------------------
//  CAJONES
// ─────────────────────────────────────────────────────────────
class CajonesImpl extends VBox {

    private final EstacionamientoController ctrl = new EstacionamientoController();
    private ObservableList<Cajon> datos;
    private TableView<Cajon> tabla;
    private FlowPane gridVisual;

    CajonesImpl() {
        setPadding(new Insets(24,28,24,28)); setSpacing(16);
        setStyle("-fx-background-color:"+UI.BG+";");
        construir(); cargar();
    }

    private void construir() {
        Button btnNuevo = UI.btnPrimario("+ Nuevo cajón");
        btnNuevo.setOnAction(e -> formulario(null));

        // Grid visual
        gridVisual = new FlowPane(); gridVisual.setHgap(8); gridVisual.setVgap(8);
        VBox cardGrid = new VBox(12); cardGrid.setStyle(UI.CARD); cardGrid.setPadding(new Insets(20));
        Label tGrid = new Label("Mapa de Cajones"); tGrid.setFont(Font.font("System",FontWeight.BOLD,13));
        cardGrid.getChildren().addAll(tGrid, gridVisual);

        // Tabla
        tabla = new TableView<>(); UI.estilizarTabla(tabla);
        datos = FXCollections.observableArrayList(); tabla.setItems(datos);
        tabla.setPrefHeight(300);

        TableColumn<Cajon,Integer> colNum = new TableColumn<>("#");
        colNum.setCellValueFactory(new PropertyValueFactory<>("numero")); colNum.setPrefWidth(60);
        TableColumn<Cajon,String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        TableColumn<Cajon,String> colEst = new TableColumn<>("Estado");
        colEst.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colEst.setCellFactory(col -> new TableCell<>(){
            @Override protected void updateItem(String val, boolean empty){
                super.updateItem(val,empty); if(empty||val==null){setGraphic(null);return;}
                String estilo = switch(val){
                    case "libre" -> UI.badgeGreen();
                    case "ocupado"    -> UI.badgeRed();
                    case "reservado" -> UI.badgeAmber();
                    case "pensionado" -> UI.badgeBlue();
                    case "fuera de servicio" -> UI.badgeGray();
                    default           -> UI.badgeGray();
                };
                setGraphic(UI.badge(val,estilo));
            }
        });

        TableColumn<Cajon,Void> colAcc = new TableColumn<>("Acciones");
        colAcc.setCellFactory(col -> new TableCell<>(){
            final Button e=UI.btnSecundario("✏️"); final Button d=UI.btnPeligro("🗑️");
            { e.setStyle(e.getStyle()+UI.BTN_SMALL); d.setStyle(d.getStyle()+UI.BTN_SMALL);
              e.setOnAction(ev->formulario(getTableView().getItems().get(getIndex())));
              d.setOnAction(ev->{ Cajon c=getTableView().getItems().get(getIndex());
                  if(UI.confirmar("Eliminar","¿Eliminar cajón #"+c.getNumero()+"?")){
                      ctrl.eliminarCajon(c.getId()); cargar();}});
            }
            @Override protected void updateItem(Void v,boolean empty){
                super.updateItem(v,empty); if(empty){setGraphic(null);return;}
                HBox h=new HBox(6,e,d);h.setAlignment(Pos.CENTER_LEFT);setGraphic(h);}
        }); colAcc.setPrefWidth(120);

        tabla.getColumns().addAll(colNum,colTipo,colEst,colAcc);
        tabla.setPlaceholder(UI.panelVacio("🅿️","No hay cajones"));

        VBox cardTabla = new VBox(12); cardTabla.setStyle(UI.CARD); cardTabla.setPadding(new Insets(20));
        Label tTabla = new Label("Administrar Cajones"); tTabla.setFont(Font.font("System",FontWeight.BOLD,13));
        cardTabla.getChildren().addAll(tTabla,tabla);

        getChildren().addAll(UI.encabezado("Cajones","Estado y gestión de cajones",btnNuevo), cardGrid, cardTabla);
    }

    private void cargar() {
        try {
            Session s = Session.getInstance();
            Integer estId = s.getEstacionamientoActualId();
            if (estId == null) {
                UI.mostrarError("Error", "Debe seleccionar un estacionamiento");
                return;
            }
            List<Cajon> lista = ctrl.obtenerCajonesPorEstacionamiento(estId);
            datos.setAll(lista);
            actualizarGrid(lista);
        } catch (Exception e){ UI.mostrarError("Error",e.getMessage()); }
    }

    private void actualizarGrid(List<Cajon> cajones) {
        gridVisual.getChildren().clear();
        for (Cajon c : cajones) {
            VBox caja = new VBox(3); caja.setAlignment(Pos.CENTER);
            caja.setPrefSize(58,52); caja.setMaxSize(58,52); caja.setCursor(javafx.scene.Cursor.HAND);
            Label num = new Label(String.valueOf(c.getNumero()));
            num.setFont(Font.font("System",FontWeight.BLACK,16));
            Label est = new Label(c.getEstado() != null ? c.getEstado().substring(0,Math.min(4,c.getEstado().length()))+"." : "");
            est.setStyle("-fx-font-size:8px;-fx-font-weight:bold;");
            caja.getChildren().addAll(num,est);
            caja.setStyle(estiloGrid(c.getEstado()));
            Tooltip.install(caja, new Tooltip("Cajón #"+c.getNumero()+"\n"+c.getTipo()+"\n"+c.getEstado()));
            caja.setOnMouseClicked(ev->formulario(c));
            gridVisual.getChildren().add(caja);
        }
    }

    private String estiloGrid(String estado) {
        String base = "-fx-background-radius:8;-fx-border-radius:8;-fx-border-width:2;";
        if (estado == null) return base + "-fx-background-color:#f1f5f9;-fx-border-color:#e2e8f0;";
        return switch(estado) {
            case "libre"   -> base+"-fx-background-color:#f0fdf4;-fx-border-color:#86efac;-fx-text-fill:#16a34a;";
            case "ocupado"      -> base+"-fx-background-color:#fff1f2;-fx-border-color:#fca5a5;-fx-text-fill:#ef4444;";
            case "reservado"-> base+"-fx-background-color:#fef3c7;-fx-border-color:#fbbf24;-fx-text-fill:#d97706;";
            case "pensionado"-> base+"-fx-background-color:#e0f2fe;-fx-border-color:#38bdf8;-fx-text-fill:#0284c7;";
            case "fuera de servicio"-> base+"-fx-background-color:#f3f4f6;-fx-border-color:#9ca3af;-fx-text-fill:#6b7280;";
            default             -> base+"-fx-background-color:#f8fafc;-fx-border-color:#e2e8f0;";
        };
    }

    private void formulario(Cajon editar) {
        Stage ven = new Stage(); ven.initModality(Modality.APPLICATION_MODAL);
        ven.setTitle(editar==null?"Nuevo cajón":"Editar cajón"); ven.setResizable(false);

        VBox cont = new VBox(14); cont.setPadding(new Insets(24)); cont.setPrefWidth(400);
        Label titulo = new Label(editar==null?"➕ Nuevo cajón":"✏️ Editar cajón #"+(editar!=null?editar.getNumero():""));
        titulo.setFont(Font.font("System",FontWeight.BOLD,15));

        TextField fNum = UI.campo("Ej: 15");
        ComboBox<String> comboTipo  = UI.combo();
        comboTipo.getItems().addAll("Normal","Minusválido","Preferente"); comboTipo.setValue("Normal");
        ComboBox<String> comboEst = UI.combo();
        comboEst.getItems().addAll("libre","ocupado","reservado","pensionado","fuera de servicio"); comboEst.setValue("libre");

        if (editar!=null){ fNum.setText(String.valueOf(editar.getNumero())); fNum.setDisable(true);
            if(editar.getTipo()!=null) comboTipo.setValue(editar.getTipo());
            if(editar.getEstado()!=null) comboEst.setValue(editar.getEstado()); }

        VBox form = new VBox(12);
        form.getChildren().addAll(UI.grupoCampo("Número *",fNum),UI.grupoCampo("Tipo *",comboTipo),UI.grupoCampo("Estado",comboEst));

        Label err = UI.errorLabel();
        Button guardar = UI.btnPrimario(editar==null?"Guardar":"Actualizar");
        Button cancelar = UI.btnSecundario("Cancelar"); cancelar.setOnAction(e->ven.close());

        guardar.setOnAction(e -> {
            try {
                Session s = Session.getInstance();
                Integer estId = s.getEstacionamientoActualId();
                if (estId == null) {
                    UI.setError(err, "Debe seleccionar un estacionamiento");
                    return;
                }
                int num = Integer.parseInt(fNum.getText().trim());
                Cajon c = editar!=null ? editar : new Cajon(num, comboTipo.getValue(), comboEst.getValue(), estId);
                c.setTipo(comboTipo.getValue()); c.setEstado(comboEst.getValue());
                if (editar==null) c.setEstacionamientoId(estId);

                String error = ctrl.validarCajon(c);
                if(error!=null){ UI.setError(err,error); return; }

                boolean ok = editar==null ? ctrl.crearCajon(c) : ctrl.actualizarCajon(c);
                if(ok){ cargar(); ven.close(); } else UI.setError(err,"Error al guardar.");
            } catch(NumberFormatException ex){ UI.setError(err,"El número debe ser entero."); }
        });

        HBox bRow=new HBox(10,cancelar,guardar); bRow.setAlignment(Pos.CENTER_RIGHT);
        cont.getChildren().addAll(titulo,UI.separador(),form,err,UI.separador(),bRow);
        ven.setScene(new Scene(cont)); ven.showAndWait();
    }
}

// ─────────────────────────────────────────────────────────────
//  ESTACIONAMIENTOS
// ─────────────────────────────────────────────────────────────
class EstacionamientosImpl extends VBox {

    private final EstacionamientoController ctrl = new EstacionamientoController();
    private ObservableList<Estacionamiento> datos;
    private TableView<Estacionamiento> tabla;

    EstacionamientosImpl() {
        setPadding(new Insets(24,28,24,28)); setSpacing(16);
        setStyle("-fx-background-color:"+UI.BG+";");
        construir(); cargar();
    }

    private void construir() {
        Button btnNuevo = UI.btnPrimario("+ Nuevo estacionamiento");
        btnNuevo.setOnAction(e->formulario(null));

        tabla = new TableView<>(); UI.estilizarTabla(tabla);
        datos = FXCollections.observableArrayList(); tabla.setItems(datos);
        VBox.setVgrow(tabla,Priority.ALWAYS);

        colStr("Nombre", Estacionamiento::getNombre, 180);
        colStr("Dirección", Estacionamiento::getDireccion, 220);
        colStr("Teléfono", Estacionamiento::getTelefono, 130);
        TableColumn<Estacionamiento,Integer> colTot = new TableColumn<>("Total");
        colTot.setCellValueFactory(new PropertyValueFactory<>("totalCajones")); colTot.setPrefWidth(80); tabla.getColumns().add(colTot);
        TableColumn<Estacionamiento,Integer> colDisp = new TableColumn<>("Disponibles");
        colDisp.setCellValueFactory(new PropertyValueFactory<>("cajonesDisponibles")); colDisp.setPrefWidth(100); tabla.getColumns().add(colDisp);

        TableColumn<Estacionamiento,Void> colAcc = new TableColumn<>("Acciones");
        colAcc.setCellFactory(col->new TableCell<>(){
            final Button e=UI.btnSecundario("✏️"); final Button d=UI.btnPeligro("🗑️");
            { e.setStyle(e.getStyle()+UI.BTN_SMALL); d.setStyle(d.getStyle()+UI.BTN_SMALL);
              e.setOnAction(ev->formulario(getTableView().getItems().get(getIndex())));
              d.setOnAction(ev->UI.mostrarInfo("Info","Eliminar estacionamiento no está disponible por seguridad.")); }
            @Override protected void updateItem(Void v,boolean empty){
                super.updateItem(v,empty); if(empty){setGraphic(null);return;}
                HBox h=new HBox(6,e,d);h.setAlignment(Pos.CENTER_LEFT);setGraphic(h);}
        }); colAcc.setPrefWidth(130); tabla.getColumns().add(colAcc);
        tabla.setPlaceholder(UI.panelVacio("🏢","No hay estacionamientos"));

        getChildren().addAll(UI.encabezado("Estacionamientos","Sucursales registradas",btnNuevo),tabla);
    }

    @SuppressWarnings("unchecked")
    private void colStr(String name, java.util.function.Function<Estacionamiento,String> fn, double w){
        TableColumn<Estacionamiento,String> c=new TableColumn<>(name);
        c.setCellValueFactory(d->new javafx.beans.property.SimpleStringProperty(fn.apply(d.getValue())));
        c.setPrefWidth(w); tabla.getColumns().add(c);
    }

    private void cargar(){ try{datos.setAll(ctrl.obtenerTodosLosEstacionamientos());}catch(Exception e){UI.mostrarError("Error",e.getMessage());} }

    private void formulario(Estacionamiento editar){
        Stage ven=new Stage(); ven.initModality(Modality.APPLICATION_MODAL);
        ven.setTitle(editar==null?"Nuevo estacionamiento":"Editar estacionamiento"); ven.setResizable(false);

        VBox cont=new VBox(14); cont.setPadding(new Insets(24)); cont.setPrefWidth(480);
        Label titulo=new Label(editar==null?"➕ Nuevo estacionamiento":"✏️ Editar estacionamiento");
        titulo.setFont(Font.font("System",FontWeight.BOLD,15));

        TextField fNombre=UI.campo("Nombre"); TextField fDir=UI.campo("Dirección");
        TextField fTel=UI.campo("Teléfono"); TextField fEmail=UI.campo("Email");
        TextField fCajones=UI.campo("Total cajones");

        if(editar!=null){ fNombre.setText(editar.getNombre()); fDir.setText(editar.getDireccion());
            fTel.setText(editar.getTelefono()); fEmail.setText(editar.getEmail());
            fCajones.setText(String.valueOf(editar.getTotalCajones())); }

        GridPane grid=new GridPane(); grid.setHgap(12); grid.setVgap(12);
        grid.add(UI.grupoCampo("Nombre *",fNombre),0,0,2,1);
        grid.add(UI.grupoCampo("Dirección *",fDir),0,1,2,1);
        grid.add(UI.grupoCampo("Teléfono",fTel),0,2);
        grid.add(UI.grupoCampo("Email",fEmail),1,2);
        grid.add(UI.grupoCampo("Total cajones *",fCajones),0,3);
        ColumnConstraints cc=new ColumnConstraints(); cc.setPercentWidth(50);
        grid.getColumnConstraints().addAll(cc,new ColumnConstraints(){{setPercentWidth(50);}});

        Label err=UI.errorLabel();
        Button guardar=UI.btnPrimario(editar==null?"Guardar":"Actualizar");
        Button cancelar=UI.btnSecundario("Cancelar"); cancelar.setOnAction(e->ven.close());

        guardar.setOnAction(e->{
            Estacionamiento est=editar!=null?editar:new Estacionamiento();
            est.setNombre(fNombre.getText().trim()); est.setDireccion(fDir.getText().trim());
            est.setTelefono(fTel.getText().trim()); est.setEmail(fEmail.getText().trim());
            try{ int cajones=Integer.parseInt(fCajones.getText().trim()); est.setTotalCajones(cajones);
                if(editar==null) est.setCajonesDisponibles(cajones); }
            catch(NumberFormatException ex){ UI.setError(err,"Cajones debe ser número entero."); return; }

            String error=ctrl.validarEstacionamiento(est);
            if(error!=null){ UI.setError(err,error); return; }
            boolean ok=editar==null?ctrl.crearEstacionamiento(est):ctrl.actualizarEstacionamiento(est);
            if(ok){cargar();ven.close();}else UI.setError(err,"Error al guardar.");
        });

        HBox bRow=new HBox(10,cancelar,guardar); bRow.setAlignment(Pos.CENTER_RIGHT);
        cont.getChildren().addAll(titulo,UI.separador(),grid,err,UI.separador(),bRow);
        ven.setScene(new Scene(cont)); ven.showAndWait();
    }
}

// ─────────────────────────────────────────────────────────────
//  REGISTROS (ENTRADA/SALIDA)
// ─────────────────────────────────────────────────────────────
class RegistrosImpl extends VBox {

    private final RegistroController ctrl = new RegistroController();
    private final VehiculoController vehCtrl = new VehiculoController();
    private final CajonController cajonCtrl = new CajonController();
    private final ConvenioController convenioCtrl = new ConvenioController();
    private final CajaController cajaCtrl = new CajaController();
    private ObservableList<RegistroEntradaSalida> datos;
    private TableView<RegistroEntradaSalida> tabla;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    RegistrosImpl() {
        setPadding(new Insets(24, 28, 24, 28));
        setSpacing(16);
        setStyle("-fx-background-color:" + UI.BG + ";");
        construir();
        cargar();
    }

    private void construir() {
        HBox acciones = new HBox(14);
        acciones.setAlignment(Pos.CENTER_LEFT);
        acciones.setPadding(new Insets(18));
        acciones.setStyle(UI.CARD);

        Button btnEntrada = UI.btnPrimario("Registrar entrada");
        Button btnSalida = UI.btnPeligro("Registrar salida");
        btnEntrada.setPrefWidth(180);
        btnSalida.setPrefWidth(180);
        btnEntrada.setOnAction(e -> formularioEntrada());
        btnSalida.setOnAction(e -> formularioSalida());
        acciones.getChildren().addAll(btnEntrada, btnSalida);

        tabla = new TableView<>();
        UI.estilizarTabla(tabla);
        datos = FXCollections.observableArrayList();
        tabla.setItems(datos);
        VBox.setVgrow(tabla, Priority.ALWAYS);

        TableColumn<RegistroEntradaSalida, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(55);
        TableColumn<RegistroEntradaSalida, Integer> colVeh = new TableColumn<>("Vehiculo ID");
        colVeh.setCellValueFactory(new PropertyValueFactory<>("vehiculoId"));
        TableColumn<RegistroEntradaSalida, Integer> colCaj = new TableColumn<>("Cajon ID");
        colCaj.setCellValueFactory(new PropertyValueFactory<>("cajonId"));
        TableColumn<RegistroEntradaSalida, String> colEnt = new TableColumn<>("Entrada");
        colEnt.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getFechaEntrada() != null ? c.getValue().getFechaEntrada().format(fmt) : ""));
        TableColumn<RegistroEntradaSalida, String> colSal = new TableColumn<>("Salida");
        colSal.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getFechaSalida() != null ? c.getValue().getFechaSalida().format(fmt) : "En curso"));
        TableColumn<RegistroEntradaSalida, String> colMonto = new TableColumn<>("Monto");
        colMonto.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getFechaSalida() != null ? String.format("$%.2f", c.getValue().getMonto()) : "-"));
        TableColumn<RegistroEntradaSalida, String> colPromo = new TableColumn<>("Convenio/Promocion");
        colPromo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getPromocionAplicada() != null ? c.getValue().getPromocionAplicada() : "-"));
        colPromo.setPrefWidth(180);
        TableColumn<RegistroEntradaSalida, String> colEst = new TableColumn<>("Estado");
        colEst.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colEst.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); return; }
                setGraphic(UI.badge(v, "Activo".equals(v) ? UI.badgeBlue() : UI.badgeGreen()));
            }
        });

        tabla.getColumns().addAll(colId, colVeh, colCaj, colEnt, colSal, colMonto, colPromo, colEst);
        tabla.setPlaceholder(UI.panelVacio("Movimientos", "No hay registros"));

        getChildren().addAll(UI.encabezado("Entrada / Salida", "Registro de movimientos del estacionamiento"), acciones, tabla);
    }

    private void formularioEntrada() {
        Integer estId = estacionamientoActual();
        if (estId == null) return;

        Stage ven = new Stage();
        ven.initModality(Modality.APPLICATION_MODAL);
        ven.setTitle("Registrar entrada");
        ven.setResizable(false);

        VBox cont = new VBox(14);
        cont.setPadding(new Insets(24));
        cont.setPrefWidth(520);

        Label titulo = new Label("Registrar entrada");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 15));

        TextField fPlaca = UI.campo("ABC-123");
        TextField fMarca = UI.campo("Marca (opcional)");
        TextField fModelo = UI.campo("Modelo (opcional)");
        TextField fColor = UI.campo("Color (opcional)");
        TextField fAnio = UI.campo("Anio (opcional)");
        ComboBox<String> comboTipo = UI.combo();
        comboTipo.getItems().addAll("Auto", "Moto", "Camioneta");
        comboTipo.setValue("Auto");

        ComboBox<Cajon> comboCajon = UI.combo();
        cargarCajones(comboCajon, estId, true);
        comboCajon.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Cajon c) {
                return c == null ? "" : "Cajon " + c.getNumero() + " - " + c.getTipo();
            }
            @Override public Cajon fromString(String s) { return null; }
        });

        Runnable autollenar = () -> {
            try {
                String placa = fPlaca.getText() == null ? "" : fPlaca.getText().trim().toUpperCase();
                if (placa.isBlank()) return;
                Vehiculo existente = vehCtrl.obtenerVehiculoPorPatente(placa);
                if (existente == null) return;
                fPlaca.setText(existente.getPatente());
                if (existente.getMarca() != null) fMarca.setText(existente.getMarca());
                if (existente.getModelo() != null) fModelo.setText(existente.getModelo());
                if (existente.getColor() != null) fColor.setText(existente.getColor());
                if (existente.getTipo() != null) comboTipo.setValue(existente.getTipo());
            } catch (Exception ignored) {
                // El autollenado no debe bloquear el registro manual.
            }
        };
        fPlaca.focusedProperty().addListener((o, old, focused) -> { if (!focused) autollenar.run(); });
        fPlaca.setOnAction(e -> autollenar.run());

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.add(UI.grupoCampo("Placa *", fPlaca), 0, 0);
        grid.add(UI.grupoCampo("Tipo *", comboTipo), 1, 0);
        grid.add(UI.grupoCampo("Cajon (opcional si tiene pension)", comboCajon), 0, 1, 2, 1);
        grid.add(UI.grupoCampo("Color", fColor), 0, 2);
        grid.add(UI.grupoCampo("Marca", fMarca), 1, 2);
        grid.add(UI.grupoCampo("Modelo", fModelo), 0, 3);
        grid.add(UI.grupoCampo("Anio", fAnio), 1, 3);
        ColumnConstraints cc = new ColumnConstraints();
        cc.setPercentWidth(50);
        grid.getColumnConstraints().addAll(cc, new ColumnConstraints() {{ setPercentWidth(50); }});

        Label err = UI.errorLabel();
        Button cancelar = UI.btnSecundario("Cancelar");
        Button guardar = UI.btnPrimario("Registrar entrada");
        cancelar.setOnAction(e -> ven.close());
        guardar.setOnAction(e -> {
            UI.setError(err, null);
                try {
                Cajon cajon = comboCajon.getValue();
                Vehiculo vehiculo = ctrl.registrarEntradaPorPlaca(
                        fPlaca.getText(), comboTipo.getValue(), fMarca.getText(),
                        fModelo.getText(), fColor.getText(), cajon != null ? cajon.getId() : 0, estId);
                cargar();
                ven.close();
                String destino = cajon != null ? " en cajon " + cajon.getNumero() : " en su cajon de pension";
                UI.mostrarInfo("Entrada registrada", "Vehiculo #" + vehiculo.getId() + destino);
            } catch (Exception ex) {
                UI.setError(err, ex.getMessage());
            }
        });

        HBox bRow = new HBox(10, cancelar, guardar);
        bRow.setAlignment(Pos.CENTER_RIGHT);
        cont.getChildren().addAll(titulo, UI.separador(), grid, err, UI.separador(), bRow);
        ven.setScene(new Scene(cont));
        ven.showAndWait();
    }

    private void formularioSalida() {
        Integer estId = estacionamientoActual();
        if (estId == null) return;

        Stage ven = new Stage();
        ven.initModality(Modality.APPLICATION_MODAL);
        ven.setTitle("Registrar salida");
        ven.setResizable(false);

        VBox cont = new VBox(14);
        cont.setPadding(new Insets(24));
        cont.setPrefWidth(480);

        Label titulo = new Label("Registrar salida");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 15));

        TextField fPlaca = UI.campo("Placa (opcional)");
        ComboBox<Cajon> comboCajon = UI.combo();
        cargarCajones(comboCajon, estId, false);
        comboCajon.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Cajon c) {
                return c == null ? "" : "Cajon " + c.getNumero() + " - " + c.getEstado();
            }
            @Override public Cajon fromString(String s) { return null; }
        });

        ComboBox<ConvenioRestaurante> comboConvenio = UI.combo();
        comboConvenio.getItems().add(null);
        try {
            comboConvenio.getItems().addAll(convenioCtrl.obtenerPorEstacionamiento(estId).stream()
                    .filter(c -> c.getEstado() == null || "Vigente".equalsIgnoreCase(c.getEstado()))
                    .toList());
        } catch (Exception ignored) {
            // Si no hay convenios configurados, se permite salida normal.
        }
        comboConvenio.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(ConvenioRestaurante c) {
                return c == null ? "Sin convenio" : "Convenio #" + c.getId() + " - Restaurante #" + c.getRestauranteId();
            }
            @Override public ConvenioRestaurante fromString(String s) { return null; }
        });
        comboConvenio.setValue(null);

        Label err = UI.errorLabel();
        Button cancelar = UI.btnSecundario("Cancelar");
        Button guardar = UI.btnPrimario("Registrar salida");
        cancelar.setOnAction(e -> ven.close());
        guardar.setOnAction(e -> {
            UI.setError(err, null);
            try {
                String placa = fPlaca.getText() == null ? "" : fPlaca.getText().trim();
                Cajon cajon = comboCajon.getValue();
                if (placa.isBlank() && cajon == null) {
                    UI.setError(err, "Ingrese una placa o seleccione un cajon ocupado.");
                    return;
                }

                RegistroEntradaSalida reg = !placa.isBlank()
                        ? ctrl.registrarSalidaPorPlaca(placa, estId)
                        : ctrl.registrarSalidaPorCajon(cajon.getId(), estId);
                if (reg == null) {
                    UI.setError(err, !placa.isBlank()
                            ? "No hay entrada activa para esa placa."
                            : "No hay entrada activa para ese cajon.");
                    return;
                }

                ConvenioRestaurante convenio = comboConvenio.getValue();
                Pago pago;
                if (convenio != null) {
                    pago = cajaCtrl.registrarPagoConvenio(reg, Session.getInstance().getUsuario(), convenio.getRestauranteId(), convenio.getId());
                } else {
                    pago = cajaCtrl.registrarPago(reg, reg.getMonto(), Pago.MetodoPago.EFECTIVO, Session.getInstance().getUsuario());
                }

                cargar();
                ven.close();
                String ticket = pago != null ? "\nTicket: " + pago.getNumeroTicket() : "";
                UI.mostrarInfo("Salida registrada", String.format("Monto a pagar: $%.2f%s", reg.getMonto(), ticket));
            } catch (Exception ex) {
                UI.setError(err, ex.getMessage());
            }
        });

        VBox form = new VBox(12,
                UI.grupoCampo("Placa", fPlaca),
                UI.grupoCampo("Cajon ocupado", comboCajon),
                UI.grupoCampo("Convenio", comboConvenio));
        HBox bRow = new HBox(10, cancelar, guardar);
        bRow.setAlignment(Pos.CENTER_RIGHT);
        cont.getChildren().addAll(titulo, UI.separador(), form, err, UI.separador(), bRow);
        ven.setScene(new Scene(cont));
        ven.showAndWait();
    }

    private void cargarCajones(ComboBox<Cajon> combo, int estId, boolean disponibles) {
        try {
            List<Cajon> cajones = cajonCtrl.obtenerCajonesPorEstacionamiento(estId).stream()
                    .filter(c -> disponibles ? esDisponible(c) : esOcupado(c))
                    .toList();
            combo.getItems().setAll(cajones);
            if (!cajones.isEmpty()) combo.setValue(cajones.get(0));
        } catch (Exception ex) {
            combo.getItems().clear();
        }
    }

    private boolean esDisponible(Cajon cajon) {
        String estado = cajon.getEstado() == null ? "" : cajon.getEstado().trim().toLowerCase();
        return estado.equals("libre") || estado.equals("disponible");
    }

    private boolean esOcupado(Cajon cajon) {
        String estado = cajon.getEstado() == null ? "" : cajon.getEstado().trim().toLowerCase();
        return estado.equals("ocupado");
    }

    private Integer estacionamientoActual() {
        Integer estId = Session.getInstance().getEstacionamientoActualId();
        if (estId == null) {
            UI.mostrarError("Error", "Debe seleccionar un estacionamiento");
        }
        return estId;
    }

    private void cargar() {
        try {
            Integer estId = estacionamientoActual();
            if (estId == null) return;
            datos.setAll(ctrl.obtenerRegistrosPorEstacionamiento(estId));
        } catch (Exception e) {
            UI.mostrarError("Error", e.getMessage());
        }
    }
}

// -----------------------------------------------------------------------------
//  PENSIONES
// ─────────────────────────────────────────────────────────────
class PensionesImpl extends VBox {

    private final PensionController ctrl = new PensionController();
    private final CajonController cajonCtrl = new CajonController();
    private final VehiculoController vehCtrl = new VehiculoController();
    private ObservableList<Pension> datos;
    private TableView<Pension> tabla;

    PensionesImpl() {
        setPadding(new Insets(24, 28, 24, 28));
        setSpacing(16);
        setStyle("-fx-background-color:" + UI.BG + ";");
        construir();
        cargar();
    }

    private void construir() {
        Button btnNuevo = UI.btnPrimario("+ Nueva pension");
        btnNuevo.setOnAction(e -> formulario(null));

        tabla = new TableView<>();
        UI.estilizarTabla(tabla);
        datos = FXCollections.observableArrayList();
        tabla.setItems(datos);
        VBox.setVgrow(tabla, Priority.ALWAYS);

        TableColumn<Pension, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(50);
        TableColumn<Pension, Integer> colCli = new TableColumn<>("Cliente ID");
        colCli.setCellValueFactory(new PropertyValueFactory<>("clienteId"));
        TableColumn<Pension, Integer> colVeh = new TableColumn<>("Vehiculo ID");
        colVeh.setCellValueFactory(new PropertyValueFactory<>("vehiculoId"));
        TableColumn<Pension, Integer> colCaj = new TableColumn<>("Cajon ID");
        colCaj.setCellValueFactory(new PropertyValueFactory<>("cajonId"));
        TableColumn<Pension, String> colEst = new TableColumn<>("Estado");
        colEst.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(ctrl.calcularEstado(c.getValue())));
        colEst.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) { setGraphic(null); return; }
                setGraphic(UI.badge(v, "Activa".equals(v) ? UI.badgeGreen() : "Proxima a vencer".equals(v) ? UI.badgeBlue() : UI.badgeRed()));
            }
        });
        TableColumn<Pension, String> colMonto = new TableColumn<>("Monto");
        colMonto.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.format("$%.2f", c.getValue().getMonto())));
        TableColumn<Pension, Void> colAcc = new TableColumn<>("Acciones");
        colAcc.setCellFactory(col -> new TableCell<>() {
            final Button e = UI.btnSecundario("Editar");
            final Button d = UI.btnPeligro("Cancelar");
            {
                e.setStyle(e.getStyle() + UI.BTN_SMALL);
                d.setStyle(d.getStyle() + UI.BTN_SMALL);
                e.setOnAction(ev -> formulario(getTableView().getItems().get(getIndex())));
                d.setOnAction(ev -> {
                    Pension p = getTableView().getItems().get(getIndex());
                    if (UI.confirmar("Cancelar pension", "Cancelar pension #" + p.getId() + "?")) {
                        try { ctrl.cancelarPension(p.getId()); cargar(); }
                        catch (Exception ex) { UI.mostrarError("Error", ex.getMessage()); }
                    }
                });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                Pension p = getTableView().getItems().get(getIndex());
                boolean vencida = "Vencida".equals(ctrl.calcularEstado(p));
                e.setDisable(vencida);
                d.setDisable(vencida);
                HBox h = new HBox(6, e, d);
                h.setAlignment(Pos.CENTER_LEFT);
                setGraphic(h);
            }
        });
        colAcc.setPrefWidth(170);

        tabla.getColumns().addAll(colId, colCli, colVeh, colCaj, colEst, colMonto, colAcc);
        tabla.setPlaceholder(UI.panelVacio("Pensiones", "No hay pensiones"));
        getChildren().addAll(UI.encabezado("Pensiones", "Contratos mensuales con cajon asignado", btnNuevo), tabla);
    }

    private void cargar() {
        try {
            Integer estId = Session.getInstance().getEstacionamientoActualId();
            if (estId == null) { datos.clear(); return; }
            datos.setAll(ctrl.obtenerPensionesActivas(estId));
        } catch (Exception e) {
            UI.mostrarError("Error", e.getMessage());
        }
    }

    private void formulario(Pension editar) {
        Stage ven = new Stage();
        ven.initModality(Modality.APPLICATION_MODAL);
        ven.setTitle(editar == null ? "Nueva pension" : "Editar pension");
        ven.setResizable(false);

        VBox cont = new VBox(14);
        cont.setPadding(new Insets(24));
        cont.setPrefWidth(560);
        Label titulo = new Label(editar == null ? "Nueva pension" : "Editar pension");
        titulo.setFont(Font.font("System", FontWeight.BOLD, 15));

        TextField fPlaca = UI.campo("ABC-123");
        TextField fMarca = UI.campo("Marca (opcional)");
        TextField fModelo = UI.campo("Modelo (opcional)");
        TextField fColor = UI.campo("Color (opcional)");
        ComboBox<String> comboTipo = UI.combo();
        comboTipo.getItems().addAll("Auto", "Moto", "Camioneta");
        comboTipo.setValue("Auto");
        TextField fMonto = UI.campo("Ej: 800.00");
        ComboBox<Cajon> comboCajon = UI.combo();
        cargarCajonesDisponibles(comboCajon, editar);
        comboCajon.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Cajon c) {
                return c == null ? "" : "Cajon " + c.getNumero() + " - " + c.getTipo() + " (" + c.getEstado() + ")";
            }
            @Override public Cajon fromString(String s) { return null; }
        });

        DatePicker dpInicio = new DatePicker(LocalDate.now());
        DatePicker dpFin = new DatePicker(LocalDate.now().plusMonths(1));
        dpInicio.setMaxWidth(Double.MAX_VALUE);
        dpFin.setMaxWidth(Double.MAX_VALUE);
        dpInicio.setStyle(UI.FIELD);
        dpFin.setStyle(UI.FIELD);

        boolean pensionVencida = false;
        if (editar != null) {
            try {
                Vehiculo veh = vehCtrl.obtenerVehiculoPorId(editar.getVehiculoId());
                if (veh != null) {
                    fPlaca.setText(veh.getPatente());
                    fMarca.setText(veh.getMarca() != null ? veh.getMarca() : "");
                    fModelo.setText(veh.getModelo() != null ? veh.getModelo() : "");
                    fColor.setText(veh.getColor() != null ? veh.getColor() : "");
                    if (veh.getTipo() != null && !veh.getTipo().isBlank()) comboTipo.setValue(veh.getTipo());
                }
            } catch (Exception ignored) {
                fPlaca.setText("Vehiculo #" + editar.getVehiculoId());
            }
            fMonto.setText(String.valueOf(editar.getMonto()));
            seleccionarCajon(comboCajon, editar.getCajonId());
            if (editar.getFechaInicio() != null) dpInicio.setValue(editar.getFechaInicio().toLocalDate());
            if (editar.getFechaFin() != null) dpFin.setValue(editar.getFechaFin().toLocalDate());
            pensionVencida = ctrl.esPensionVencida(editar);
        }
        fPlaca.setDisable(editar != null);

        Runnable autollenar = () -> {
            try {
                String placa = fPlaca.getText() == null ? "" : fPlaca.getText().trim().toUpperCase();
                if (placa.isBlank()) return;
                Vehiculo existente = vehCtrl.obtenerVehiculoPorPatente(placa);
                if (existente == null) return;
                fPlaca.setText(existente.getPatente());
                if (existente.getMarca() != null) fMarca.setText(existente.getMarca());
                if (existente.getModelo() != null) fModelo.setText(existente.getModelo());
                if (existente.getColor() != null) fColor.setText(existente.getColor());
                if (existente.getTipo() != null && !existente.getTipo().isBlank()) comboTipo.setValue(existente.getTipo());
            } catch (Exception ignored) {
                // El autollenado no debe bloquear una placa nueva.
            }
        };
        fPlaca.focusedProperty().addListener((o, old, focused) -> { if (!focused) autollenar.run(); });
        fPlaca.setOnAction(e -> autollenar.run());

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.add(UI.grupoCampo("Placa *", fPlaca), 0, 0);
        grid.add(UI.grupoCampo("Tipo *", comboTipo), 1, 0);
        grid.add(UI.grupoCampo("Cajon asignado *", comboCajon), 0, 1);
        grid.add(UI.grupoCampo("Monto a cobrar *", fMonto), 1, 1);
        grid.add(UI.grupoCampo("Color", fColor), 0, 2);
        grid.add(UI.grupoCampo("Marca", fMarca), 1, 2);
        grid.add(UI.grupoCampo("Modelo", fModelo), 0, 3);
        grid.add(UI.grupoCampo("Fecha inicio", dpInicio), 0, 4);
        grid.add(UI.grupoCampo("Fecha fin", dpFin), 1, 4);
        ColumnConstraints cc = new ColumnConstraints();
        cc.setPercentWidth(50);
        grid.getColumnConstraints().addAll(cc, new ColumnConstraints() {{ setPercentWidth(50); }});

        Label err = UI.errorLabel();
        Label avisoCobro = UI.alertaInfo(editar == null
                ? "Al crear la pension se registrara automaticamente el cobro en caja."
                : "La edicion no genera un nuevo cobro.");
        Label avisoVencida = new Label();
        avisoVencida.setStyle("-fx-text-fill:#92400e;-fx-font-weight:bold;");
        if (pensionVencida) {
            avisoVencida.setText("Esta pension esta vencida y no puede editarse. Cree una nueva para renovarla.");
        }

        Button guardar = UI.btnPrimario(editar == null ? "Guardar" : "Actualizar");
        Button cancelar = UI.btnSecundario("Cancelar");
        cancelar.setOnAction(e -> ven.close());
        guardar.setOnAction(e -> {
            try {
                Cajon cajon = comboCajon.getValue();
                if (cajon == null) { UI.setError(err, "Seleccione un cajon disponible."); return; }
                double monto = Double.parseDouble(fMonto.getText().trim());
                Integer estId = Session.getInstance().getEstacionamientoActualId();
                if (estId == null) { UI.setError(err, "Debe seleccionar un estacionamiento"); return; }

                if (editar == null) {
                    Pago pago = ctrl.crearPensionPorPlacaConCobro(
                            fPlaca.getText(), comboTipo.getValue(), fMarca.getText(),
                            fModelo.getText(), fColor.getText(), cajon.getId(), estId,
                            dpInicio.getValue().atStartOfDay(), dpFin.getValue().atTime(23, 59),
                            monto, Session.getInstance().getUsuario());
                    cargar();
                    ven.close();
                    UI.mostrarInfo("Pension registrada",
                            "Pension creada y cobro registrado: " + (pago != null ? pago.getNumeroTicket() : "sin ticket"));
                    return;
                }

                editar.setCajonId(cajon.getId());
                editar.setMonto(monto);
                editar.setFechaInicio(dpInicio.getValue().atStartOfDay());
                editar.setFechaFin(dpFin.getValue().atTime(23, 59));
                editar.setEstacionamientoId(estId);
                if (editar.getEstado() == null) editar.setEstado("Activa");

                boolean ok = ctrl.actualizarPension(editar);
                if (ok) { cargar(); ven.close(); }
                else UI.setError(err, "Error al guardar.");
            } catch (NumberFormatException ex) {
                UI.setError(err, "El monto debe ser numerico.");
            } catch (Exception ex) {
                UI.setError(err, ex.getMessage());
            }
        });

        HBox bRow = new HBox(10, cancelar, guardar);
        bRow.setAlignment(Pos.CENTER_RIGHT);
        cont.getChildren().addAll(titulo, UI.separador(), grid, avisoCobro, err, avisoVencida, UI.separador(), bRow);
        if (pensionVencida) guardar.setDisable(true);
        ven.setScene(new Scene(cont));
        ven.showAndWait();
    }

    private void cargarCajonesDisponibles(ComboBox<Cajon> combo, Pension editar) {
        try {
            Integer estId = Session.getInstance().getEstacionamientoActualId();
            if (estId == null) return;
            List<Cajon> cajones = cajonCtrl.obtenerCajonesPorEstacionamiento(estId).stream()
                    .filter(c -> esDisponible(c) || (editar != null && c.getId() == editar.getCajonId()))
                    .toList();
            combo.getItems().setAll(cajones);
            if (!cajones.isEmpty()) combo.setValue(cajones.get(0));
        } catch (Exception ignored) {
            combo.getItems().clear();
        }
    }

    private void seleccionarCajon(ComboBox<Cajon> combo, int cajonId) {
        combo.getItems().stream()
                .filter(c -> c.getId() == cajonId)
                .findFirst()
                .ifPresent(combo::setValue);
    }

    private boolean esDisponible(Cajon cajon) {
        String estado = cajon.getEstado() == null ? "" : cajon.getEstado().trim().toLowerCase();
        return estado.equals("libre") || estado.equals("disponible");
    }
}

// -----------------------------------------------------------------------------
//  PRECIOS
// ─────────────────────────────────────────────────────────────
class PreciosImpl extends VBox {

    private final PrecioController ctrl = new PrecioController();
    private ObservableList<Precio> datos;
    private TableView<Precio> tabla;

    PreciosImpl() {
        setPadding(new Insets(24,28,24,28)); setSpacing(16);
        setStyle("-fx-background-color:"+UI.BG+";");
        construir(); cargar();
    }

    private void construir() {
        Button btnNuevo=UI.btnPrimario("+ Nuevo precio");
        btnNuevo.setOnAction(e->formulario(null));

        tabla=new TableView<>(); UI.estilizarTabla(tabla); datos=FXCollections.observableArrayList(); tabla.setItems(datos); VBox.setVgrow(tabla,Priority.ALWAYS);

        col("Tipo vehículo","tipoVehiculo",150);
        colMonto("Precio/hora","precioHora");
        colMonto("Precio/media","precioMedia");
        colMonto("Precio/día","precioDia");

        TableColumn<Precio,Void> colAcc=new TableColumn<>("Acciones");
        colAcc.setCellFactory(col->new TableCell<>(){
            final Button e=UI.btnSecundario("✏️"); final Button d=UI.btnPeligro("🗑️");
            { e.setStyle(e.getStyle()+UI.BTN_SMALL); d.setStyle(d.getStyle()+UI.BTN_SMALL);
              e.setOnAction(ev->formulario(getTableView().getItems().get(getIndex())));
              d.setOnAction(ev->{ Precio p=getTableView().getItems().get(getIndex());
                  if(UI.confirmar("Eliminar","¿Eliminar tarifa "+p.getTipoVehiculo()+"?")){
                      try{ctrl.eliminarPrecio(p.getId());cargar();}catch(Exception ex){UI.mostrarError("Error",ex.getMessage());}
                  }}); }
            @Override protected void updateItem(Void v,boolean empty){
                super.updateItem(v,empty); if(empty){setGraphic(null);return;}
                HBox h=new HBox(6,e,d);h.setAlignment(Pos.CENTER_LEFT);setGraphic(h);}
        }); colAcc.setPrefWidth(120); tabla.getColumns().add(colAcc);
        tabla.setPlaceholder(UI.panelVacio("💵","No hay precios configurados"));
        getChildren().addAll(UI.encabezado("Precios","Tarifas del estacionamiento",btnNuevo),tabla);
    }

    private void col(String name,String prop,double w){TableColumn<Precio,?> c=new TableColumn<>(name); c.setCellValueFactory(new PropertyValueFactory<>(prop)); c.setPrefWidth(w); tabla.getColumns().add(c);}
    private void colMonto(String name,String prop){
        TableColumn<Precio,String> c=new TableColumn<>(name);
        c.setCellValueFactory(d->{try{double v=((Number)Precio.class.getMethod("get"+Character.toUpperCase(prop.charAt(0))+prop.substring(1)).invoke(d.getValue())).doubleValue(); return new javafx.beans.property.SimpleStringProperty(String.format("$%.2f",v));}catch(Exception e){return new javafx.beans.property.SimpleStringProperty("-");}});
        c.setPrefWidth(110); tabla.getColumns().add(c);
    }

    private void cargar(){
        try{
            Session s = Session.getInstance();
            Integer estId = s.getEstacionamientoActualId();
            if (estId == null) {
                datos.clear();
                return;
            }
            datos.setAll(ctrl.obtenerPreciosPorEstacionamiento(estId));
        } catch(Exception e){UI.mostrarError("Error",e.getMessage());}
    }

    private void formulario(Precio editar){
        Stage ven=new Stage(); ven.initModality(Modality.APPLICATION_MODAL);
        ven.setTitle(editar==null?"Nuevo precio":"Editar precio"); ven.setResizable(false);
        VBox cont=new VBox(14); cont.setPadding(new Insets(24)); cont.setPrefWidth(420);
        Label titulo=new Label(editar==null?"➕ Nuevo precio":"✏️ Editar precio");
        titulo.setFont(Font.font("System",FontWeight.BOLD,15));

        ComboBox<String> comboTipo=UI.combo(); comboTipo.getItems().addAll("Auto","Moto","Camioneta"); comboTipo.setValue("Auto");
        TextField fHora=UI.campo("0.00"); TextField fMedia=UI.campo("0.00"); TextField fDia=UI.campo("0.00");
        Session s=Session.getInstance();
        TextField fEst=UI.campo("ID del estacionamiento");
        Integer seleccionEstId = s.getEstacionamientoActualId();
        fEst.setText(seleccionEstId != null ? String.valueOf(seleccionEstId) : "");
        fEst.setDisable(true);

        if(editar!=null){ if(editar.getTipoVehiculo()!=null)comboTipo.setValue(editar.getTipoVehiculo());
            fHora.setText(String.valueOf(editar.getPrecioHora())); fMedia.setText(String.valueOf(editar.getPrecioMedia())); fDia.setText(String.valueOf(editar.getPrecioDia())); }

        VBox form=new VBox(12);
        form.getChildren().addAll(UI.grupoCampo("Tipo vehículo *",comboTipo),UI.grupoCampo("Precio/hora *",fHora),UI.grupoCampo("Precio/media hora *",fMedia),UI.grupoCampo("Precio/día *",fDia),UI.grupoCampo("ID Estacionamiento",fEst));

        Label err=UI.errorLabel();
        Button guardar=UI.btnPrimario(editar==null?"Guardar":"Actualizar");
        Button cancelar=UI.btnSecundario("Cancelar"); cancelar.setOnAction(e->ven.close());

        guardar.setOnAction(e->{
            try{
                double hora=Double.parseDouble(fHora.getText().trim());
                double media=Double.parseDouble(fMedia.getText().trim());
                double dia=Double.parseDouble(fDia.getText().trim());
                Integer estId = seleccionEstId;
                if (estId == null) { UI.setError(err, "Seleccione un estacionamiento"); return; }
                Precio p=editar!=null?editar:new Precio(comboTipo.getValue(),hora,media,dia,estId);
                if(editar!=null){ p.setTipoVehiculo(comboTipo.getValue()); p.setPrecioHora(hora); p.setPrecioMedia(media); p.setPrecioDia(dia); }

                String error=ctrl.validarPrecio(p);
                if(error!=null){ UI.setError(err,error); return; }
                boolean ok=editar==null?ctrl.crearPrecio(p):ctrl.actualizarPrecio(p);
                if(ok){cargar();ven.close();}else UI.setError(err,"Error al guardar.");
            }catch(NumberFormatException ex){ UI.setError(err,"Montos deben ser numéricos."); }
            catch(Exception ex){ UI.setError(err,ex.getMessage()); }
        });

        HBox bRow=new HBox(10,cancelar,guardar); bRow.setAlignment(Pos.CENTER_RIGHT);
        cont.getChildren().addAll(titulo,UI.separador(),form,err,UI.separador(),bRow);
        ven.setScene(new Scene(cont)); ven.showAndWait();
    }
}

// ─────────────────────────────────────────────────────────────
//  PROMOCIONES
// ─────────────────────────────────────────────────────────────
class PromocionesImpl extends VBox {

    private final PromocionController ctrl = new PromocionController();
    private ObservableList<Promocion> datos;
    private TableView<Promocion> tabla;

    PromocionesImpl() {
        setPadding(new Insets(24,28,24,28)); setSpacing(16);
        setStyle("-fx-background-color:"+UI.BG+";");
        construir(); cargar();
    }

    private void construir(){
        Button btnNuevo=UI.btnPrimario("+ Nueva promoción"); btnNuevo.setOnAction(e->formulario(null));
        tabla=new TableView<>(); UI.estilizarTabla(tabla); datos=FXCollections.observableArrayList(); tabla.setItems(datos); VBox.setVgrow(tabla,Priority.ALWAYS);

        col("Nombre","nombre",180);
        colNum("Descuento %","descuentoPorcentaje");
        TableColumn<Promocion,String> colFijo=new TableColumn<>("Descuento fijo");
        colFijo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
            String.format("$%.2f", c.getValue().getDescuentoFijo())));
        colFijo.setPrefWidth(110);
        tabla.getColumns().add(colFijo);
        TableColumn<Promocion,String> colHoras=new TableColumn<>("Horas gratis");
        colHoras.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
            String.valueOf(c.getValue().getHorasGratis())));
        colHoras.setPrefWidth(100);
        tabla.getColumns().add(colHoras);
        col("Tipo vehículo","tipoVehiculo",120);
        TableColumn<Promocion,String> colVig=new TableColumn<>("Vigente");
        colVig.setCellValueFactory(c->new javafx.beans.property.SimpleStringProperty(c.getValue().isActiva()?"Sí":"No"));
        colVig.setCellFactory(col->new TableCell<>(){@Override protected void updateItem(String v,boolean empty){super.updateItem(v,empty); if(empty||v==null){setGraphic(null);return;} setGraphic(UI.badge(v,"Sí".equals(v)?UI.badgeGreen():UI.badgeGray()));}});
        tabla.getColumns().add(colVig);

        TableColumn<Promocion,Void> colAcc=new TableColumn<>("Acciones");
        colAcc.setCellFactory(col->new TableCell<>(){
            final Button e=UI.btnSecundario("✏️"); final Button d=UI.btnPeligro("🗑️");
            { e.setStyle(e.getStyle()+UI.BTN_SMALL); d.setStyle(d.getStyle()+UI.BTN_SMALL);
              e.setOnAction(ev->formulario(getTableView().getItems().get(getIndex())));
              d.setOnAction(ev->{Promocion p=getTableView().getItems().get(getIndex());
                  if(UI.confirmar("Eliminar","¿Eliminar \""+p.getNombre()+"\"?")){
                      try{ctrl.eliminarPromocion(p.getId());cargar();}catch(Exception ex){UI.mostrarError("Error",ex.getMessage());}
                  }});}
            @Override protected void updateItem(Void v,boolean empty){super.updateItem(v,empty);if(empty){setGraphic(null);return;}HBox h=new HBox(6,e,d);h.setAlignment(Pos.CENTER_LEFT);setGraphic(h);}
        }); colAcc.setPrefWidth(120); tabla.getColumns().add(colAcc);
        tabla.setPlaceholder(UI.panelVacio("🎫","No hay promociones"));
        getChildren().addAll(UI.encabezado("Promociones","Descuentos y ofertas especiales",btnNuevo),tabla);
    }

    private void col(String n,String p,double w){TableColumn<Promocion,?> c=new TableColumn<>(n); c.setCellValueFactory(new PropertyValueFactory<>(p)); c.setPrefWidth(w); tabla.getColumns().add(c);}
    private void colNum(String n,String p){TableColumn<Promocion,String> c=new TableColumn<>(n);c.setCellValueFactory(d->{ try{double v=((Number)Promocion.class.getMethod("get"+Character.toUpperCase(p.charAt(0))+p.substring(1)).invoke(d.getValue())).doubleValue();return new javafx.beans.property.SimpleStringProperty(v+"%");}catch(Exception e){return new javafx.beans.property.SimpleStringProperty("-");}});c.setPrefWidth(110);tabla.getColumns().add(c);}

    private void cargar(){
        try{
            Session s = Session.getInstance();
            Integer estId = s.getEstacionamientoActualId();
            if (estId == null) {
                datos.clear();
                return;
            }
            datos.setAll(ctrl.obtenerPromocionesPorEstacionamiento(estId));
        }catch(Exception e){UI.mostrarError("Error",e.getMessage());}
    }

    private void formulario(Promocion editar){
        Stage ven=new Stage(); ven.initModality(Modality.APPLICATION_MODAL); ven.setTitle(editar==null?"Nueva promoción":"Editar promoción"); ven.setResizable(false);
        VBox cont=new VBox(14); cont.setPadding(new Insets(24)); cont.setPrefWidth(460);
        Label titulo=new Label(editar==null?"➕ Nueva promoción":"✏️ Editar promoción"); titulo.setFont(Font.font("System",FontWeight.BOLD,15));

        TextField fNombre=UI.campo("Nombre de la promoción"); TextField fDesc=UI.campo("Descripción"); TextField fDescPct=UI.campo("Ej: 20");
        TextField fDescFijo=UI.campo("Ej: 150.00"); TextField fHorasGratis=UI.campo("Ej: 2");
        ComboBox<String> comboTipo=UI.combo(); comboTipo.getItems().addAll("Todos","Auto","Moto","Camioneta"); comboTipo.setValue("Todos");
        CheckBox cbActiva=new CheckBox("Promoción activa"); cbActiva.setSelected(true);
        TextField fEst=UI.campo("ID estacionamiento"); Session s=Session.getInstance(); Integer seleccionEstId=s.getEstacionamientoActualId(); fEst.setText(seleccionEstId != null ? String.valueOf(seleccionEstId) : ""); fEst.setDisable(true);
        javafx.scene.control.DatePicker dpIni=new javafx.scene.control.DatePicker(java.time.LocalDate.now());
        javafx.scene.control.DatePicker dpFin=new javafx.scene.control.DatePicker(java.time.LocalDate.now().plusMonths(1));
        dpIni.setMaxWidth(Double.MAX_VALUE); dpFin.setMaxWidth(Double.MAX_VALUE); dpIni.setStyle(UI.FIELD); dpFin.setStyle(UI.FIELD);

        if(editar!=null){ fNombre.setText(editar.getNombre()); fDesc.setText(editar.getDescripcion());
            fDescPct.setText(String.valueOf(editar.getDescuentoPorcentaje())); fDescFijo.setText(String.valueOf(editar.getDescuentoFijo()));
            fHorasGratis.setText(String.valueOf(editar.getHorasGratis())); if(editar.getTipoVehiculo()!=null)comboTipo.setValue(editar.getTipoVehiculo());
            cbActiva.setSelected(editar.isActiva()); if(editar.getFechaInicio()!=null)dpIni.setValue(editar.getFechaInicio().toLocalDate());
            if(editar.getFechaFin()!=null)dpFin.setValue(editar.getFechaFin().toLocalDate()); }

        VBox form=new VBox(12); form.getChildren().addAll(UI.grupoCampo("Nombre *",fNombre),UI.grupoCampo("Descripción",fDesc),UI.grupoCampo("Descuento %",fDescPct),UI.grupoCampo("Descuento fijo",fDescFijo),UI.grupoCampo("Horas gratis",fHorasGratis),UI.grupoCampo("Tipo vehículo",comboTipo),UI.grupoCampo("Desde",dpIni),UI.grupoCampo("Hasta",dpFin),cbActiva);

        Label err=UI.errorLabel();
        Button guardar=UI.btnPrimario(editar==null?"Guardar":"Actualizar"); Button cancelar=UI.btnSecundario("Cancelar"); cancelar.setOnAction(e->ven.close());
        guardar.setOnAction(e->{
            try{
                double pct=Double.parseDouble(fDescPct.getText().trim().isEmpty()?"0":fDescPct.getText().trim());
                double fijo=Double.parseDouble(fDescFijo.getText().trim().isEmpty()?"0":fDescFijo.getText().trim());
                int horas=Integer.parseInt(fHorasGratis.getText().trim().isEmpty()?"0":fHorasGratis.getText().trim());
                Integer estId = seleccionEstId;
                if (estId == null) { UI.setError(err, "Debe seleccionar un estacionamiento"); return; }
                Promocion p=editar!=null?editar:new Promocion(fNombre.getText().trim(),fDesc.getText().trim(),pct,fijo,horas,dpIni.getValue().atStartOfDay(),dpFin.getValue().atTime(23,59),comboTipo.getValue(),estId);
                if(editar!=null){ p.setNombre(fNombre.getText().trim()); p.setDescripcion(fDesc.getText().trim()); p.setDescuentoPorcentaje(pct); p.setDescuentoFijo(fijo); p.setHorasGratis(horas); p.setTipoVehiculo(comboTipo.getValue()); p.setActiva(cbActiva.isSelected()); p.setFechaInicio(dpIni.getValue().atStartOfDay()); p.setFechaFin(dpFin.getValue().atTime(23,59)); }
                String error=ctrl.validarPromocion(p); if(error!=null){UI.setError(err,error);return;}
                boolean ok=editar==null?ctrl.crearPromocion(p):ctrl.actualizarPromocion(p);
                if(ok){cargar();ven.close();}else UI.setError(err,"Error al guardar.");
            }catch(NumberFormatException ex){UI.setError(err,"Los valores numéricos deben ser válidos.");}
            catch(Exception ex){UI.setError(err,ex.getMessage());}
        });
        HBox bRow=new HBox(10,cancelar,guardar); bRow.setAlignment(Pos.CENTER_RIGHT);
        cont.getChildren().addAll(titulo,UI.separador(),form,err,UI.separador(),bRow);
        ven.setScene(new Scene(cont)); ven.showAndWait();
    }
}

// ─────────────────────────────────────────────────────────────
//  NOTIFICACIONES
// ─────────────────────────────────────────────────────────────
class NotificacionesImpl extends VBox {

    private final NotificacionController ctrl = new NotificacionController();
    private final EstacionamientoController estCtrl = new EstacionamientoController();
    private VBox listaContainer;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    NotificacionesImpl() {
        setPadding(new Insets(24,28,24,28)); setSpacing(16);
        setStyle("-fx-background-color:"+UI.BG+";");
        construir(); cargar();
    }

    private void construir(){
        Button btnNueva=UI.btnPrimario("+ Nueva notificación"); btnNueva.setOnAction(e->formulario());
        Button btnLeerTodo=UI.btnSecundario("✓ Marcar todas leídas");
        btnLeerTodo.setOnAction(e->{ try{
            Session s=Session.getInstance(); if(s.getUsuario()==null)return;
            List<Notificacion> notifs=ctrl.obtenerNotificacionesPorUsuario(s.getUsuario().getId());
            for(Notificacion n:notifs){if(!n.isLeida())ctrl.marcarComoLeida(n.getId());}
            cargar();
        }catch(Exception ex){UI.mostrarError("Error",ex.getMessage());}});

        ScrollPane scroll=new ScrollPane(); scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:transparent;-fx-background:transparent;");
        listaContainer=new VBox(10); listaContainer.setPadding(new Insets(0,0,16,0));
        scroll.setContent(listaContainer); VBox.setVgrow(scroll,Priority.ALWAYS);

        getChildren().addAll(UI.encabezado("Notificaciones","Alertas del sistema",btnLeerTodo,btnNueva),scroll);
    }

    private void cargar(){
        listaContainer.getChildren().clear();
        try{
            Session s=Session.getInstance(); if(s.getUsuario()==null) return;
            List<Notificacion> lista=ctrl.obtenerNotificacionesPorUsuario(s.getUsuario().getId());
            if(lista.isEmpty()){listaContainer.getChildren().add(UI.panelVacio("🔔","Sin notificaciones"));return;}
            for(Notificacion n:lista) listaContainer.getChildren().add(crearItem(n));
        }catch(Exception e){listaContainer.getChildren().add(UI.alertaError("Error: "+e.getMessage()));}
    }

    private HBox crearItem(Notificacion n){
        HBox item=new HBox(14); item.setAlignment(Pos.CENTER_LEFT); item.setPadding(new Insets(14,16,14,16));
        item.setStyle("-fx-background-color:"+(n.isLeida()?"white":"#eff6ff")+";-fx-background-radius:10;-fx-border-color:"+(n.isLeida()?UI.BORDER:"#93c5fd")+";-fx-border-radius:10;-fx-border-width:1;");

        String ico=switch(n.getTipo()!=null?n.getTipo():"Info"){ case "Advertencia"->"⚠️"; case "Error"->"❌"; case "Recordatorio"->"🔔"; default->"ℹ️"; };
        Label icoLbl=new Label(ico); icoLbl.setFont(Font.font(22));

        VBox info=new VBox(3); HBox.setHgrow(info,Priority.ALWAYS);
        Label titulo=new Label(n.getTitulo()); titulo.setFont(Font.font("System",FontWeight.BOLD,13));
        Label msg=new Label(n.getMensaje()); msg.setStyle("-fx-text-fill:"+UI.MUTED+";-fx-font-size:12px;"); msg.setWrapText(true);
        Label fecha=new Label(n.getFecha()!=null?n.getFecha().format(fmt):""); fecha.setStyle("-fx-text-fill:#94a3b8;-fx-font-size:10px;");
        info.getChildren().addAll(titulo,msg,fecha);

        VBox acc=new VBox(6); acc.setAlignment(Pos.CENTER);
        if(!n.isLeida()){ Button btnLeer=UI.btnSecundario("✓"); btnLeer.setStyle(btnLeer.getStyle()+UI.BTN_SMALL);
            btnLeer.setOnAction(e->{try{ctrl.marcarComoLeida(n.getId());cargar();}catch(Exception ex){UI.mostrarError("Error",ex.getMessage());}}); acc.getChildren().add(btnLeer); }
        Button btnDel=UI.btnPeligro("🗑️"); btnDel.setStyle(btnDel.getStyle()+UI.BTN_SMALL);
        btnDel.setOnAction(e->{try{ctrl.eliminarNotificacion(n.getId());cargar();}catch(Exception ex){UI.mostrarError("Error",ex.getMessage());}});
        acc.getChildren().add(btnDel);

        item.getChildren().addAll(icoLbl,info,acc);
        return item;
    }
    private void formulario(){
        Stage ven=new Stage(); ven.initModality(Modality.APPLICATION_MODAL); ven.setTitle("Nuevo mensaje"); ven.setResizable(false);
        VBox cont=new VBox(14); cont.setPadding(new Insets(24)); cont.setPrefWidth(480);
        Label titulo=new Label("Nuevo mensaje"); titulo.setFont(Font.font("System",FontWeight.BOLD,15));
        TextField fTitulo=UI.campo("Titulo"); TextArea fMsg=UI.areaTexto("Mensaje...",4);
        ComboBox<String> comboTipo=UI.combo(); comboTipo.getItems().addAll("Info","Advertencia","Error","Recordatorio"); comboTipo.setValue("Info");

        Session sesion = Session.getInstance();
        boolean esAdmin = sesion.isAdmin();
        ComboBox<Estacionamiento> comboDestino = UI.combo();
        if (esAdmin) {
            try { comboDestino.getItems().addAll(estCtrl.obtenerTodosLosEstacionamientos()); } catch (Exception ignored) {}
            comboDestino.setConverter(new javafx.util.StringConverter<>() {
                @Override public String toString(Estacionamiento e) { return e != null ? e.getNombre() : ""; }
                @Override public Estacionamiento fromString(String s) { return null; }
            });
            if (!comboDestino.getItems().isEmpty()) comboDestino.setValue(comboDestino.getItems().get(0));
        }

        Label destinoInfo = UI.alertaInfo(esAdmin
                ? "El mensaje se enviara a los usuarios activos del estacionamiento seleccionado."
                : "El mensaje se enviara a los administradores.");

        Label err=UI.errorLabel();
        Button guardar=UI.btnPrimario("Enviar"); Button cancelar=UI.btnSecundario("Cancelar"); cancelar.setOnAction(e->ven.close());
        guardar.setOnAction(e->{
            if(sesion.getUsuario()==null){UI.setError(err,"Sesion no encontrada.");return;}
            if(fTitulo.getText().isBlank()){UI.setError(err,"El titulo es obligatorio.");return;}
            if(fMsg.getText().isBlank()){UI.setError(err,"El mensaje es obligatorio.");return;}
            try{
                String tituloMsg = fTitulo.getText().trim();
                String mensaje = fMsg.getText().trim();
                int enviados;
                if (esAdmin) {
                    Estacionamiento destino = comboDestino.getValue();
                    if (destino == null) { UI.setError(err, "Seleccione un estacionamiento destino."); return; }
                    enviados = ctrl.enviarAEstacionamiento(destino.getId(), tituloMsg, mensaje, comboTipo.getValue());
                } else {
                    String nombreEst = sesion.getEstacionamientoActualNombre() != null
                            ? sesion.getEstacionamientoActualNombre()
                            : "Estacionamiento";
                    enviados = ctrl.enviarAAdministradores("[" + nombreEst + "] " + tituloMsg, mensaje, comboTipo.getValue());
                }
                cargar(); ven.close(); UI.mostrarInfo("Mensaje enviado", "Destinatarios: " + enviados);
            }catch(Exception ex){UI.setError(err,ex.getMessage());}
        });
        HBox bRow=new HBox(10,cancelar,guardar); bRow.setAlignment(Pos.CENTER_RIGHT);
        cont.getChildren().addAll(titulo,UI.separador(),destinoInfo);
        if (esAdmin) cont.getChildren().add(UI.grupoCampo("Estacionamiento destino", comboDestino));
        cont.getChildren().addAll(UI.grupoCampo("Tipo",comboTipo),UI.grupoCampo("Titulo *",fTitulo),UI.grupoCampo("Mensaje *",fMsg),err,UI.separador(),bRow);
        ven.setScene(new Scene(cont)); ven.showAndWait();
    }
}

// -----------------------------------------------------------------------------
//  REPORTES
// ─────────────────────────────────────────────────────────────
class ReportesImpl extends ScrollPane {

    private final EstacionamientoController estCtrl = new EstacionamientoController();
    private final RegistroController regCtrl = new RegistroController();
    private final PensionController penCtrl = new PensionController();
    private final PromocionController promoCtrl = new PromocionController();
    private final GeneradorPDF generadorPDF;
    private final GeneradorExcel generadorExcel;
    private RadioButton rbReporteGeneral;
    private RadioButton rbReporteEstacionamiento;
    private ComboBox<Estacionamiento> comboReporteEstacionamiento;
    private List<CheckBox> checksTablasReporte;

    ReportesImpl() {
        System.out.println("[DEBUG] ReportesImpl constructor");
        String carpetaReportes = System.getProperty("user.home") + File.separator + "Descargas";
        generadorPDF = new GeneradorPDF(
        carpetaReportes,
        ConexionDB.getInstancia().getConexion()
);

generadorExcel = new GeneradorExcel(
        carpetaReportes,
        ConexionDB.getInstancia().getConexion()
);
        
        setFitToWidth(true); setStyle("-fx-background:"+UI.BG+";-fx-background-color:"+UI.BG+";");
        VBox contenido=new VBox(20); contenido.setPadding(new Insets(24,28,24,28)); contenido.setStyle("-fx-background-color:"+UI.BG+";");

        Label aviso = new Label("Reportes cargados correctamente");
        aviso.setStyle("-fx-text-fill:"+UI.MUTED+";-fx-font-size:11px;");

        contenido.getChildren().addAll(
            UI.encabezado("Reportes", "Estado y métricas del sistema"),
            aviso,
            crearPanelGeneradores(),
            crearStatsEstacionamientos(), crearPensionesActivas(), crearPromocionesVigentes()
        );
        setContent(contenido);
    }
  private VBox crearPanelGeneradores(){

    VBox card = new VBox(12);
    card.setStyle(UI.CARD);
    card.setPadding(new Insets(20));

    Label titulo = new Label("Exportar reportes");
    titulo.setFont(Font.font("System", FontWeight.BOLD, 13));

    Label desc = new Label("Seleccione alcance, tablas y formato de salida");
    desc.setStyle("-fx-text-fill:"+UI.MUTED+";-fx-font-size:11px;");

    ToggleGroup alcance = new ToggleGroup();
    rbReporteGeneral = new RadioButton("Reporte general");
    rbReporteEstacionamiento = new RadioButton("Por estacionamiento");
    rbReporteGeneral.setToggleGroup(alcance);
    rbReporteEstacionamiento.setToggleGroup(alcance);
    rbReporteEstacionamiento.setSelected(true);

    comboReporteEstacionamiento = UI.combo();
    try {
        comboReporteEstacionamiento.getItems().addAll(estCtrl.obtenerTodosLosEstacionamientos());
        Integer actual = Session.getInstance().getEstacionamientoActualId();
        Estacionamiento seleccionado = comboReporteEstacionamiento.getItems().stream()
                .filter(e -> actual != null && e.getId() == actual)
                .findFirst()
                .orElse(comboReporteEstacionamiento.getItems().isEmpty() ? null : comboReporteEstacionamiento.getItems().get(0));
        comboReporteEstacionamiento.setValue(seleccionado);
    } catch (Exception ignored) {
    }
    comboReporteEstacionamiento.setConverter(new javafx.util.StringConverter<>() {
        @Override public String toString(Estacionamiento e) { return e != null ? e.getNombre() : ""; }
        @Override public Estacionamiento fromString(String s) { return null; }
    });
    comboReporteEstacionamiento.disableProperty().bind(rbReporteGeneral.selectedProperty());

    HBox filtrosReporte = new HBox(12,
            rbReporteGeneral,
            rbReporteEstacionamiento,
            UI.grupoCampo("Estacionamiento", comboReporteEstacionamiento));
    filtrosReporte.setAlignment(Pos.CENTER_LEFT);

    checksTablasReporte = List.of(
            new CheckBox("estacionamientos"),
            new CheckBox("cajones"),
            new CheckBox("registros_entrada_salida"),
            new CheckBox("vehiculos"),
            new CheckBox("pensiones"),
            new CheckBox("pagos"),
            new CheckBox("promociones")
    );
    checksTablasReporte.forEach(c -> c.setSelected(true));
    FlowPane tablas = new FlowPane(12, 8);
    tablas.getChildren().addAll(checksTablasReporte);

    FlowPane botones = new FlowPane(12, 12);
    botones.setAlignment(Pos.CENTER_LEFT);
    botones.setPrefWrapLength(900);

    Button btnPdfOcupacion = botonReporte("PDF Ocupacion", "#dc2626");
    btnPdfOcupacion.setOnAction(e -> generarReportePdfOcupacion());
    Button btnPdfIngresos = botonReporte("PDF Ingresos", "#dc2626");
    btnPdfIngresos.setOnAction(e -> generarReportePdfIngresos());
    Button btnPdfPensiones = botonReporte("PDF Pensiones", "#dc2626");
    btnPdfPensiones.setOnAction(e -> generarReportePdfPensiones());
    Button btnExcelOcupacion = botonReporte("Excel Ocupacion", "#16a34a");
    btnExcelOcupacion.setOnAction(e -> generarReporteExcelOcupacion());
    Button btnExcelIngresos = botonReporte("Excel Ingresos", "#16a34a");
    btnExcelIngresos.setOnAction(e -> generarReporteExcelIngresos());
    Button btnExcelPensiones = botonReporte("Excel Pensiones", "#16a34a");
    btnExcelPensiones.setOnAction(e -> generarReporteExcelPensiones());
    Button btnPdfDia = botonReporte("PDF Dia", "#dc2626");
    btnPdfDia.setOnAction(e -> generarPdfDia());
    Button btnPdfMes = botonReporte("PDF Mes", "#dc2626");
    btnPdfMes.setOnAction(e -> generarPdfMes());
    Button btnPdfAnio = botonReporte("PDF Anio", "#dc2626");
    btnPdfAnio.setOnAction(e -> generarPdfAnio());
    Button btnExcelDia = botonReporte("Excel Dia", "#16a34a");
    btnExcelDia.setOnAction(e -> generarExcelDia());
    Button btnExcelMes = botonReporte("Excel Mes", "#16a34a");
    btnExcelMes.setOnAction(e -> generarExcelMes());
    Button btnExcelAnio = botonReporte("Excel Anio", "#16a34a");
    btnExcelAnio.setOnAction(e -> generarExcelAnio());
    Button btnExcelCompleto = botonReporte("Excel Completo", "#2563eb");
    btnExcelCompleto.setOnAction(e -> generarExcelCompleto());
    Button btnExcelSeleccion = botonReporte("Excel Seleccion", "#0f766e");
    btnExcelSeleccion.setOnAction(e -> generarExcelSeleccionado());

    botones.getChildren().addAll(
            btnPdfOcupacion, btnPdfIngresos, btnPdfPensiones,
            btnExcelOcupacion, btnExcelIngresos, btnExcelPensiones,
            btnPdfDia, btnPdfMes, btnPdfAnio,
            btnExcelDia, btnExcelMes, btnExcelAnio,
            btnExcelCompleto, btnExcelSeleccion
    );

    card.getChildren().addAll(
            new VBox(3, titulo, desc),
            filtrosReporte,
            UI.grupoCampo("Tablas para exportacion seleccionada", tablas),
            botones
    );

    return card;
}

private Button botonReporte(String texto, String color) {
    Button b = new Button(texto);
    b.setStyle("-fx-background-color:"+color+";-fx-text-fill:white;-fx-font-weight:bold;-fx-font-size:12px;-fx-background-radius:8;-fx-cursor:hand;-fx-padding:10 16;");
    return b;
}

private String seleccionarCarpetaGuardado(String tipo){

    DirectoryChooser chooser =
            new DirectoryChooser();

    chooser.setTitle(
            "Guardar " + tipo + " en..."
    );

    File initialDir =
            new File(
                    System.getProperty("user.home")
                    + File.separator
                    + "Descargas"
            );

    if(!initialDir.exists()){
        initialDir =
                new File(
                        System.getProperty("user.home")
                        + File.separator
                        + "Downloads"
                );
    }

    if(!initialDir.exists()){
        initialDir =
                new File(
                        System.getProperty("user.home")
                );
    }

    chooser.setInitialDirectory(initialDir);

    File carpeta =
            chooser.showDialog(null);

    return carpeta != null
            ? carpeta.getAbsolutePath()
            : null;
}
private void generarPdfDia(){

    String ruta =
            seleccionarCarpetaGuardado("PDF Día");

    if(ruta == null) return;

    try{

        GeneradorPDF gen =
                new GeneradorPDF(
                        ruta,
                        ConexionDB.getInstancia().getConexion()
                );

        boolean ok =
                gen.generarPdfDia(
                        LocalDate.now()
                );

        UI.mostrarInfo(
                "PDF",
                ok
                ? "PDF generado correctamente"
                : "Error al generar PDF"
        );

    }catch(Exception e){

        UI.mostrarError(
                "Error",
                e.getMessage()
        );
    }
}
private void generarPdfMes(){

    String ruta =
            seleccionarCarpetaGuardado("PDF Mes");

    if(ruta == null) return;

    try{

        LocalDate hoy =
                LocalDate.now();

        GeneradorPDF gen =
                new GeneradorPDF(
                        ruta,
                        ConexionDB.getInstancia().getConexion()
                );

        boolean ok =
                gen.generarPdfMes(
                        hoy.getYear(),
                        hoy.getMonthValue()
                );

        UI.mostrarInfo(
                "PDF",
                ok
                ? "PDF generado correctamente"
                : "Error al generar PDF"
        );

    }catch(Exception e){

        UI.mostrarError(
                "Error",
                e.getMessage()
        );
    }
}


private Integer estacionamientoReporteActual(){
    if (rbReporteGeneral != null && rbReporteGeneral.isSelected()) {
        UI.mostrarError(
                "Reportes",
                "Este reporte requiere seleccionar un estacionamiento. Use los reportes generales o cambie el alcance."
        );
        return null;
    }

    if (comboReporteEstacionamiento != null && comboReporteEstacionamiento.getValue() != null) {
        return comboReporteEstacionamiento.getValue().getId();
    }

    Session s =
            Session.getInstance();

    Integer estId =
            s.getEstacionamientoActualId();

    if(estId == null){
        estId =
                s.getEstacionamientoId();
    }

    if(estId == null){
        UI.mostrarError(
                "Reportes",
                "Seleccione un estacionamiento para generar el reporte"
        );
    }

    return estId;
}

private void generarReportePdfOcupacion(){
    String ruta =
            seleccionarCarpetaGuardado("PDF Ocupacion");

    Integer estId =
            estacionamientoReporteActual();

    if(ruta == null || estId == null) return;

    try{
        GeneradorPDF gen =
                new GeneradorPDF(
                        ruta,
                        ConexionDB.getInstancia().getConexion()
                );

        boolean ok =
                gen.generarReporteOcupacion(estId);

        UI.mostrarInfo(
                "PDF",
                ok ? "Reporte generado correctamente" : "Error al generar PDF"
        );
    }catch(Exception e){
        UI.mostrarError("Error", e.getMessage());
    }
}

private void generarReportePdfIngresos(){
    String ruta =
            seleccionarCarpetaGuardado("PDF Ingresos");

    Integer estId =
            estacionamientoReporteActual();

    if(ruta == null || estId == null) return;

    try{
        GeneradorPDF gen =
                new GeneradorPDF(
                        ruta,
                        ConexionDB.getInstancia().getConexion()
                );

        boolean ok =
                gen.generarReporteIngresos(
                        estId,
                        LocalDate.now()
                );

        UI.mostrarInfo(
                "PDF",
                ok ? "Reporte generado correctamente" : "Error al generar PDF"
        );
    }catch(Exception e){
        UI.mostrarError("Error", e.getMessage());
    }
}

private void generarReportePdfPensiones(){
    String ruta =
            seleccionarCarpetaGuardado("PDF Pensiones");

    Integer estId =
            estacionamientoReporteActual();

    if(ruta == null || estId == null) return;

    try{
        GeneradorPDF gen =
                new GeneradorPDF(
                        ruta,
                        ConexionDB.getInstancia().getConexion()
                );

        boolean ok =
                gen.generarReportePensiones(estId);

        UI.mostrarInfo(
                "PDF",
                ok ? "Reporte generado correctamente" : "Error al generar PDF"
        );
    }catch(Exception e){
        UI.mostrarError("Error", e.getMessage());
    }
}

private void generarReporteExcelOcupacion(){
    String ruta =
            seleccionarCarpetaGuardado("Excel Ocupacion");

    Integer estId =
            estacionamientoReporteActual();

    if(ruta == null || estId == null) return;

    try{
        GeneradorExcel gen =
                new GeneradorExcel(
                        ruta,
                        ConexionDB.getInstancia().getConexion()
                );

        boolean ok =
                gen.generarReporteOcupacionBD(estId);

        UI.mostrarInfo(
                "Excel",
                ok ? "Reporte generado correctamente" : "Error al generar Excel"
        );
    }catch(Exception e){
        UI.mostrarError("Error", e.getMessage());
    }
}

private void generarReporteExcelIngresos(){
    String ruta =
            seleccionarCarpetaGuardado("Excel Ingresos");

    Integer estId =
            estacionamientoReporteActual();

    if(ruta == null || estId == null) return;

    try{
        GeneradorExcel gen =
                new GeneradorExcel(
                        ruta,
                        ConexionDB.getInstancia().getConexion()
                );

        boolean ok =
                gen.generarReporteIngresosBD(
                        estId,
                        LocalDate.now(),
                        LocalDate.now()
                );

        UI.mostrarInfo(
                "Excel",
                ok ? "Reporte generado correctamente" : "Error al generar Excel"
        );
    }catch(Exception e){
        UI.mostrarError("Error", e.getMessage());
    }
}

private void generarReporteExcelPensiones(){
    String ruta =
            seleccionarCarpetaGuardado("Excel Pensiones");

    Integer estId =
            estacionamientoReporteActual();

    if(ruta == null || estId == null) return;

    try{
        GeneradorExcel gen =
                new GeneradorExcel(
                        ruta,
                        ConexionDB.getInstancia().getConexion()
                );

        boolean ok =
                gen.generarReportePensionBD(estId);

        UI.mostrarInfo(
                "Excel",
                ok ? "Reporte generado correctamente" : "Error al generar Excel"
        );
    }catch(Exception e){
        UI.mostrarError("Error", e.getMessage());
    }
}


private void generarPdfAnio(){

    String ruta =
            seleccionarCarpetaGuardado("PDF Año");

    if(ruta == null) return;

    try{

        int anio =
                LocalDate.now().getYear();

        GeneradorPDF gen =
                new GeneradorPDF(
                        ruta,
                        ConexionDB.getInstancia().getConexion()
                );

        boolean ok =
                gen.generarPdfAnio(anio);

        UI.mostrarInfo(
                "PDF",
                ok
                ? "PDF generado correctamente"
                : "Error al generar PDF"
        );

    }catch(Exception e){

        UI.mostrarError(
                "Error",
                e.getMessage()
        );
    }
}


private void generarExcelDia(){

    String ruta =
            seleccionarCarpetaGuardado(
                    "Excel Día"
            );

    if(ruta == null) return;

    try{

        GeneradorExcel gen =
                new GeneradorExcel(
                        ruta,
                        ConexionDB.getInstancia().getConexion()
                );

        boolean ok =
                gen.generarBaseDiaExcel(
                        LocalDate.now()
                );

        UI.mostrarInfo(
                "Excel",
                ok
                ? "Excel generado correctamente"
                : "Error al generar Excel"
        );

    }catch(Exception e){

        UI.mostrarError(
                "Error",
                e.getMessage()
        );
    }
}


private void generarExcelMes(){

    String ruta =
            seleccionarCarpetaGuardado(
                    "Excel Mes"
            );

    if(ruta == null) return;

    try{

        LocalDate hoy =
                LocalDate.now();

        GeneradorExcel gen =
                new GeneradorExcel(
                        ruta,
                        ConexionDB.getInstancia().getConexion()
                );

        boolean ok =
                gen.generarBaseMesExcel(
                        hoy.getYear(),
                        hoy.getMonthValue()
                );

        UI.mostrarInfo(
                "Excel",
                ok
                ? "Excel generado correctamente"
                : "Error al generar Excel"
        );

    }catch(Exception e){

        UI.mostrarError(
                "Error",
                e.getMessage()
        );
    }
}


private void generarExcelAnio(){

    String ruta =
            seleccionarCarpetaGuardado(
                    "Excel Año"
            );

    if(ruta == null) return;

    try{

        int anio =
                LocalDate.now().getYear();

        GeneradorExcel gen =
                new GeneradorExcel(
                        ruta,
                        ConexionDB.getInstancia().getConexion()
                );

        boolean ok =
                gen.generarBaseAnioExcel(
                        anio
                );

        UI.mostrarInfo(
                "Excel",
                ok
                ? "Excel generado correctamente"
                : "Error al generar Excel"
        );

    }catch(Exception e){

        UI.mostrarError(
                "Error",
                e.getMessage()
        );
    }
}

private void generarExcelCompleto(){

    String ruta =
            seleccionarCarpetaGuardado(
                    "Excel Completo"
            );

    if(ruta == null) return;

    try{

        GeneradorExcel gen =
                new GeneradorExcel(
                        ruta,
                        ConexionDB.getInstancia().getConexion()
                );

        boolean ok =
                gen.generarBaseCompletaExcel();

        UI.mostrarInfo(
                "Excel",
                ok
                ? "Base completa exportada"
                : "Error al exportar"
        );

    }catch(Exception e){

        UI.mostrarError(
                "Error",
                e.getMessage()
        );
    }
}

private void generarExcelSeleccionado(){

    String ruta =
            seleccionarCarpetaGuardado(
                    "Excel Seleccion"
            );

    if(ruta == null) return;

    try{
        List<String> tablas = checksTablasReporte.stream()
                .filter(CheckBox::isSelected)
                .map(CheckBox::getText)
                .toList();

        if (tablas.isEmpty()) {
            UI.mostrarError("Reportes", "Seleccione al menos una tabla.");
            return;
        }

        Integer estId = null;
        if (rbReporteEstacionamiento != null && rbReporteEstacionamiento.isSelected()) {
            if (comboReporteEstacionamiento == null || comboReporteEstacionamiento.getValue() == null) {
                UI.mostrarError("Reportes", "Seleccione un estacionamiento.");
                return;
            }
            estId = comboReporteEstacionamiento.getValue().getId();
        }

        GeneradorExcel gen =
                new GeneradorExcel(
                        ruta,
                        ConexionDB.getInstancia().getConexion()
                );

        boolean ok =
                gen.generarTablasSeleccionadasExcel(tablas, estId);

        UI.mostrarInfo(
                "Excel",
                ok
                ? "Exportacion seleccionada generada"
                : "Error al exportar seleccion"
        );

    }catch(Exception e){

        UI.mostrarError(
                "Error",
                e.getMessage()
        );
    }
}




    private HBox crearStatsEstacionamientos(){
        HBox hb=new HBox(14);
        try{
            List<Estacionamiento> ests=estCtrl.obtenerTodosLosEstacionamientos();
            int total=0,disp=0;
            for(Estacionamiento e:ests){total+=e.getTotalCajones();disp+=e.getCajonesDisponibles();}
            int ocup=total-disp; int pct=total>0?(ocup*100/total):0;
            double ing=0; Session s=Session.getInstance();
            Integer estId = s.getEstacionamientoActualId();
            if(estId!=null) ing=regCtrl.obtenerIngresoDelDia(estId,LocalDateTime.now());

            hb.getChildren().addAll(
                stat("🏢 Estacionamientos",String.valueOf(ests.size()),"registrados",UI.BLUE),
                stat("🚗 Ocupados",String.valueOf(ocup),"cajones",UI.RED),
                stat("✅ Libres",String.valueOf(disp),"disponibles",UI.GREEN),
                stat("📊 Ocupación",pct+"%","del total",UI.AMBER),
                stat("💰 Ingreso hoy",String.format("$%.0f",ing),"estimado",UI.GREEN)
            );
        }catch(Exception e){hb.getChildren().add(UI.alertaError("Error: "+e.getMessage()));}
        return hb;
    }

    private VBox stat(String label,String valor,String detalle,String color){
        VBox card=new VBox(4); card.setPadding(new Insets(16,18,16,18)); card.setStyle(UI.CARD); HBox.setHgrow(card,Priority.ALWAYS);
        Label lbl=new Label(label); lbl.setStyle("-fx-text-fill:"+UI.MUTED+";-fx-font-size:10px;-fx-font-weight:bold;");
        Label val=new Label(valor); val.setFont(Font.font("System",FontWeight.BLACK,22)); val.setStyle("-fx-text-fill:"+color+";");
        Label det=new Label(detalle); det.setStyle("-fx-text-fill:"+UI.MUTED+";-fx-font-size:10px;");
        card.getChildren().addAll(lbl,val,det); return card;
    }

    private VBox crearPensionesActivas(){
        VBox card=new VBox(12); card.setStyle(UI.CARD); card.setPadding(new Insets(20));
        Label titulo=new Label("Pensiones Activas"); titulo.setFont(Font.font("System",FontWeight.BOLD,13));
        try{
            Session s=Session.getInstance(); Integer estId=s.getEstacionamientoActualId();
            if (estId == null) { card.getChildren().addAll(titulo,UI.alertaInfo("Seleccione un estacionamiento para ver pensiones activas.")); return card; }
            List<Pension> pensiones=penCtrl.obtenerPensionesActivas(estId);
            if(pensiones.isEmpty()){card.getChildren().addAll(titulo,UI.alertaInfo("No hay pensiones activas."));return card;}
            double total=pensiones.stream().mapToDouble(Pension::getMonto).sum();
            Label resumen=UI.alertaInfo(pensiones.size()+" pensiones activas · Ingreso mensual estimado: $"+String.format("%.2f",total));
            card.getChildren().addAll(titulo,resumen);
            for(Pension p:pensiones){
                HBox fila=new HBox(14); fila.setAlignment(Pos.CENTER_LEFT); fila.setPadding(new Insets(8,0,8,0));
                fila.setStyle("-fx-border-color:transparent transparent #f1f5f9 transparent;-fx-border-width:1;");
                Label cli=new Label("Cliente #"+p.getClienteId()+" · Cajón #"+p.getCajonId()); cli.setStyle("-fx-font-size:12px;");
                Region sp=new Region(); HBox.setHgrow(sp,Priority.ALWAYS);
                Label monto=new Label(String.format("$%.2f/mes",p.getMonto())); monto.setStyle("-fx-font-weight:bold;-fx-text-fill:"+UI.GREEN+";");
                fila.getChildren().addAll(cli,sp,monto); card.getChildren().add(fila);
            }
        }catch(Exception e){card.getChildren().add(UI.alertaError("Error: "+e.getMessage()));}
        return card;
    }

    private VBox crearPromocionesVigentes(){
        VBox card=new VBox(12); card.setStyle(UI.CARD); card.setPadding(new Insets(20));
        Label titulo=new Label("🎫 Promociones Vigentes"); titulo.setFont(Font.font("System",FontWeight.BOLD,13));
        try{
            Session s=Session.getInstance(); Integer estId=s.getEstacionamientoActualId();
            if (estId == null) { card.getChildren().addAll(titulo,UI.alertaInfo("Seleccione un estacionamiento para ver promociones.")); return card; }
            List<Promocion> promos=promoCtrl.obtenerPromocionesVigentes(estId);
            if(promos.isEmpty()){card.getChildren().addAll(titulo,UI.alertaInfo("No hay promociones vigentes."));return card;}
            card.getChildren().add(titulo);
            for(Promocion p:promos){
                HBox fila=new HBox(14); fila.setAlignment(Pos.CENTER_LEFT); fila.setPadding(new Insets(10,12,10,12));
                fila.setStyle("-fx-background-color:#f5f3ff;-fx-background-radius:8;-fx-border-color:#c4b5fd;-fx-border-radius:8;-fx-border-width:1;");
                Label nombre=new Label(p.getNombre()); nombre.setStyle("-fx-font-weight:bold;");
                Region sp=new Region(); HBox.setHgrow(sp,Priority.ALWAYS);
                Label desc=UI.badge(p.getDescuentoPorcentaje()+"%",UI.badgeAmber());
                fila.getChildren().addAll(nombre,sp,desc); card.getChildren().add(fila);
            }
        }catch(Exception e){card.getChildren().add(UI.alertaError("Error: "+e.getMessage()));}
        return card;
    }
}

// ─────────────────────────────────────────────────────────────
//  CONFIGURACION
// ─────────────────────────────────────────────────────────────
class ConfiguracionImpl extends ScrollPane {

    private final ConfiguracionController ctrl = new ConfiguracionController();
    private ObservableList<Configuracion> datos;

    ConfiguracionImpl() {
        setFitToWidth(true); setStyle("-fx-background:"+UI.BG+";-fx-background-color:"+UI.BG+";");
        VBox contenido=new VBox(20); contenido.setPadding(new Insets(24,28,24,28)); contenido.setStyle("-fx-background-color:"+UI.BG+";");

        Label titulo=new Label("⚙️ Configuración del Sistema"); titulo.setFont(Font.font("System",FontWeight.BLACK,20));
        Label sub=new Label("Solo accesible para Administradores"); sub.setStyle("-fx-text-fill:"+UI.MUTED+";-fx-font-size:12px;");

        contenido.getChildren().addAll(new VBox(4,titulo,sub), crearTablaConfiguraciones(), crearInfoSistema());
        setContent(contenido);
    }

    private VBox crearTablaConfiguraciones(){
        VBox card=new VBox(14); card.setStyle(UI.CARD); card.setPadding(new Insets(20));
        Label titulo=new Label("Parámetros del sistema"); titulo.setFont(Font.font("System",FontWeight.BOLD,13));

        TableView<Configuracion> tabla=new TableView<>(); UI.estilizarTabla(tabla);
        datos=FXCollections.observableArrayList(); tabla.setItems(datos); tabla.setPrefHeight(280);

        TableColumn<Configuracion,String> colClave=new TableColumn<>("Clave"); colClave.setCellValueFactory(new PropertyValueFactory<>("clave")); colClave.setPrefWidth(200);
        TableColumn<Configuracion,String> colValor=new TableColumn<>("Valor"); colValor.setCellValueFactory(new PropertyValueFactory<>("valor")); colValor.setPrefWidth(200);
        TableColumn<Configuracion,String> colDesc=new TableColumn<>("Descripción"); colDesc.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        tabla.getColumns().addAll(colClave,colValor,colDesc);
        tabla.setPlaceholder(UI.panelVacio("⚙️","No hay configuraciones"));

        try{ datos.setAll(ctrl.obtenerTodasLasConfiguraciones()); }
        catch(Exception e){ tabla.setPlaceholder(UI.alertaError("Error al cargar: "+e.getMessage())); }

        card.getChildren().addAll(titulo,tabla);
        return card;
    }

    private VBox crearInfoSistema(){
        VBox card=new VBox(12); card.setStyle(UI.CARD); card.setPadding(new Insets(20));
        Label titulo=new Label("ℹ️ Información del Sistema"); titulo.setFont(Font.font("System",FontWeight.BOLD,13));
        GridPane grid=new GridPane(); grid.setHgap(40); grid.setVgap(8);
        String[][] info={{"Versión","1.0.0"},{"Framework","JavaFX 23"},{"Base de datos","MySQL (com.estacionamiento)"},{"Arquitectura","MVC - DAO Pattern"},{"Driver","MySQL Connector/J 8.3"}};
        for(int i=0;i<info.length;i++){
            Label k=new Label(info[i][0]); k.setStyle("-fx-text-fill:"+UI.MUTED+";-fx-font-size:12px;");
            Label v=new Label(info[i][1]); v.setStyle("-fx-font-weight:bold;-fx-font-size:12px;");
            grid.add(k,0,i); grid.add(v,1,i);
        }
        card.getChildren().addAll(titulo,grid); return card;
    }
}





