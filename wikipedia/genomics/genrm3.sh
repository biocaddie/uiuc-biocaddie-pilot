#!/bin/bash

#
# Generate RM3 queries from PubMed for execution on external collection
#

base=/data/trecgenomics
mkdir -p $base/queries/wikipedia-rm3/2006
mu=2500
for fbTerms in 5 10 20 50
do
   for fbDocs in 5 10 20 50 
   do
      for fbOrigWeight in  0.0 0.1 0.2 0.3 0.4 0.5 0.6 0.7 0.8 0.9 1.0
      do
         echo "scripts/run.sh edu.gslis.biocaddie.util.GetFeedbackQueries -input $base/queries/queries.combined.orig.2006 -output $base/queries/wikipedia-rm3/2006/queries.mu:$mu,fbTerms:$fbTerms,fbDocs:$fbDocs,fbOrigWeight:$fbOrigWeight -index /data/wikipedia/indexes/20150901/index -fbDocs $fbDocs -fbTerms $fbTerms -rmLambda $fbOrigWeight -maxResults $fbDocs -stoplist data/stoplist.all -mu $mu"
      done
   done
done
