# Krovetz

# uiuc-rm3
IndriRunQuery -index=`pwd`/index/biocaddie_all/ -runID=uiuc-rm3 -trecFormat=true  queries/queries.official.krovetz -fbDocs=10 -fbTerms=25 -fbOrigWeight=0.75 > results/official/rm3-10-25-0.75.krovetz-all.out

# uiuc-sdm
./dm.pl queries/queries.official.krovetz 0.75 0.1 0.15  > queries/sdm/queries.official.krovetz.sdm.0.75-0.1-0.15
IndriRunQuery -index=`pwd`/index/biocaddie_all.krovetz/ -runID=uiuc-sdm -trecFormat=true  queries/sdm/queries.official.krovetz.sdm.0.75-0.1-0.15 > results/official/sdm-0.75-0.1-0.15.krovetz.out

#uiuc-pmc
./run.sh edu.gslis.biocaddie.util.GetFeedbackQueries -input queries/queries.official.krovetz -output queries/queries.official.krovetz.pubmed.rm3.10-25-0.75.json -index /data0/pubmed/indexes/pubmed.krovetz -fbDocs 10 -fbTerms 25 -rmLambda 0.75 -maxResults 1000 -stoplist stoplist.all  -mu 2500

./run.sh edu.gslis.biocaddie.util.RunScorer -queries queries/queries.official.krovetz.pubmed.rm3.10-25-0.75.json -mu 2500 -index index/biocaddie_all.krovetz -stoplist stoplist.all -maxResults 1000 > results/official/pubmed.official.krovetz.10-25-0.75.out

#uiuc-prior1
./prior1.pl results/official/rm3-10-25-0.75.krovetz-all.out true> results/official/prior1-rm3-10-25-0.75.krovetz-all.out
./prior1.pl results/official/pubmed.official.krovetz.10-25-0.75.out true> results/official/prior1-pubmed.official.krovetz.10-25-0.75.out

#uiuc-prior2
./prior2.pl results/official/rm3-10-25-0.75.krovetz-all.out true> results/official/prior2-rm3-10-25-0.75.krovetz-all.out
./prior2.pl results/official/pubmed.official.krovetz.10-25-0.75.out true> results/official/prior2-pubmed.official.krovetz.10-25-0.75.out
