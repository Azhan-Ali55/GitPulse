package com.gitpulse.Home;

import com.gitpulse.Algorithm.*;
import com.gitpulse.dashboard.*;
import com.gitpulse.dashboard.LoginScreen;
import com.gitpulse.model.Commit;
import com.gitpulse.model.Contributor;
import com.gitpulse.model.Repository;
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
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class DashboardScreen {

    private static final String NAVY    = "#0D1117";
    private static final String CYAN    = "#00D4FF";
    private static final String WHITE   = "#E6EDF3";
    private static final String SURFACE = "#161B22";
    private static final String BORDER  = "#21262D";
    private static final String GREEN   = "#3FB950";
    private static final String ORANGE  = "#F0883E";
    private static final String RED     = "#FF6B6B";

    private final String           owner;
    private final String           repoName;
    private final Repository       loadedRepo;
    private final RepositoryReport report;
    private StackPane              centerArea;

    // Tracks the currently-active sidebar button for highlight state
    private Button activeBtn = null;

    public DashboardScreen(Repository repository, RepositoryReport report) {
        this.loadedRepo = repository;
        this.report     = report;
        this.owner      = repository.getOwner();
        this.repoName   = repository.getName();
    }

    public void show(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + NAVY + ";");
        root.setPrefSize(1280, 800);
        root.setTop(buildTopBar());
        root.setLeft(buildSidebar());

        centerArea = new StackPane();
        centerArea.setStyle("-fx-background-color: " + NAVY + ";");
        root.setCenter(centerArea);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.setTitle("GitPulse — Dashboard");
        stage.show();

        showSummaryPanel();

        root.setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(600), root);
        fade.setToValue(1);
        fade.play();
    }

    // ─────────────────────────────────────────────────────────────────
    // PANEL SWITCHER — animates the swap
    // ─────────────────────────────────────────────────────────────────
    private void switchPanel(javafx.scene.Node newContent) {
        if (centerArea.getChildren().isEmpty()) {
            centerArea.getChildren().setAll(newContent);
            return;
        }
        javafx.scene.Node old = centerArea.getChildren().get(0);
        FadeTransition fadeOut = new FadeTransition(Duration.millis(120), old);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            centerArea.getChildren().setAll(newContent);
            newContent.setOpacity(0);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), newContent);
            fadeIn.setToValue(1);
            fadeIn.play();
        });
        fadeOut.play();
    }

    // ─────────────────────────────────────────────────────────────────
    // PANEL 1 — OVERVIEW SUMMARY  (rich SummaryPanel from dashboard package)
    // ─────────────────────────────────────────────────────────────────
    private void showSummaryPanel() {
        // Use the rich SummaryPanel (com.gitpulse.dashboard) as the primary view
        SummaryPanel panel = new SummaryPanel(loadedRepo, report);
        switchPanel(panel.build());
    }

    // ─────────────────────────────────────────────────────────────────
    // PANEL 2 — CONTRIBUTORS  (rich ContributorPanel from dashboard package)
    // ─────────────────────────────────────────────────────────────────
    private void showContributorsPanel() {
        ContributorPanel panel = new ContributorPanel(report);
        switchPanel(panel.build());
    }

    // ─────────────────────────────────────────────────────────────────
    // PANEL 3 — ACTIVITY GRAPHS  (rich ActivityPanel from dashboard package)
    // ─────────────────────────────────────────────────────────────────
    private void showGraphsPanel() {
        ActivityPanel panel = new ActivityPanel(report);
        switchPanel(panel.build());
    }

    // ─────────────────────────────────────────────────────────────────
    // PANEL 4 — WEEKLY SUMMARIES  (NEW — WeeklySummariesPanel)
    // ─────────────────────────────────────────────────────────────────
    private void showWeeklySummariesPanel() {
        WeeklySummariesPanel panel = new WeeklySummariesPanel();
        switchPanel(panel.build(loadedRepo));
    }

    // ─────────────────────────────────────────────────────────────────
    // PANEL 5 — UNIQUE INSIGHTS  (rich InsightsPanel from dashboard package)
    // ─────────────────────────────────────────────────────────────────
    private void showInsightsPanel() {
        InsightsPanel panel = new InsightsPanel(report);
        switchPanel(panel.build());
    }

    // ─────────────────────────────────────────────────────────────────
    // PANEL 6 — COMMIT HISTORY
    // ─────────────────────────────────────────────────────────────────
    private void showCommitHistoryPanel() {
        VBox content = new VBox(24);
        content.setPadding(new Insets(32));

        Label title    = sectionTitle("🕐  Commit History");
        Label countLbl = subLabel("Total: " + loadedRepo.getCommits().size() + " commits");

        VBox commitsCard = buildCard("📝  All Commits");
        List<Commit> commits = loadedRepo.getCommits();

        if (commits.isEmpty()) {
            commitsCard.getChildren().add(dimLabel("No commit data available."));
        } else {
            for (Commit c : commits) {
                VBox row = new VBox(6);
                row.setPadding(new Insets(12, 0, 12, 0));
                row.setStyle("-fx-border-color:transparent transparent "
                        + BORDER + " transparent;-fx-border-width:1;");

                HBox meta = new HBox(10);
                meta.setAlignment(Pos.CENTER_LEFT);

                Label shaLbl = new Label(c.getSha().substring(0, Math.min(7, c.getSha().length())));
                shaLbl.setFont(Font.font("Courier New", FontWeight.BOLD, 11));
                shaLbl.setTextFill(Color.web(CYAN));
                shaLbl.setStyle(
                        "-fx-background-color:" + CYAN + "22;" +
                                "-fx-border-color:" + CYAN + "55;-fx-border-width:1;" +
                                "-fx-border-radius:4;-fx-background-radius:4;-fx-padding:2 8 2 8;"
                );

                Label authorLbl = new Label("@" + c.getAuthorName());
                authorLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                authorLbl.setTextFill(Color.web(WHITE));

                Label dateLbl = new Label(formatDate(c.getDate()));
                dateLbl.setFont(Font.font("Segoe UI", 11));
                dateLbl.setTextFill(Color.web(WHITE + "44"));

                meta.getChildren().addAll(shaLbl, authorLbl, dateLbl);

                String msg = c.getMessage().split("\n")[0];
                if (msg.length() > 120) msg = msg.substring(0, 117) + "...";
                Label msgLbl = new Label(msg);
                msgLbl.setFont(Font.font("Segoe UI", 13));
                msgLbl.setTextFill(Color.web(WHITE + "CC"));
                msgLbl.setWrapText(true);

                Label emailLbl = new Label(c.getAuthorEmail());
                emailLbl.setFont(Font.font("Segoe UI", 11));
                emailLbl.setTextFill(Color.web(WHITE + "33"));

                row.getChildren().addAll(meta, msgLbl, emailLbl);
                commitsCard.getChildren().add(row);
            }
        }

        content.getChildren().addAll(title, countLbl, commitsCard);
        switchPanel(styledScroll(content));
    }

    // ─────────────────────────────────────────────────────────────────
    // PANEL 7 — PROJECT HEALTH
    // ─────────────────────────────────────────────────────────────────
    private void showHealthPanel() {
        VBox content = new VBox(24);
        content.setPadding(new Insets(32));

        Label title = sectionTitle("❤  Project Health");

        VBox healthCard = buildCard("🏥  Health Assessment");
        String health = report.getProjectHealthLabel();
        String healthColor = switch (health) {
            case "Healthy"   -> GREEN;
            case "Active"    -> CYAN;
            case "At Risk"   -> ORANGE;
            case "Stale"     -> "#8B949E";
            case "Abandoned" -> RED;
            default          -> WHITE;
        };

        Label healthLbl = new Label(health);
        healthLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 48));
        healthLbl.setTextFill(Color.web(healthColor));
        healthLbl.setStyle(
                "-fx-effect:dropshadow(gaussian," + healthColor + ",12,0.4,0,0);");
        healthCard.getChildren().add(healthLbl);

        VBox detailsCard = buildCard("📋  Health Details");
        addInsightRow(detailsCard, "🏷  Status",      health);
        addInsightRow(detailsCard, "🕐  Last Commit", formatDate(loadedRepo.getLastCommitDate()));
        addInsightRow(detailsCard, "📈  Trend",
                report.getActivityTrend() != null
                        ? report.getActivityTrend().getTrendType().name() : "Unknown");
        addInsightRow(detailsCard, "🚌  Bus-Driver Risk",
                report.isBusDriver()
                        ? "⚠  HIGH — " + report.getDominantContributor() : "✅  LOW");
        addInsightRow(detailsCard, "🤝  Team Balance",
                report.getCollaborationLabel()
                        + String.format(" (%.0f/100)", report.getCollaborationIndex()));
        addInsightRow(detailsCard, "🔥  Best Streak",
                report.getPeakStreakDays() + " consecutive days");
        addInsightRow(detailsCard, "🔇  Longest Gap",
                report.getLongestGapDays() + " days without commits");

        VBox peaksCard = buildCard("📅  Activity Peaks");
        List<MonthlyStats> mostActive  = report.getMostActiveMonths();
        List<MonthlyStats> leastActive = report.getLeastActiveMonths();

        if (mostActive != null && !mostActive.isEmpty()) {
            Label mHeader = new Label("🔥  Most Active Months");
            mHeader.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            mHeader.setTextFill(Color.web(GREEN));
            peaksCard.getChildren().add(mHeader);
            for (MonthlyStats ms : mostActive) {
                addInsightRow(peaksCard, "   " + ms.getLabel(),
                        ms.getCommitCount() + " commits");
            }
        }
        if (leastActive != null && !leastActive.isEmpty()) {
            Label lHeader = new Label("❄  Least Active Months");
            lHeader.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            lHeader.setTextFill(Color.web(ORANGE));
            lHeader.setPadding(new Insets(12, 0, 0, 0));
            peaksCard.getChildren().add(lHeader);
            for (MonthlyStats ms : leastActive) {
                addInsightRow(peaksCard, "   " + ms.getLabel(),
                        ms.getCommitCount() + " commits");
            }
        }

        content.getChildren().addAll(title, healthCard, detailsCard, peaksCard);
        switchPanel(styledScroll(content));
    }

    // ─────────────────────────────────────────────────────────────────
    // PANEL 8 — REPO AI SUMMARY
    // ─────────────────────────────────────────────────────────────────
    private void showRepoSummaryPanel() {
        VBox content = new VBox(24);
        content.setPadding(new Insets(32));

        Label title = sectionTitle("📋  Repository AI Summary");
        Label sub   = subLabel("Gemini AI analysis of your repository");

        VBox card = buildCard("🤖  AI Repository Summary");
        Label loading = dimLabel("⏳  Generating AI summary...");
        card.getChildren().add(loading);

        content.getChildren().addAll(title, sub, card);
        switchPanel(styledScroll(content));

        DataService ds = new DataService();
        Task<String> task = new Task<>() {
            @Override protected String call() { return ds.getRepositorySummary(loadedRepo); }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            card.getChildren().remove(loading);
            Label result = new Label(task.getValue());
            result.setFont(Font.font("Segoe UI", 14));
            result.setTextFill(Color.web(WHITE + "CC"));
            result.setWrapText(true);
            result.setLineSpacing(4);
            card.getChildren().add(result);
        }));
        task.setOnFailed(e -> Platform.runLater(() -> {
            card.getChildren().remove(loading);
            card.getChildren().add(dimLabel("❌  " + task.getException().getMessage()));
        }));
        Thread t = new Thread(task); t.setDaemon(true); t.start();
    }

    // ─────────────────────────────────────────────────────────────────
    // PANEL 9 — README AI SUMMARY
    // ─────────────────────────────────────────────────────────────────
    private void showReadmeSummaryPanel() {
        VBox content = new VBox(24);
        content.setPadding(new Insets(32));

        Label title = sectionTitle("📖  README AI Summary");
        Label sub   = subLabel("Gemini AI analysis of your README file");

        VBox card = buildCard("🤖  AI README Summary");
        Label loading = dimLabel("⏳  Generating README summary...");
        card.getChildren().add(loading);

        content.getChildren().addAll(title, sub, card);
        switchPanel(styledScroll(content));

        DataService ds = new DataService();
        Task<String> task = new Task<>() {
            @Override protected String call() { return ds.getReadmeSummary(loadedRepo); }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            card.getChildren().remove(loading);
            Label result = new Label(task.getValue());
            result.setFont(Font.font("Segoe UI", 14));
            result.setTextFill(Color.web(WHITE + "CC"));
            result.setWrapText(true);
            result.setLineSpacing(4);
            card.getChildren().add(result);
        }));
        task.setOnFailed(e -> Platform.runLater(() -> {
            card.getChildren().remove(loading);
            card.getChildren().add(dimLabel("❌  " + task.getException().getMessage()));
        }));
        Thread t = new Thread(task); t.setDaemon(true); t.start();
    }

    // ─────────────────────────────────────────────────────────────────
    // TOP BAR
    // ─────────────────────────────────────────────────────────────────
    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(0, 24, 0, 20));
        bar.setPrefHeight(56);
        bar.setStyle(
                "-fx-background-color:" + SURFACE + ";" +
                        "-fx-border-color:" + BORDER + ";-fx-border-width:0 0 1 0;"
        );

        Canvas logo = buildLogo(36, 36);

        Label gitL = new Label("Git");
        gitL.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        gitL.setTextFill(Color.web(WHITE));

        Label pulseL = new Label("Pulse");
        pulseL.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        pulseL.setTextFill(Color.web(CYAN));
        pulseL.setStyle("-fx-effect:dropshadow(gaussian,#00D4FF,3,0.12,0,0);");

        HBox logoBox = new HBox(8, logo, gitL, pulseL);
        logoBox.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label repoPill = new Label("📁  " + owner + " / " + repoName);
        repoPill.setFont(Font.font("Segoe UI", 12));
        repoPill.setTextFill(Color.web(WHITE + "99"));
        repoPill.setStyle(
                "-fx-background-color:" + NAVY + ";-fx-border-color:" + BORDER + ";" +
                        "-fx-border-width:1;-fx-border-radius:20;-fx-background-radius:20;" +
                        "-fx-padding:4 14 4 14;"
        );

        String health = report.getProjectHealthLabel();
        String healthColor = switch (health) {
            case "Healthy"   -> GREEN;
            case "Active"    -> CYAN;
            case "At Risk"   -> ORANGE;
            default          -> RED;
        };
        Label healthBadge = new Label("❤  " + health);
        healthBadge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        healthBadge.setTextFill(Color.web(NAVY));
        healthBadge.setStyle(
                "-fx-background-color:" + healthColor + ";" +
                        "-fx-border-radius:20;-fx-background-radius:20;-fx-padding:4 14 4 14;"
        );

        bar.getChildren().addAll(logoBox, spacer, new HBox(12, repoPill, healthBadge));
        return bar;
    }

    // ─────────────────────────────────────────────────────────────────
    // SIDEBAR — 9 functional buttons across 3 sections
    // ─────────────────────────────────────────────────────────────────
    private VBox buildSidebar() {
        VBox sidebar = new VBox(4);
        sidebar.setPadding(new Insets(20, 10, 20, 10));
        sidebar.setPrefWidth(220);
        sidebar.setStyle(
                "-fx-background-color:" + SURFACE + ";" +
                        "-fx-border-color:" + BORDER + ";-fx-border-width:0 1 0 0;"
        );

        Label nav = new Label("NAVIGATION");
        nav.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        nav.setTextFill(Color.web(WHITE + "44"));
        nav.setStyle("-fx-padding:0 0 8 8;");
        sidebar.getChildren().add(nav);

        // ── ANALYTICS section
        sidebar.getChildren().add(sectionDivider("ANALYTICS"));
        addSidebarBtn(sidebar, "📊", "Overview",         () -> showSummaryPanel(),         true /* default active */);
        addSidebarBtn(sidebar, "👥", "Contributors",     () -> showContributorsPanel(),     false);
        addSidebarBtn(sidebar, "📈", "Activity Graphs",  () -> showGraphsPanel(),           false);
        addSidebarBtn(sidebar, "📅", "Weekly Summaries", () -> showWeeklySummariesPanel(),  false);
        addSidebarBtn(sidebar, "⚡", "Unique Insights",  () -> showInsightsPanel(),         false);

        // ── HISTORY section
        sidebar.getChildren().add(sectionDivider("HISTORY"));
        addSidebarBtn(sidebar, "🕐", "Commit History",   () -> showCommitHistoryPanel(),    false);
        addSidebarBtn(sidebar, "❤", "Project Health",   () -> showHealthPanel(),           false);

        // ── AI section
        sidebar.getChildren().add(sectionDivider("AI INSIGHTS"));
        addSidebarBtn(sidebar, "📋", "Repo Summary",     () -> showRepoSummaryPanel(),      false);
        addSidebarBtn(sidebar, "📖", "README Summary",   () -> showReadmeSummaryPanel(),    false);

        Region push = new Region();
        VBox.setVgrow(push, Priority.ALWAYS);
        sidebar.getChildren().add(push);

        Separator sep = new Separator();
        sidebar.getChildren().add(sep);

        Button logoutBtn = new Button("↩  Disconnect");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setStyle(
                "-fx-background-color:transparent;-fx-text-fill:" + RED + ";" +
                        "-fx-font-size:13px;-fx-alignment:CENTER-LEFT;" +
                        "-fx-padding:10 15 10 15;-fx-cursor:hand;" +
                        "-fx-border-radius:8;-fx-background-radius:8;"
        );
        logoutBtn.setOnAction(e ->
                new LoginScreen().show((Stage) logoutBtn.getScene().getWindow()));
        sidebar.getChildren().add(logoutBtn);

        return sidebar;
    }

    /**
     * Adds a sidebar button with an active-highlight state.
     * Clicking any button deactivates the previous one and activates itself.
     *
     * @param defaultActive true only for the button that should start highlighted
     *                      (Overview, since we open on that panel)
     */
    private void addSidebarBtn(VBox sidebar, String icon,
                               String label, Runnable action,
                               boolean defaultActive) {
        Button btn = new Button(icon + "  " + label);
        btn.setMaxWidth(Double.MAX_VALUE);

        if (defaultActive) {
            styleSidebarBtnActive(btn);
            activeBtn = btn;
        } else {
            styleSidebarBtn(btn, false);
        }

        btn.setOnMouseEntered(e -> { if (btn != activeBtn) styleSidebarBtn(btn, true); });
        btn.setOnMouseExited(e  -> { if (btn != activeBtn) styleSidebarBtn(btn, false); });

        btn.setOnAction(e -> {
            // Deactivate previous
            if (activeBtn != null && activeBtn != btn) {
                styleSidebarBtn(activeBtn, false);
            }
            // Activate this one
            styleSidebarBtnActive(btn);
            activeBtn = btn;
            action.run();
        });

        sidebar.getChildren().add(btn);
    }

    // ─────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────

    private Label sectionDivider(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 9));
        lbl.setTextFill(Color.web(WHITE + "33"));
        lbl.setStyle("-fx-padding:12 0 4 8;");
        return lbl;
    }

    private void addInsightRow(VBox card, String key, String value) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(5, 0, 5, 0));

        Label k = new Label(key);
        k.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        k.setTextFill(Color.web(WHITE + "77"));
        k.setMinWidth(200);

        Label v = new Label(value);
        v.setFont(Font.font("Segoe UI", 13));
        v.setTextFill(Color.web(WHITE));
        v.setWrapText(true);

        row.getChildren().addAll(k, v);
        card.getChildren().add(row);
    }

    private ScrollPane styledScroll(VBox content) {
        ScrollPane s = new ScrollPane(content);
        s.setFitToWidth(true);
        s.setStyle("-fx-background:" + NAVY + ";-fx-background-color:" + NAVY + ";");
        return s;
    }

    private VBox buildCard(String title) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setStyle(
                "-fx-background-color:" + SURFACE + ";-fx-border-color:" + BORDER + ";" +
                        "-fx-border-width:1;-fx-border-radius:12;-fx-background-radius:12;"
        );
        Label t = new Label(title);
        t.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        t.setTextFill(Color.web(WHITE));
        card.getChildren().addAll(t, new Separator());
        return card;
    }

    private Label sectionTitle(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        l.setTextFill(Color.web(WHITE));
        return l;
    }

    private Label subLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", 13));
        l.setTextFill(Color.web(WHITE + "55"));
        return l;
    }

    private Label dimLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Segoe UI", 13));
        l.setTextFill(Color.web(WHITE + "44"));
        l.setPadding(new Insets(8, 0, 8, 0));
        return l;
    }

    private String formatDate(String iso) {
        if (iso == null || iso.isBlank()) return "Unknown";
        try { return iso.substring(0, 10); } catch (Exception e) { return iso; }
    }

    // Sidebar button style — normal / hover
    private void styleSidebarBtn(Button btn, boolean hovered) {
        btn.setStyle(
                "-fx-background-color:" + (hovered ? CYAN + "22" : "transparent") + ";" +
                        "-fx-text-fill:" + (hovered ? CYAN : WHITE) + ";" +
                        "-fx-font-size:13px;-fx-alignment:CENTER-LEFT;" +
                        "-fx-padding:9 15 9 15;-fx-cursor:hand;" +
                        "-fx-border-radius:8;-fx-background-radius:8;"
        );
    }

    // Sidebar button style — active (currently selected panel)
    private void styleSidebarBtnActive(Button btn) {
        btn.setStyle(
                "-fx-background-color:" + CYAN + "33;" +
                        "-fx-text-fill:" + CYAN + ";" +
                        "-fx-font-size:13px;-fx-font-weight:bold;-fx-alignment:CENTER-LEFT;" +
                        "-fx-padding:9 15 9 15;-fx-cursor:hand;" +
                        "-fx-border-color:" + CYAN + "55;-fx-border-width:0 0 0 2;" +
                        "-fx-border-radius:8;-fx-background-radius:8;"
        );
    }

    private Canvas buildLogo(double w, double h) {
        Canvas c = new Canvas(w, h);
        GraphicsContext gc = c.getGraphicsContext2D();
        double cx = w / 2, cy = h / 2, r = w * 0.42;
        gc.setStroke(Color.web(CYAN + "33")); gc.setLineWidth(4);
        gc.strokeOval(cx - r - 4, cy - r - 4, (r + 4) * 2, (r + 4) * 2);
        gc.setStroke(Color.web(CYAN)); gc.setLineWidth(2);
        gc.strokeOval(cx - r, cy - r, r * 2, r * 2);
        gc.setStroke(Color.web(CYAN)); gc.setLineWidth(1.8);
        gc.setLineCap(StrokeLineCap.ROUND);
        double s = w / 120.0;
        double[] xs = {cx-28*s, cx-16*s, cx-6*s, cx+4*s, cx+14*s, cx+22*s, cx+28*s};
        double[] ys = {cy,      cy,      cy-24*s, cy+18*s, cy-14*s, cy,      cy};
        gc.beginPath(); gc.moveTo(xs[0], ys[0]);
        for (int i = 1; i < xs.length; i++) gc.lineTo(xs[i], ys[i]);
        gc.stroke();
        gc.setFill(Color.web(CYAN));
        gc.fillOval(xs[0]-3, ys[0]-3, 6, 6);
        gc.fillOval(xs[6]-3, ys[6]-3, 6, 6);
        gc.fillOval(xs[2]-3, ys[2]-3, 6, 6);
        return c;
    }
}