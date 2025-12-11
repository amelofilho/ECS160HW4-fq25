package com.ecs160;

import org.springframework.stereotype.Component;

@Component
public class FrameworkAdapter {

    private final Launcher launcher;

    public FrameworkAdapter() {
        this.launcher = new Launcher();
        System.out.println("[FrameworkAdapter] Starting Launcher");
    }

    public String summarizeIssue(String issueText) {
        // should later call LLM code... 
        return "summary placeholder";
    }
}