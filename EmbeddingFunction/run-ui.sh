#!/bin/bash

# Build the project
echo "Building the project..."
mvn clean package

# Run the UI server
echo "Starting the UI server..."
java -cp target/user-embedding-service-1.0-SNAPSHOT.jar com.sample.WebServer

# Open the browser (optional)
# open http://localhost:8080 