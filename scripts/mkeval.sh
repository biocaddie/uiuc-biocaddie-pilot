#!/bin/bash

if [ -z "$1" ]; then
   echo "./mkeval.sh <model> <topics> <collection>"
   exit 1;
fi
model=$1

if [ -z "$2" ]; then
   echo "./mkeval.sh <model> <topics> <collection>"
   exit 1;
fi
topics=$2

if [ -z "$3" ]; then
   echo "./mkeval.sh <model> <topics> <collection>"
   exit 1;
fi
col=$3

qrels=/data/bioCaddie/qrels/biocaddie.qrels.$col

# Calculate metrics using trec_eval
for file in `find output/$model/$col/$topics -type f -size +0`;
do
    basename=`basename $file .out`;
    mkdir -p eval/$model/$col/$topics
    trec_eval -c -q -m all_trec $qrels $file > eval/$model/$col/$topics/$basename.eval;
done

# Leave on query out cross-validation
mkdir -p loocv
for metric in map ndcg ndcg_cut_5 ndcg_cut_10 ndcg_cut_20 P_5 P_10 P_20
do
    scripts/run.sh edu.gslis.biocaddie.util.CrossValidation -input eval/$model/$col/$topics -metric $metric -output loocv/$model.$col.$topics.$metric.out
done
