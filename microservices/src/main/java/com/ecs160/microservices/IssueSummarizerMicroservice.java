package com.ecs160.microservices;

import com.ecs160.annotations.Microservice;

import java.io.IOException;

import com.ecs160.annotations.Endpoint;


// uncomment the above imports and annotation usage as needed

@Microservice
public class IssueSummarizerMicroservice {
    public IssueSummarizerMicroservice(){
    }

    @Endpoint(url = "/summarize_issue")
    public String summarizeIssue(String issueJson) {
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

