package com.ecs160;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {

    private final FrameworkAdapter adapter;

    public PingController(FrameworkAdapter adapter) {
        this.adapter = adapter;
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}