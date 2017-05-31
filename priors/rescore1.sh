
input=$1
output=$2

mkdir -p $output

find $input -type f | while read file
do
   filename=`basename $file`
   scripts/run.sh edu.gslis.biocaddie.util.BiocaddiePrior1 -input $file -output $output/$filename -qrels qrels/biocaddie.qrels.combined -run prior1 -source /data/willis8/bioCaddie//data/biocaddie-doc-repo.out
done
