package com.ecs160.hw.model;

import java.util.List;
import com.ecs160.persistence.annotations.*;

@PersistableObject
public class Commit {

    @Id
    @PersistableField
    private String sha;         // Unique ID of the commit
    
    @PersistableField
    private String commit_url;  // API URL for commit details (if needed later)
    
    @PersistableField
    private String authorName;
    
    @PersistableField
    private String date;        // Timestamp for commit comparison (e.g., "new commits")
    
    @PersistableField 
    private String modifiedFiles;  //since using a comma separated string
    // private List<String> modifiedFiles; // for the top 3 most modified files

    // Constructor for parsing last 50 commits
    public Commit(String sha, String commit_url, String authorName, String date) {
        this.sha = sha;
        this.commit_url = commit_url;
        this.authorName = authorName;
        this.date = date;
        this.modifiedFiles = "";
        // this.modifiedFiles = new ArrayList<>(); // initialize list
    }

    // Constructor parsing modified files, top 3 most modified, etc
    public Commit(String sha, String commit_url, String authorName, String date, List<String> modifiedFiles) {
        this.sha = sha;
        this.commit_url = commit_url;
        this.authorName = authorName;
        this.date = date;
        // this.modifiedFiles = modifiedFiles;
          this.modifiedFiles = String.join(",", modifiedFiles);
    }

    public String getSha() {
        return sha;
    }
    public String getDate() {
        return date;
    }
    public String getAuthorName() {
        return authorName;
    }
    public String getCommitUrl() {
        return commit_url;
    }
    // public List<String> getModifiedFiles() { return modifiedFiles; }

    // Helper method if you want List<String> access
    public List<String> getModifiedFilesList() {
        if (modifiedFiles == null || modifiedFiles.isEmpty()) {
            return List.of();
        }
        return List.of(modifiedFiles.split(","));
    }
    
    // works w CSV strings.. 
    public void addModifiedFile(String fileName) {
        if (this.modifiedFiles == null || this.modifiedFiles.isEmpty()) {
            this.modifiedFiles = fileName;
        } else {
            this.modifiedFiles = this.modifiedFiles + "," + fileName;
        }
    }


    // public void addModifiedFile(String fileName) {
    //     this.modifiedFiles.add(fileName);
    // }



}

