# How to run: 
### 1. Ensure you have downloaded OLLAMA and loaded the deepcoder: 1.5B model.
### 2. Ensure the 'dump.rdb' file is present in the main-app.
### 3. Start all the services as follows:

#### Start Redis Server, in 'main-app': 
  
    redis-server
*Make sure keys are loaded from dump.rdb*
*Some of us had issues with redis deleting the dump every time we restarted redis, if thats the case make sure dump is in the right location and that no instances of redis is already running. If all else fails, run the script, and then put the correct dump back into main_app and then just call mvn exec:java*
  
#### Start Ollama LLM in root directory:

    ollama serve
  
#### Run Spring Boot framework in 'microservices':
    cd microservices
    mvn spring-boot:run
*Should see "Started SpringBootApp in x seconds..."*
    
#### Run program in root directory with: 
    ./script.sh

*It takes a while to run as we run comprehensive unit tests for each framework. In the end our program will print out common Issues from `IssueList1` and `IssueList2`.*

*(Some notes: For some reason we get an empty result when ollama runs too much, try exiting and restarting ollama if that happens)*



