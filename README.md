# How to run: 
### 1. Ensure you have downloaded OLLAMA and loaded the deepcoder: 1.5B model.
### 2. Ensure the 'dump.rdb' file is present in the main-app folder.
### 3. Turn on Services:

#### Start Redis Server, in 'main-app': 
  
    redis-server
*Make sure keys are loaded from dump.rdb*
  
#### Start Ollama LLM in root directory:

    ollama serve
  
#### Run Spring Boot framework in 'microservices':
    cd microservices
    mvn spring-boot:run
*Should see "Started SpringBootApp in x seconds..."*

    
#### Run program in root directory with: 
    ./script.sh

*Takes a while to run as we run comprehensive unit tests for each framework. In the end our program will print out common Issues from `IssueList1` and `IssueList2`.*


*(Some notes: For some reason we get an empty result when ollama runs too much, perhaps ollama saves our... )*



