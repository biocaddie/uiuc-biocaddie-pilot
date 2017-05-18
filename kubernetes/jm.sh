#!/bin/bash

if [ -z "$1" ]; then
   echo "./jm.sh <topics> <collection>"
   exit 1;
fi
topics=$1

if [ -z "$2" ]; then
   echo "./jm.sh <topics> <collection>"
   exit 1;
fi
col=$2

# NOTE: These are paths internal to the container
base=/data/biocaddie
src_base=/root/biocaddie
for lambda in 0.0 0.1 0.2 0.3 0.4 0.5 0.6 0.7 0.8 0.9 1.0
do
#   echo "IndriRunQuery -index=$base/indexes/biocaddie_all/ -trecFormat=true -rule=method:jm,lambda:$lambda queries/queries.$col.$topics > output/jm/$col/$topics/lambda=$lambda"
   cat kubernetes/job.yaml \
          | sed -e "s#{{[ ]*name[ ]*}}#$topics-$col-jm-$lambda#g" \
          | sed -e "s#{{[ ]*index[ ]*}}#$base/indexes/biocaddie_all/#" \
          | sed -e "s#{{[ ]*queries[ ]*}}#$src_base/queries/queries.$col.$topics#" \
          | sed -e "s#{{[ ]*output[ ]*}}#$src_base/output/jm/$col/$topics/$lambda.out#" \
          | sed -e "s#{{[ ]*args[ ]*}}#-rule=method:jm,lambda:$lambda#" \
          | kubectl create -f -
done
