#!/bin/bash

#
# Run the PubMed RM3 queries against the biocaddie index
#

base=/data/trecgenomics

mkdir -p genomics-output/wikipedia-rm3/2006
find $base/queries/wikipedia-rm3/2006 -type f | while read file
do
    for mu in 50 250 500 1000 2500 5000 10000
    do
        fileName=`basename $file`
        echo "IndriRunQuery -index=$base/indexes/trecgenomics2006_all -rule=method:dir,mu:$mu -trecFormat $file > genomics-output/wikipedia-rm3/2006/$fileName,dir-mu:$mu"
    done
done

# pubmed/indri/runqueries.sh short combined | parallel -j 10 bash -c "{}"
