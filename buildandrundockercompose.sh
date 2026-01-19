#!/bin/bash
echo ".................................................................."
echo "Stop preview docker images ..."
# Use -r to avoid error if no containers are running
docker ps -q | xargs -r docker stop

echo "Building project with Java 21..."
export JAVA_HOME=/usr/local/graalvm-community-openjdk-21.0.2+13.1
export PATH=$JAVA_HOME/bin:$PATH
mvn clean package -Pproduction

echo "Run docker-compose up -d --build ..."

docker-compose up -d --build

echo ".................................................................."