#!/bin/sh
set -e
main=$1
override=$2
if [ ! -f $override ] ; then
  echo "no override"
  exit 0
fi
tmp=tmp.yaml
yq eval-all '. as $item ireduce ({}; . * $item)' "${main}" $override >$tmp
cp $tmp ${main}
rm $tmp
echo "merge saved to ${main}."
