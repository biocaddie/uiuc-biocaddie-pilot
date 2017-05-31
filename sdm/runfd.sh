#!/bin/bash

if [ -z "$1" ]; then
   echo "./runfd.sh <topics> <collection>"
   exit 1;
fi
topics=$1

if [ -z "$2" ]; then
   echo "./runfd.sh <topics> <collection>"
   exit 1;
fi
col=$2

base=/data/biocaddie

mkdir -p output/fdm/$col/$topics
find queries/fdm/$col/$topics -type f | while read file
do
    for mu in 50 250 500 1000 2500 5000 10000
    do
        fileName=`basename $file`
        if [ ! -f "output/fdm/$col/$topics/$fileName,dir-mu:$mu" ]
        then 
            echo "IndriRunQuery -index=$base/indexes/biocaddie_all -rule=method:dir,mu:$mu -trecFormat $file > output/fdm/$col/$topics/$fileName,dir-mu:$mu"
        fi 
    done
done

#sdm/runqueries.sh short combined | parallel -j 20 bash -c "{}" &
