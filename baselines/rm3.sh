#!/bin/bash

if [ -z "$1" ]; then
   echo "./rm3 <topics> <collection>"
   exit 1;
fi
topics=$1

if [ -z "$2" ]; then
   echo "./rm3 <topics> <collection>"
   exit 1;
fi
col=$2

base=/data/biocaddie
mkdir -p output/rm3/$col/$topics
for mu in 100 250 500 750 1000 2000 3000 
do
   for fbTerms in 10 25 50 75 100
   do 
      for fbDocs in 10 25 50 75 100
      do
         for fbOrigWeight in  0.1 0.2 0.3 0.4 0.5 0.6 0.7 0.8 0.9
         do
             echo "IndriRunQuery -index=$base/indexes/biocaddie_all/ -trecFormat=true -rule=method:dir,mu:$mu -fbDocs=$fbDocs -fbTerms=$fbTerms -fbOrigWeight=$fbOrigWeight $base/queries/queries.$col.$topics > output/rm3/$col/$topics/mu=$mu:fbTerms=$fbTerms:fbDocs=$fbDocs:fbOrigWeight=$fbOrigWeight.out"
         done
      done
   done
done

