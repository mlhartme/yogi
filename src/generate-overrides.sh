#!/bin/sh
# args: <destFile>
set -e
echo "userProperties: $(cat src/test/etc/user.properties | base64)"> $2
