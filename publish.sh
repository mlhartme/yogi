#!/bin/sh
set -e
echo "copying ..."
docker save yogi:latest | bzip2 | ssh mops 'bunzip2 | docker load'
ssh root@mops systemctl restart yogi
