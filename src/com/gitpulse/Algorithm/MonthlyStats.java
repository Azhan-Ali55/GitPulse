package com.gitpulse.Algorithm;

import java.time.Month;

public class MonthlyStats {

    private final int year;
    private final Month month;
    private final int commitCount;
    private final int uniqueContributors;
    private final double avgCommitsPerWeek;
    private String activityTag = "Normal";

    public MonthlyStats(int year, Month month, int commitCount,
                        int uniqueContributors, double avgCommitsPerWeek) {
        this.year                = year;
        this.month               = month;
        this.commitCount         = commitCount;
        this.uniqueContributors  = uniqueContributors;
        this.avgCommitsPerWeek   = avgCommitsPerWeek;
    }

    // Getters
    public int getYear()                  { return year; }
    public Month getMonth()               { return month; }
    public int getCommitCount()           { return commitCount; }
    public int getUniqueContributors()    { return uniqueContributors; }
    public double getAvgCommitsPerWeek()  { return avgCommitsPerWeek; }
    public String getActivityTag()        { return activityTag; }

    // Setter
    public void setActivityTag(String tag) { this.activityTag = tag; }

    @Override
    public String toString() {
        return year + "-" + month + " | Commits: " + commitCount +
                " | Contributors: " + uniqueContributors +
                " | Avg/week: " + String.format("%.1f", avgCommitsPerWeek) +
                " | Tag: " + activityTag;
    }
}