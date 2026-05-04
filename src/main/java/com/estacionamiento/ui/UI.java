package com.estacionamiento.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Centraliza colores, estilos y componentes reutilizables de la UI.
 */
public final class UI {

    private UI() {}

    // ---- Colores ----
    public static final String BG        = "#f4f5f7";
    public static final String SIDEBAR   = "#12122a";
    public static final String ACCENT    = "#3b82f6";
    public static final String GOLD      = "#f5c842";
    public static final String BORDER    = "#e2e8f0";
    public static final String TEXT      = "#1e293b";
    public static final String MUTED     = "#64748b";
    public static final String GREEN     = "#22c55e";
    public static final String RED       = "#ef4444";
    public static final String AMBER     = "#f59e0b";
    public static final String BLUE      = "#3b82f6";

    // ---- Estilos de botones ----
    public static final String BTN_PRIMARY =
        "-fx-background-color:#3b82f6;-fx-text-fill:white;-fx-font-weight:bold;" +
        "-fx-font-size:13px;-fx-background-radius:8;-fx-cursor:hand;-fx-padding:9 18;";
    public static final String BTN_DANGER =
        "-fx-background-color:#ef4444;-fx-text-fill:white;-fx-font-weight:bold;" +
        "-fx-font-size:13px;-fx-background-radius:8;-fx-cursor:hand;-fx-padding:9 18;";
    public static final String BTN_SECONDARY =
        "-fx-background-color:#f1f5f9;-fx-text-fill:#374151;-fx-font-weight:bold;" +
        "-fx-font-size:13px;-fx-background-radius:8;-fx-cursor:hand;-fx-padding:9 18;";
    public static final String BTN_SMALL = "-fx-font-size:11px;-fx-padding:5 12;";
    public static final String FIELD =
        "-fx-background-color:#fafafa;-fx-border-color:#e2e8f0;-fx-border-width:1.5;" +
        "-fx-border-radius:8;-fx-background-radius:8;-fx-padding:8 12;-fx-font-size:13px;";
    public static final String CARD =
        "-fx-background-color:white;-fx-background-radius:12;" +
        "-fx-border-color:#e2e8f0;-fx-border-radius:12;-fx-border-width:1;";
    public static final String ALERT_DANGER =
        "-fx-background-color:#fff1f2;-fx-border-color:#fca5a5;-fx-border-width:1;" +
        "-fx-border-radius:8;-fx-background-radius:8;-fx-padding:10 14;-fx-text-fill:#991b1b;-fx-font-size:12px;";
    public static final String ALERT_SUCCESS =
        "-fx-background-color:#f0fdf4;-fx-border-color:#86efac;-fx-border-width:1;" +
        "-fx-border-radius:8;-fx-background-radius:8;-fx-padding:10 14;-fx-text-fill:#166534;-fx-font-size:12px;";
    public static final String ALERT_INFO =
        "-fx-background-color:#eff6ff;-fx-border-color:#93c5fd;-fx-border-width:1;" +
        "-fx-border-radius:8;-fx-background-radius:8;-fx-padding:10 14;-fx-text-fill:#1e40af;-fx-font-size:12px;";

    // ---- Badges ----
    public static String badgeGreen() { return "-fx-background-color:#dcfce7;-fx-text-fill:#166534;-fx-background-radius:20;-fx-padding:3 9;-fx-font-size:10px;-fx-font-weight:bold;"; }
    public static String badgeRed()   { return "-fx-background-color:#fee2e2;-fx-text-fill:#991b1b;-fx-background-radius:20;-fx-padding:3 9;-fx-font-size:10px;-fx-font-weight:bold;"; }
    public static String badgeAmber() { return "-fx-background-color:#fef9c3;-fx-text-fill:#92400e;-fx-background-radius:20;-fx-padding:3 9;-fx-font-size:10px;-fx-font-weight:bold;"; }
    public static String badgeBlue()  { return "-fx-background-color:#dbeafe;-fx-text-fill:#1e40af;-fx-background-radius:20;-fx-padding:3 9;-fx-font-size:10px;-fx-font-weight:bold;"; }
    public static String badgeGray()  { return "-fx-background-color:#f1f5f9;-fx-text-fill:#475569;-fx-background-radius:20;-fx-padding:3 9;-fx-font-size:10px;-fx-font-weight:bold;"; }

    // ---- Componentes ----
    public static TextField campo(String placeholder) {
        TextField tf = new TextField();
        tf.setPromptText(placeholder);
        tf.setStyle(FIELD);
        return tf;
    }

    public static PasswordField campoPassword(String placeholder) {
        PasswordField pf = new PasswordField();
        pf.setPromptText(placeholder);
        pf.setStyle(FIELD);
        return pf;
    }

    public static TextArea areaTexto(String placeholder, int filas) {
        TextArea ta = new TextArea();
        ta.setPromptText(placeholder);
        ta.setPrefRowCount(filas);
        ta.setWrapText(true);
        ta.setStyle(FIELD);
        return ta;
    }

    public static <T> ComboBox<T> combo() {
        ComboBox<T> cb = new ComboBox<>();
        cb.setMaxWidth(Double.MAX_VALUE);
        cb.setStyle(FIELD);
        return cb;
    }

    public static Label etiquetaCampo(String texto) {
        Label lbl = new Label(texto.toUpperCase());
        lbl.setStyle("-fx-font-size:11px;-fx-font-weight:bold;-fx-text-fill:#374151;");
        return lbl;
    }

    public static VBox grupoCampo(String label, javafx.scene.Node campo) {
        VBox vb = new VBox(5);
        vb.getChildren().addAll(etiquetaCampo(label), campo);
        return vb;
    }

    public static Button btnPrimario(String texto) {
        Button b = new Button(texto); b.setStyle(BTN_PRIMARY); b.setMaxWidth(Double.MAX_VALUE); return b;
    }
    public static Button btnPeligro(String texto) {
        Button b = new Button(texto); b.setStyle(BTN_DANGER); return b;
    }
    public static Button btnSecundario(String texto) {
        Button b = new Button(texto); b.setStyle(BTN_SECONDARY); return b;
    }

    public static HBox encabezado(String titulo, String sub, Button... botones) {
        VBox textos = new VBox(2);
        Label t = new Label(titulo);
        t.setFont(Font.font("System", FontWeight.BOLD, 17));
        t.setStyle("-fx-text-fill:" + TEXT + ";");
        textos.getChildren().add(t);
        if (sub != null && !sub.isBlank()) {
            Label s = new Label(sub);
            s.setStyle("-fx-text-fill:" + MUTED + ";-fx-font-size:12px;");
            textos.getChildren().add(s);
        }
        HBox hb = new HBox(12);
        hb.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textos, Priority.ALWAYS);
        hb.getChildren().add(textos);
        for (Button b : botones) hb.getChildren().add(b);
        hb.setPadding(new Insets(0, 0, 16, 0));
        return hb;
    }

    public static Separator separador() {
        return new Separator();
    }

    public static Label badge(String texto, String estilo) {
        Label l = new Label(texto); l.setStyle(estilo); return l;
    }

    public static Label alertaError(String msg) {
        Label l = new Label("❌  " + msg);
        l.setStyle(ALERT_DANGER); l.setWrapText(true); l.setMaxWidth(Double.MAX_VALUE);
        return l;
    }
    public static Label alertaExito(String msg) {
        Label l = new Label("✅  " + msg);
        l.setStyle(ALERT_SUCCESS); l.setWrapText(true); l.setMaxWidth(Double.MAX_VALUE);
        return l;
    }
    public static Label alertaInfo(String msg) {
        Label l = new Label("ℹ️  " + msg);
        l.setStyle(ALERT_INFO); l.setWrapText(true); l.setMaxWidth(Double.MAX_VALUE);
        return l;
    }

    public static VBox panelVacio(String icono, String msg) {
        VBox v = new VBox(12);
        v.setAlignment(Pos.CENTER);
        v.setPadding(new Insets(48, 24, 48, 24));
        Label ico = new Label(icono); ico.setFont(Font.font(48)); ico.setStyle("-fx-opacity:0.4;");
        Label txt = new Label(msg); txt.setStyle("-fx-text-fill:" + MUTED + ";-fx-font-size:14px;"); txt.setWrapText(true); txt.setAlignment(Pos.CENTER);
        v.getChildren().addAll(ico, txt);
        return v;
    }

    public static <T> void estilizarTabla(TableView<T> tv) {
        tv.setStyle("-fx-background-color:white;-fx-border-color:#e2e8f0;-fx-border-radius:8;-fx-background-radius:8;");
        tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    public static boolean confirmar(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle(titulo); a.setHeaderText(null); a.setContentText(msg);
        return a.showAndWait().map(b -> b == ButtonType.OK).orElse(false);
    }

    public static void mostrarError(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(titulo); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    public static void mostrarInfo(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(titulo); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }

    /** Muestra/oculta un Label de error */
    public static void setError(Label lbl, String msg) {
        if (msg == null) { lbl.setVisible(false); lbl.setManaged(false); }
        else { lbl.setText("❌  " + msg); lbl.setVisible(true); lbl.setManaged(true); }
    }

    /** Crea un Label de error inicialmente oculto */
    public static Label errorLabel() {
        Label l = new Label();
        l.setStyle(ALERT_DANGER); l.setMaxWidth(Double.MAX_VALUE);
        l.setWrapText(true); l.setVisible(false); l.setManaged(false);
        return l;
    }
}
