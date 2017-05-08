#!/bin/bash

if [ -z "$1" ]; then
   echo "./gensd.sh <topics> <collection>"
   exit 1;
fi
topics=$1

if [ -z "$2" ]; then
   echo "./gensd.sh <topics> <collection>"
   exit 1;
fi
col=$2

base=/data/bioCaddie
mkdir -p output/sdm/$col/$topics
mkdir -p queries/sdm/$col/$topics

for row in `cat sdm/weights.txt`
do
   IFS=',' read -r -a weights <<< "$row"

   scripts/dm.pl queries/queries.$col.$topics sd ${weights[0]} ${weights[1]} ${weights[2]} > queries/sdm/$col/$topics/sdm-${weights[0]}-${weights[1]}-${weights[2]}
done
