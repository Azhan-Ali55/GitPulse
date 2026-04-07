package com.gitpulse.model;

import java.util.ArrayList;
import java.util.List;

public class Repository {
    // Declaring attributes
    private final String owner;
    private final String name;
    private String description;
    private String language;
    private List<Commit> commits;           // Storing all the commits of the repository
    private List<Contributor> contributors; // Storing all the Contributors of the repository

    // Constructor to initialize the attributes
    public Repository(String owner, String name) {
        this.owner = owner;
        this.name = name;
        this.commits = new ArrayList<>();
        this.contributors = new ArrayList<>();
    }

    // Setters for adding commits and contributors
    public void addCommit(Commit c) { commits.add(c); }
    public void addContributor(Contributor c) { contributors.add(c); }

    // Setters for fields fetched from API
    public void addDescription(String description) { this.description = description; }
    public void addLanguage(String language) { this.language = language; }

    // Getters for all fields
    public String getOwner() { return owner; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getLanguage() { return language; }
    public List<Commit> getCommits() { return commits; }
    public List<Contributor> getContributors() { return contributors; }

    // toString method to view the Repository details
    @Override
    public String toString() {
        return "Repository{" + "owner='" + owner + '\'' + ", name='" + name + '\'' +
                ", language='" + language + '\'' + ", commits=" + commits.size() +
                ", contributors=" + contributors.size() + '}';
    }
}
