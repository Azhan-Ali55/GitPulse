package com.gitpulse.parser;

import com.gitpulse.model.Commit;
import com.gitpulse.model.Contributor;
import com.gitpulse.model.Repository;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GitPulseJsonParser {
    // Method to parse commits from JSON string
    public void parseCommits(String json, Repository repository) {
        JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
        // Extract data using loop
        for(JsonElement element : arr) {
            JsonObject obj = element.getAsJsonObject();
            // Get sha
            String sha = obj.get("sha").getAsString();

            // Get commit
            JsonObject commit = obj.getAsJsonObject("commit");
            String message = commit.get("message").getAsString();

            // Get author info
            JsonObject author = commit.getAsJsonObject("author");
            String name  = author.get("name").getAsString();
            String email = author.get("email").getAsString();
            String date  = author.get("date").getAsString();

            // Add commit to repository
            repository.addCommit(new Commit(sha, name, email, date, message));
        }
    }

    // Parse contributors from JSON string
    public void parseContributors(String json, Repository repository) {
        JsonArray arr = JsonParser.parseString(json).getAsJsonArray();

        for (JsonElement element : arr) {
            JsonObject obj = element.getAsJsonObject();

            // Get contributor info
            String username = obj.get("login").getAsString();
            String avatarUrl = obj.get("avatar_url").getAsString();
            int totalCommits = obj.get("contributions").getAsInt();

            // Add contributor to repository
            repository.addContributor(new Contributor(username, avatarUrl, totalCommits));
        }
    }

    // Parse repository info from JSON string
    public void parseRepositoryInfo(String json, Repository repository) {
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

        // Check for null before parsing
        if (!obj.get("description").isJsonNull()) {
            repository.setDescription(obj.get("description").getAsString());
        }
        if (!obj.get("language").isJsonNull()) {
            repository.setLanguage(obj.get("language").getAsString());
        }
    }
}
