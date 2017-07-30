# Indexes

## Converting data

This section describes the steps required to convert the bioCADDIE benchmark and test collection data for indexing.

### bioCADDIE benchmark data

Convert bioCADDIE benchmark data to trectext format.

Download the [BioCADDIE benchmark collection in JSON format](https://biocaddie.org/sites/default/files/update_json_folder.zip).
```bash
mkdir -p /data/biocaddie/data
cd /data/biocaddie/data
wget https://biocaddie.org/sites/default/files/update_json_folder.zip
```

Convert data to TREC-text format:
```bash
cd ~/biocaddie
scripts/dats2trec.sh
```

Note: You may see the following error, which is expected:
```bash
java.lang.ClassCastException: com.google.gson.JsonNull cannot be cast to com.google.gson.JsonObject
	at edu.gslis.biocaddie.util.DATSToTrecText.main(DATSToTrecText.java:61)
```

This converts the benchmark data to trectext format.  This produces a file ``/data/biocaddie/data/biocaddie_all.txt``. You can remove the original benchmark data, if desired.

Build the indexes (see below).

## PubMed Open Access data

Converting PubMed data to trectext.

Download the PubMed oa_bulk datasets to ``/data/pubmed/oa_bulk``:

```bash
mkdir -p /data/pubmed/oa_bulk
cd /data/pubmed/oa_bulk
wget ftp://ftp.ncbi.nlm.nih.gov/pub/pmc/oa_bulk/non_comm_use.0-9A-B.txt.tar.gz
wget ftp://ftp.ncbi.nlm.nih.gov/pub/pmc/oa_bulk/non_comm_use.C-H.txt.tar.gz
wget ftp://ftp.ncbi.nlm.nih.gov/pub/pmc/oa_bulk/non_comm_use.I-N.txt.tar.gz
wget ftp://ftp.ncbi.nlm.nih.gov/pub/pmc/oa_bulk/non_comm_use.O-Z.txt.tar.gz
```

```bash
cd ~/biocaddie
scripts/pmc2trec.sh
```

`.


## Building indexes

This section describes the steps required to build Indri, Lucene and ElasticSearch indexes

### bioCADDIE Indri

Use ``IndriBuildIndex`` to build the ``biocaddie_all`` index (customize paths as needed):
```bash
mkdir -p /data/biocaddie/indexes
cd ~/biocaddie
IndriBuildIndex index/build_index.biocaddie.params
```

The following command will build a Krovetz-stemmed index:
```bash
mkdir -p /data/biocaddie/indexes
cd ~/biocaddie
IndriBuildIndex index/build_index.biocaddie.krovetz.params
```

### bioCADDIE Lucene

The following command will build a Lucene 6.5 index with no stemming:
```bash
mkdir -p /data/biocaddie/lucene/
cd ~/biocaddie
scripts/run.sh edu.gslis.lucene.main.LuceneBuildIndex index/lucene_biocaddie.yaml
```

The following command will build a Lucene 6.5 index with no snowball stemming:
```bash
mkdir -p /data/biocaddie/lucene/
cd ~/biocaddie
scripts/run.sh edu.gslis.lucene.main.LuceneBuildIndex index/lucene_biocaddie_snowball.yaml
```

### bioCADDIE ElasticSearch

This assumes a running ElasticSearch instance:
```bash
cd ~/biocaddie/elasticsearch/biocaddie
./create-index.sh
./index-biocaddie.sh
```

### PubMed Indri

This produces output in ``/data/pubmed/trecText/`` containing the TREC-formatted documents.

Create the pubmed index:
```bash
mkdir -p /data/pubmed/indexes
cd ~/biocaddie
IndriBuildIndex index/build_index.pubmed.params
```

This will create an Indri index in ``/data/pubmed/indexes/pubmed``


### PubMed Lucene

The following command will build a Lucene 6.5 index with no stemming:
```bash
mkdir -p /data/biocaddie/lucene/
cd ~/biocaddie
scripts/run.sh edu.gslis.lucene.main.LuceneBuildIndex index/lucene_pubmed.yaml
```


