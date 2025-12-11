package com.ecs160;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IssueComparatorController {

    private final FrameworkAdapter adapter;

    public IssueComparatorController(FrameworkAdapter adapter) {
        this.adapter = adapter;
    }

    @PostMapping("/compare_issues")
    public String compareIssues(@RequestBody String issueJsonArray) {
        return adapter.compareIssues(issueJsonArray);
    }
}