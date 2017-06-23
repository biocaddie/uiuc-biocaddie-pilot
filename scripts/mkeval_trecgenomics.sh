#!/bin/bash

if [ -z "$1" ]; then
   echo "./mkeval_trecgenomics.sh <model> <topics> <collection>"
   exit 1;
fi
model=$1

if [ -z "$2" ]; then
   echo "./mkeval_trecgenomics.sh <model> <topics> <collection>"
   exit 1;
fi
topics=$2

if [ -z "$3" ]; then
   echo "./mkeval_trecgenomics.sh <model> <topics> <collection>"
   exit 1;
fi
col=$3

qrels=/data/trecgenomics/qrels/trecgenomics-qrels-nondup.txt

# Calculate metrics using trec_eval
for file in `find /data/trecgenomics/output/$model/$col/$topics -type f -size +0`;
do
    basename=`basename $file .out`;
    mkdir -p /data/trecgenomics/eval/$model/$col/$topics
    trec_eval -c -q -m all_trec $qrels $file > /data/trecgenomics/eval/$model/$col/$topics/$basename.eval;
done

# Leave on query out cross-validation
mkdir -p /data/trecgenomics/loocv
for metric in map ndcg ndcg_cut_5 ndcg_cut_10 ndcg_cut_20 ndcg_cut_100 P_5 P_10 P_20 P_100
do
    scripts/run.sh edu.gslis.biocaddie.util.CrossValidation -input /data/trecgenomics/eval/$model/$col/$topics -metric $metric -output /data/trecgenomics/loocv/$model.$col.$topics.$metric.out
done
