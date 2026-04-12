package com.gitpulse.service;

import com.gitpulse.api.GitHubApiClient;
import com.gitpulse.model.Repository;
import com.gitpulse.parser.GitPulseJsonParser;

public class DataService {
    // Declaring attributes
    private final GitHubApiClient apiClient;
    private final GitPulseJsonParser parser;

    // Constructor
    public DataService() {
        this.apiClient = new GitHubApiClient();
        this.parser    = new GitPulseJsonParser();
    }

    // Method to return all the repository data
    public Repository loadRepository(String owner, String repositoryName) {
        Repository repository = new Repository(owner, repositoryName);

        try {
            // Fetch and parse repository info
            String repoJson = apiClient.fetchRepositoryInfo(owner, repositoryName);
            parser.parseRepositoryInfo(repoJson, repository);

            // Fetch and parse commits
            String commitsJson = apiClient.fetchCommits(owner, repositoryName);
            parser.parseCommits(commitsJson, repository);

            // Step 3 — fetch and parse contributors
            String contributorsJson = apiClient.fetchContributors(owner, repositoryName);
            parser.parseContributors(contributorsJson, repository);
        } catch (Exception e) {
            System.err.println("Error loading repository: " + e.getMessage());
        }
        return repository;
    }
}
