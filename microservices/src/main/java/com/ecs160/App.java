package com.ecs160;

public class App 
{
    public static void main( String[] args )
    {
        Launcher myLauncher = new Launcher();
        int port = 8000;
        myLauncher.launch(port);
        // summarize the issue call llm
        //System.out.println( "Starting Server on port" + port + "!" );
    }
}
