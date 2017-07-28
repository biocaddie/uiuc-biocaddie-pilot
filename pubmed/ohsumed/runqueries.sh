#!/bin/bash

#
# Run the PubMed RM3 queries against the biocaddie index
#

if [ -z "$1" ]; then
   echo "./runqueries.sh <topics> <collection>"
   exit 1;
fi
topics=$1

if [ -z "$2" ]; then
   echo "./runqueries.sh <topics> <collection>"
   exit 1;
fi
col=$2

base=/data/ohsumed

mkdir -p ohsumed-output/pubmed-rm3/$col/$topics
find $base/queries/pubmed-rm3/$col/$topics -type f | while read file
do
    for mu in 50 250 500 1000 2500 5000 10000
    do
        fileName=`basename $file`
        echo "IndriRunQuery -index=$base/indexes/ohsumed_all -rule=method:dir,mu:$mu -trecFormat $file > ohsumed-output/pubmed-rm3/$col/$topics/$fileName,dir-mu:$mu"
    done
done

# pubmed/indri/runqueries.sh short combined | parallel -j 10 bash -c "{}"
