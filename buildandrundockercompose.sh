#!/bin/bash
echo ".................................................................."
echo "Stop preview docker images ..."
# Use -r to avoid error if no containers are running
docker ps -q | xargs -r docker stop

echo "Building project ..."
mvn clean package

echo "Run docker-compose up -d --build ..."

docker-compose up -d --build

echo ".................................................................."