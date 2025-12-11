# How to run our project
### 1. Ensure you have downloaded OLLAMA and loaded the deepcoder:1.5B model.
### 2. Ensure the 'dump.rdb' file is present in the main-app folder.
### 3. Run the following commands on four different terminals:
  
    1. In 'main-app', run the 'redis-server' command. Ensure the server is initialized and wait until Redis has finished loading RDB.
  
    2. In root dir, run the 'ollama serve' command to start ollama.
  
    3. In 'microservices', run 'mvn spring-boot:run'
    
    4. In root dir, run './script.sh'. The command may take a while to run as we run comprehensive unit tests for each framework. In the end, our program will print out common Issues from `IssueList1` and `IssueList2`.

Some notes:
For some reason we get an empty result when ollama runs too much, perhaps ollama saves our... 



