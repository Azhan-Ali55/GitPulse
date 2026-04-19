package com.gitpulse.Home;

import com.gitpulse.model.Commit;
import com.gitpulse.model.Contributor;
import com.gitpulse.model.Repository;
import com.gitpulse.model.WeeklySummary;
import com.gitpulse.service.DataService;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;

public class DashboardScreen {

    // ── Colors ────────────────────────────────────────────────────────
    private static final String NAVY    = "#0D1117";
    private static final String CYAN    = "#00D4FF";
    private static final String WHITE   = "#E6EDF3";
    private static final String SURFACE = "#161B22";
    private static final String BORDER  = "#21262D";

    // ── State ─────────────────────────────────────────────────────────
    private final String repoUrl;
    private final String username;
    private final String owner;
    private final String repoName;

    // The center content area — swapped when sidebar buttons are clicked
    private StackPane centerArea;
    private Repository loadedRepo = null;

    public DashboardScreen(String repoUrl, String username) {
        this.repoUrl  = repoUrl;
        this.username = username;

        // Parse owner and repo name from URL
        // e.g. https://github.com/torvalds/linux → owner=torvalds, repo=linux
        String path = repoUrl.replace("https://github.com/", "").trim();
        String[] parts = path.split("/");
        this.owner    = parts.length > 0 ? parts[0] : username;
        this.repoName = parts.length > 1 ? parts[1] : "unknown";
    }

    public void show(Stage stage) {

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + NAVY + ";");
        root.setPrefSize(1280 , 800);
        // Top bar
        root.setTop(buildTopBar());

        // Sidebar
        root.setLeft(buildSidebar());

        // Center — starts with loading spinner
        centerArea = new StackPane();
        centerArea.setStyle("-fx-background-color: " + NAVY + ";");
        root.setCenter(centerArea);

        // Scene
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setTitle("GitPulse — Dashboard");
        stage.show();

        // Entrance animation
        root.setOpacity(0);
        root.setTranslateY(30);
        FadeTransition fade = new FadeTransition(Duration.millis(600), root);
        fade.setToValue(1);
        TranslateTransition slide = new TranslateTransition(Duration.millis(600), root);
        slide.setToY(0);
        new ParallelTransition(fade, slide).play();

        // Start fetching real data
        fetchData();
    }

    // ── Fetch data from GitHub on background thread ───────────────────
    private void fetchData() {
        showLoading("Fetching repository data from GitHub...");

        DataService dataService = new DataService();
        Task<Repository> task = dataService.loadRepositoryAsync(owner, repoName);

        task.setOnSucceeded(e -> {
            loadedRepo = task.getValue();
            Platform.runLater(() -> showSummaryPanel());
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> showError(
                    "Failed to load repository. Check your GitHub token and repo URL."
            ));
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    // ── Loading spinner panel ─────────────────────────────────────────
    private void showLoading(String message) {
        VBox box = new VBox(20);
        box.setAlignment(Pos.CENTER);

        Arc spinner = new Arc(0, 0, 28, 28, 90, 270);
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

        StackPane spinWrap = new StackPane(spinner);
        spinWrap.setPrefSize(70, 70);

        Label lbl = new Label(message);
        lbl.setFont(Font.font("Segoe UI", 14));
        lbl.setTextFill(Color.web(WHITE + "88"));

        box.getChildren().addAll(spinWrap, lbl);
        centerArea.getChildren().setAll(box);
    }

    // ── Error panel ───────────────────────────────────────────────────
    private void showError(String message) {
        VBox box = new VBox(12);
        box.setAlignment(Pos.CENTER);

        Label icon = new Label("⚠");
        icon.setFont(Font.font("Segoe UI", 40));
        icon.setTextFill(Color.web("#FF6B6B"));

        Label lbl = new Label(message);
        lbl.setFont(Font.font("Segoe UI", 14));
        lbl.setTextFill(Color.web(WHITE + "88"));
        lbl.setWrapText(true);
        lbl.setMaxWidth(500);
        lbl.setTextAlignment(TextAlignment.CENTER);

        box.getChildren().addAll(icon, lbl);
        centerArea.getChildren().setAll(box);
    }

    // ─────────────────────────────────────────────────────────────────
    // ── PANEL 1: Summary ─────────────────────────────────────────────
    // ─────────────────────────────────────────────────────────────────
    private void showSummaryPanel() {
        if (loadedRepo == null) { showError("No data loaded."); return; }

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: " + NAVY + "; -fx-background-color: " + NAVY + ";");

        VBox content = new VBox(24);
        content.setPadding(new Insets(32));

        // Page header
        Label title = new Label("📊  Summary");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        title.setTextFill(Color.web(WHITE));

        Label sub = new Label(owner + " / " + repoName);
        sub.setFont(Font.font("Segoe UI", 13));
        sub.setTextFill(Color.web(WHITE + "55"));

        // ── Stat cards row ────────────────────────────────────────────
        HBox statsRow = new HBox(16);
        statsRow.getChildren().addAll(
                buildStatCard("📝  Commits",      String.valueOf(loadedRepo.getCommits().size())),
                buildStatCard("👥  Contributors", String.valueOf(loadedRepo.getContributors().size())),
                buildStatCard("🌐  Language",     nvl(loadedRepo.getLanguage(), "N/A")),
                buildStatCard("🕐  Last Commit",  formatDate(loadedRepo.getLastCommitDate()))
        );

        // ── Repo info card ────────────────────────────────────────────
        VBox infoCard = buildCard("📋  Repository Info");
        addInfoRow(infoCard, "Owner",       owner);
        addInfoRow(infoCard, "Repository",  repoName);
        addInfoRow(infoCard, "Language",    nvl(loadedRepo.getLanguage(), "Not specified"));
        addInfoRow(infoCard, "Description", nvl(loadedRepo.getDescription(), "No description"));
        addInfoRow(infoCard, "Last Commit", nvl(loadedRepo.getLastCommitDate(), "Unknown"));

        // ── Top contributors quick list ────────────────────────────────
        VBox contribCard = buildCard("👥  Top Contributors");
        List<Contributor> top5 = loadedRepo.getContributors().stream().limit(5).toList();
        if (top5.isEmpty()) {
            contribCard.getChildren().add(dimLabel("No contributor data available."));
        } else {
            for (Contributor c : top5) {
                HBox row = new HBox(12);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(6, 0, 6, 0));

                Label nameL = new Label("@" + c.getUsername());
                nameL.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
                nameL.setTextFill(Color.web(CYAN));
                nameL.setMinWidth(160);

                Label commitsL = new Label(c.getTotalCommits() + " commits");
                commitsL.setFont(Font.font("Segoe UI", 12));
                commitsL.setTextFill(Color.web(WHITE + "88"));

                // Progress bar showing share
                double share = loadedRepo.getCommits().isEmpty() ? 0
                        : (double) c.getTotalCommits() / loadedRepo.getCommits().size();
                ProgressBar pb = new ProgressBar(share);
                pb.setPrefWidth(180);
                pb.setStyle(
                        "-fx-accent: " + CYAN + ";" +
                                "-fx-background-color: " + BORDER + ";" +
                                "-fx-background-radius: 4; -fx-border-radius: 4;"
                );

                row.getChildren().addAll(nameL, commitsL, pb);
                contribCard.getChildren().add(row);
                contribCard.getChildren().add(new Separator());
            }
        }

        content.getChildren().addAll(title, sub, statsRow, infoCard, contribCard);
        scroll.setContent(content);
        centerArea.getChildren().setAll(scroll);
    }

    // ─────────────────────────────────────────────────────────────────
    // ── PANEL 2: Graphs ───────────────────────────────────────────────
    // ─────────────────────────────────────────────────────────────────
    private void showGraphsPanel() {
        if (loadedRepo == null) { showError("No data loaded."); return; }

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: " + NAVY + "; -fx-background-color: " + NAVY + ";");

        VBox content = new VBox(24);
        content.setPadding(new Insets(32));

        Label title = new Label("📈  Graphs");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        title.setTextFill(Color.web(WHITE));

        // ── Weekly commit bar chart ────────────────────────────────────
        VBox chartCard = buildCard("📅  Weekly Commit Activity");

        if (loadedRepo.getWeeklyActivity() == null || loadedRepo.getWeeklyActivity().isEmpty()) {
            chartCard.getChildren().add(dimLabel("No weekly activity data available."));
        } else {
            // Find max commits in a week for scaling
            int maxCommits = loadedRepo.getWeeklyActivity().values().stream()
                    .mapToInt(List::size).max().orElse(1);

            // Show last 12 weeks
            var weeks = loadedRepo.getWeeklyActivity().entrySet().stream()
                    .sorted(java.util.Map.Entry.<java.time.LocalDate, List<Commit>>comparingByKey().reversed())
                    .limit(12)
                    .sorted(java.util.Map.Entry.comparingByKey())
                    .toList();

            HBox bars = new HBox(8);
            bars.setAlignment(Pos.BOTTOM_LEFT);
            bars.setPadding(new Insets(16, 0, 8, 0));
            bars.setPrefHeight(200);

            for (var entry : weeks) {
                int count = entry.getValue().size();
                double barHeight = maxCommits == 0 ? 4
                        : Math.max(4, (double) count / maxCommits * 160);

                VBox barGroup = new VBox(4);
                barGroup.setAlignment(Pos.BOTTOM_CENTER);

                // Count label on top
                Label countLbl = new Label(String.valueOf(count));
                countLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
                countLbl.setTextFill(Color.web(CYAN));

                // The bar
                Region bar = new Region();
                bar.setPrefWidth(36);
                bar.setPrefHeight(barHeight);
                bar.setStyle(
                        "-fx-background-color: " + CYAN + ";" +
                                "-fx-background-radius: 4 4 0 0;"
                );

                // Week label below
                String weekLbl = entry.getKey().toString().substring(5); // MM-DD
                Label dateLbl = new Label(weekLbl);
                dateLbl.setFont(Font.font("Segoe UI", 9));
                dateLbl.setTextFill(Color.web(WHITE + "55"));
                dateLbl.setRotate(-35);

                barGroup.getChildren().addAll(countLbl, bar, dateLbl);
                bars.getChildren().add(barGroup);
            }

            chartCard.getChildren().add(bars);
        }

        // ── Contributor pie-style list ────────────────────────────────
        VBox pieCard = buildCard("👥  Contributor Share");

        if (loadedRepo.getContributors().isEmpty()) {
            pieCard.getChildren().add(dimLabel("No contributor data."));
        } else {
            int totalCommits = loadedRepo.getCommits().size();
            for (Contributor c : loadedRepo.getContributors().stream().limit(8).toList()) {
                double pct = totalCommits == 0 ? 0
                        : (double) c.getTotalCommits() / totalCommits * 100;

                HBox row = new HBox(12);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(5, 0, 5, 0));

                Label nameLbl = new Label("@" + c.getUsername());
                nameLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                nameLbl.setTextFill(Color.web(WHITE));
                nameLbl.setMinWidth(150);

                ProgressBar pb = new ProgressBar(pct / 100);
                pb.setPrefWidth(220);
                pb.setStyle(
                        "-fx-accent: " + CYAN + ";" +
                                "-fx-background-color: " + BORDER + ";" +
                                "-fx-background-radius: 4;"
                );

                Label pctLbl = new Label(String.format("%.1f%%", pct));
                pctLbl.setFont(Font.font("Segoe UI", 11));
                pctLbl.setTextFill(Color.web(CYAN));

                row.getChildren().addAll(nameLbl, pb, pctLbl);
                pieCard.getChildren().add(row);
            }
        }

        content.getChildren().addAll(title, chartCard, pieCard);
        scroll.setContent(content);
        centerArea.getChildren().setAll(scroll);
    }

    // ─────────────────────────────────────────────────────────────────
    // ── PANEL 3: Commit History ───────────────────────────────────────
    // ─────────────────────────────────────────────────────────────────
    private void showCommitHistoryPanel() {
        if (loadedRepo == null) { showError("No data loaded."); return; }

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: " + NAVY + "; -fx-background-color: " + NAVY + ";");

        VBox content = new VBox(24);
        content.setPadding(new Insets(32));

        Label title = new Label("🕐  Commit History");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        title.setTextFill(Color.web(WHITE));

        Label countLbl = new Label("Showing " + loadedRepo.getCommits().size() + " commits");
        countLbl.setFont(Font.font("Segoe UI", 13));
        countLbl.setTextFill(Color.web(WHITE + "55"));

        VBox commitsCard = buildCard("📝  All Commits");

        if (loadedRepo.getCommits().isEmpty()) {
            commitsCard.getChildren().add(dimLabel("No commit data available."));
        } else {
            for (Commit c : loadedRepo.getCommits()) {
                VBox commitRow = new VBox(4);
                commitRow.setPadding(new Insets(10, 0, 10, 0));
                commitRow.setStyle(
                        "-fx-border-color: transparent transparent " + BORDER + " transparent;" +
                                "-fx-border-width: 1;"
                );

                // Top row: author + date
                HBox meta = new HBox(12);
                meta.setAlignment(Pos.CENTER_LEFT);

                Label authorLbl = new Label("@" + c.getAuthorName());
                authorLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                authorLbl.setTextFill(Color.web(CYAN));

                Label dateLbl = new Label(formatDate(c.getDate()));
                dateLbl.setFont(Font.font("Segoe UI", 11));
                dateLbl.setTextFill(Color.web(WHITE + "44"));

                Label shaLbl = new Label(c.getSha().substring(0, 7));
                shaLbl.setFont(Font.font("Courier New", 11));
                shaLbl.setTextFill(Color.web(WHITE + "33"));
                shaLbl.setStyle(
                        "-fx-background-color: " + BORDER + ";" +
                                "-fx-background-radius: 4;" +
                                "-fx-padding: 1 6 1 6;"
                );

                meta.getChildren().addAll(authorLbl, dateLbl, shaLbl);

                // Message
                String msg = c.getMessage().split("\n")[0]; // first line only
                if (msg.length() > 100) msg = msg.substring(0, 97) + "...";

                Label msgLbl = new Label(msg);
                msgLbl.setFont(Font.font("Segoe UI", 13));
                msgLbl.setTextFill(Color.web(WHITE + "CC"));
                msgLbl.setWrapText(true);

                commitRow.getChildren().addAll(meta, msgLbl);
                commitsCard.getChildren().add(commitRow);
            }
        }

        content.getChildren().addAll(title, countLbl, commitsCard);
        scroll.setContent(content);
        centerArea.getChildren().setAll(scroll);
    }

    // ─────────────────────────────────────────────────────────────────
    // ── PANEL 4: Suggestions (AI via Gemini) ─────────────────────────
    // ─────────────────────────────────────────────────────────────────
    private void showSuggestionsPanel() {
        if (loadedRepo == null) { showError("No data loaded."); return; }

        VBox content = new VBox(24);
        content.setPadding(new Insets(32));
        content.setStyle("-fx-background-color: " + NAVY + ";");

        Label title = new Label("💡  AI Suggestions");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        title.setTextFill(Color.web(WHITE));

        VBox repoSummaryCard = buildCard("📋  Repository Summary");
        VBox readmeCard      = buildCard("📖  README Summary");

        Label repoLoading = dimLabel("Generating AI summary...");
        Label readmeLoading = dimLabel("Generating README summary...");
        repoSummaryCard.getChildren().add(repoLoading);
        readmeCard.getChildren().add(readmeLoading);

        content.getChildren().addAll(title, repoSummaryCard, readmeCard);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: " + NAVY + "; -fx-background-color: " + NAVY + ";");
        centerArea.getChildren().setAll(scroll);

        // ── Fetch AI summaries on background threads ──────────────────
        DataService ds = new DataService();

        // Repo summary
        Task<String> repoTask = new Task<>() {
            @Override protected String call() {
                return ds.getRepositorySummary(loadedRepo);
            }
        };
        repoTask.setOnSucceeded(e -> Platform.runLater(() -> {
            repoSummaryCard.getChildren().remove(repoLoading);
            Label result = new Label(repoTask.getValue());
            result.setFont(Font.font("Segoe UI", 13));
            result.setTextFill(Color.web(WHITE + "CC"));
            result.setWrapText(true);
            repoSummaryCard.getChildren().add(result);
        }));
        repoTask.setOnFailed(e -> Platform.runLater(() -> {
            repoSummaryCard.getChildren().remove(repoLoading);
            repoSummaryCard.getChildren().add(dimLabel("Could not generate summary."));
        }));

        // README summary
        Task<String> readmeTask = new Task<>() {
            @Override protected String call() {
                return ds.getReadmeSummary(loadedRepo);
            }
        };
        readmeTask.setOnSucceeded(e -> Platform.runLater(() -> {
            readmeCard.getChildren().remove(readmeLoading);
            Label result = new Label(readmeTask.getValue());
            result.setFont(Font.font("Segoe UI", 13));
            result.setTextFill(Color.web(WHITE + "CC"));
            result.setWrapText(true);
            readmeCard.getChildren().add(result);
        }));
        readmeTask.setOnFailed(e -> Platform.runLater(() -> {
            readmeCard.getChildren().remove(readmeLoading);
            readmeCard.getChildren().add(dimLabel("Could not generate README summary."));
        }));

        Thread t1 = new Thread(repoTask); t1.setDaemon(true); t1.start();
        Thread t2 = new Thread(readmeTask); t2.setDaemon(true); t2.start();
    }

    // ─────────────────────────────────────────────────────────────────
    // ── TOP BAR ───────────────────────────────────────────────────────
    // ─────────────────────────────────────────────────────────────────
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

        Canvas logo = buildLogo(36, 36);

        Label gitLabel = new Label("Git");
        gitLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        gitLabel.setTextFill(Color.web(WHITE));

        Label pulseLabel = new Label("Pulse");
        pulseLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        pulseLabel.setTextFill(Color.web(CYAN));
        pulseLabel.setStyle("-fx-effect: dropshadow(gaussian, #00D4FF, 3, 0.12, 0, 0);");

        HBox logoBox = new HBox(8, logo, gitLabel, pulseLabel);
        logoBox.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label repoPill = new Label("📁  " + owner + "/" + repoName);
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

    // ─────────────────────────────────────────────────────────────────
    // ── SIDEBAR ───────────────────────────────────────────────────────
    // ─────────────────────────────────────────────────────────────────
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

        // Each button calls a different panel
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

            // Wire each button to its panel
            btn.setOnAction(e -> {
                switch (item[1]) {
                    case "Summary"        -> showSummaryPanel();
                    case "Graphs"         -> showGraphsPanel();
                    case "Commit History" -> showCommitHistoryPanel();
                    case "Suggestions"    -> showSuggestionsPanel();
                }
            });

            sidebar.getChildren().add(btn);
        }

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
        logoutBtn.setOnAction(e -> {
            com.gitpulse.dashboard.LoginScreen login = new com.gitpulse.dashboard.LoginScreen();
            login.show((Stage) logoutBtn.getScene().getWindow());
        });

        sidebar.getChildren().addAll(push, sep, logoutBtn);
        return sidebar;
    }

    // ─────────────────────────────────────────────────────────────────
    // ── HELPERS ───────────────────────────────────────────────────────
    // ─────────────────────────────────────────────────────────────────

    /** Titled card container */
    private VBox buildCard(String title) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setStyle(
                "-fx-background-color: " + SURFACE + ";" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 12;" +
                        "-fx-background-radius: 12;"
        );

        Label t = new Label(title);
        t.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        t.setTextFill(Color.web(WHITE));

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: " + BORDER + ";");

        card.getChildren().addAll(t, sep);
        return card;
    }

    /** Key-value info row inside a card */
    private void addInfoRow(VBox card, String key, String value) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(4, 0, 4, 0));

        Label k = new Label(key + ":");
        k.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        k.setTextFill(Color.web(WHITE + "66"));
        k.setMinWidth(110);

        Label v = new Label(value);
        v.setFont(Font.font("Segoe UI", 13));
        v.setTextFill(Color.web(WHITE));
        v.setWrapText(true);

        row.getChildren().addAll(k, v);
        card.getChildren().add(row);
    }

    /** Stat card (number + label) */
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

    /** Dim placeholder label */
    private Label dimLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", 13));
        l.setTextFill(Color.web(WHITE + "44"));
        return l;
    }

    /** Null-safe string helper */
    private String nvl(String val, String fallback) {
        return (val == null || val.isBlank()) ? fallback : val;
    }

    /** Format ISO date to readable short form */
    private String formatDate(String isoDate) {
        if (isoDate == null || isoDate.isBlank()) return "Unknown";
        try { return isoDate.substring(0, 10); }
        catch (Exception e) { return isoDate; }
    }

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
        double[] xs = {cx-28*scale, cx-16*scale, cx-6*scale,
                cx+4*scale,  cx+14*scale, cx+22*scale, cx+28*scale};
        double[] ys = {cy, cy, cy-24*scale, cy+18*scale, cy-14*scale, cy, cy};

        gc.beginPath();
        gc.moveTo(xs[0], ys[0]);
        for (int i = 1; i < xs.length; i++) gc.lineTo(xs[i], ys[i]);
        gc.stroke();

        gc.setFill(Color.web(CYAN));
        gc.fillOval(xs[0]-3, ys[0]-3, 6, 6);
        gc.fillOval(xs[6]-3, ys[6]-3, 6, 6);
        gc.fillOval(xs[2]-3, ys[2]-3, 6, 6);

        return c;
    }
}