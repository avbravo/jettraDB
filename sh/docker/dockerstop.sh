#!/bin/bash
echo "Deteniendo los contenedores de docker..."
docker stop $(docker ps -q)
