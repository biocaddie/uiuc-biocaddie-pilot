#!/bin/bash

if [ -z "$1" ]; then
   echo "./tfidfexp.sh <topics> <collection>"
   exit 1;
fi
topics=$1

if [ -z "$2" ]; then
   echo "./tfidfexp.sh <topics> <collection>"
   exit 1;
fi
col=$2

base=/data/bioCaddie
mkdir -p output/tfidfexp/$col/$topics
for b in 0.0 0.1 0.2 0.3 0.4 0.5 0.6 0.7 0.8 0.9 1.0 
do
   for k1 in 0 0.2 0.5 0.7 1.0 1.2 1.5 1.7 2.0
   do 
      for fbDocs in 10 25 50 75 100
      do
          echo "IndriRunQuery -index=$base/indexes/biocaddie_all/ -trecFormat=true -baseline=tfidf,k1:$k1,b:$b -fbDocs=$fbDocs queries/queries.$col.$topics > output/tfidfexp/$col/$topics/k1=$k1:b=$b:fbDocs=$fbDocs.out"
      done
   done
done
