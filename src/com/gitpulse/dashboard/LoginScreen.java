package com.gitpulse.dashboard;

import com.gitpulse.Home.DashboardScreen;

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

public class LoginScreen {

    private static final String NAVY    = "#0D1117";
    private static final String CYAN    = "#00D4FF";
    private static final String WHITE   = "#E6EDF3";
    private static final String SURFACE = "#161B22";
    private static final String BORDER  = "#21262D";

    // Dummy credentials for now
    private static final String DUMMY_REPO = "https://github.com/dummy/gitpulse-demo";
    private static final String DUMMY_USER = "gitpulse";

    public void show(Stage stage) {

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: " + NAVY + ";");
        root.setPrefSize(1280 , 800);
        VBox center = new VBox(32);
        center.setAlignment(Pos.CENTER);
//        center.setMaxWidth(1920);

        //Logo
        Canvas logo = buildLogo(90, 90);

        //Brand name
        Label gitLabel = new Label("Git");
        gitLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 36));
        gitLabel.setTextFill(Color.web(WHITE));

        Label pulseLabel = new Label("Pulse");
        pulseLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 36));
        pulseLabel.setTextFill(Color.web(CYAN));
        // Minimized glow — much softer than splash screen
        pulseLabel.setStyle("-fx-effect: dropshadow(gaussian, #00D4FF, 3, 0.15, 0, 0);");

        HBox brandBox = new HBox(0, gitLabel, pulseLabel);
        brandBox.setAlignment(Pos.CENTER);

        Label tagline = new Label("Connect your repository to get started");
        tagline.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        tagline.setTextFill(Color.web(WHITE + "77"));

        VBox logoSection = new VBox(10, logo, brandBox, tagline);
        logoSection.setAlignment(Pos.CENTER);

        //Login Card
        VBox card = new VBox(18);
        card.setStyle(
                "-fx-background-color: " + SURFACE + ";" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 14;" +
                        "-fx-background-radius: 14;" +
                        "-fx-padding: 32;"
        );
        card.setMaxWidth(480);

        Label cardTitle = new Label("Repository Access");
        cardTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        cardTitle.setTextFill(Color.web(WHITE));

        Label cardSub = new Label("Enter your GitHub repository URL and username to continue.");
        cardSub.setFont(Font.font("Segoe UI", 13));
        cardSub.setTextFill(Color.web(WHITE + "66"));
        cardSub.setWrapText(true);

        // Repo URL field
        Label repoLabel = new Label("Repository URL");
        repoLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        repoLabel.setTextFill(Color.web(CYAN));

        TextField repoField = new TextField();
        repoField.setPromptText("https://github.com/username/repository");
        styleTextField(repoField);
        repoField.focusedProperty().addListener((obs, old, focused) ->
                styleTextFieldFocus(repoField, focused));

        // Username field
        Label userLabel = new Label("GitHub Username");
        userLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        userLabel.setTextFill(Color.web(CYAN));

        TextField userField = new TextField();
        userField.setPromptText("e.g. torvalds");
        styleTextField(userField);
        userField.focusedProperty().addListener((obs, old, focused) ->
                styleTextFieldFocus(userField, focused));

        // Error label (hidden by default)
        Label errorLabel = new Label("");
        errorLabel.setFont(Font.font("Segoe UI", 12));
        errorLabel.setTextFill(Color.web("#FF6B6B"));
        errorLabel.setVisible(false);

        // Analyze button
        Button analyzeBtn = new Button("⚡  Connect & Analyze");
        analyzeBtn.setMaxWidth(Double.MAX_VALUE);
        stylePrimaryButton(analyzeBtn, false);
        analyzeBtn.setOnMouseEntered(e -> stylePrimaryButton(analyzeBtn, true));
        analyzeBtn.setOnMouseExited(e -> stylePrimaryButton(analyzeBtn, false));

        analyzeBtn.setOnAction(e -> {
            String repo = repoField.getText().trim();
            String user = userField.getText().trim();

            if (repo.isEmpty() || user.isEmpty()) {
                errorLabel.setText("⚠  Please fill in both fields.");
                errorLabel.setVisible(true);
                return;
            }

            // Dummy validation
            if (repo.equals(DUMMY_REPO) && user.equalsIgnoreCase(DUMMY_USER)) {
                errorLabel.setVisible(false);
                DashboardScreen dashboard = new DashboardScreen(repo, user);
                dashboard.show(stage);
            } else {
                errorLabel.setText("⚠  Invalid credentials. Use the demo repo to continue.");
                errorLabel.setVisible(true);
            }
        });

        // Demo hint
        Label hint = new Label("Demo → URL: " + DUMMY_REPO + "  |  User: " + DUMMY_USER);
        hint.setFont(Font.font("Segoe UI", 11));
        hint.setTextFill(Color.web(WHITE + "44"));
        hint.setWrapText(true);

        card.getChildren().addAll(
                cardTitle, cardSub,
                repoLabel, repoField,
                userLabel, userField,
                errorLabel, analyzeBtn, hint
        );

        center.getChildren().addAll(logoSection, card);

        // ── Powered by ────────────────────────────────────────────────
        Label powered = new Label("Powered by Gate");
        powered.setFont(Font.font("Segoe UI", 11));
        powered.setTextFill(Color.web(CYAN + "66"));
        powered.setStyle("-fx-padding: 0 0 14 0;");
        StackPane.setAlignment(powered, Pos.BOTTOM_CENTER);

        root.getChildren().addAll(center, powered);

        // ── Scene ─────────────────────────────────────────────────────
//        Scene scene = new Scene(root, 720, 600);
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setTitle("GitPulse — Connect Repository");
        stage.show();

        // ── Entrance animation ────────────────────────────────────────
        center.setOpacity(0);
        center.setTranslateY(20);
        FadeTransition fade = new FadeTransition(Duration.millis(500), center);
        fade.setToValue(1);
        TranslateTransition slide = new TranslateTransition(Duration.millis(500), center);
        slide.setToY(0);
        new ParallelTransition(fade, slide).play();
    }

    // ── Same logo builder as SplashScreen ─────────────────────────────
    private Canvas buildLogo(double w, double h) {
        Canvas c = new Canvas(w, h);
        GraphicsContext gc = c.getGraphicsContext2D();

        double cx = w / 2, cy = h / 2, r = w * 0.42;

        gc.setStroke(Color.web(CYAN + "33"));
        gc.setLineWidth(8);
        gc.strokeOval(cx - r - 6, cy - r - 6, (r + 6) * 2, (r + 6) * 2);

        gc.setStroke(Color.web(CYAN));
        gc.setLineWidth(3);
        gc.strokeOval(cx - r, cy - r, r * 2, r * 2);

        gc.setStroke(Color.web(CYAN));
        gc.setLineWidth(2.8);
        gc.setLineCap(StrokeLineCap.ROUND);

        double[] xs = {cx - 28, cx - 16, cx - 6, cx + 4, cx + 14, cx + 22, cx + 28};
        double[] ys = {cy,      cy,      cy - 24, cy + 18, cy - 14, cy,      cy    };

        gc.beginPath();
        gc.moveTo(xs[0], ys[0]);
        for (int i = 1; i < xs.length; i++) gc.lineTo(xs[i], ys[i]);
        gc.stroke();

        gc.setFill(Color.web(CYAN));
        gc.fillOval(cx - 28 - 4, cy - 4, 8, 8);
        gc.fillOval(cx + 28 - 4, cy - 4, 8, 8);
        gc.fillOval(cx - 4,      cy - 24 - 4, 8, 8);

        gc.setFill(Color.web(CYAN + "55"));
        gc.fillOval(cx - 28 - 7, cy - 7, 14, 14);
        gc.fillOval(cx + 28 - 7, cy - 7, 14, 14);

        return c;
    }

    private void styleTextField(TextField field) {
        field.setStyle(
                "-fx-background-color: " + NAVY + ";" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-text-fill: " + WHITE + ";" +
                        "-fx-prompt-text-fill: #8B949E;" +
                        "-fx-font-size: 13px;" +
                        "-fx-padding: 10 14 10 14;"
        );
    }

    private void styleTextFieldFocus(TextField field, boolean focused) {
        field.setStyle(
                "-fx-background-color: " + NAVY + ";" +
                        "-fx-border-color: " + (focused ? CYAN : BORDER) + ";" +
                        "-fx-border-width: " + (focused ? "1.5" : "1") + ";" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-text-fill: " + WHITE + ";" +
                        "-fx-prompt-text-fill: #8B949E;" +
                        "-fx-font-size: 13px;" +
                        "-fx-padding: 10 14 10 14;"
        );
    }

    private void stylePrimaryButton(Button btn, boolean hovered) {
        btn.setStyle(
                "-fx-background-color: " + (hovered ? "#33DDFF" : CYAN) + ";" +
                        "-fx-text-fill: " + NAVY + ";" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 12 0 12 0;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;" +
                        (hovered ? "-fx-effect: dropshadow(gaussian, #00D4FF, 14, 0.4, 0, 0);" : "")
        );
    }
}