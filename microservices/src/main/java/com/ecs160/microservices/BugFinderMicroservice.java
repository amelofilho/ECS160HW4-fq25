package com.ecs160.microservices;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ecs160.annotations.Microservice;
import java.lang.reflect.Type;

import java.io.IOException;
import java.util.Map;

import com.ecs160.annotations.Endpoint;


// uncomment the annotations when you have created them
@Microservice
public class BugFinderMicroservice {
    private static final Gson gson = new Gson();
    private static final Type type = new TypeToken<Map<String, String>>() {}.getType();

    public BugFinderMicroservice(){

    }
    
    @Endpoint(url = "/find_bugs")
    public String findBugs(String code) {
        Map<String, String> map = gson.fromJson(code, type);
        String Cfile_content = map.get("file_content");
        String cfile_name = map.get("file_name");
        llmservice llm = new llmservice();
        try{
            return llm.find_bugs(Cfile_content, cfile_name);
        }
        catch(IOException e){
            return e.getMessage();
        }
    }
}

