#!/bin/sh
echo "copying ..."
docker save yogi:latest | bzip2 | ssh mops 'bunzip2 | docker load'
export DOCKER_HOST=mops:2375
docker-compose -f mops-compose.yaml down
docker-compose -f mops-compose.yaml up -d
docker-compose -f mops-compose.yaml logs -f
