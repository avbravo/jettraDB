#!/bin/bash
echo "Building project to ensure latest driver is available..."
mvn clean install -DskipTests

echo "Compiling and running Java Load Test..."
# This assumes the JARs are in local maven repo
mvn exec:java -pl jettra-driver-java -Dexec.mainClass="io.jettra.test.JettraLoadTest"
