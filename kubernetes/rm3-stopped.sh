#!/bin/bash

if [ -z "$1" ]; then
   echo "./rm3-stopped.sh <topics> <collection>"
   exit 1;
fi
topics=$1

if [ -z "$2" ]; then
   echo "./rm3-stopped.sh <topics> <collection>"
   exit 1;
fi
col=$2

# NOTE: These are paths internal to the container
base=/data/biocaddie
src_base=/root/biocaddie
for mu in 50 250 500 1000 2500 5000 10000
do
   for fbTerms in 5 10 20 50
   do
      for fbDocs in 5 10 20 50
      do
         for fbOrigWeight in  0.0 0.1 0.2 0.3 0.4 0.5 0.6 0.7 0.8 0.9 1.0
         do
#             echo "IndriRunQuery -index=$base/indexes/biocaddie_all/ -trecFormat=true -rule=method:dir,mu:$mu -fbDocs=$fbDocs -fbTerms=$fbTerms -fbOrigWeight=$fbOrigWeight $base/queries/queries.$col.$topics data/stoplist.indri.params > output/rm3-stopped/$col/$topics/mu=$mu:fbTerms=$fbTerms:fbDocs=$fbDocs:fbOrigWeight=$fbOrigWeight.out"
             cat kubernetes/job.yaml \
                    | sed -e "s#{{[ ]*name[ ]*}}#$topics-$col-rm3-stopped-$mu-$fbTerms-$fbDocs-$fbOrigWeight#g" \
                    | sed -e "s#{{[ ]*index[ ]*}}#$base/indexes/biocaddie_all/#" \
                    | sed -e "s#{{[ ]*queries[ ]*}}#$src_base/queries/queries.$col.$topics#" \
                    | sed -e "s#{{[ ]*stoplist[ ]*}}#$src_base/data/stoplist.indri.params#" \
                    | sed -e "s#{{[ ]*output[ ]*}}#$src_base/output/rm3-stopped/$col/$topics/mu=$mu:fbTerms=$fbTerms:fbDocs=$fbDocs:fbOrigWeight=$fbOrigWeight.out#" \
                    | sed -e "s#{{[ ]*args[ ]*}}#-rule=method:dir,mu:$mu -fbDocs=$fbDocs -fbTerms=$fbTerms -fbOrigWeight=$fbOrigWeight#" \
                    | kubectl create -f -
         done
      done
   done
done
