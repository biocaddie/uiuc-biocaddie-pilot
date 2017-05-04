#!/bin/bash

if [ -z "$1" ]; then
   echo "./genrm3.sh <topics> <collection>"
   exit 1;
fi
topics=$1

if [ -z "$2" ]; then
   echo "./genrm3.sh <topics> <collection>"
   exit 1;
fi
col=$2

base=/data/bioCaddie
mkdir -p output/wikipedia/$col/$topics
mkdir -p queries/wikipedia/$col/$topics
for mu in 100 500 1000 2500
do
   for fbTerms in 5 10 20 50
   do
      for fbDocs in 5 10 20 50 
      do
         for fbOrigWeight in  0.1 0.3 0.5 0.7 0.9
         do
            scripts/run.sh edu.gslis.biocaddie.util.GetFeedbackQueries -input $base/queries/queries.$col.$topics -output queries/wikipedia/$col/$topics/queries.mu:$mu,fbTerms:$fbTerms,fbDocs:$fbDocs,fbOrigWeight:$fbOrigWeight -index /data/wikipedia/indexes/20150901/index -fbDocs $fbDocs -fbTerms $fbTerms -rmLambda $fbOrigWeight -maxResults $fbDocs -stoplist data/stoplist.all -mu $mu
         done
      done
   done
done

