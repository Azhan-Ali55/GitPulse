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
root.setPrefSize(1280,800);
        // Top bar always fixed and visible
        HBox topBar = buildTopBar();
        topBar.setMinHeight(56);
        topBar.setPrefHeight(56);
        topBar.setMaxHeight(56);
        root.setTop(topBar);
        root.setLeft(buildSidebar());

        centerArea = new StackPane();
        centerArea.setStyle("-fx-background-color: " + NAVY + ";");
        root.setCenter(centerArea);

        Scene scene = new Scene(root, 1280, 800);

        stage.setScene(scene);
        stage.setTitle("GitPulse — Dashboard");
        stage.show();                  // show FIRST so layout pass runs
        stage.setMaximized(true);      // THEN maximize

        showSummaryPanel();

        root.setOpacity(0);
        FadeTransition fade = new FadeTransition(Duration.millis(600), root);
        fade.setToValue(1);
        fade.play();
    }

    // PANEL SWITCHER — animates the swap
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

    // OVERVIEW SUMMARY  (rich SummaryPanel from dashboard package)
    private void showSummaryPanel() {
        // Use the rich SummaryPanel (com.gitpulse.dashboard) as the primary view
        SummaryPanel panel = new SummaryPanel(loadedRepo, report);
        switchPanel(panel.build());
    }

    // PANEL 2 — CONTRIBUTORS  (rich ContributorPanel from dashboard package)
    private void showContributorsPanel() {
        ContributorPanel panel = new ContributorPanel(report);
        switchPanel(panel.build());
    }

    // PANEL 3 — ACTIVITY GRAPHS  (rich ActivityPanel from dashboard package)
    private void showGraphsPanel() {
        ActivityPanel panel = new ActivityPanel(report);
        switchPanel(panel.build());
    }

    // PANEL 4 — WEEKLY SUMMARIES  (NEW — WeeklySummariesPanel)
    private void showWeeklySummariesPanel() {
        WeeklySummariesPanel panel = new WeeklySummariesPanel();
        switchPanel(panel.build(loadedRepo));
    }

    // PANEL 5 — UNIQUE INSIGHTS  (rich InsightsPanel from dashboard package)
    private void showInsightsPanel() {
        InsightsPanel panel = new InsightsPanel(report);
        switchPanel(panel.build());
    }

    // Charts
    private void showChartsPanel() {
        ChartsPanel panel = new ChartsPanel(loadedRepo, report);
        centerArea.getChildren().setAll(panel.build());
    }

    // PANEL 6 — COMMIT HISTORY
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

    // PANEL 7 — PROJECT HEALTH
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

    // PANEL 8 — REPO AI SUMMARY
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

    // PANEL 9 — README AI SUMMARY
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

    // TOP BAR
    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(0, 24, 0, 20));
        bar.setPrefHeight(56);
        bar.setMinHeight(56);
        bar.setMaxHeight(56);
        bar.setStyle(
                "-fx-background-color:" + SURFACE + ";" +
                        "-fx-border-color:" + BORDER + ";" +
                        "-fx-border-width:0 0 1 0;"
        );

        Canvas logo = buildLogo(32, 32);

        Label gitL = new Label("Git");
        gitL.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        gitL.setTextFill(Color.web(WHITE));

        Label pulseL = new Label("Pulse");
        pulseL.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        pulseL.setTextFill(Color.web(CYAN));
        pulseL.setStyle("-fx-effect:dropshadow(gaussian,#00D4FF,3,0.12,0,0);");

        // Zero spacing between Git and Pulse so it reads GitPulse
        HBox nameBox = new HBox(0, gitL, pulseL);
        nameBox.setAlignment(Pos.CENTER_LEFT);

        HBox logoBox = new HBox(8, logo, nameBox);
        logoBox.setAlignment(Pos.CENTER_LEFT);
        logoBox.setMinWidth(170);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Truncate long repo names so they don't push logo off screen
        String repoText = owner + " / " + repoName;
        if (repoText.length() > 45) repoText = repoText.substring(0, 42) + "...";

        Label repoPill = new Label("📁  " + repoText);
        repoPill.setFont(Font.font("Segoe UI", 11));
        repoPill.setTextFill(Color.web(WHITE + "99"));
        repoPill.setStyle(
                "-fx-background-color:" + NAVY + ";" +
                        "-fx-border-color:" + BORDER + ";" +
                        "-fx-border-width:1;" +
                        "-fx-border-radius:20;" +
                        "-fx-background-radius:20;" +
                        "-fx-padding:4 12 4 12;"
        );

        String health = report.getProjectHealthLabel();
        String healthColor = switch (health) {
            case "Healthy" -> GREEN;
            case "Active"  -> CYAN;
            case "At Risk" -> ORANGE;
            default        -> RED;
        };

        Label healthBadge = new Label("❤  " + health);
        healthBadge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        healthBadge.setTextFill(Color.web(NAVY));
        healthBadge.setStyle(
                "-fx-background-color:" + healthColor + ";" +
                        "-fx-border-radius:20;" +
                        "-fx-background-radius:20;" +
                        "-fx-padding:4 12 4 12;"
        );

        HBox rightBox = new HBox(10, repoPill, healthBadge);
        rightBox.setAlignment(Pos.CENTER_RIGHT);

        bar.getChildren().addAll(logoBox, spacer, rightBox);
        return bar;
    }
    // SIDEBAR — 9 functional buttons across 3 sections
    private VBox buildSidebar() {

        // ── Scrollable nav area ───────────────────────────────────────────
        VBox navItems = new VBox(4);
        navItems.setPadding(new Insets(16, 10, 8, 10));
        navItems.setStyle("-fx-background-color:" + SURFACE + ";");

        Label nav = new Label("NAVIGATION");
        nav.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        nav.setTextFill(Color.web(WHITE + "44"));
        nav.setStyle("-fx-padding:0 0 8 8;");
        navItems.getChildren().add(nav);

        navItems.getChildren().add(sectionDivider("ANALYTICS"));
        addSidebarBtn(navItems, "📊", "Overview",
                () -> showSummaryPanel(), true);
        addSidebarBtn(navItems, "👥", "Contributors",
                () -> showContributorsPanel(), false);
        addSidebarBtn(navItems, "📈", "Activity Graphs",
                () -> showGraphsPanel(), false);
        addSidebarBtn(navItems, "📅", "Weekly Summaries",
                () -> showWeeklySummariesPanel(), false);
        addSidebarBtn(navItems, "⚡", "Unique Insights",
                () -> showInsightsPanel(), false);
        addSidebarBtn(navItems, "📊", "Charts & Graphs",
                () -> showChartsPanel(), false);

        navItems.getChildren().add(sectionDivider("HISTORY"));
        addSidebarBtn(navItems, "🕐", "Commit History",
                () -> showCommitHistoryPanel(), false);
        addSidebarBtn(navItems, "❤", "Project Health",
                () -> showHealthPanel(), false);

        navItems.getChildren().add(sectionDivider("AI INSIGHTS"));
        addSidebarBtn(navItems, "📋", "Repo Summary",
                () -> showRepoSummaryPanel(), false);
        addSidebarBtn(navItems, "📖", "README Summary",
                () -> showReadmeSummaryPanel(), false);

        // Wrap in ScrollPane so buttons never go off screen
        ScrollPane navScroll = new ScrollPane(navItems);
        navScroll.setFitToWidth(true);
        navScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        navScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        navScroll.setStyle(
                "-fx-background:" + SURFACE + ";" +
                        "-fx-background-color:" + SURFACE + ";" +
                        "-fx-border-color:transparent;"
        );
        VBox.setVgrow(navScroll, Priority.ALWAYS);

        // ── Fixed bottom: About + Logout ──────────────────────────────────
        VBox bottomArea = new VBox(2);
        bottomArea.setPadding(new Insets(8, 10, 14, 10));
        bottomArea.setStyle(
                "-fx-background-color:" + SURFACE + ";" +
                        "-fx-border-color:" + BORDER +
                        " transparent transparent transparent;" +
                        "-fx-border-width:1;"
        );

        Button aboutBtn = new Button("ℹ  About Us");
        aboutBtn.setMaxWidth(Double.MAX_VALUE);
        aboutBtn.setStyle(
                "-fx-background-color:transparent;" +
                        "-fx-text-fill:" + WHITE + "88;" +
                        "-fx-font-size:13px;-fx-alignment:CENTER-LEFT;" +
                        "-fx-padding:10 15 10 15;-fx-cursor:hand;" +
                        "-fx-border-radius:8;-fx-background-radius:8;"
        );
        aboutBtn.setOnMouseEntered(e -> aboutBtn.setStyle(
                "-fx-background-color:" + CYAN + "22;" +
                        "-fx-text-fill:" + CYAN + ";" +
                        "-fx-font-size:13px;-fx-alignment:CENTER-LEFT;" +
                        "-fx-padding:10 15 10 15;-fx-cursor:hand;" +
                        "-fx-border-radius:8;-fx-background-radius:8;"
        ));
        aboutBtn.setOnMouseExited(e -> aboutBtn.setStyle(
                "-fx-background-color:transparent;" +
                        "-fx-text-fill:" + WHITE + "88;" +
                        "-fx-font-size:13px;-fx-alignment:CENTER-LEFT;" +
                        "-fx-padding:10 15 10 15;-fx-cursor:hand;" +
                        "-fx-border-radius:8;-fx-background-radius:8;"
        ));
        aboutBtn.setOnAction(e -> showAboutPanel());

        Button logoutBtn = new Button("↩  Logout");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setStyle(
                "-fx-background-color:transparent;" +
                        "-fx-text-fill:" + RED + ";" +
                        "-fx-font-size:13px;-fx-alignment:CENTER-LEFT;" +
                        "-fx-padding:10 15 10 15;-fx-cursor:hand;" +
                        "-fx-border-radius:8;-fx-background-radius:8;"
        );
        logoutBtn.setOnMouseEntered(e -> logoutBtn.setStyle(
                "-fx-background-color:#FF6B6B22;" +
                        "-fx-text-fill:" + RED + ";" +
                        "-fx-font-size:13px;-fx-font-weight:bold;" +
                        "-fx-alignment:CENTER-LEFT;" +
                        "-fx-padding:10 15 10 15;-fx-cursor:hand;" +
                        "-fx-border-radius:8;-fx-background-radius:8;"
        ));
        logoutBtn.setOnMouseExited(e -> logoutBtn.setStyle(
                "-fx-background-color:transparent;" +
                        "-fx-text-fill:" + RED + ";" +
                        "-fx-font-size:13px;-fx-alignment:CENTER-LEFT;" +
                        "-fx-padding:10 15 10 15;-fx-cursor:hand;" +
                        "-fx-border-radius:8;-fx-background-radius:8;"
        ));
        logoutBtn.setOnAction(e -> {
            Stage currentStage = (Stage) logoutBtn.getScene().getWindow();
            currentStage.setScene(null);
            new LoginScreen().show(currentStage);
        });

        bottomArea.getChildren().addAll(aboutBtn, logoutBtn);

        // ── Outer wrapper ─────────────────────────────────────────────────
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(220);
        sidebar.setMinWidth(220);
        sidebar.setMaxWidth(220);
        sidebar.setStyle(
                "-fx-background-color:" + SURFACE + ";" +
                        "-fx-border-color:" + BORDER + ";" +
                        "-fx-border-width:0 1 0 0;"
        );
        sidebar.getChildren().addAll(navScroll, bottomArea);
        return sidebar;
    }
    /**
      Adds a sidebar button with an active-highlight state.
      Clicking any button deactivates the previous one and activates itself.

      @param defaultActive true only for the button that should start highlighted
                           (Overview, since we open on that panel)
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

    // HELPERS
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
    private void showAboutPanel() {
        VBox content = new VBox(24);
        content.setPadding(new Insets(40));
        content.setStyle("-fx-background-color:" + NAVY + ";");

        Label title = new Label("ℹ  About GitPulse");
        title.setFont(Font.font("Times New Roman", FontWeight.BOLD, 32));
        title.setTextFill(Color.web(CYAN));
        title.setStyle("-fx-effect:dropshadow(gaussian," + CYAN + ",6,0.2,0,0);");

        VBox aboutCard = buildCard("🚀  What is GitPulse?");
        String[] paragraphs = {
                "GitPulse is an instant GitHub repository analytics dashboard " +
                        "that gives developers and project managers deep insights into " +
                        "their codebase — insights that GitHub itself does not provide.",
                "Simply paste any public GitHub repository URL and GitPulse " +
                        "fetches live data, runs advanced algorithms, and presents " +
                        "everything in a clean visual dashboard.",
                "From contributor rankings and activity trends to AI-powered " +
                        "summaries via Google Gemini, GitPulse turns raw Git data into " +
                        "actionable intelligence."
        };
        for (String para : paragraphs) {
            Label p = new Label(para);
            p.setFont(Font.font("Times New Roman", FontWeight.NORMAL, 15));
            p.setTextFill(Color.web(WHITE + "CC"));
            p.setWrapText(true);
            p.setLineSpacing(5);
            p.setPadding(new Insets(0, 0, 6, 0));
            aboutCard.getChildren().add(p);
        }

        VBox featuresCard = buildCard("✨  Features");
        String[][] features = {
                {"📊", "Overview",        "Plain-English project summary with unique insights"},
                {"👥", "Contributors",    "Ranked contributor scores with consistency and recency"},
                {"📈", "Activity Graphs", "Weekly bar charts and monthly breakdown"},
                {"⚡", "Unique Insights", "Busiest hour, day, longest gap, collaboration index"},
                {"❤", "Project Health",  "Healthy / Active / At Risk / Stale / Abandoned"},
                {"📋", "Repo Summary",    "AI-generated repository analysis via Gemini"},
                {"📖", "README Summary",  "AI-generated README breakdown via Gemini"},
                {"🕐", "Commit History",  "Full paginated commit log with author and date"},
        };
        for (String[] f : features) {
            HBox row = new HBox(14);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(7, 0, 7, 0));
            row.setStyle("-fx-border-color:transparent transparent "
                    + BORDER + " transparent;-fx-border-width:1;");
            Label icon = new Label(f[0]);
            icon.setFont(Font.font("Segoe UI", 18));
            icon.setMinWidth(28);
            Label featureName = new Label(f[1]);
            featureName.setFont(Font.font("Times New Roman", FontWeight.BOLD, 15));
            featureName.setTextFill(Color.web(CYAN));
            featureName.setMinWidth(150);
            Label desc = new Label(f[2]);
            desc.setFont(Font.font("Times New Roman", FontWeight.NORMAL, 14));
            desc.setTextFill(Color.web(WHITE + "99"));
            desc.setWrapText(true);
            row.getChildren().addAll(icon, featureName, desc);
            HBox.setHgrow(desc, Priority.ALWAYS);
            featuresCard.getChildren().add(row);
        }

        VBox techCard = buildCard("👨‍💻  Built With");
        String[] tech = {
                "☕  Java 25           — Core language",
                "🎨  JavaFX 26         — UI framework",
                "🐙  GitHub REST API   — Repository data",
                "🤖  Google Gemini AI  — Smart summaries",
                "📦  Gson              — JSON parsing",
        };
        for (String t : tech) {
            Label lbl = new Label(t);
            lbl.setFont(Font.font("Times New Roman", FontWeight.NORMAL, 14));
            lbl.setTextFill(Color.web(WHITE + "BB"));
            lbl.setPadding(new Insets(4, 0, 4, 0));
            techCard.getChildren().add(lbl);
        }

        // Team Gate card
        VBox teamCard = buildCard("👥  Meet The Team — Team Gate");

        Label teamDesc = new Label(
                "We are NUST '29 students of Software Engineering, " +
                        "passionate about technology and building tools that " +
                        "make developers' lives easier."
        );
        teamDesc.setFont(Font.font("Times New Roman", FontWeight.NORMAL, 14));
        teamDesc.setTextFill(Color.web(WHITE + "CC"));
        teamDesc.setWrapText(true);
        teamDesc.setLineSpacing(4);
        teamDesc.setPadding(new Insets(0, 0, 12, 0));
        teamCard.getChildren().add(teamDesc);

        String[][] members = {
                {"👨‍💻", "Syed Muhammad Usman Shah"},
                {"👨‍💻", "Azhan Ali"},
                {"👨‍💻", "Muhammad Nouman Majeed"},
        };
        for (String[] member : members) {
            HBox memberRow = new HBox(14);
            memberRow.setAlignment(Pos.CENTER_LEFT);
            memberRow.setPadding(new Insets(8, 0, 8, 0));
            memberRow.setStyle("-fx-border-color:transparent transparent "
                    + BORDER + " transparent;-fx-border-width:1;");
            Label icon = new Label(member[0]);
            icon.setFont(Font.font("Segoe UI", 18));
            Label name = new Label(member[1]);
            name.setFont(Font.font("Times New Roman", FontWeight.BOLD, 16));
            name.setTextFill(Color.web(CYAN));
            memberRow.getChildren().addAll(icon, name);
            teamCard.getChildren().add(memberRow);
        }

        Label regards = new Label("— Regards, Team Gate  🚀");
        regards.setFont(Font.font("Times New Roman", FontWeight.BOLD, 14));
        regards.setTextFill(Color.web(WHITE + "66"));
        regards.setPadding(new Insets(14, 0, 0, 0));
        teamCard.getChildren().add(regards);

        Label version = new Label(
                "GitPulse v1.0   •   Powered by Gate   •   NUST '29");
        version.setFont(Font.font("Times New Roman", FontWeight.NORMAL, 12));
        version.setTextFill(Color.web(WHITE + "33"));
        version.setPadding(new Insets(10, 0, 0, 0));

        content.getChildren().addAll(
                title, aboutCard, featuresCard, techCard, teamCard, version);
        switchPanel(styledScroll(content));
    }
}