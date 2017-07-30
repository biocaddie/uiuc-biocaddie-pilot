# PubMed Expansion

This directory contains scripts required to run the PubMed expansion models. The PubMed experiments require two stages.  

First, we generate feedback queries from the PubMed index using ``edu.gslis.biocaddie.util.GetFeedbackQueries`. Second, we run those queries against the target index.

For Indri, expansion models are estimated using RM3, sweeping the model parameters (mu, fbDocs, fbTerms, lambda).

For Lucene, expansion models are estimated using Rocchio.

This section assumes that you have an existing PubMed index under ``/data/pubmed/indexes/pubmed``:

## bioCADDIE Indri

Create the RM expanded queries:
```bash
pubmed/indri/genrm3.sh short test
```
This will generate a set of expanded queries (mu=2500) under ``queries/pubmed/``. 

Now run the queries against the BioCADDIE index:
```bash
pubmed/indri/runqueries.sh short test | parallel -j <jobs> bash -c "{}"

This will generate a set of TREC-formatted output files under the usual ``output`` directory.

## bioCADDIE Lucene

Create the BM25+Rocchio expanded queries:
```bash
pubmed/lucene/genrocchio.sh short test
```
This will generate a set of expanded queries (mu=2500) under ``queries/pubmed/``. 

Now run the queries against the bioCADDIE index index:
```bash
pubmed/lucene/runqueries-lucene.sh short test | parallel -j <jobs> bash -c "{}"

This will generate a set of TREC-formatted output files under the usual ``output`` directory.
