#!/bin/bash

if [ -z "$1" ]; then
   echo "./jm.sh <topics> <collection>"
   exit 1;
fi
topics=$1

if [ -z "$2" ]; then
   echo "./jm.sh <topics> <collection>"
   exit 1;
fi
col=$2

base=/data/bioCaddie
mkdir -p output/jm/$col/$topics
for lambda in 0.0 0.1 0.2 0.3 0.4 0.5 0.6 0.7 0.8 0.9 1.0
do
   IndriRunQuery -index=$base/indexes/biocaddie_all/ -trecFormat=true -rule=method:jm,lambda:$lambda $base/queries/queries.$col.$topics > output/jm/$col/$topics/lambda=$lambda
done
