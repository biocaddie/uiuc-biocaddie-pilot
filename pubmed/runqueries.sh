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

mkdir -p output/pubmed/$col/$topics
echo queries/pubmed/$col/$topics
find queries/pubmed/$col/$topics -type f | while read file
do
    fileName=`basename $file`
    IndriRunQuery -index=$base/indexes/biocaddie_all -trecFormat $file > output/pubmed/$col/$topics/$fileName
done
