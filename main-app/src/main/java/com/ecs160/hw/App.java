package com.ecs160.hw;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.ecs160.persistence.*; 

import com.ecs160.hw.util.*;
import com.ecs160.hw.model.*;
import com.google.gson.Gson;


public class App  {
    public static void main(String[] args) {
        Gson gson = new Gson();

        RedisDB db = new RedisDB("localhost", 6379);

        //HttpFetcher httpFetcher = new HttpFetcher(8000);

        // parse selected_repo.dat for repo id
        SelectedRepoData data = SelectedRepoLoader.loadSelectedRepo();

        // fetch raw repo object from redis
        Repo raw = (Repo) db.getObject(String.valueOf(data.repoId));

        // wrap repo inside a proxy for lazyloading
        Repo repo = (Repo) db.createProxy(raw);
        if (repo == null) {
            throw new RuntimeException("Repo not found in Redis: " + data.repoId);
        }
        System.out.println("Loaded repo: " + repo.getName());

        // clone the repo
        HttpFetcher fetcher = new HttpFetcher(8000);
        String cloneUrl = repo.getHtmlUrl() + ".git";
        try {
            fetcher.cloneRepo(cloneUrl, repo.getName());
        } catch (IOException e) {
            System.out.println("Failed to clone repo: " + e.getMessage());
        }

        // lazy loading issues
        List<Issue> ListOfIssues = repo.getOpenIssues();
        //List<Issue> ListOfIssuesHalf = ListOfIssues.subList(0, 5);
        System.out.println("\nIssueList1 (GitHub issues): " + ListOfIssues.size());

        // load .c files listed in selected_repo.dat
        String base = "cloned_repos/" + repo.getName() + "/";
        //StringBuilder allCode = new StringBuilder();

        //PART 5
        
        List <String> IssueList1 = new ArrayList<String>();
        for (Issue issue : ListOfIssues) {
            try{
                String issueBody = issue.getBody();
                //System.out.println(issueBody);
                String issueSummary = fetcher.httpRequestNeat("/summarize_issue", issueBody);
                //System.out.println(issueSummary);
                IssueList1.add(issueSummary);
            }
            catch(IOException e) {
                System.out.println(e.getMessage());
            }
        }
        System.out.println("Summarized " + ListOfIssues.size() + " Issues from Cloned Repo's Issues!");
        
        //PART 6
        List <String> ListOfFiles = new ArrayList<String>();

        for (String file : data.files) {
            String fullPath = base + file;
            Map <String, String> fileMap = new HashMap<String, String>();
            try {
                //System.out.println("Reading: " + fullPath);
                String content = Files.readString(Paths.get(fullPath));
                fileMap.put("file_content", content);
                fileMap.put("file_name", file);
                String fileJson = gson.toJson(fileMap);

                //System.out.println(fileJson);

                ListOfFiles.add(fileJson);
            } catch (Exception e) {
                System.out.println("Failed to read file: " + fullPath);
            }
            
        }
        

        List <String> IssueList2 = new ArrayList<String>();

        for (String fileJson : ListOfFiles) {
            try{
                String bugReport = fetcher.httpRequestNeat("/find_bugs", fileJson);
                //System.out.println(bugReport);
                IssueList2.add(bugReport);
            }
            catch(IOException e) {
                System.out.println(e.getMessage());
            }
        }
        System.out.println("Looked at " + ListOfFiles.size() + " Files for Bugs!");
        
        //Part 7
        Map<String, List<String>> ListofIssuesList1and2 = new HashMap<>();
        ListofIssuesList1and2.put("issueList1", IssueList1);
        ListofIssuesList1and2.put("issueList2", IssueList2);
        String simularitiesList = "";
        String ListofIssuesList1and2JsonString = gson.toJson(ListofIssuesList1and2);

        try{
            simularitiesList = fetcher.httpRequestNeat("/check_equivalence", ListofIssuesList1and2JsonString);
            System.out.println(simularitiesList);
            }
            catch(IOException e) {
                System.out.println(e.getMessage());
        }
        


        //make it a map of files to do indivudally
        //then make sure its in the format when passing into the thing
        // Map <String, String> files = new HashMap;
        // then make a gson object from this and do whatever
        // then pass that into the thing

    }
}