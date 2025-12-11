package com.ecs160.microservices;


// import the Microservice annotation we created
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ecs160.annotations.Microservice;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.ecs160.annotations.Endpoint;


// uncomment the above imports when the annotations are created

@Microservice
public class IssueComparatorMicroservice {
    
    private static final Gson gson = new Gson();
    private static final Type type = new TypeToken<Map<String, java.util.List<String>>>() {}.getType();

    public IssueComparatorMicroservice(){

    }
    
    @Endpoint(url = "/check_equivalence")
    public String checkEquivalence(String issueJSonArray) {
        //parse into list then back to json string
        Map<String, List<String>> map = gson.fromJson(issueJSonArray, type);
        List<String> issuelist1 = map.get("issueList1");
        List<String> issueList2 = map.get("issueList2");
        String issueList1json = gson.toJson(issuelist1);
        String issueList2json = gson.toJson(issueList2);
        
        llmservice llm = new llmservice();
        try{
            return llm.IssueSummaryCompactor(issueList1json, issueList2json);
        }
        catch(IOException e){
            return e.getMessage();
        }
    }   
}

