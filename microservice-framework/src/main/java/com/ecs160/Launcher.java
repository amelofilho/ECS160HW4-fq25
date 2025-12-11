package com.ecs160;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ecs160.annotations.Endpoint;
import com.ecs160.annotations.Microservice;

import com.ClassLoaderHelper;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

class Launcher {

    private Map<String, Method> endpointMap;
    
    public Launcher() {
        endpointMap = new HashMap<>();
    }

    public String getEndPointMethod(String url) {
        if (!url.startsWith("/")) { //make sure that it matches the context
            url = "/" + url;
        }
        if (endpointMap.containsKey(url)){
            return (endpointMap.get(url)).getName();
        } 
        return null;
    }

    public Map<String, Method> getEndPointMap() {
        return endpointMap;
    }

    public void createHttpServer(int port){
         try {
            HttpServer MyServer = HttpServer.create(new InetSocketAddress(port), 0);
            MyServer.createContext("/", new MyHandler(endpointMap));
            MyServer.setExecutor(null);
            MyServer.start();
        } catch (IOException e) {
            System.out.println("Failed to Start Server" + e.getMessage());
        }
    }


    public boolean launch(int port) {
        ClassLoaderHelper classloader = new ClassLoaderHelper();
        List<Class<?>> allClasses = classloader.listClassesInAllJarsInOwnDirectory();
        for(Class<?> clazz : allClasses){
            if(clazz.isAnnotationPresent(Microservice.class)){
                for (Method m : clazz.getDeclaredMethods()) {
                    if (m.isAnnotationPresent(Endpoint.class)) {
                        Endpoint endpoint = m.getAnnotation(Endpoint.class);
                            //potentaily add code if there is no annotation or its blank
                        String url = endpoint.url();
                        if (!url.startsWith("/")) { //make sure that it matches the context
                            url = "/" + url;
                        }
                        if(!endpointMap.containsKey(url)){
                            endpointMap.put(url, m);
                        }        
                        //assume there is an annotation and it has a url
                        //save to the registry
                        //just a mapping of endpoint to the class/filename
                        //if endpoint already present skip this
                    }
                }
            }
        }
        //
        createHttpServer(port);
        /* essentially just
        while(true){  
                recieve queries from the client
                get the arguments from the query
                based on the endpoint get the class (lookup in the map)
                object o = class() (default constructor)
                method.invoke(0, args) 
          }
        */
        return true;
          
    }

}
