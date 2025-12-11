#!/bin/bash

# Assumes ollama, redis and springboot are running 

cd persistence-framework
mvn clean install
# Insert mvn command to insert to local repository
cd ..

cd microservice-framework
mvn clean install
# Insert mvn command to insert to local repository
cd ..

cd microservices
mvn clean install
cd ..

cd main-app
mvn clean install
mvn exec:java
cd ..
