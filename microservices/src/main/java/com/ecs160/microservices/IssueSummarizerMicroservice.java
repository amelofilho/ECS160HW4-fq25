package com.ecs160.microservices;

import java.io.IOException;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class IssueSummarizerMicroservice {
    public IssueSummarizerMicroservice(){
    }

    @PostMapping("/summarize_issue")
    public String summarizeIssue(@RequestBody String issueJson) {
        //no need to parse just one issue
        
        llmservice llm = new llmservice();
        try{
            return llm.summarize_Issue(issueJson);
        }
        catch(IOException e){
            return e.getMessage();
        }
    }
}

