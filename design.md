# bioCADDIE Pilot Design

The NDS bioCADDIE pilot consists of the following components:

* Indri, Lucene, and ElasticSearch indexes and index creation scripts/configuration files.
* Java utility classes to convert the bioCADDIE and PubMed data to TREC text format.
* [ir-tools](https://github.com/uiucGSLIS/ir-tools) framework for evaluation which supports evaluation over both Indri and Lucene indexes and implements the RM3 and Rocchio expansion  models.
* A set of scripts to run the baseline and expansion models, sweeping parameter combinations. The scripts can be used with GNU ``parallel`` or Kubernetes for parallelization.
* Java utilility classes to generate RM3 and BM25+Rocchio queries from Indri and Lucene indexes.
* A set of scripts to generate ``trec_eval`` output for model comparison.
* Java utility class and wrapper scripts to perform leave-one-query-out cross-validation for parameter estimation, optimizing for multiple metrics.
* ``R`` scripts to compare retrieval models using a one-tailed t-test.
* A prototype ElasticSearch plugin that implements [BM25+Rocchio expansion](https://github.com/nds-org/elasticsearch-queryexpansion-plugin).
* A prototype ElasicSearch plugin that implements the [repository prior calculation](https://github.com/nds-org/elasticsearch-queryexpansion-plugin).
