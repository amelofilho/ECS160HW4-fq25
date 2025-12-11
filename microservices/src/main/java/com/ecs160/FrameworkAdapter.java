package com.ecs160;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.ecs160.microservices.IssueComparatorMicroservice;
import com.ecs160.microservices.llmservice;

@Component
public class FrameworkAdapter {

    private final llmservice llm = new llmservice();
    private final IssueComparatorMicroservice comparator = new IssueComparatorMicroservice();

    public FrameworkAdapter() {
        System.out.println("[FrameworkAdapter] Initialized");
    }

    public String summarizeIssue(String issueText) {
        try {
            return llm.summarize_Issue(issueText);
        } catch (IOException e) {
            return "ERROR: " + e.getMessage();
        }
    }

    public String findBug(String fileContent, String fileName) {
        try {
            return llm.find_bugs(fileContent, fileName);
        } catch (IOException e) {
            return "ERROR: " + e.getMessage();
        }
    }

    public String compareIssues(String issueJsonArray) {
        return comparator.checkEquivalence(issueJsonArray);
    }
}