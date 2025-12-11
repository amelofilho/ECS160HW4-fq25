package com.ecs160;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IssueComparatorController {

    private final FrameworkAdapter adapter;

    public IssueComparatorController(FrameworkAdapter adapter) {
        this.adapter = adapter;
    }

    @GetMapping("/compare_issues")
    public String compareIssues(@RequestParam("issues") String issueJsonArray) {
        return adapter.compareIssues(issueJsonArray);
    }
}