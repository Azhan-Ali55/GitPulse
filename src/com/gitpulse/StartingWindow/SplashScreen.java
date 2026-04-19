package com.gitpulse.StartingWindow;

import com.gitpulse.dashboard.LoginScreen;


import javafx.animation.*;
        import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
        import javafx.scene.paint.Color;
import javafx.scene.shape.*;
        import javafx.scene.text.*;
        import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class SplashScreen {

    //3  Whole project colors
    private static final String NAVY  = "#0D1117";
    private static final String CYAN  = "#00D4FF";
    private static final String WHITE = "#E6EDF3";

    public void show(Stage stage) {

        //Root Pane
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: " + NAVY + ";");
        root.setPrefSize(1280 , 800);

        // Center content VBox
        VBox center = new VBox(18);
        center.setAlignment(Pos.CENTER);
        center.setTranslateY(-20);

        // Logo (SVG-style drawn with Canvas)
        Canvas logo = buildLogo(120, 120);

        //GitPulse name
        Label gitLabel = new Label("Git");
        gitLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 48));
        gitLabel.setTextFill(Color.web(WHITE));

        Label pulseLabel = new Label("Pulse");
        pulseLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 48));
        pulseLabel.setTextFill(Color.web(CYAN));
        pulseLabel.setStyle("-fx-effect: dropshadow(gaussian, #00D4FF, 6, 0.25, 0, 0);");

        HBox nameBox = new HBox(0, gitLabel, pulseLabel);
        nameBox.setAlignment(Pos.CENTER);

        // Tagline
        Label tagline = new Label("Instant Repository Analytics");
        tagline.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 15));
        tagline.setTextFill(Color.web(WHITE + "99"));

        //Spinner
        Arc spinner = new Arc(0, 0, 22, 22, 90, 270);
        spinner.setType(ArcType.OPEN);
        spinner.setStroke(Color.web(CYAN));
        spinner.setStrokeWidth(3);
        spinner.setFill(Color.TRANSPARENT);
        spinner.setStrokeLineCap(StrokeLineCap.ROUND);

        RotateTransition spin = new RotateTransition(Duration.millis(900), spinner);
        spin.setByAngle(360);
        spin.setCycleCount(Animation.INDEFINITE);
        spin.setInterpolator(Interpolator.LINEAR);
        spin.play();

        StackPane spinnerWrap = new StackPane(spinner);
        spinnerWrap.setPrefSize(60, 60);

        // Loading label
        Label loading = new Label("Initializing...");
        loading.setFont(Font.font("Segoe UI", 12));
        loading.setTextFill(Color.web(WHITE + "66"));

        center.getChildren().addAll(logo, nameBox, tagline, spinnerWrap, loading);

        // "Powered by Gate" bottom-left
        Label powered = new Label("Powered by Gate");
        powered.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        powered.setTextFill(Color.web(CYAN + "99"));
        powered.setStyle("-fx-padding: 0 0 14 16;");
        StackPane.setAlignment(powered, javafx.geometry.Pos.BOTTOM_LEFT);

        root.getChildren().addAll(center, powered);

        //Scene
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setMaximized(true);
        stage.setTitle("GitPulse");
        stage.show();

        // Zoom-from-center entrance animation
        center.setScaleX(0.01);
        center.setScaleY(0.01);
        center.setOpacity(0);

        ScaleTransition zoomIn = new ScaleTransition(Duration.millis(700), center);
        zoomIn.setFromX(0.01);
        zoomIn.setFromY(0.01);
        zoomIn.setToX(1.0);
        zoomIn.setToY(1.0);
        zoomIn.setInterpolator(Interpolator.SPLINE(0.17, 0.67, 0.35, 1.4)); // overshoot spring

        FadeTransition fadeIn = new FadeTransition(Duration.millis(700), center);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ParallelTransition entrance = new ParallelTransition(zoomIn, fadeIn);
        entrance.play();

        // Wait 5 s then open Dashboard
        PauseTransition wait = new PauseTransition(Duration.seconds(5));
        wait.setOnFinished(e -> {
            spin.stop();
            LoginScreen dashboard = new LoginScreen();
            dashboard.show(stage);
        });

        entrance.setOnFinished(ev -> wait.play());
    }

    //Build the GitPulse logo on a Canvas
    private Canvas buildLogo(double w, double h) {
        Canvas c = new Canvas(w, h);
        GraphicsContext gc = c.getGraphicsContext2D();

        double cx = w / 2, cy = h / 2, r = w * 0.42;

        // Outer glow ring
        gc.setStroke(Color.web(CYAN + "33"));
        gc.setLineWidth(8);
        gc.strokeOval(cx - r - 6, cy - r - 6, (r + 6) * 2, (r + 6) * 2);

        // Main circle
        gc.setStroke(Color.web(CYAN));
        gc.setLineWidth(3);
        gc.strokeOval(cx - r, cy - r, r * 2, r * 2);

        // Pulse wave inside (3 arcs — like a heartbeat/activity icon)
        gc.setStroke(Color.web(CYAN));
        gc.setLineWidth(2.8);
        gc.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);

        double[] xs = {cx - 28, cx - 16, cx - 6, cx + 4, cx + 14, cx + 22, cx + 28};
        double[] ys = {cy,      cy,      cy - 24, cy + 18, cy - 14, cy,      cy};

        gc.beginPath();
        gc.moveTo(xs[0], ys[0]);
        for (int i = 1; i < xs.length; i++) {
            gc.lineTo(xs[i], ys[i]);
        }
        gc.stroke();

        // Branch dots (represent git nodes)
        gc.setFill(Color.web(CYAN));
        gc.fillOval(cx - 28 - 4, cy - 4, 8, 8);
        gc.fillOval(cx + 28 - 4, cy - 4, 8, 8);
        gc.fillOval(cx - 4,      cy - 24 - 4, 8, 8);

        // Small glow dots
        gc.setFill(Color.web(CYAN + "55"));
        gc.fillOval(cx - 28 - 7, cy - 7, 14, 14);
        gc.fillOval(cx + 28 - 7, cy - 7, 14, 14);

        return c;
    }
}