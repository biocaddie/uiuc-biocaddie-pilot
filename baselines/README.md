# Baseline models

This directory contains scripts to run the baseline models on each test collection (bioCADDIE, OHSUMED, TREC CDS, TREC Genomics).  

* ```indri```: Scripts to run Indri baseline models using the bioCADDIE test collection.
* ```lucene```: Scripts to run Lucene baseline models using the bioCADDIE test collection.
* ```ohsumed```: Scripts to run Indri baseline models using the OHSUMED test collection.
* ```trecdds```: Scripts to run Indri baseline models using the TREC CDS test collection.
* ```genomics```: Scripts to run Indri baseline models using the TREC Genomics test collection.

The provided scripts define the parameter values for each model and generate output intended to be submitted to the GNU ```parallel``` program.

To run these scripts using GNU ```parallel```: 
```bash
baselines/<model>.sh <topics> <collection> | parallel -j <jobs> bash -c "{}"
```

Where ``<topics>`` is one of ``short, stopped, orig`` and collection is one of ``train, test, combined``. For example:
```bash
baselines/dir.sh short test | parallel -j 20 bash -c "{}"
```

This will produce an output directory ``output/dir/test/short`` containing one output file per parameter combination in TREC format.

## Indri baseline models
The following baseline models are used with the Indri runs:
* ```dir```: Query likelihood retrieval model with Dirichlet smoothing
* ```jm```: Query likelhood retrieval model with Jelinek-Mercer smoothing
* ```two```: Query likelhood retrieval model with twos-stage smoothing
* ```okapi```: OKAPI BM25 retrieval model
* ```tfidf```: Indri's default TFIDF baseline
* ```rm3```: Relevance model retrieval with original query interpolation
* ```okapi-exp```: OKAPI BM25 retrieval model with Rocchio expansion

Addition baseline variations are available using  ```krovetz``` stemming and ```stopword``` removal.

## Lucene baseline models
* ```dir```: Lucene's query likelihood retrieval model with Dirichlet smoothing
* ```jm```: Lucene's  query likelhood retrieval model with Jelinek-Mercer smoothing
* ```tfidf``: Lucene's classic TFIDF retrieval model
* ```bm25```: Lucene's BM25 retrieval model
* ```rocchio```: BM25+Rocchio expansion using our implementation

Addition baseline variations are available using Lucene's ```snowball``` stemming and ```stopword``` removal.
