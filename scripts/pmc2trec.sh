# Convert PMC OA data to TREC-text format

mkdir -p /data/pubmed/trecText
for gzfile in `ls /data/pubmed/oa_bulk/*gz`
do
   file=`basename $gzfile .tar.gz`
   scripts/run.sh edu.gslis.biocaddie.util.PMCToTrecText -input $gzfile -output /data/pubmed/trecText/$file
   gzip /data/pubmed/trecText/$file
done
