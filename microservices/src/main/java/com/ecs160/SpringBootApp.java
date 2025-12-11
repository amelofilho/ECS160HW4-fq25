package com.ecs160;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringBootApp 
{
    public static void main( String[] args )
    {
        // Launcher myLauncher = new Launcher();
        // int port = 8000;
        // myLauncher.launch(port);
        // summarize the issue call llm
        //System.out.println( "Starting Server on port" + port + "!" );

        SpringApplication.run(SpringBootApp.class, args);


    }
}
