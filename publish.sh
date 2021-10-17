#!/bin/sh
set -e
echo "copying ..."
docker save yogi:latest | bzip2 | ssh mops.schmizzolin.de 'bunzip2 | docker load'
ssh root@mops.schmizzolin.de systemctl restart yogi
