package com.ecs160.microservices;

import java.io.IOException;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class llmservice {
    private static final Gson gson = new Gson();
    private static final String MODEL = "deepcoder:1.5b";

    public llmservice() {
       
    }

    // Send prompt to ollama and ask for their request

    public String generateResponse(String prompt, JsonObject format) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost("http://localhost:11434/api/chat");

            // Form a JSON request body
            JsonObject requestBody= new JsonObject();
            requestBody.addProperty("model",MODEL);
            JsonArray messages = new JsonArray();
            JsonObject message = new JsonObject();
            message.addProperty("role", "user");
            message.addProperty("content", prompt);
            messages.add(message);
            requestBody.add("messages", messages);
            // make into one complete response
            requestBody.addProperty("stream",false);

            //add format
            requestBody.add("format", format);

            //request
            StringEntity entity= new StringEntity(gson.toJson(requestBody));
            request.setEntity(entity);
            request.setHeader("Content-Type","application/json");

            try (ClassicHttpResponse response = client.executeOpen(null, request, null)) {
                int status = response.getCode();
                if (status != 200) {
                    throw new IOException("Error: HTTP " + status);
                }

                try {
                    // Return just the text
                    String response_text= EntityUtils.toString(response.getEntity());
                    JsonObject model_ollama_response=gson.fromJson(response_text, JsonObject.class);
                    JsonObject meesageObj=model_ollama_response.getAsJsonObject("message");
                    String response_clean=meesageObj.get("content").getAsString();
                    return response_clean.trim();
                } catch (org.apache.hc.core5.http.ParseException e) {
                    throw new IOException("Failed to parse reponse", e);
                }
            }
        }
    }


    // Generate JSON Format based on OLLAMA structured output shown here: https://ollama.com/blog/structured-outputs

    private JsonObject formatOutput(boolean array){

        JsonObject bugObject= new JsonObject();
        bugObject.addProperty("type", "object");

        JsonObject properties =new JsonObject();
        
        JsonObject type= new JsonObject();
        type.addProperty("type", "string");
        properties.add("bug_type",type);

        JsonObject line=new JsonObject();
        line.addProperty("type", "string");
        properties.add("line",line);

        JsonObject description =new JsonObject();
        description.addProperty("type", "string");
        properties.add("description",description);

        JsonObject filename= new JsonObject();
        filename.addProperty("type", "string");
        properties.add("filename",filename);

        bugObject.add("properties",properties);

        JsonArray required =new JsonArray();
        required.add("bug_type");
        required.add("line");
        required.add("description");
        required.add("filename");
        bugObject.add("required",required);


        // If array is requested we put inside array
        if(array)
        {
            JsonObject arrayformat= new JsonObject();
            arrayformat.addProperty("type", "array");
            arrayformat.add("items",bugObject);
            return arrayformat;
        }

        return bugObject;
    }

    // Summarize issue of bugs

    public String summarize_Issue(String issueJson) throws IOException{
        //System.out.println("Enteredd LLMSERVICE");
        // JsonObject issue=gson.fromJson(issueJson,JsonObject.class);
        String description=issueJson;
        //System.out.println("JSON COMPLETED ");
        if(description==null){
            throw new IOException("No description field found in issue JSON");
        }

        String prompt = String.format(
            "You are a debugging assistant\n"+
            "You are tasked to extract bug in the given JSON format below\n" +
            "Descrption of Issue: %s\n" +
            "ONLY output a JSON object with the given fields:\n" +
            "- bug_type: the type of bug\n" +
            "- line: line of where the bug occured. If no line is mentioned, respond with: undefined line\n"+
            "- description: description of the bug\n" +
            "- filename: affected file. If no filename is mentioned, respond with: undefined filename\n\n",
        description
         );
         
        return generateResponse(prompt,formatOutput(false));

    }

    // Find bugs given a C program

    public String find_bugs(String Cfile_content,String cfile_name)throws IOException{

        String prompt = String.format(
            "You are a C Code file debugging assistant\n"+
            "You are tasked to extract bug present in the C Code and answer in the given JSON format below\n" +
            "C Code: %s\n" +
            "ONLY output a JSON object with the given fields:\n" +
            "- bug_type: the type of bug\n" +
            "- line: line of where the bug occured. Please go through the content and find the line number\n"+
            "- description: description of the bug\n" +
            "- filename:%s\n\n",
            Cfile_content,cfile_name
         );

        return generateResponse(prompt,formatOutput(false));

    }


    // Find common issues from two lists
    
    public String IssueSummaryCompactor(String list1,String list2)throws IOException{

          String prompt = String.format(
            "You are an issue finding assistant\n"+
            "You are tasked to find issues that are common in both list1 and list2\n" +
            "Issues are considered common if they have semantically similar descriptions.\n"+
            "List1: %s\n" +
            "List2: %s\n" +
            "Output a JSON array with all common issues you found.\n" +
            "Each Common Issue MUST have these fields:\n" +
            "- bug_type: the type of bug\n" +
            "- line: line of where the bug occurred. If no line is mentioned respond with: undefined\n" +
            "- description: description of the bug\n" +
            "- filename: affected file. If no filename is mentioned respond with: undefined\n\n" +
            "OUTPUT FORMAT:\n" +
            "[\n" +
            "  {\"bug_type\":\"...\",\"line\":\"...\",\"description\":\"...\",\"filename\":\"...\"},\n" +
            "  {\"bug_type\":\"...\",\"line\":\"...\",\"description\":\"...\",\"filename\":\"...\"},\n" +
            "  ...\n" +
            "]\n\n" +
            "If no common issues found, return: []\n\n",
            list1, list2
         );

        return generateResponse(prompt,formatOutput(true));
    }

    public static void main(String[] args) {
       
    }
}


