package com.ecs160;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BugFinderController {

    private final FrameworkAdapter adapter;

    public BugFinderController(FrameworkAdapter adapter) {
        this.adapter = adapter;
    }

    @GetMapping("/find_bug")
    public String findBug(
        @RequestParam("content") String content,
        @RequestParam(value = "filename", defaultValue = "unknown.c") String filename) {
            return adapter.findBug(content, filename);
    }
}