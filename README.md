# NDS bioCADDIE Pilot 

<img src="https://github.com/craig-willis/ndslabs/blob/master/docs/images/logos/NDS-badge.png" width="100" alt="NDS"> <img src="https://biocaddie.org/sites/default/files/biocaddie-logo.png" alt="bioCADDIE">

This repository contains code for the NDS [bioCADDIE pilot](https://biocaddie.org/expansion-models-biomedical-data-search) and submission to the [2016 bioCADDIE Dataset Retrieval Challenge](biocaddie.org/biocaddie-2016-dataset-retrieval-challenge). The pilot project explores expansion models for biomedical data search focusing on query expansion using external collections (e.g., PubMed, Wikipedia), document expansion, and document priors based on dataset source repository.

This repository includes:

* Scripts to run [baseline models](/baselines) under Indri (QL, OKAPI, RM3) and Lucene (BM25, Rocchio) baseline models for the bioCADDIE, OHSUMED, and TREC Genomics test collections.
* Scripts to run [PubMed](/pubmed) and [Wikipedia](/wikipedia) expansion models, both RM3 and Rocchio, for bioCADDIE, OHSUMED and TREC Genomics collections.
* Implementation of the [Rocchio algorithm for use with Lucene](https://github.com/uiucGSLIS/ir-tools/blob/master/src/main/java/edu/gslis/lucene/expansion/Rocchio.java) via the [ir-tools](https://github.com/uiucGSLIS/ir-tools/) toolkit. A separate [Rocchio plugin for ElasticSearch](https://github.com/nds-org/elasticsearch-queryexpansion-plugin) is also available as a product of this pilot.
* Scripts to create [Indri](/index), [Lucene](/index), and [ElasticSearch](/elasticsearch) indexes for bioCADDIE and PubMed data.
* Scripts to run [baselines using Kubernetes](/kubernetes) with associated Docker images.
* Source [queries](/queries) used for evaluation.
* Scripts to generate [evalaution output, leave-one-query-out cross validation, and statistical comparison](/scripts). 
* Java source code for expansion model generation, PubMed ingest, and cross-validation


## Prerequisites

The pilot code in this repository requires the following software:
* Indri 5.8 with JNI support (liblemur.so, liblemur_jni.so, libindri.so, libindri_jni.so)
* Java 1.8 (JDK) with Maven
* R

This submission relies on the [ir-tools](https://github.com/uiucGSLIS/ir-tools) framework maintained by Miles Efron's lab at the University of Illinois at Urbana-Champaign.

You can either install prerequisites or use our provided Docker container.

## Install prerequisites
The following instructions assume an Ubuntu system running as root user:

```bash
apt-get update
apt-get install openjdk-8-jdk-headless maven
apt-get install r-base
apt-get install build-essential git parallel vim wget zlibc zlib1g zlib1g-dev
```

Build and install Indri:
```bash
cd /usr/local/src
wget https://sourceforge.net/projects/lemur/files/lemur/indri-5.8/indri-5.8.tar.gz/download -O indri-5.8.tar.gz
tar xvfz indri-5.8.tar.gz
cd indri-5.8
./configure --enable-java --with-javahome=/usr/lib/jvm/java-8-openjdk-amd64
make 
make install
```

Build and install trec_eval:
```bash
cd /usr/local/src
wget http://trec.nist.gov/trec_eval/trec_eval_latest.tar.gz
tar xvfz trec_eval_latest.tar.gz
cd trec_eval.9.0/
make
make install
```

## Run Docker image
Instead of installing the prerequisites on your system, the provided Docker image contains all of the required dependencies. The following example assumes that you've downloaded the BioCADDIE benchmark data to /data/biocaddie.

```bash
docker run -it -v /data/biocaddie:/data/biocaddie -v /data/pubmed:/data/pubmed ndslabs/indri bash
```

## Clone this repository and build artifacts

Download and install the ir-tools and indri libraries (Note: we're working to [add these to the Maven Central repository](https://opensource.ncsa.illinois.edu/jira/browse/NDS-849)):
```bash
wget https://github.com/nds-org/biocaddie/releases/download/v0.1/ir-utils-0.0.1-SNAPSHOT.jar
mvn install:install-file -Dfile=ir-utils-0.0.1-SNAPSHOT.jar -DgroupId=edu.gslis -DartifactId=ir-utils -Dversion=0.0.1-SNAPSHOT -Dpackaging=jar
mvn install:install-file -Dfile=/usr/local/share/indri/indri.jar -DgroupId=indri -DartifactId=indri -Dversion=5.8 -Dpackaging=jar
```

```bash
cd ~
git clone https://github.com/nds-org/biocaddie
cd biocaddie
mvn install
```

## Replication steps

This section describes the steps to repeat our 2016 BioCADDIE challenge submissions. The basic steps are:

* [Convert benchmark json data to trectext format](/index)
* [Build biocaddie Indri index](/index)
* [Run baseline models using Indri](/baselines)
* [Convert PubMed collection to trectext format](/index)
* [Build pubmed Indri index](/index)
* [Run PubMed expansion models](/pubmed)
* [Run models using repository priors](/priors)

## Qrels and queries

Please see the [queries](/queries) directory for information about the different query formats used for evaluation.

## Cross-validation

The ``mkeval.sh`` script generates ``trec_eval`` output files for each parameter combination and then runs leave-one-query-out cross validation (loocv) on the results, optimizing for specific metrics.

```bash
scripts/mkeval.sh <model> <topics> <collection>
```

The loocv process optimizes for the following metrics: map, ndcg, P_20, ndcg_cut_20.  This process generates one output file per metric for the model/collection/topics.  For example:

``loocv/model.collection.topics.metric.out``

The output file is formatted as:
``<query>	<parameter combination> 	<metric value>``

## Comparing model output 
To compare models, use the ``compare.R`` script. This runs a paired one-tailed t-test comparing two models and outputs the metrics averages and p-value: 

```bash
Rscript scripts/compare.R <collection> <from model> <to model> <topics>
```

For example:
```bash
Rscript scripts/compare.R combined tfidf dir short
```

This will report the p-values of a paired, one-tailed t-test with the alternative hypothesis that <to model> is greater than <from model>.

The model comparisons can be used to select the best model from the training data.  Model parameter estimates must be determined from the LOOCV output.


