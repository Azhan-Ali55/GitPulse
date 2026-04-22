package com.gitpulse.service;

import java.util.List;
import com.gitpulse.api.GitHubApiClient;
import com.gitpulse.model.Repository;
import com.gitpulse.model.WeeklySummary;
import com.gitpulse.parser.*;
import com.gitpulse.util.ErrorHandler;

public class DataService {
    // Declaring attributes
    private final GitHubApiClient apiClient;

    // Simple in-memory cache to avoid re-fetching same repo
    private static final java.util.Map<String, Repository> cache =
            new java.util.concurrent.ConcurrentHashMap<>();

    // Constructor
    public DataService() {
        this.apiClient = new GitHubApiClient();
    }

    // Method to load all the repository data
    public Repository loadRepository(String owner, String repositoryName) {
        // Return cached version if available
        String cacheKey = owner + "/" + repositoryName;
        if (cache.containsKey(cacheKey)) {
            ErrorHandler.log("DataService", "Returning cached repo: " + cacheKey);
            return cache.get(cacheKey);
        }

        Repository repository = new Repository(owner, repositoryName);
        try {
            // Define a list of parsing tasks
            List<ParserTask> tasks = List.of(
                    new ParserTask(new RepositoryParser(),
                            apiClient.fetchRepositoryInfo(owner, repositoryName)),

                    new ParserTask(new CommitsParser(),
                            apiClient.fetchCommits(owner, repositoryName)),

                    new ParserTask(new ContributorsParser(),
                            apiClient.fetchContributors(owner, repositoryName)),

                    new ParserTask(new CommitStatsParser(),
                            apiClient.fetchCommits(owner, repositoryName)),

                    new ParserTask(new ReadmeParser(),
                            apiClient.fetchReadme(owner, repositoryName)),

                    new ParserTask(new LastCommitDateParser(),
                            apiClient.fetchLastCommit(owner, repositoryName))
            );
            for (ParserTask task : tasks) {
                try {
                    if (task.json == null || task.json.isEmpty()) continue;
                    task.parser.parse(task.json, repository);
                } catch (Exception e) {
                    ErrorHandler.log("DataService", "Parser failed: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            ErrorHandler.log("DataService", "Error loading repository: " + e.getMessage());
        }
        // Cache the result
        cache.put(owner + "/" + repositoryName, repository);
        return repository;
    }
    // Get AI summary of repository
    public String getRepositorySummary(Repository repository) {
        // Return cached summary if already generated
        if (repository.getRepositorySummary() != null) {
            return repository.getRepositorySummary();
        }

        PromptGenerator generator = new RepositorySummaryPromptGenerator(repository);
        AiSummaryService aiService = new AiSummaryService();
        String summary = aiService.getSummary(generator);
        repository.setRepositorySummary(summary);
        return summary;
    }

    // Get AI summary of README
    public String getReadmeSummary(Repository repository) {
        // Return cached summary if already generated
        if (repository.getReadmeSummary() != null) {
            return repository.getReadmeSummary();
        }

        if (repository.getReadme() == null || repository.getReadme().isEmpty()) {
            return "No README available for this repository.";
        }
        PromptGenerator generator = new ReadmeSummaryPromptGenerator(repository.getReadme());
        AiSummaryService aiService = new AiSummaryService();
        String summary = aiService.getSummary(generator);
        repository.setReadmeSummary(summary);
        return summary;
    }

    // Get AI summary of weekly stats
    public List<WeeklySummary> getWeeklySummaries(Repository repository) {
        // Return cached summary if already generated
        if (repository.getWeeklySummaries() != null) {
            return repository.getWeeklySummaries();
        }

        WeeklySummaryService service = new WeeklySummaryService();
        List<WeeklySummary> summaries =
                service.generate(repository.getWeeklyActivity());

        for (WeeklySummary ws : summaries) {
            PromptGenerator generator = new WeeklySummaryPromptGenerator(ws);
            AiSummaryService ai = new AiSummaryService();

            ws.setSummaryText(ai.getSummary(generator));
        }
        repository.setWeeklySummaries(summaries);
        return summaries;
    }

    // Method for background threading for JavaFX
    public javafx.concurrent.Task<Repository> loadRepositoryAsync(String owner, String repositoryName) {
        return new javafx.concurrent.Task<>() {
            @Override
            protected Repository call() {
                return loadRepository(owner, repositoryName);
            }
        };
    }
}

// Defining class to wrap a unique task
class ParserTask {
    GitHubJsonParser parser;
    String json;

    // Constructor
    ParserTask(GitHubJsonParser parser, String json) {
        this.parser = parser;
        this.json = json;
    }
}
