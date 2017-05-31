
input=$1
output=$2

mkdir -p $output

find $input -type f | while read file
do
   filename=`basename $file`
   scripts/run.sh edu.gslis.biocaddie.util.BiocaddiePrior2 -input $file -output $output/$filename -run prior2 -numDocs 100 -source /data/willis8/bioCaddie//data/biocaddie-doc-repo.out
done
