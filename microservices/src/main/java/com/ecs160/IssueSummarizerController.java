package com.ecs160;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IssueSummarizerController {

    private final FrameworkAdapter adapter;
    
    public IssueSummarizerController(FrameworkAdapter adapter) {
        this.adapter = adapter;
    }
    @GetMapping("/summarize_issue")
    public String summarizeIssue(@RequestParam("issue") String issue) {
        // placeholder 
        return "summary placeholder";
    }
}