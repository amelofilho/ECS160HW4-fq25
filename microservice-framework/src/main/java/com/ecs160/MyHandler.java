package com.ecs160;
import java.lang.reflect.Method;
import java.util.Map;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.nio.charset.StandardCharsets;

class MyHandler implements HttpHandler {

    private Map<String, Method> endpointMap;
    
    public MyHandler(Map<String, Method> endpointMap) {
        this.endpointMap = endpointMap;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String returnString = "";
        String url = exchange.getRequestURI().getPath();
        Method m = endpointMap.get(url);
        String httpMethod = exchange.getRequestMethod();
        //System.out.println(httpMethod + " request for " + url);
        if (httpMethod.equalsIgnoreCase("POST")) {
            if(m != null){
                try {
                    InputStream inputStream = exchange.getRequestBody(); 
                    StringBuilder stringBuilder = new StringBuilder(); 
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader); 
                    //read the post paramtaers from the exchange
                    String line; 
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                    }

                    String query = stringBuilder.toString();
                    Class<?> clazz = m.getDeclaringClass();
                    Object o = clazz.getDeclaredConstructor().newInstance();
                    Object returnVal = m.invoke(o, new Object[] {query}); //need to wrap it to make sure it always works
                    returnString = returnVal.toString();
                    //returnString = returnVal.stringify(); find
                    exchange.sendResponseHeaders(200, returnString.length()); //SUCCESS
                }   catch (Exception e) { 
                    returnString = "Internal Error";
                    exchange.sendResponseHeaders(500, returnString.length()); //internal failure to start server
                }  
                
            }
            else{
                returnString = "Endpoint Not Found";
                exchange.sendResponseHeaders(404, returnString.length()); //endpoint not found
            }
        }
        else{
            returnString = "Only Post Allowed"; //we only can use post as a functionality
            exchange.sendResponseHeaders(405, returnString.length());
        }
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(returnString.getBytes());
        }
    }

}

