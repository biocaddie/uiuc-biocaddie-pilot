# Convert PMC OA data to TREC-text format

for gzfile in `ls /data0/pubmed/oa_bulk/*gz`
do
   file=`basename $gzfile .tar.gz`
   ./run.sh edu.gslis.biocaddie.util.PMCToTrecText -input $gzfile -output /data0/pubmed/trecText/$file &
   #gzip /data0/pubmed/trecText/$file
done
