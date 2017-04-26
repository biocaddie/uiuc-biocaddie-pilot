#!/bin/bash

# Embarassing script to get the repo associated with each qrel
for line in `cat qrels/biocaddie.qrels.csv`; 
do 
    id=`echo $line | cut -f3 -d,`; 
    rel=`echo $line | cut -f4 -d,`; 
    di=`dumpindex index/biocaddie di docno $id`;
    repo=`dumpindex index/biocaddie dt $di | grep REPO`;
echo $id,$rel,$repo; 
done

