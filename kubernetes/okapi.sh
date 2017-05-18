#!/bin/bash

if [ -z "$1" ]; then
   echo "./okapi.sh <topics> <collection>"
   exit 1;
fi
topics=$1

if [ -z "$2" ]; then
   echo "./okapi.sh <topics> <collection>"
   exit 1;
fi
col=$2

# NOTE: These are paths internal to the container
base=/data/biocaddie
src_base=/root/biocaddie
for b in 0.0 0.1 0.2 0.3 0.4 0.5 0.6 0.7 0.8 0.9 1.0 
do
   for k1 in 1.0 1.2 1.5 1.7 2.0
   do 
      for k3 in 1.0 1.2 1.5 1.7 2.0
      do 
#         echo "IndriRunQuery -index=$base/indexes/biocaddie_all/ -trecFormat=true -baseline=okapi,k1:$k1,k3:$k3,b:$b queries/queries.$col.$topics > output/okapi/$col/$topics/k1=$k1:k3=$k3:b=$b.out"
         cat kubernetes/job.yaml \
             | sed -e "s#{{[ ]*name[ ]*}}#$topics-$col-okapi-$k1-$k3-$b#g" \
             | sed -e "s#{{[ ]*index[ ]*}}#$base/indexes/biocaddie_all/#" \
             | sed -e "s#{{[ ]*queries[ ]*}}#$src_base/queries/queries.$col.$topics#" \
             | sed -e "s#{{[ ]*stoplist[ ]*}}##" \
             | sed -e "s#{{[ ]*output[ ]*}}#$src_base/output/okapi/$col/$topics/k1=$k1:k3=$k3:b$b.out#" \
             | sed -e "s#{{[ ]*args[ ]*}}#-baseline=okapi,k1:$k1,k3:$k3,b:$b#" \
             | kubectl create -f -
      done
   done
done

