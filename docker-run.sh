#!/bin/sh
#
# Run yogi locally
echo "Starting Yogi ... stop with ctrl-c"
echo
echo "user.properties"
cat "$(pwd)/src/test/etc/user.properties"
echo
echo
mkdir -p .yogilib
docker run -it -p 8080:8080 --mount type=bind,source="$(pwd)"/src/test/etc,target=/usr/local/yogi/etc --mount type=bind,source="$(pwd)"/.yogilib,target=/usr/local/yogi/run yogi
