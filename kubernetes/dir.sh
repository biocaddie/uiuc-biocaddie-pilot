#!/bin/bash

if [ -z "$1" ]; then
   echo "./dir.sh <topics> <collection>"
   exit 1;
fi
topics=$1

if [ -z "$2" ]; then
   echo "./dir.sh <topics> <collection>"
   exit 1;
fi
col=$2

REDIS_SERVICE_HOST=10.0.0.49

# NOTE: These are paths internal to the container
base=/data/biocaddie
src_base=/root/biocaddie
mkdir -p output/dir/$col/$topics
for mu in 50 250 500 1000 2500 5000 10000
do
#   echo "IndriRunQuery -index=$base/indexes/biocaddie_all/ -trecFormat=true -rule=method:dir,mu:$mu queries/queries.$col.$topics > output/dir/$col/$topics/$mu.out"
#   cat kubernetes/job.yaml \
#          | sed -e "s#{{[ ]*name[ ]*}}#$topics-$col-dir-$mu#g" \
#          | sed -e "s#{{[ ]*index[ ]*}}#$base/indexes/biocaddie_all/#" \
#          | sed -e "s#{{[ ]*queries[ ]*}}#$src_base/queries/queries.$col.$topics#" \
#          | sed -e "s#{{[ ]*stoplist[ ]*}}##" \
#          | sed -e "s#{{[ ]*output[ ]*}}#$src_base/output/dir/$col/$topics/$mu.out#" \
#          | sed -e "s#{{[ ]*args[ ]*}}#-rule=method:dir,mu:$mu#" \
#          | kubectl create -f -

    redis-cli -h ${REDIS_SERVICE_HOST} rpush "dir/$col/$topics" "IndriRunQuery -index=$base/indexes/biocaddie_all/ -trecFormat=true -rule=method:dir,mu:$mu queries/queries.$col.$topics > output/dir/$col/$topics/$mu.out"
done
