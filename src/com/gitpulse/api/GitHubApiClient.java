package com.gitpulse.api;

import com.gitpulse.util.Constants;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GitHubApiClient extends ApiClient {
    // Constructor
    public GitHubApiClient() {
        super(Constants.TOKEN, Constants.BASE_URL);
    }

    // Method to make HTTP request
    private String get(String endpoint) throws Exception {
        // Building a URL for unique repository
        URL url = new URL(baseUrl + endpoint);
        // Setting up the connection to the repository through url
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Sending request to the connection
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "token " + token);
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
        connection.setRequestProperty("User-Agent", "GitPulse-App");
        connection.setConnectTimeout(Constants.TIMEOUT);
        connection.setReadTimeout(Constants.TIMEOUT);

        // Storing the response code from the connection
        int responseCode = connection.getResponseCode();

        // Handling invalid responses
        if(!isValidResponse(responseCode)) {
            handleError(responseCode);
        }

        // Reading the response from the connection and returning it to display
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        return response.toString();
    }

    // Error handling
    private void handleError(int statusCode) throws Exception {
        switch (statusCode) {
            case 401: throw new Exception("Invalid token! Check your GitHub token");
            case 403: throw new Exception("Rate limit exceeded! Try again later");
            case 404: throw new Exception("Repository not found!");
            default:  throw new Exception("HTTP error: " + statusCode);
        }
    }

    // Implementing abstract methods from ApiClient
    @Override
    public String fetchRepositoryInfo(String owner, String repo) throws Exception {
        return get("/repos/" + owner + "/" + repo);
    }

    @Override
    public String fetchCommits(String owner, String repo) throws Exception {
        return get("/repos/" + owner + "/" + repo + "/commits?per_page=100");
    }

    @Override
    public String fetchContributors(String owner, String repo) throws Exception {
        return get("/repos/" + owner + "/" + repo + "/contributors");
    }
}
