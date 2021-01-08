#!/bin/sh
#
# Run yogi locally
echo "Starting Yogi ..."
mkdir -p target/run
docker run -it -p 8080:8080 --mount type=bind,source="$(pwd)"/src/test/etc,target=/usr/local/yogi/etc --mount type=bind,source="$(pwd)"/target/run,target=/usr/local/yogi/run yogi
