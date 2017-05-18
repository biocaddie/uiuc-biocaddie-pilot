#!/bin/bash

if [ -z "$1" ]; then
   echo "./two.sh <topics> <collection>"
   exit 1;
fi
topics=$1

if [ -z "$2" ]; then
   echo "./two.sh <topics> <collection>"
   exit 1;
fi
col=$2

# NOTE: These are paths internal to the container
base=/data/biocaddie
src_base=/rot/biocaddie
for mu in 50 250 500 1000 2500 5000 10000
do
   for lambda in 0.0 0.1 0.2 0.3 0.4 0.5 0.6 0.7 0.8 0.9 1.0
   do
#       echo "IndriRunQuery -index=$base/indexes/biocaddie_all/ -trecFormat=true -rule=method:two,mu:$mu,lambda:$lambda queries/queries.$col.$topics > output/two/$col/$topics/mu=$mu:lambda=$lambda.out"
       cat kubernetes/job.yaml \
          | sed -e "s#{{[ ]*name[ ]*}}#$topics-$col-two-$mu-$lambda#g" \
          | sed -e "s#{{[ ]*index[ ]*}}#$base/indexes/biocaddie_all/#" \
          | sed -e "s#{{[ ]*queries[ ]*}}#$src_base/queries/queries.$col.$topics#" \
          | sed -e "s#{{[ ]*output[ ]*}}#$src_base/output/two/$col/$topics/mu=$mu:lambda=$lambda.out#" \
          | sed -e "s#{{[ ]*args[ ]*}}#-rule=method:two,mu:$mu,lambda:$lambda#" \
          | kubectl create -f -
   done
done
