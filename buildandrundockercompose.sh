#!/bin/bash
echo ".................................................................."
echo "Stopping existing project containers..."
docker-compose down

echo "Building project from root..."
# Ensure we build everything to resolve local dependencies
if mvn clean install -DskipTests=true; then
    echo "Build successful. Starting containers..."
    docker-compose up -d --build
else
    echo "BUILD FAILED! Not starting containers."
    exit 1
fi

echo ".................................................................."