package com.ecs160;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IssueSummarizerController {

    private final FrameworkAdapter adapter;

    public IssueSummarizerController(FrameworkAdapter adapter) {
        this.adapter = adapter;
    }

    @PostMapping("/summarize_issue")
    public String summarizeIssue(@RequestBody String issueJson) {
        return adapter.summarizeIssue(issueJson);
    }
}