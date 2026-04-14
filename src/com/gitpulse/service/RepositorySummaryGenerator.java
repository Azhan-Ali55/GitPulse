package com.gitpulse.service;

import com.gitpulse.model.Contributor;
import com.gitpulse.model.Repository;

public class RepositorySummaryGenerator implements PromptGenerator {
    private final Repository repository;

    // Constructor
    public RepositorySummaryGenerator(Repository repository) {
        this.repository = repository;
    }
    // Defining method to generate prompt
    @Override
    public String generatePrompt() {
        StringBuilder prompt = new StringBuilder();
        // Giving prompt
        prompt.append("Analyze this GitHub repository and give a short professional summary:\n\n");

        // Basic repo info
        prompt.append("Repository: " + repository.getOwner() + "/" + repository.getName() + "\n");
        prompt.append("Description: " + repository.getDescription() + "\n");
        prompt.append("Language: " + repository.getLanguage() + "\n");
        prompt.append("Total Commits: " + repository.getCommits().size() + "\n");
        prompt.append("Total Contributors: " + repository.getContributors().size() + "\n\n");

        // Top contributors
        prompt.append("Contributors:\n");
        for (Contributor c : repository.getContributors()) {
            prompt.append("- " + c.getUsername() +
                    ": " + c.getTotalCommits() + " commits\n");
        }

        // Instructions
        prompt.append("\nBased on this data please provide:\n");
        prompt.append("1. A short overall summary of the repository\n");
        prompt.append("2. Team activity assessment\n");
        prompt.append("3. Any concerns about contributor balance\n");
        prompt.append("Keep the response concise and professional.");
        return prompt.toString();
    }

    public String generateReadmePrompt(String readmeContent) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Analyze this GitHub repository README and give a short professional summary:\n\n");
        prompt.append("README Content:\n");
        prompt.append(readmeContent + "\n\n");
        prompt.append("Based on this README please provide:\n");
        prompt.append("1. What this project does in simple terms\n");
        prompt.append("2. Who it is for\n");
        prompt.append("3. Key features mentioned\n");
        prompt.append("Keep the response concise and professional.");

        return prompt.toString();
    }
}
