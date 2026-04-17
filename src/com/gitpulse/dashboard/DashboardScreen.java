package com.gitpulse.dashboard;


import javafx.animation.*;
        import javafx.geometry.*;
        import javafx.scene.Scene;
import javafx.scene.control.*;
        import javafx.scene.layout.*;
        import javafx.scene.paint.Color;
import javafx.scene.text.*;
        import javafx.stage.Stage;
import javafx.util.Duration;

public class DashboardScreen {

    private static final String NAVY    = "#0D1117";
    private static final String CYAN    = "#00D4FF";
    private static final String WHITE   = "#E6EDF3";
    private static final String SURFACE = "#161B22";
    private static final String BORDER  = "#21262D";

    public void show(Stage stage) {

        // ── Layout skeleton ───────────────────────────────────────────
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + NAVY + ";");

        // ── Top bar ───────────────────────────────────────────────────
        HBox topBar = buildTopBar();
        root.setTop(topBar);

        // ── Input panel ───────────────────────────────────────────────
        VBox inputPanel = buildInputPanel();
        root.setCenter(inputPanel);

        // ── Scene ─────────────────────────────────────────────────────
        Scene scene = new Scene(root, 1100, 700);
        stage.setScene(scene);
        stage.setTitle("GitPulse — Analytics Dashboard");
        stage.show();

        // ── Slide-up entrance ─────────────────────────────────────────
        root.setTranslateY(40);
        root.setOpacity(0);
        TranslateTransition slide = new TranslateTransition(Duration.millis(600), root);
        slide.setToY(0);
        FadeTransition fade = new FadeTransition(Duration.millis(600), root);
        fade.setToValue(1);
        new ParallelTransition(slide, fade).play();
    }

    // ── Top navigation bar ────────────────────────────────────────────
    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(0, 24, 0, 24));
        bar.setPrefHeight(56);
        bar.setStyle(
                "-fx-background-color: " + SURFACE + ";" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-width: 0 0 1 0;"
        );

        // Logo text
        Label logo = new Label("Git");
        logo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        logo.setTextFill(Color.web(WHITE));

        Label logoCyan = new Label("Pulse");
        logoCyan.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        logoCyan.setTextFill(Color.web(CYAN));
        logoCyan.setStyle("-fx-effect: dropshadow(gaussian, #00D4FF, 8, 0.4, 0, 0);");

        HBox logoBox = new HBox(0, logo, logoCyan);
        logoBox.setAlignment(Pos.CENTER_LEFT);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Nav items
        String[] navItems = {"Dashboard", "Repositories", "Contributors", "Settings"};
        HBox navBox = new HBox(8);
        navBox.setAlignment(Pos.CENTER);
        for (String item : navItems) {
            Button btn = new Button(item);
            btn.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-text-fill: " + WHITE + "99;" +
                            "-fx-font-size: 13px;" +
                            "-fx-cursor: hand;" +
                            "-fx-padding: 6 14 6 14;" +
                            "-fx-border-radius: 6; -fx-background-radius: 6;"
            );
            btn.setOnMouseEntered(e -> btn.setStyle(
                    "-fx-background-color: " + CYAN + "22;" +
                            "-fx-text-fill: " + CYAN + ";" +
                            "-fx-font-size: 13px;" +
                            "-fx-cursor: hand;" +
                            "-fx-padding: 6 14 6 14;" +
                            "-fx-border-radius: 6; -fx-background-radius: 6;"
            ));
            btn.setOnMouseExited(e -> btn.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-text-fill: " + WHITE + "99;" +
                            "-fx-font-size: 13px;" +
                            "-fx-cursor: hand;" +
                            "-fx-padding: 6 14 6 14;" +
                            "-fx-border-radius: 6; -fx-background-radius: 6;"
            ));
            navBox.getChildren().add(btn);
        }

        bar.getChildren().addAll(logoBox, spacer, navBox);
        return bar;
    }

    // ── Center input panel ────────────────────────────────────────────
    private VBox buildInputPanel() {
        VBox panel = new VBox(28);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(60, 100, 60, 100));

        // Heading
        Label heading = new Label("Analyze Any GitHub Repository");
        heading.setFont(Font.font("Segoe UI", FontWeight.BOLD, 32));
        heading.setTextFill(Color.web(WHITE));

        Label sub = new Label(
                "Paste a GitHub repository URL and get instant visual analytics — contributors, commit history, activity graphs, and more."
        );
        sub.setFont(Font.font("Segoe UI", 15));
        sub.setTextFill(Color.web(WHITE + "88"));
        sub.setWrapText(true);
        sub.setMaxWidth(600);
        sub.setTextAlignment(TextAlignment.CENTER);

        // Input card
        VBox card = new VBox(16);
        card.setStyle(
                "-fx-background-color: " + SURFACE + ";" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 12;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 28;"
        );
        card.setMaxWidth(640);

        Label repoLabel = new Label("Repository URL");
        repoLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        repoLabel.setTextFill(Color.web(CYAN));

        TextField repoField = new TextField();
        repoField.setPromptText("https://github.com/username/repository");
        repoField.setStyle(
                "-fx-background-color: " + NAVY + ";" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-text-fill: " + WHITE + ";" +
                        "-fx-prompt-text-fill: #8B949E;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 10 14 10 14;"
        );
        repoField.focusedProperty().addListener((obs, old, focused) -> {
            if (focused) {
                repoField.setStyle(
                        "-fx-background-color: " + NAVY + ";" +
                                "-fx-border-color: " + CYAN + ";" +
                                "-fx-border-width: 1.5;" +
                                "-fx-border-radius: 8;" +
                                "-fx-background-radius: 8;" +
                                "-fx-text-fill: " + WHITE + ";" +
                                "-fx-prompt-text-fill: #8B949E;" +
                                "-fx-font-size: 14px;" +
                                "-fx-padding: 10 14 10 14;"
                );
            } else {
                repoField.setStyle(
                        "-fx-background-color: " + NAVY + ";" +
                                "-fx-border-color: " + BORDER + ";" +
                                "-fx-border-width: 1;" +
                                "-fx-border-radius: 8;" +
                                "-fx-background-radius: 8;" +
                                "-fx-text-fill: " + WHITE + ";" +
                                "-fx-prompt-text-fill: #8B949E;" +
                                "-fx-font-size: 14px;" +
                                "-fx-padding: 10 14 10 14;"
                );
            }
        });

        Label userLabel = new Label("GitHub Username (optional)");
        userLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        userLabel.setTextFill(Color.web(CYAN));

        TextField userField = new TextField();
        userField.setPromptText("e.g. torvalds");
        userField.setStyle(
                "-fx-background-color: " + NAVY + ";" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-text-fill: " + WHITE + ";" +
                        "-fx-prompt-text-fill: #8B949E;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 10 14 10 14;"
        );

        Button analyzeBtn = new Button("⚡  Generate Analytics");
        analyzeBtn.setMaxWidth(Double.MAX_VALUE);
        analyzeBtn.setStyle(
                "-fx-background-color: " + CYAN + ";" +
                        "-fx-text-fill: " + NAVY + ";" +
                        "-fx-font-size: 15px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 12 0 12 0;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;"
        );
        analyzeBtn.setOnMouseEntered(e -> analyzeBtn.setStyle(
                "-fx-background-color: #33DDFF;" +
                        "-fx-text-fill: " + NAVY + ";" +
                        "-fx-font-size: 15px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 12 0 12 0;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, #00D4FF, 14, 0.5, 0, 0);"
        ));
        analyzeBtn.setOnMouseExited(e -> analyzeBtn.setStyle(
                "-fx-background-color: " + CYAN + ";" +
                        "-fx-text-fill: " + NAVY + ";" +
                        "-fx-font-size: 15px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 12 0 12 0;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;"
        ));

        card.getChildren().addAll(repoLabel, repoField, userLabel, userField, analyzeBtn);

        // Feature chips
        HBox chips = new HBox(12);
        chips.setAlignment(Pos.CENTER);
        String[] features = {"📊 Commit Graph", "👥 Contributors", "🕐 Timeline", "🌿 Branches", "⭐ Stars"};
        for (String f : features) {
            Label chip = new Label(f);
            chip.setStyle(
                    "-fx-background-color: " + SURFACE + ";" +
                            "-fx-border-color: " + BORDER + ";" +
                            "-fx-border-width: 1;" +
                            "-fx-border-radius: 20;" +
                            "-fx-background-radius: 20;" +
                            "-fx-text-fill: " + WHITE + "88;" +
                            "-fx-font-size: 12px;" +
                            "-fx-padding: 5 12 5 12;"
            );
            chips.getChildren().add(chip);
        }

        panel.getChildren().addAll(heading, sub, card, chips);
        return panel;
    }
}