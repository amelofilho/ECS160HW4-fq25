package com.ecs160.hw.model;

import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.ecs160.persistence.annotations.*;

@PersistableObject
public class Repo {
    @Id
    @PersistableField
    private String id; 
    @PersistableField
    private String name; 
    @PersistableField
    private String html_url;
     @LazyLoad (field = "recentCommits")
    private List<Commit> recentCommits = null;
    @LazyLoad (field = "OpenIssues")
    private List<Issue> OpenIssues = null;
   
    private int forks;
    private String language;
    private String ownerLogin;
    private int open_issues_count;
    private List<Repo> forksList;
    private int commitCount; //do we need this we need to calcualte later at some point
    private int newCommitCount; 
    private int issueCount;
    private String created_at;
    private String commit_html;
    private String issue_html;
    private int stargazers_count;
    private Map<String, Integer> languages;
    private boolean tutorial = false;      

    public Repo() {
        this.forksList = new ArrayList<>(20);
        
        // commented out for lazy loading
        // this.recentCommits = new ArrayList<>(50); 
        // this.OpenIssues = new ArrayList<>(10);
        
        this.newCommitCount = 0;
    }

    public Repo(
        String name, String ownerLogin, String html_url, 
        int forks, String language, int open_issues_count, 
        String commit_html, String issue_html, int stargazers_count, String created_at
        ){

            this.forksList = new ArrayList<>(20);
            
            // commented out for lazy loading
            // this.recentCommits = new ArrayList<>(50); 
            // this.OpenIssues = new ArrayList<>(10);
            
            this.name = name;
            this.ownerLogin = ownerLogin;
            this.html_url = html_url;
            this.forks = forks;
            this.language = language;
            this.open_issues_count = open_issues_count;
            
            this.commitCount = 0;
            this.created_at = created_at;
            this.commit_html = commit_html;
            this.issue_html = issue_html;
            this.stargazers_count = stargazers_count;
    }

    public Repo(
        String name, String ownerLogin, String html_url, 
        int forks, String language, int open_issues_count, 
        String commit_html, String issue_html, int stargazers_count, String created_at, 
        int newCommitCount, boolean tutorial
        ){

            // commented out for lazy loading
            this.forksList = new ArrayList<>(20);
            // this.recentCommits = new ArrayList<>(50);
            // this.OpenIssues = new ArrayList<>(10);
            
            this.name = name;
            this.ownerLogin = ownerLogin;
            this.html_url = html_url;
            this.forks = forks;
            this.language = language;
            this.open_issues_count = open_issues_count;
            this.commitCount = 0;
            this.created_at = created_at;
            this.commit_html = commit_html;
            this.issue_html = issue_html;
            this.stargazers_count = stargazers_count;
    }

    public int getStargazersCount() {
        return stargazers_count;
    }

    public String getName() {
        return name;
    }
    public String getOwnerLogin() {
        return this.ownerLogin;
    }
    public String getHtmlUrl() {
        return html_url;
    }
    public int getForks() {
        return forks;
    }
    public String getLanguage() {
        return language;
    }
    public int getOpenIssuesCount() {
        return open_issues_count;
    }
    public List<Repo> getForksList() {
        return forksList;
    }
    public List<Commit> getRecentCommits() {
        return recentCommits;
    }
    public List<Issue> getOpenIssues() {
        return OpenIssues;
    }
    public void setOpenIssues(List<Issue> issues) {
        this.OpenIssues = issues;
    }
    public int getCommitCount() {
        return commitCount;
    }
    public String getCreatedAt() {
        return created_at;
    }
    public void setCommitCount(int commitCount) {
        this.commitCount = commitCount;
    }
    public void addFork(Repo fork) {
        this.forksList.add(fork);
    }
    public void setForksList(List<Repo> forkList) {
        this.forksList = forkList;
    }
    public void setNewCommits(int newCommitCount) {
        this.newCommitCount = newCommitCount;
    }
    public void addRecentCommit(Commit commit) {
        this.recentCommits.add(commit);
    }
    public void addIssue(Issue issue) {
        this.OpenIssues.add(issue);
    }
    public String getCommitHtml() {
        return commit_html;
    }
    public String getIssueHtml() {
        return issue_html;
    }
    public int getNumIssues() {
        this.issueCount = OpenIssues.size();
        return this.OpenIssues.size();
    }

    public int getNewCommitCount() {
        return this.newCommitCount;
    }

    public void setOpenIssuesCount(int issueCount) {
        this.issueCount = issueCount;
    }

    public void setLanguages(Map<String, Integer> languages) {
        this.languages = languages;
    }
    public void setTutorial(Boolean tutorial) {
        this.tutorial = tutorial;
    }

    public int getNewCommitsFromForks() {
        int newCommits = 0;
        for (Repo fork: forksList){
            newCommits+=fork.getNewCommitCount();
        }
        return newCommits;

    }
    
    /*Method to check percentage of .c and .rs file to fill in tutorial field */
    public void setTutorialField() {

        if (this.languages != null) {
            String[] languages = {"C","Rust","C++","Perl","Go","Java","Python","JavaScript"," Swift","Kotlin","Ruby","PHP","C#","TypeScript"};

            int non_tutorial_count=0;
            int totalCount = 0;

            //count non tutorial languages
            for(String lang: languages){
                if (this.languages.containsKey(lang)){
                    non_tutorial_count+=this.languages.get(lang);
                }
            }

            //count total languages
            for (int count : this.languages.values()) {
                totalCount += count;
            }

            if ((double)non_tutorial_count / totalCount > 0.75) {
                    this.tutorial = false;
                    return;
            }

        }
        this.tutorial = true;
    }


    //getter for tutorial
    public boolean isTutorial() {
        return this.tutorial;
    }

    //getter for languages
    public Map<String, Integer> getLanguages() {
        return this.languages;
    }


    // Inside Repo.java
    public List<String> getMostModifiedFiles() { // Renamed for clarity
        
        if (this.recentCommits == null || this.recentCommits.isEmpty()) {
            return Collections.emptyList(); // Return empty list instead of a string message
        }


        // counts how many times a file was modded (file path) 
        Map<String, Integer> fileCounts = new LinkedHashMap<>();
        
        //iterate through all modified files from recent commits
        for (Commit commit : this.recentCommits) {
            for (String fileName : commit.getModifiedFilesList()) {
                if (fileCounts.containsKey(fileName)) {
                    // If the file is already in the map, increment its count.
                    int currentCount = fileCounts.get(fileName);
                    fileCounts.put(fileName, currentCount + 1);
                } else {
                    // If the file is new to the map set its initial count to 1.
                    fileCounts.put(fileName, 1);
                }
            }
        }
        
        if (fileCounts.isEmpty()) {
            return Collections.emptyList();
        }

        // extracts the maps kv pairs into a list of Map.Entry objects.
        List<Map.Entry<String, Integer>> fileCountEntries = new ArrayList<>(fileCounts.entrySet());

        // sorting list of filenames in descending order by count
        Collections.sort(fileCountEntries, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> currentFileEntry, Map.Entry<String, Integer> nextFileEntry) {
                
                int currentCount = currentFileEntry.getValue();
                int nextCount = nextFileEntry.getValue();

                // If the next file count is greater, return 1 to move it forward 
                if (nextCount > currentCount) {
                    return 1;
                } 
                // If the next file count is smaller, return -1 to keep it after
                else if (nextCount < currentCount) {
                    return -1;
                } 
                // If counts are equal
                else {
                    return 0;
                }
            }
        });

        // determines how many items to keep; top 3 or the actual list size (if less than 3) 
        int topFilesCount = Math.min(3, fileCountEntries.size());
        
        // create a sublist of only fileCountEntries indexed 0 to 3-1 
        fileCountEntries = fileCountEntries.subList(0, topFilesCount);

        // store the top 3 keys (file names) from the fileCountEntries sublists
        List<String> topFileNames = new ArrayList<>();
        
        // iterate through the final list of entries and extract only the filename (the Key).
        for (Map.Entry<String, Integer> entry : fileCountEntries) {
            topFileNames.add(entry.getKey());
        }

        return topFileNames;
    }
    
}
