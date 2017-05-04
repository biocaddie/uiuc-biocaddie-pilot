#!/bin/bash

if [ -z "$1" ]; then
   echo "./genqueries.sh <topics> <collection>"
   exit 1;
fi
topics=$1

if [ -z "$2" ]; then
   echo "./genqueries.sh <topics> <collection>"
   exit 1;
fi
col=$2

base=/data/bioCaddie
mkdir -p output/fdm/$col/$topics
mkdir -p queries/fdm/$col/$topics

for row in `cat sdm/weights.txt`
do
   IFS=',' read -r -a weights <<< "$row"

   scripts/dm.pl queries/queries.$col.$topics fd ${weights[0]} ${weights[1]} ${weights[2]} > queries/fdm/$col/$topics/fdm-${weights[0]}-${weights[1]}-${weights[2]}
done
