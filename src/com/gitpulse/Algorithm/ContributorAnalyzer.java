package com.gitpulse.Algorithm;

import com.gitpulse.model.Commit;
import com.gitpulse.model.Contributor;
import com.gitpulse.model.Repository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/*
 I designed this algorithm to test "Who is doing the work, and who has gone quiet?"

 WHAT IT DOES :
      Note:(I used AI to get these measures)
1. Gives every contributor a score out of 100 based on:
    - How many commits they made  (volume)
    - How regularly they commit   (consistency)
    - How recently they were seen (recency)
2. Labels each person: Top Contributor / Active / Occasional / Inactive
3. Identifies "Inactive" contributors (silent for 90+ days relative to repo's last commit)
  SCORING FORMULA:
    overallScore = (commitShareScore * 0.50)    // 50% weight on volume
                + (consistencyScore  * 0.30)    // 30% weight on regularity
                + (recencyScore      * 0.20)    // 20% weight on recency

 */
public class ContributorAnalyzer {

    /**
     * Entry point: pass in a loaded Repository, get back a sorted list of ContributorScores.
     */
    public List<ContributorScore> analyze(Repository repository) {
        List<Contributor> contributors = repository.getContributors();
        List<Commit>      commits      = repository.getCommits();

        if (contributors == null || contributors.isEmpty()) return Collections.emptyList();

        int totalCommits = contributors.stream().mapToInt(Contributor::getTotalCommits).sum();
        if (totalCommits == 0) return Collections.emptyList();

        //  determine the repo's own latest commit date as the recency anchor.
        // Using Instant.now() penalises contributors of completed/archived repos unfairly.
        Instant repoLatestCommit = latestCommitInstant(commits);

        Map<String, List<Instant>> commitDatesByAuthor = buildCommitDateMap(commits);

        List<ContributorScore> scores = new ArrayList<>();
        for (Contributor contributor : contributors) {
            ContributorScore score = scoreContributor(
                    contributor, totalCommits, commitDatesByAuthor, repoLatestCommit
            );
            scores.add(score);
        }

        scores.sort((a, b) -> Double.compare(b.getOverallScore(), a.getOverallScore()));
        return scores;
    }



    private ContributorScore scoreContributor(Contributor contributor,
                                              int totalCommits,
                                              Map<String, List<Instant>> commitDatesByAuthor,
                                              Instant repoLatestCommit) {
        String username  = contributor.getUsername();
        int    myCommits = contributor.getTotalCommits();

        // ── 1. Commit Share Score (0–100)
        double commitShare      = (myCommits * 100.0) / totalCommits;
        double commitShareScore = Math.min(commitShare * 2.0, 100.0);

        // ── 2. Resolve author date
        // Try exact match first, then case-insensitive fallback.
        List<Instant> myDates = resolveDates(username, commitDatesByAuthor);

        // ── 3. Consistency Score (0–100)
        double consistencyScore = calculateConsistencyScore(myDates);

        // ── 4. Recency Score (0–100)
        // FIX #2: measure recency relative to the repo's last commit, not today.
        double recencyScore = calculateRecencyScore(myDates, repoLatestCommit);

        // ── 5. Weighted Overall Score
        // FIX #5: no artificial floor — score reflects real performance.
        double overallScore = (commitShareScore * 0.50)
                + (consistencyScore  * 0.30)
                + (recencyScore       * 0.20);

        // ── 6. Activity Label ─────────────────────────────────────────────────
        String label = classifyContributor(overallScore, recencyScore);

        return new ContributorScore(
                username, myCommits, commitShare,
                consistencyScore, recencyScore, overallScore, label
        );
    }

    /**
      Consistency = fraction of distinct weeks in which the contributor made
      at least one commit, expressed as 0–100.
      Important thing. if totalWeeks == 0 (all commits within same week) → return 100.0,
      meaning "as consistent as possible given the time window".
     */
    private double calculateConsistencyScore(List<Instant> dates) {
        if (dates.isEmpty()) return 0.0;

        Instant earliest = dates.stream().min(Instant::compareTo).orElse(Instant.now());
        Instant latest   = dates.stream().max(Instant::compareTo).orElse(Instant.now());

        // FIX #1: DAYS.between works on Instant; WEEKS.between does not.
        long totalWeeks = ChronoUnit.DAYS.between(earliest, latest) / 7;

        // FIX #3: single-week contributor → perfectly consistent for that window.
        if (totalWeeks == 0) return 100.0;

        // Count distinct week-slots (days since epoch / 7) the person committed in.
        Set<Long> activeWeeks = new HashSet<>();
        for (Instant d : dates) {
            long weekSlot = ChronoUnit.DAYS.between(Instant.EPOCH, d) / 7;
            activeWeeks.add(weekSlot);
        }

        return Math.min((activeWeeks.size() * 100.0) / totalWeeks, 100.0);
    }

    /**
     * Recency = exponential decay measured from the repo's own last commit date.
     
      Score = 100 × e^( –daysSinceLastCommit / 60 )
        0 days before repo's last commit  → 100
        60 days before repo's last commit → ~37
        120 days                          → ~14
        180+ days                         →  ~5
     */
    private double calculateRecencyScore(List<Instant> dates, Instant repoLatestCommit) {
        if (dates.isEmpty()) return 0.0;
        Instant lastCommit = dates.stream().max(Instant::compareTo).orElse(Instant.EPOCH);
        // How many days before the repo's final commit did this person last contribute?
        long daysBefore = ChronoUnit.DAYS.between(lastCommit, repoLatestCommit);
        if (daysBefore < 0) daysBefore = 0; // guard for clock skew
        return 100.0 * Math.exp(-daysBefore / 60.0);
    }

    /**
      Assigns a plain-English activity label.
      "Inactive" threshold raised from recencyScore < 10 (which fires at ~90 days
      before NOW) to recencyScore < 5 (fires at ~180 days before the repo anchor),
      giving fairer treatment to contributors who were active near the repo's end.
     */
    private String classifyContributor(double overallScore, double recencyScore) {
        if (recencyScore < 5.0)  return "Inactive";        // silent for 180+ days before repo end
        if (overallScore >= 60)  return "Top Contributor";
        if (overallScore >= 35)  return "Active";
        if (overallScore >= 15)  return "Occasional";
        return "Inactive";
    }

    /**
      Builds a map of authorName → list of commit Instants.
      Stores both original-case and lowercase keys to help with
      case-insensitive fallback lookup in resolveDates().
     */
    private Map<String, List<Instant>> buildCommitDateMap(List<Commit> commits) {
        Map<String, List<Instant>> map = new HashMap<>();
        if (commits == null) return map;

        for (Commit commit : commits) {
            String author = commit.getAuthorName();
            if (author == null) continue;
            try {
                Instant instant = Instant.parse(commit.getDate());
                // Store under original name AND lowercase for fallback matching
                map.computeIfAbsent(author, k -> new ArrayList<>()).add(instant);
                String lower = author.toLowerCase();
                if (!lower.equals(author)) {
                    map.computeIfAbsent(lower, k -> new ArrayList<>()).add(instant);
                }
            } catch (Exception ignored) {}
        }
        return map;
    }

    /**
       Resolves a contributor's commit dates despite login-vs-name mismatch.
      Priority: exact match → lowercase match → empty list.
     */
    private List<Instant> resolveDates(String username,
                                       Map<String, List<Instant>> commitDatesByAuthor) {
        // 1. Exact match
        if (commitDatesByAuthor.containsKey(username)) {
            return commitDatesByAuthor.get(username);
        }
        // 2. Case-insensitive match
        String lower = username.toLowerCase();
        if (commitDatesByAuthor.containsKey(lower)) {
            return commitDatesByAuthor.get(lower);
        }
        // 3. Partial match: author name contains the username or vice-versa
        for (Map.Entry<String, List<Instant>> entry : commitDatesByAuthor.entrySet()) {
            String key = entry.getKey().toLowerCase();
            if (key.contains(lower) || lower.contains(key)) {
                return entry.getValue();
            }
            // 4. Word-level match: "Azhan Ali" → ["azhan", "ali"] vs "azhan-ali55"
            for (String word : key.split("[\\s\\-_]+")) {
                if (word.length() >= 4 && lower.contains(word)) {
                    return entry.getValue();
                }
            }
        }
        return Collections.emptyList();
    }
    /**
      Returns the latest commit Instant across all commits.
      Used as the recency anchor so scores are repo-relative, not clock-relative.
     /
    private Instant latestCommitInstant(List<Commit> commits) {
        if (commits == null || commits.isEmpty()) return Instant.now();
        return commits.stream()
                .map(c -> { try { return Instant.parse(c.getDate()); } catch (Exception e) { return null; } })
                .filter(Objects::nonNull)
                .max(Instant::compareTo)
                .orElse(Instant.now());
    }

    // ── Public convenience filters
    /**
      Returns only Top Contributors.
      Falls back to the highest scorer if nobody qualifies, so the list is never empty
      for a repo that has real commits.
     */
    public List<ContributorScore> getTopContributors(List<ContributorScore> ranked) {
        List<ContributorScore> top = ranked.stream()
                .filter(c -> c.getActivityLabel().equals("Top Contributor"))
                .collect(Collectors.toList());

        // Fallback: if scoring left nobody as "Top Contributor" (e.g. short-lived repo),
        // return the single highest-scoring contributor so the UI is never blank.
        if (top.isEmpty() && !ranked.isEmpty()) {
            top = Collections.singletonList(ranked.get(0));
        }
        return top;
    }
    /** Returns contributors labelled Inactive. */
    public List<ContributorScore> getInactiveContributors(List<ContributorScore> ranked) {
        return ranked.stream()
                .filter(c -> c.getActivityLabel().equals("Inactive"))
                .collect(Collectors.toList());
    }
}
