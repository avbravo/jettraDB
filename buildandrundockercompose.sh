#!/bin/bash
echo "Building project ..."
mvn clean package
echo "Stop preview docker images ..."
docker stop $(docker ps -q)
echo "Run  docker-compose up -d..."

docker-compose up -d