#!/bin/bash

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

base=/data/bioCaddie

mkdir -p output/wikipedia/$col/$topics
find queries/wikipedia/$col/$topics -type f | while read file
do
    for mu in 100 500 1000 2500
    do
        fileName=`basename $file`
        echo "IndriRunQuery -index=$base/indexes/biocaddie_all -rule=method:dir,mu:$mu -trecFormat $file > output/wikipedia/$col/$topics/$fileName,dir-mu:$mu"
    done
done

# pubmed/runqueries.sh short combined | parallel -j 10 bash -c "{}"
