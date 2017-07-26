#!/bin/bash

if [ -z "$1" ]; then
   echo "./bm25.sh <topics> <collection>"
   exit 1;
fi
topics=$1

if [ -z "$2" ]; then
   echo "./bm25.sh <topics> <collection>"
   exit 1;
fi
col=$2

base=/data/biocaddie/
mkdir -p lucene-output/bm25/$col/$topics
for b in 0.0 0.1 0.2 0.3 0.4 0.5 0.6 0.7 0.8 0.9 1.0
do
   for k1 in 0.2 0.4 0.6 0.8 1.0 1.2 1.5 1.7 2.0
   do
         echo "scripts/run.sh edu.gslis.lucene.main.LuceneRunQuery -index $base/lucene/biocaddie_all.6.6.0/shard0/ -queryfile queries/queries.$col.$topics  -format indri -field text -similarity method:bm25,k1:$k1,b:$b > lucene-output/bm25/$col/$topics/k1=$k1:b=$b.out"
   done
done

