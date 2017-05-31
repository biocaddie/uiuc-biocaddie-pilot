#!/bin/bash

# Embarassing script to get the repo associated with each qrel
for line in `cat qrels/biocaddie.qrels.combined | sed 's/  */,/g'`;
do
    id=`echo $line | cut -f3 -d,`;
    rel=`echo $line | cut -f4 -d,`;
    di=`dumpindex /data/biocaddie/indexes/biocaddie_all di docno $id`;
    repo=`dumpindex /data/biocaddie/indexes/biocaddie_all dt $di | grep "<REPOSITORY>" | sed 's/<[^>]*>//g'`
echo $id,$rel,$repo;
done
