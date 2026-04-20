package com.gitpulse.Home;

import com.gitpulse.Algorithm.*;
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

    private final String          owner;
    private final String          repoName;
    private final Repository      loadedRepo;
    private final RepositoryReport report;
    private StackPane             centerArea;

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
    // PANEL 1 — OVERVIEW SUMMARY
    // ─────────────────────────────────────────────────────────────────
    private void showSummaryPanel() {
        VBox content = new VBox(24);
        content.setPadding(new Insets(32));

        Label title = sectionTitle("📊  Overview");
        Label sub   = subLabel(owner + " / " + repoName
                + "   •   " + nvl(loadedRepo.getLanguage(), "N/A")
                + "   •   Last commit: " + formatDate(loadedRepo.getLastCommitDate()));

        // Stat cards
        HBox statsRow = new HBox(16);
        statsRow.getChildren().addAll(
                buildStatCard("📝  Commits",       String.valueOf(loadedRepo.getCommits().size())),
                buildStatCard("👥  Contributors",  String.valueOf(loadedRepo.getContributors().size())),
                buildStatCard("🌐  Language",      nvl(loadedRepo.getLanguage(), "N/A")),
                buildStatCard("❤  Health",        report.getProjectHealthLabel())
        );

        // Plain English summary card
        VBox summaryCard = buildCard("📝  Plain-English Project Summary");
        Label summaryText = new Label(report.getPlainEnglishSummary());
        summaryText.setFont(Font.font("Segoe UI", 14));
        summaryText.setTextFill(Color.web(WHITE + "CC"));
        summaryText.setWrapText(true);
        summaryText.setLineSpacing(4);
        summaryCard.getChildren().add(summaryText);

        // Unique insights card
        VBox insightsCard = buildCard("⚡  Unique Insights  (not available on GitHub)");
        addInsightRow(insightsCard, "🕐  Busiest Hour",
                formatHour((int) report.getBusyHourOfDay()) + " UTC");
        addInsightRow(insightsCard, "📅  Busiest Day",
                nvl(report.getBusyDayOfWeek(), "Unknown"));
        addInsightRow(insightsCard, "⏱  Avg. Time Between Commits",
                String.format("%.1f hours", report.getAvgTimeBetweenCommits()));
        addInsightRow(insightsCard, "🔇  Longest Quiet Period",
                report.getLongestGapDays() + " days");
        addInsightRow(insightsCard, "🔥  Peak Daily Streak",
                report.getPeakStreakDays() + " consecutive days");
        addInsightRow(insightsCard, "🤝  Collaboration Style",
                report.getCollaborationLabel()
                        + String.format("  (score: %.0f/100)", report.getCollaborationIndex()));
        addInsightRow(insightsCard, "🚌  Bus-Driver Risk",
                report.isBusDriver()
                        ? "⚠  YES — " + report.getDominantContributor() + " holds >50% of commits"
                        : "✅  No single point of failure");
        addInsightRow(insightsCard, "📈  Activity Trend",
                report.getActivityTrend() != null
                        ? report.getActivityTrend().getPlainEnglish() : "Unknown");

        content.getChildren().addAll(title, sub, statsRow, summaryCard, insightsCard);
        centerArea.getChildren().setAll(styledScroll(content));
    }

    // ─────────────────────────────────────────────────────────────────
    // PANEL 2 — CONTRIBUTORS
    // ─────────────────────────────────────────────────────────────────
    private void showContributorsPanel() {
        VBox content = new VBox(24);
        content.setPadding(new Insets(32));

        Label title = sectionTitle("👥  Contributors");

        // Ranked scores card
        VBox rankedCard = buildCard("🏆  Contributor Rankings  (scored by volume + consistency + recency)");
        List<ContributorScore> ranked = report.getRankedContributors();

        if (ranked == null || ranked.isEmpty()) {
            rankedCard.getChildren().add(dimLabel("No contributor data available."));
        } else {
            int rank = 1;
            for (ContributorScore cs : ranked) {
                VBox row = new VBox(4);
                row.setPadding(new Insets(10, 0, 10, 0));
                row.setStyle("-fx-border-color: transparent transparent "
                        + BORDER + " transparent; -fx-border-width:1;");

                HBox topRow = new HBox(12);
                topRow.setAlignment(Pos.CENTER_LEFT);

                Label rankLbl = new Label("#" + rank++);
                rankLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
                rankLbl.setTextFill(Color.web(CYAN));
                rankLbl.setMinWidth(36);

                Label nameLbl = new Label("@" + cs.getUsername());
                nameLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
                nameLbl.setTextFill(Color.web(WHITE));
                nameLbl.setMinWidth(160);

                Label labelBadge = new Label(cs.getActivityLabel());
                labelBadge.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
                labelBadge.setTextFill(Color.web(NAVY));
                labelBadge.setStyle(
                        "-fx-background-color:" + labelColor(cs.getActivityLabel()) + ";" +
                                "-fx-background-radius:12;-fx-padding:2 10 2 10;"
                );

                Label scoreLbl = new Label(String.format("Score: %.1f/100", cs.getOverallScore()));
                scoreLbl.setFont(Font.font("Segoe UI", 11));
                scoreLbl.setTextFill(Color.web(WHITE + "66"));

                topRow.getChildren().addAll(rankLbl, nameLbl, labelBadge, scoreLbl);

                HBox bars = new HBox(16);
                bars.setAlignment(Pos.CENTER_LEFT);
                bars.setPadding(new Insets(4, 0, 0, 48));

                bars.getChildren().addAll(
                        miniBar("Commits",     cs.getCommitShare() / 100),
                        miniBar("Consistency", cs.getConsistencyScore() / 100),
                        miniBar("Recency",     cs.getRecencyScore() / 100)
                );

                row.getChildren().addAll(topRow, bars);
                rankedCard.getChildren().add(row);
            }
        }

        // Top contributors card
        VBox topCard = buildCard("⭐  Top Contributors");
        List<ContributorScore> top = report.getTopContributors();
        if (top == null || top.isEmpty()) {
            topCard.getChildren().add(dimLabel("No top contributors identified."));
        } else {
            for (ContributorScore cs : top) {
                HBox row = new HBox(12);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(6, 0, 6, 0));

                Label n = new Label("@" + cs.getUsername());
                n.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
                n.setTextFill(Color.web(CYAN));
                n.setMinWidth(160);

                Label c = new Label(cs.getTotalCommits() + " commits  ("
                        + String.format("%.1f%%", cs.getCommitShare()) + ")");
                c.setFont(Font.font("Segoe UI", 12));
                c.setTextFill(Color.web(WHITE + "88"));

                row.getChildren().addAll(n, c);
                topCard.getChildren().add(row);
            }
        }

        // Inactive contributors card
        VBox inactiveCard = buildCard("😴  Inactive Contributors");
        List<ContributorScore> inactive = report.getInactiveContributors();
        if (inactive == null || inactive.isEmpty()) {
            inactiveCard.getChildren().add(dimLabel("No inactive contributors — great!"));
        } else {
            for (ContributorScore cs : inactive) {
                HBox row = new HBox(12);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(5, 0, 5, 0));

                Label n = new Label("@" + cs.getUsername());
                n.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                n.setTextFill(Color.web(RED));
                n.setMinWidth(160);

                Label c = new Label(cs.getTotalCommits() + " commits total");
                c.setFont(Font.font("Segoe UI", 12));
                c.setTextFill(Color.web(WHITE + "55"));

                row.getChildren().addAll(n, c);
                inactiveCard.getChildren().add(row);
            }
        }

        content.getChildren().addAll(title, rankedCard, topCard, inactiveCard);
        centerArea.getChildren().setAll(styledScroll(content));
    }

    // ─────────────────────────────────────────────────────────────────
    // PANEL 3 — ACTIVITY GRAPHS
    // ─────────────────────────────────────────────────────────────────
    private void showGraphsPanel() {
        VBox content = new VBox(24);
        content.setPadding(new Insets(32));

        Label title = sectionTitle("📈  Activity Graphs");

        // Activity trend card
        VBox trendCard = buildCard("📊  Activity Trend");
        if (report.getActivityTrend() != null) {
            ActivityTrend trend = report.getActivityTrend();
            String trendIcon = switch (trend.getTrendType()) {
                case GROWING  -> "📈";
                case DECLINING -> "📉";
                case STABLE   -> "➡";
                case SPORADIC -> "〰";
                case INACTIVE -> "💤";
            };
            addInsightRow(trendCard, trendIcon + "  Trend",
                    trend.getTrendType().name());
            addInsightRow(trendCard, "📝  Description",
                    trend.getPlainEnglish());
            addInsightRow(trendCard, "📐  Slope",
                    String.format("%+.2f commits/month", trend.getSlopePerMonth()));
            addInsightRow(trendCard, "📊  Volatility",
                    String.format("%.2f", trend.getVolatility()));
        }

        // Weekly bar chart
        VBox chartCard = buildCard("📅  Weekly Commit Activity (last 16 weeks)");
        Map<LocalDate, List<Commit>> weeklyMap = loadedRepo.getWeeklyActivity();

        if (weeklyMap == null || weeklyMap.isEmpty()) {
            chartCard.getChildren().add(dimLabel("No weekly activity data available."));
        } else {
            int maxCommits = weeklyMap.values().stream()
                    .mapToInt(List::size).max().orElse(1);

            var weeks = weeklyMap.entrySet().stream()
                    .sorted(Map.Entry.<LocalDate, List<Commit>>comparingByKey().reversed())
                    .limit(16)
                    .sorted(Map.Entry.comparingByKey())
                    .toList();

            HBox barsArea = new HBox(6);
            barsArea.setAlignment(Pos.BOTTOM_LEFT);
            barsArea.setPadding(new Insets(16, 8, 8, 8));

            for (var entry : weeks) {
                int count = entry.getValue().size();
                double barH = Math.max(6, (double) count / maxCommits * 180);

                VBox bg = new VBox(4);
                bg.setAlignment(Pos.BOTTOM_CENTER);

                Label cntLbl = new Label(String.valueOf(count));
                cntLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
                cntLbl.setTextFill(Color.web(CYAN));

                Region bar = new Region();
                bar.setPrefWidth(42);
                bar.setPrefHeight(barH);
                bar.setStyle(
                        "-fx-background-color:linear-gradient(to top,#00D4FF,#00D4FF88);" +
                                "-fx-background-radius:4 4 0 0;"
                );

                Label dateLbl = new Label(entry.getKey().toString().substring(5));
                dateLbl.setFont(Font.font("Segoe UI", 9));
                dateLbl.setTextFill(Color.web(WHITE + "66"));

                bg.getChildren().addAll(cntLbl, bar, dateLbl);
                barsArea.getChildren().add(bg);
            }

            ScrollPane barScroll = new ScrollPane(barsArea);
            barScroll.setFitToHeight(true);
            barScroll.setPrefHeight(260);
            barScroll.setStyle(
                    "-fx-background:" + SURFACE + ";-fx-background-color:" + SURFACE + ";");
            chartCard.getChildren().add(barScroll);
        }

        // Monthly breakdown card
        VBox monthlyCard = buildCard("🗓  Monthly Breakdown");
        List<MonthlyStats> monthly = report.getMonthlyBreakdown();

        if (monthly == null || monthly.isEmpty()) {
            monthlyCard.getChildren().add(dimLabel("No monthly data available."));
        } else {
            for (MonthlyStats ms : monthly) {
                HBox row = new HBox(12);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(5, 0, 5, 0));

                Label monthLbl = new Label(ms.getLabel());
                monthLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
                monthLbl.setTextFill(Color.web(WHITE));
                monthLbl.setMinWidth(130);

                Label cntLbl = new Label(ms.getCommitCount() + " commits");
                cntLbl.setFont(Font.font("Segoe UI", 12));
                cntLbl.setTextFill(Color.web(CYAN));
                cntLbl.setMinWidth(90);

                Label contribLbl = new Label(ms.getUniqueContributors() + " contributors");
                contribLbl.setFont(Font.font("Segoe UI", 11));
                contribLbl.setTextFill(Color.web(WHITE + "66"));
                contribLbl.setMinWidth(110);

                Label tagLbl = new Label(ms.getActivityTag());
                tagLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
                tagLbl.setTextFill(Color.web(NAVY));
                String tagColor = ms.getActivityTag().equals("Most Active") ? GREEN
                        : ms.getActivityTag().equals("Least Active") ? ORANGE : BORDER;
                tagLbl.setStyle(
                        "-fx-background-color:" + tagColor + ";" +
                                "-fx-background-radius:10;-fx-padding:2 8 2 8;"
                );

                row.getChildren().addAll(monthLbl, cntLbl, contribLbl, tagLbl);
                monthlyCard.getChildren().add(row);
            }
        }

        content.getChildren().addAll(title, trendCard, chartCard, monthlyCard);
        centerArea.getChildren().setAll(styledScroll(content));
    }

    // ─────────────────────────────────────────────────────────────────
    // PANEL 4 — COMMIT HISTORY
    // ─────────────────────────────────────────────────────────────────
    private void showCommitHistoryPanel() {
        VBox content = new VBox(24);
        content.setPadding(new Insets(32));

        Label title = sectionTitle("🕐  Commit History");
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

                Label shaLbl = new Label(c.getSha().substring(0, 7));
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
        centerArea.getChildren().setAll(styledScroll(content));
    }

    // ─────────────────────────────────────────────────────────────────
    // PANEL 5 — REPO AI SUMMARY
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
        centerArea.getChildren().setAll(styledScroll(content));

        DataService ds = new DataService();
        Task<String> task = new Task<>() {
            @Override protected String call() {
                return ds.getRepositorySummary(loadedRepo);
            }
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
    // PANEL 6 — README AI SUMMARY
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
        centerArea.getChildren().setAll(styledScroll(content));

        DataService ds = new DataService();
        Task<String> task = new Task<>() {
            @Override protected String call() {
                return ds.getReadmeSummary(loadedRepo);
            }
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
        addInsightRow(detailsCard, "🏷  Status",
                health);
        addInsightRow(detailsCard, "🕐  Last Commit",
                formatDate(loadedRepo.getLastCommitDate()));
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

        // Most/Least active months
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
        centerArea.getChildren().setAll(styledScroll(content));
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

        bar.getChildren().addAll(logoBox, spacer,
                new HBox(12, repoPill, healthBadge));
        return bar;
    }

    // ─────────────────────────────────────────────────────────────────
    // SIDEBAR — 7 functional buttons
    // ─────────────────────────────────────────────────────────────────
    private VBox buildSidebar() {
        VBox sidebar = new VBox(4);
        sidebar.setPadding(new Insets(20, 10, 20, 10));
        sidebar.setPrefWidth(215);
        sidebar.setStyle(
                "-fx-background-color:" + SURFACE + ";" +
                        "-fx-border-color:" + BORDER + ";-fx-border-width:0 1 0 0;"
        );

        Label nav = new Label("NAVIGATION");
        nav.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        nav.setTextFill(Color.web(WHITE + "44"));
        nav.setStyle("-fx-padding:0 0 8 8;");
        sidebar.getChildren().add(nav);

        // Section: Analytics
        sidebar.getChildren().add(sectionDivider("ANALYTICS"));

        addSidebarBtn(sidebar, "📊", "Overview",        () -> showSummaryPanel());
        addSidebarBtn(sidebar, "👥", "Contributors",    () -> showContributorsPanel());
        addSidebarBtn(sidebar, "📈", "Activity Graphs", () -> showGraphsPanel());
        addSidebarBtn(sidebar, "🕐", "Commit History",  () -> showCommitHistoryPanel());
        addSidebarBtn(sidebar, "❤", "Project Health",  () -> showHealthPanel());

        // Section: AI
        sidebar.getChildren().add(sectionDivider("AI INSIGHTS"));
        addSidebarBtn(sidebar, "📋", "Repo Summary",   () -> showRepoSummaryPanel());
        addSidebarBtn(sidebar, "📖", "README Summary", () -> showReadmeSummaryPanel());

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
        logoutBtn.setOnAction(e -> {
            new LoginScreen().show((Stage) logoutBtn.getScene().getWindow());
        });
        sidebar.getChildren().add(logoutBtn);

        return sidebar;
    }

    // ─────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────
    private void addSidebarBtn(VBox sidebar, String icon,
                               String label, Runnable action) {
        Button btn = new Button(icon + "  " + label);
        btn.setMaxWidth(Double.MAX_VALUE);
        styleSidebarBtn(btn, false);
        btn.setOnMouseEntered(e -> styleSidebarBtn(btn, true));
        btn.setOnMouseExited(e -> styleSidebarBtn(btn, false));
        btn.setOnAction(e -> action.run());
        sidebar.getChildren().add(btn);
    }

    private Label sectionDivider(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 9));
        lbl.setTextFill(Color.web(WHITE + "33"));
        lbl.setStyle("-fx-padding:12 0 4 8;");
        return lbl;
    }

    private HBox miniBar(String label, double value) {
        HBox box = new HBox(6);
        box.setAlignment(Pos.CENTER_LEFT);

        Label lbl = new Label(label);
        lbl.setFont(Font.font("Segoe UI", 10));
        lbl.setTextFill(Color.web(WHITE + "55"));
        lbl.setMinWidth(70);

        ProgressBar pb = new ProgressBar(Math.min(1.0, Math.max(0.0, value)));
        pb.setPrefWidth(100);
        pb.setStyle(
                "-fx-accent:" + CYAN + ";-fx-background-color:" + BORDER + ";" +
                        "-fx-background-radius:3;"
        );

        Label pct = new Label(String.format("%.0f%%", value * 100));
        pct.setFont(Font.font("Segoe UI", 10));
        pct.setTextFill(Color.web(CYAN));

        box.getChildren().addAll(lbl, pb, pct);
        return box;
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

    private String labelColor(String label) {
        return switch (label) {
            case "Top Contributor" -> GREEN;
            case "Active"          -> CYAN;
            case "Occasional"      -> ORANGE;
            default                -> RED;
        };
    }

    private String formatHour(int hour) {
        if (hour < 0) return "Unknown";
        if (hour == 0)  return "12 AM";
        if (hour == 12) return "12 PM";
        return hour < 12 ? hour + " AM" : (hour - 12) + " PM";
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

    private VBox buildStatCard(String label, String value) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(18, 22, 18, 22));
        card.setStyle(
                "-fx-background-color:" + SURFACE + ";-fx-border-color:" + BORDER + ";" +
                        "-fx-border-width:1;-fx-border-radius:12;-fx-background-radius:12;"
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

    private String nvl(String val, String fallback) {
        return (val == null || val.isBlank()) ? fallback : val;
    }

    private String formatDate(String iso) {
        if (iso == null || iso.isBlank()) return "Unknown";
        try { return iso.substring(0, 10); } catch (Exception e) { return iso; }
    }

    private void styleSidebarBtn(Button btn, boolean hovered) {
        btn.setStyle(
                "-fx-background-color:" + (hovered ? CYAN + "22" : "transparent") + ";" +
                        "-fx-text-fill:" + (hovered ? CYAN : WHITE) + ";" +
                        "-fx-font-size:13px;-fx-alignment:CENTER-LEFT;" +
                        "-fx-padding:9 15 9 15;-fx-cursor:hand;" +
                        "-fx-border-radius:8;-fx-background-radius:8;"
        );
    }

    private Canvas buildLogo(double w, double h) {
        Canvas c = new Canvas(w, h);
        GraphicsContext gc = c.getGraphicsContext2D();
        double cx = w/2, cy = h/2, r = w*0.42;
        gc.setStroke(Color.web(CYAN+"33")); gc.setLineWidth(4);
        gc.strokeOval(cx-r-4,cy-r-4,(r+4)*2,(r+4)*2);
        gc.setStroke(Color.web(CYAN)); gc.setLineWidth(2);
        gc.strokeOval(cx-r,cy-r,r*2,r*2);
        gc.setStroke(Color.web(CYAN)); gc.setLineWidth(1.8);
        gc.setLineCap(StrokeLineCap.ROUND);
        double s = w/120.0;
        double[] xs = {cx-28*s,cx-16*s,cx-6*s,cx+4*s,cx+14*s,cx+22*s,cx+28*s};
        double[] ys = {cy,cy,cy-24*s,cy+18*s,cy-14*s,cy,cy};
        gc.beginPath(); gc.moveTo(xs[0],ys[0]);
        for (int i=1;i<xs.length;i++) gc.lineTo(xs[i],ys[i]);
        gc.stroke();
        gc.setFill(Color.web(CYAN));
        gc.fillOval(xs[0]-3,ys[0]-3,6,6);
        gc.fillOval(xs[6]-3,ys[6]-3,6,6);
        gc.fillOval(xs[2]-3,ys[2]-3,6,6);
        return c;
    }
}