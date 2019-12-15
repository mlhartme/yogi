#!/bin/sh
echo "copying ..."
docker save yogi:latest | bzip2 | ssh mops 'bunzip2 | docker load'
echo "starting ..."
export DOCKER_HOST=mops:2375
echo host: $DOCKER_HOST
docker-compose -f mops-compose.yaml down
docker-compose -f mops-compose.yaml up
docker-compose -f mops-compose.yaml logs
