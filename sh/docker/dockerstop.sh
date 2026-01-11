#!/bin/bash
echo "Deteniendo los contenedores de docker..."
docker ps -q | xargs -r docker stop
