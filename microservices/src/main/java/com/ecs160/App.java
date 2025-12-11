package com.ecs160;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App 
{
    public static void main( String[] args )
    {
        // Launcher myLauncher = new Launcher();
        // int port = 8000;
        // myLauncher.launch(port);
        // summarize the issue call llm
        //System.out.println( "Starting Server on port" + port + "!" );

        SpringApplication.run(App.class, args);


    }
}
