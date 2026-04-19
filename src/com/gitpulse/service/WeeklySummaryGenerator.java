package com.gitpulse.service;

import com.gitpulse.model.WeeklySummary;
public class WeeklySummaryGenerator implements PromptGenerator {
    private final WeeklySummary summary;

    // Constructor
    public WeeklySummaryGenerator(WeeklySummary summary) {
        this.summary = summary;
    }

    @Override
    public String generatePrompt() {
        return """
            Analyze this week's Git activity:
            - Week: %s
            - Total commits: %d
            - Top contributor: %s
            """.formatted(
                summary.getWeekStart(),
                summary.getTotalCommits(),
                summary.getTopAuthor()
        );
    }
}