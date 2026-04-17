package com.gitpulse.Home;

import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;

public class DashboardScreen {

    private static final String NAVY    = "#0D1117";
    private static final String CYAN    = "#00D4FF";
    private static final String WHITE   = "#E6EDF3";
    private static final String SURFACE = "#161B22";
    private static final String BORDER  = "#21262D";

    private final String repoUrl;
    private final String username;

    public DashboardScreen(String repoUrl, String username) {
        this.repoUrl  = repoUrl;
        this.username = username;
    }

    public void show(Stage stage) {

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + NAVY + ";");
        root.setPrefSize(1280 , 800);
        // ── Top bar ───────────────────────────────────────────────────
        root.setTop(buildTopBar());

        // ── Sidebar ───────────────────────────────────────────────────
        root.setLeft(buildSidebar());

        // ── Main content ──────────────────────────────────────────────
        root.setCenter(buildMainContent());

        // ── Scene ─────────────────────────────────────────────────────
        Scene scene = new Scene(root);
//        Scene scene = new Scene(root, 1100, 700);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setTitle("GitPulse — Dashboard");
        stage.show();

        // ── Entrance animation ────────────────────────────────────────
        root.setOpacity(0);
        root.setTranslateY(30);
        FadeTransition fade = new FadeTransition(Duration.millis(600), root);
        fade.setToValue(1);
        TranslateTransition slide = new TranslateTransition(Duration.millis(600), root);
        slide.setToY(0);
        new ParallelTransition(fade, slide).play();
    }

    // ── Top bar with logo ─────────────────────────────────────────────
    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(0, 24, 0, 20));
        bar.setPrefHeight(56);
        bar.setStyle(
                "-fx-background-color: " + SURFACE + ";" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-width: 0 0 1 0;"
        );

        // Custom logo (same as splash, smaller)
        Canvas logo = buildLogo(36, 36);

        Label gitLabel = new Label("Git");
        gitLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        gitLabel.setTextFill(Color.web(WHITE));

        Label pulseLabel = new Label("Pulse");
        pulseLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        pulseLabel.setTextFill(Color.web(CYAN));
        // Minimized glow
        pulseLabel.setStyle("-fx-effect: dropshadow(gaussian, #00D4FF, 3, 0.12, 0, 0);");

        HBox logoBox = new HBox(8, logo, gitLabel, pulseLabel);
        logoBox.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Repo info pill
        Label repoPill = new Label("📁  " + repoUrl.replace("https://github.com/", ""));
        repoPill.setFont(Font.font("Segoe UI", 12));
        repoPill.setTextFill(Color.web(WHITE + "99"));
        repoPill.setStyle(
                "-fx-background-color: " + NAVY + ";" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 20;" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 4 14 4 14;"
        );

        // User badge
        Label userBadge = new Label("👤  " + username);
        userBadge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        userBadge.setTextFill(Color.web(CYAN));
        userBadge.setStyle(
                "-fx-background-color: " + CYAN + "22;" +
                        "-fx-border-color: " + CYAN + "55;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 20;" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 4 14 4 14;"
        );

        HBox rightBox = new HBox(12, repoPill, userBadge);
        rightBox.setAlignment(Pos.CENTER_RIGHT);

        bar.getChildren().addAll(logoBox, spacer, rightBox);
        return bar;
    }

    // ── Sidebar with nav buttons ──────────────────────────────────────
    private VBox buildSidebar() {
        VBox sidebar = new VBox(6);
        sidebar.setPadding(new Insets(24, 12, 24, 12));
        sidebar.setPrefWidth(200);
        sidebar.setStyle(
                "-fx-background-color: " + SURFACE + ";" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-width: 0 1 0 0;"
        );

        Label menuHeader = new Label("NAVIGATION");
        menuHeader.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        menuHeader.setTextFill(Color.web(WHITE + "44"));
        menuHeader.setStyle("-fx-padding: 0 0 10 8;");

        String[][] menuItems = {
                {"📊", "Summary"},
                {"📈", "Graphs"},
                {"🕐", "Commit History"},
                {"💡", "Suggestions"}
        };

        sidebar.getChildren().add(menuHeader);

        for (String[] item : menuItems) {
            Button btn = new Button(item[0] + "  " + item[1]);
            btn.setMaxWidth(Double.MAX_VALUE);
            styleSidebarBtn(btn, false);
            btn.setOnMouseEntered(e -> styleSidebarBtn(btn, true));
            btn.setOnMouseExited(e -> styleSidebarBtn(btn, false));
            sidebar.getChildren().add(btn);
        }

        // Push a divider + logout to bottom
        Region push = new Region();
        VBox.setVgrow(push, Priority.ALWAYS);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: " + BORDER + ";");

        Button logoutBtn = new Button("↩  Disconnect");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #FF6B6B;" +
                        "-fx-font-size: 13px;" +
                        "-fx-alignment: CENTER-LEFT;" +
                        "-fx-padding: 10 15 10 15;" +
                        "-fx-cursor: hand;" +
                        "-fx-border-radius: 8; -fx-background-radius: 8;"
        );

        sidebar.getChildren().addAll(push, sep, logoutBtn);
        return sidebar;
    }

    // ── Main content area (dummy cards) ──────────────────────────────
    private ScrollPane buildMainContent() {
        VBox content = new VBox(24);
        content.setPadding(new Insets(32, 32, 32, 32));

        // Page title
        Label pageTitle = new Label("Dashboard");
        pageTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        pageTitle.setTextFill(Color.web(WHITE));

        Label pageSub = new Label("Repository overview for " + repoUrl);
        pageSub.setFont(Font.font("Segoe UI", 13));
        pageSub.setTextFill(Color.web(WHITE + "55"));

        // Stats row
        HBox statsRow = new HBox(16);
        statsRow.getChildren().addAll(
                buildStatCard("⭐  Stars",         "2,847"),
                buildStatCard("🍴  Forks",          "312"),
                buildStatCard("📝  Commits",        "1,492"),
                buildStatCard("👥  Contributors",   "28")
        );

        // Placeholder panels
        HBox panels = new HBox(20);
        panels.getChildren().addAll(
                buildPlaceholderPanel("📈  Commit Activity Graph",   480, 240),
                buildPlaceholderPanel("👥  Top Contributors",         280, 240)
        );

        HBox panels2 = new HBox(20);
        panels2.getChildren().addAll(
                buildPlaceholderPanel("🕐  Recent Commit History",    380, 220),
                buildPlaceholderPanel("💡  AI Suggestions",           380, 220)
        );

        content.getChildren().addAll(pageTitle, pageSub, statsRow, panels, panels2);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle(
                "-fx-background: " + NAVY + ";" +
                        "-fx-background-color: " + NAVY + ";"
        );
        return scroll;
    }

    // ── Stat card ─────────────────────────────────────────────────────
    private VBox buildStatCard(String label, String value) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(18, 22, 18, 22));
        card.setStyle(
                "-fx-background-color: " + SURFACE + ";" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 12;" +
                        "-fx-background-radius: 12;"
        );
        HBox.setHgrow(card, Priority.ALWAYS);

        Label lbl = new Label(label);
        lbl.setFont(Font.font("Segoe UI", 12));
        lbl.setTextFill(Color.web(WHITE + "66"));

        Label val = new Label(value);
        val.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        val.setTextFill(Color.web(CYAN));

        card.getChildren().addAll(lbl, val);
        return card;
    }

    // ── Placeholder content panel ─────────────────────────────────────
    private VBox buildPlaceholderPanel(String title, double w, double h) {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(20));
        panel.setPrefWidth(w);
        panel.setPrefHeight(h);
        panel.setStyle(
                "-fx-background-color: " + SURFACE + ";" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 12;" +
                        "-fx-background-radius: 12;"
        );
        HBox.setHgrow(panel, Priority.ALWAYS);

        Label t = new Label(title);
        t.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        t.setTextFill(Color.web(WHITE));

        Label placeholder = new Label("Coming soon — data will appear here");
        placeholder.setFont(Font.font("Segoe UI", 12));
        placeholder.setTextFill(Color.web(WHITE + "33"));

        panel.getChildren().addAll(t, placeholder);
        return panel;
    }

    // ── Sidebar button style ──────────────────────────────────────────
    private void styleSidebarBtn(Button btn, boolean hovered) {
        btn.setStyle(
                "-fx-background-color: " + (hovered ? CYAN + "22" : "transparent") + ";" +
                        "-fx-text-fill: " + (hovered ? CYAN : WHITE) + ";" +
                        "-fx-font-size: 13px;" +
                        "-fx-alignment: CENTER-LEFT;" +
                        "-fx-padding: 10 15 10 15;" +
                        "-fx-cursor: hand;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;"
        );
    }

    // ── Same logo builder ─────────────────────────────────────────────
    private Canvas buildLogo(double w, double h) {
        Canvas c = new Canvas(w, h);
        GraphicsContext gc = c.getGraphicsContext2D();

        double cx = w / 2, cy = h / 2, r = w * 0.42;

        gc.setStroke(Color.web(CYAN + "33"));
        gc.setLineWidth(4);
        gc.strokeOval(cx - r - 4, cy - r - 4, (r + 4) * 2, (r + 4) * 2);

        gc.setStroke(Color.web(CYAN));
        gc.setLineWidth(2);
        gc.strokeOval(cx - r, cy - r, r * 2, r * 2);

        gc.setStroke(Color.web(CYAN));
        gc.setLineWidth(1.8);
        gc.setLineCap(StrokeLineCap.ROUND);

        double scale = w / 120.0;
        double[] xs = {cx - 28 * scale, cx - 16 * scale, cx - 6 * scale,
                cx + 4 * scale,  cx + 14 * scale, cx + 22 * scale, cx + 28 * scale};
        double[] ys = {cy, cy, cy - 24 * scale, cy + 18 * scale,
                cy - 14 * scale, cy, cy};

        gc.beginPath();
        gc.moveTo(xs[0], ys[0]);
        for (int i = 1; i < xs.length; i++) gc.lineTo(xs[i], ys[i]);
        gc.stroke();

        gc.setFill(Color.web(CYAN));
        gc.fillOval(xs[0] - 3, ys[0] - 3, 6, 6);
        gc.fillOval(xs[6] - 3, ys[6] - 3, 6, 6);
        gc.fillOval(xs[2] - 3, ys[2] - 3, 6, 6);

        return c;
    }
}