package com.estacionamiento.ui.main;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.estacionamiento.ui.Session;
import com.estacionamiento.ui.UI;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Barra lateral de navegación.
 * Los módulos visibles dependen del rol del usuario autenticado.
 */
public class Sidebar extends VBox {

    public enum Modulo {
        DASHBOARD        ("📊", "Dashboard",         "Principal"),
        ESTACIONAMIENTOS ("🏢", "Estacionamientos",  "Principal"),
        CAJONES          ("🅿️",  "Cajones",           "Principal"),
        REGISTROS        ("🚗", "Entrada / Salida",   "Operaciones"),
        CLIENTES         ("👤", "Clientes",           "Gestión"),
        VEHICULOS        ("🚙", "Vehículos",          "Gestión"),
        PENSIONES        ("👥", "Pensiones",          "Servicios"),
        PRECIOS          ("💵", "Precios",            "Servicios"),
        PROMOCIONES      ("🎫", "Promociones",        "Servicios"),
        NOTIFICACIONES   ("🔔", "Notificaciones",     "Sistema"),
        REPORTES         ("📈", "Reportes",           "Sistema"),
        CONFIGURACION    ("⚙️",  "Configuración",     "Sistema");

        public final String icono, etiqueta, seccion;
        Modulo(String i, String e, String s) { icono=i; etiqueta=e; seccion=s; }
    }

    private Modulo moduloActivo;
    private final Consumer<Modulo> onNavegar;
    private final List<Button> botones = new ArrayList<>();
    private Label badgeNotif;

    public Sidebar(Consumer<Modulo> onNavegar) {
        this.onNavegar = onNavegar;
        construirUI();
    }

    private void construirUI() {
        setStyle("-fx-background-color:" + UI.SIDEBAR + ";");
        setPrefWidth(230); 
        setMinWidth(230); 
        setMaxWidth(230);
        setSpacing(0);

        VBox logo = crearLogo();
        VBox info = crearInfoUsuario();
        ScrollPane scroll = crearScrollArea();
        VBox cierre = crearPanelCierre();
        
        getChildren().addAll(logo, info, scroll, cierre);
        VBox.setVgrow(scroll, Priority.ALWAYS);
    }

    private VBox crearLogo() {
        VBox v = new VBox(2);
        v.setPadding(new Insets(22, 20, 14, 20));
        v.setStyle("-fx-border-color:transparent transparent rgba(255,255,255,0.08) transparent;-fx-border-width:1;");
        Label logo = new Label("P·PARK");
        logo.setFont(Font.font("System", FontWeight.BLACK, 20));
        logo.setStyle("-fx-text-fill:" + UI.GOLD + ";-fx-letter-spacing:4;");
        Label sub = new Label("SISTEMA DE GESTIÓN");
        sub.setStyle("-fx-text-fill:rgba(255,255,255,0.35);-fx-font-size:9px;-fx-letter-spacing:1.5;");
        v.getChildren().addAll(logo, sub);
        return v;
    }

    private VBox crearInfoUsuario() {
        Session s = Session.getInstance();
        VBox outer = new VBox();
        outer.setPadding(new Insets(10, 12, 10, 12));
        VBox inner = new VBox(2);
        inner.setPadding(new Insets(10, 12, 10, 12));
        inner.setStyle("-fx-background-color:rgba(255,255,255,0.05);-fx-background-radius:8;");
        Label nombre = new Label("👤 " + s.getNombreCompleto());
        nombre.setStyle("-fx-text-fill:white;-fx-font-size:13px;-fx-font-weight:bold;");
        Label rol = new Label(s.getRolNombre().toUpperCase());
        rol.setStyle("-fx-text-fill:rgba(255,255,255,0.4);-fx-font-size:9px;-fx-letter-spacing:0.8;");
        inner.getChildren().addAll(nombre, rol);
        outer.getChildren().add(inner);
        return outer;
    }

    private VBox crearMenu() {
        VBox menu = new VBox(0);
        menu.setPadding(new Insets(8, 0, 8, 0));
        String seccionActual = "";

        for (Modulo m : Modulo.values()) {
            if (!tienePermiso(m)) continue;

            if (!m.seccion.equals(seccionActual)) {
                seccionActual = m.seccion;
                Label sec = new Label(seccionActual.toUpperCase());
                sec.setStyle("-fx-text-fill:rgba(255,255,255,0.3);-fx-font-size:9px;-fx-letter-spacing:1;");
                sec.setPadding(new Insets(12, 20, 4, 20));
                menu.getChildren().add(sec);
            }

            Button btn = crearBoton(m);
            botones.add(btn);
            menu.getChildren().add(btn);
        }
        return menu;
    }

    private ScrollPane crearScrollArea() {
        VBox contenido = new VBox(0);
        contenido.setPrefWidth(200);
        contenido.setMinWidth(200);
        contenido.setStyle("-fx-background-color:transparent;");
        contenido.setPadding(new Insets(0));
        contenido.getChildren().add(crearMenu());
        
        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(false);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background:transparent;-fx-background-color:transparent;-fx-padding:0;-fx-control-inner-background:#12122a;");
        scroll.setMinHeight(100);
        return scroll;
    }

    private Button crearBoton(Modulo m) {
        HBox cont = new HBox(10);
        cont.setAlignment(Pos.CENTER_LEFT);
        Label ico = new Label(m.icono); ico.setFont(Font.font(16)); ico.setMinWidth(20);
        Label lbl = new Label(m.etiqueta);

        if (m == Modulo.NOTIFICACIONES) {
            badgeNotif = new Label("!");
            badgeNotif.setStyle("-fx-background-color:#ef4444;-fx-text-fill:white;-fx-background-radius:10;-fx-padding:1 6;-fx-font-size:9px;-fx-font-weight:bold;");
            Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
            cont.getChildren().addAll(ico, lbl, sp, badgeNotif);
        } else {
            cont.getChildren().addAll(ico, lbl);
        }

        Button btn = new Button();
        btn.setGraphic(cont);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPadding(new Insets(10, 20, 10, 20));
        btn.setUserData(m);
        aplicarEstilo(btn, false);

        btn.setOnAction(e -> { setActivo(m); onNavegar.accept(m); });
        btn.setOnMouseEntered(e -> { if (btn.getUserData() != moduloActivo) btn.setStyle(hoverStyle()); });
        btn.setOnMouseExited(e -> aplicarEstilo(btn, btn.getUserData() == moduloActivo));
        return btn;
    }

    private Region spacer() {
        Region r = new Region(); VBox.setVgrow(r, Priority.ALWAYS); return r;
    }

    private VBox crearPanelCierre() {
        VBox outer = new VBox(); 
        outer.setPadding(new Insets(8, 12, 12, 12));
        VBox inner = new VBox(8); 
        inner.setPadding(new Insets(12));
        inner.setStyle("-fx-background-color:rgba(255,255,255,0.05);-fx-background-radius:10;");
        Label lbl = new Label("Sesión activa");
        lbl.setStyle("-fx-text-fill:rgba(255,255,255,0.4);-fx-font-size:10px;");
        Button btnCerrar = new Button("🚪 Cerrar sesión");
        btnCerrar.setMaxWidth(Double.MAX_VALUE);
        btnCerrar.setStyle("-fx-background-color:rgba(239,68,68,0.15);-fx-text-fill:#fca5a5;-fx-font-size:11px;-fx-font-weight:bold;-fx-background-radius:6;-fx-cursor:hand;-fx-padding:7 12;");
        btnCerrar.setOnAction(e -> { Session.getInstance().cerrar(); onNavegar.accept(null); });
        inner.getChildren().addAll(lbl, btnCerrar);
        outer.getChildren().add(inner);
        return outer;
    }

    // ---- Permisos ----
    private boolean tienePermiso(Modulo m) {
        Session s = Session.getInstance();
        return switch (m) {
            case ESTACIONAMIENTOS, CONFIGURACION -> s.isAdmin();
            case REPORTES, PRECIOS, PROMOCIONES  -> s.isAdmin() || s.isEncargado();
            default -> true;
        };
    }

    // ---- Estilos ----
    public void setActivo(Modulo m) {
        moduloActivo = m;
        for (Button b : botones) aplicarEstilo(b, b.getUserData() == m);
    }

    private void aplicarEstilo(Button b, boolean activo) {
        b.setStyle(activo ? activoStyle() : normalStyle());
    }

    private String activoStyle()  { return "-fx-background-color:rgba(59,130,246,0.15);-fx-text-fill:white;-fx-border-color:transparent transparent transparent #3b82f6;-fx-border-width:0 0 0 3;-fx-cursor:hand;-fx-alignment:CENTER-LEFT;"; }
    private String normalStyle()  { return "-fx-background-color:transparent;-fx-text-fill:rgba(255,255,255,0.6);-fx-border-color:transparent;-fx-cursor:hand;-fx-alignment:CENTER-LEFT;"; }
    private String hoverStyle()   { return "-fx-background-color:rgba(255,255,255,0.06);-fx-text-fill:white;-fx-border-color:transparent;-fx-cursor:hand;-fx-alignment:CENTER-LEFT;"; }

    public void setBadgeNotif(int cantidad) {
        if (badgeNotif == null) return;
        if (cantidad > 0) { badgeNotif.setText(String.valueOf(cantidad)); badgeNotif.setVisible(true); badgeNotif.setManaged(true); }
        else              { badgeNotif.setVisible(false); badgeNotif.setManaged(false); }
    }
}
