package com.ecs160;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

@RestController
public class BugFinderController {

    private final FrameworkAdapter adapter;
    private final Gson gson = new Gson();

    public BugFinderController(FrameworkAdapter adapter) {
        this.adapter = adapter;
    }

    @PostMapping("/find_bugs")
    public String findBugs(@RequestBody String json) {
        JsonObject obj = gson.fromJson(json, JsonObject.class);

        String content = obj.get("file_content").getAsString();
        String filename = obj.get("file_name").getAsString();

        return adapter.findBug(content, filename);
    }
}