#!/bin/bash

if [ -z "$1" ]; then
   echo "./tfidf.sh <topics> <collection>"
   exit 1;
fi
topics=$1

if [ -z "$2" ]; then
   echo "./tfidf.sh <topics> <collection>"
   exit 1;
fi
col=$2

base=/data/biocaddie/
mkdir -p lucene-output/tfidf/$col/$topics
echo "scripts/run.sh edu.gslis.lucene.main.LuceneRunQuery -index $base/lucene/biocaddie_all/shard0/ -queryfile queries/queries.$col.$topics  -format indri -field text -similarity method:tfidf > lucene-output/tfidf/$col/$topics/tfidf.out"
