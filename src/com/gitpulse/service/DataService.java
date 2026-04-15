package com.gitpulse.service;

import java.util.List;
import com.gitpulse.api.GitHubApiClient;
import com.gitpulse.model.Repository;
import com.gitpulse.parser.*;
import com.gitpulse.util.ErrorHandler;

//public class DataService {
//    // Declaring attributes
//    private final GitHubApiClient apiClient;
//    private final GitPulseJsonParser parser;
//
//    // Constructor
//    public DataService() {
//        this.apiClient = new GitHubApiClient();
//        this.parser    = new GitPulseJsonParser();
//    }
//
//    // Method to return all the repository data
//    public Repository loadRepository(String owner, String repositoryName) {
//        Repository repository = new Repository(owner, repositoryName);
//
//        try {
//            // Fetch and parse repository info
//            String repoJson = apiClient.fetchRepositoryInfo(owner, repositoryName);
//            parser.parseRepositoryInfo(repoJson, repository);
//
//            // Fetch and parse commits
//            String commitsJson = apiClient.fetchCommits(owner, repositoryName);
//            parser.parseCommits(commitsJson, repository);
//
//            // Fetch and parse contributors
//            String contributorsJson = apiClient.fetchContributors(owner, repositoryName);
//            parser.parseContributors(contributorsJson, repository);
//        } catch (Exception e) {
//            System.err.println("Error loading repository: " + e.getMessage());
//        }
//        return repository;
//    }
//}
public class DataService {
    // Declaring attributes
    private final GitHubApiClient apiClient;

    // Constructor
    public DataService() {
        this.apiClient = new GitHubApiClient();
    }

    // Method to load all the repository data
    public Repository loadRepository(String owner, String repositoryName) {
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

                    new ParserTask(new WeeklyStatsParser(),
                            apiClient.fetchWeeklyStats(owner, repositoryName)),

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
        return repository;
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
