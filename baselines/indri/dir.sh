#!/bin/bash

if [ -z "$1" ]; then
   echo "./dir.sh <topics> <collection>"
   exit 1;
fi
topics=$1

if [ -z "$2" ]; then
   echo "./dir.sh <topics> <collection>"
   exit 1;
fi
col=$2

base=/data/biocaddie
mkdir -p output/dir/$col/$topics
for mu in 50 250 500 1000 2500 5000 10000
do
   echo "IndriRunQuery -index=$base/indexes/biocaddie_all/ -trecFormat=true -rule=method:dir,mu:$mu queries/queries.$col.$topics > output/dir/$col/$topics/$mu.out"
done
