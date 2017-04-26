#!/bin/bash

if [ -z "$1" ]; then
   echo "./dir <topics> <collection>"
   exit 1;
fi
topics=$1

if [ -z "$2" ]; then
   echo "./dir <topics> <collection>"
   exit 1;
fi
col=$2

base=/data/bioCaddie
mkdir -p output/dir/$col/$topics
for mu in 100 250 500 750 1000 2000 3000 4000 5000 6000 7000 8000 9000 10000
do
   IndriRunQuery -index=$base/indexes/biocaddie_all/ -trecFormat=true -rule=method:dir,mu:$mu $base/queries/queries.$col.$topics > output/dir/$col/$topics/$mu.out
done
