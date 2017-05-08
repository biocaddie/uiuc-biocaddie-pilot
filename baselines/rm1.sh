#!/bin/bash

if [ -z "$1" ]; then
   echo "./rm1.sh <topics> <collection>"
   exit 1;
fi
topics=$1

if [ -z "$2" ]; then
   echo "./rm1.sh <topics> <collection>"
   exit 1;
fi
col=$2

base=/data/biocaddie
mkdir -p output/rm1/$col/$topics
for mu in 100 250 500 750 1000 2000 3000 
do
   for fbTerms in 10 25 50 75 100
   do 
      for fbDocs in 10 25 50 75 100
      do
         echo "IndriRunQuery -index=$base/indexes/biocaddie_all/ -trecFormat=true -rule=method:dir,mu:$mu -fbDocs=$fbDocs -fbTerms=$fbTerms  $base/queries/queries.$col.$topics > output/rm1/$col/$topics/mu=$mu:fbTerms=$fbTerms:fbDocs=$fbDocs.out"
      done
   done
done

