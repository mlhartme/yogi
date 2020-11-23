#!/bin/sh
set -e
echo "copying ..."
docker save yogi:latest | bzip2 | ssh bachcraft.schmizzolin.de 'bunzip2 | docker load'
ssh root@bachcraft.schmizzolin.de systemctl restart yogi
