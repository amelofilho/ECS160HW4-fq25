package com.ecs160.hw;

import java.io.IOException;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.ContentType;
import org.apache.commons.io.FileUtils;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Hello world!
 *
 */
public class HttpFetcher {
    private Integer port = 8000;
    
    public HttpFetcher(int port){
        this.port = port;
    }

    public String httpRequestNeat(String endpoint, String parameters) throws IOException{
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String url = "http://localhost:"+ port.toString() + endpoint;
            HttpPost request = new HttpPost(url);
            StringEntity entity = new StringEntity(parameters, ContentType.APPLICATION_JSON); //parameters pased sepratedly from url
            request.setEntity(entity); 

            try (ClassicHttpResponse response = client.executeOpen(null, request, null)) {
                int status = response.getCode();
                if (status != 200) {
                    throw new IOException("Error: HTTP " + status);
                }
                String returnVal = EntityUtils.toString(response.getEntity());
                return returnVal;
            } 
            catch (org.apache.hc.core5.http.ParseException e) {
                    throw new IOException("Failed to parse reponse", e);
            }
        }
    }

    public void cloneRepo(String cloneUrl, String repoName) throws IOException {
      try{
        String folderDestination="./cloned_repos/"+repoName;
        File repoDirectory = new File(folderDestination);
        if(repoDirectory.exists() && repoDirectory.isDirectory()){
            System.out.println("Directory already exists, deleting directory before cloning: " + repoName);
            FileUtils.cleanDirectory(repoDirectory);     
        }
        
        ProcessBuilder processBuilder=new ProcessBuilder("git","clone","--depth","1",cloneUrl,folderDestination);
        Process process = processBuilder.start();
        int exitCode= process.waitFor();
        if(exitCode!=0){
            System.out.println("Error cloning repository: " + repoName);
        }
        else{
            System.out.println("Successfully cloned repository: " + repoName);
        }
      }
        catch(Exception e){
            System.out.println("Exception occurred while cloning repo: " + repoName + " Error: " + e.getMessage());
        }
    }
}
