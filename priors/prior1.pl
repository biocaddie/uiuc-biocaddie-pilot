#!/usr/bin/perl

#
# ./prior1.pl <input file> 
#
# Rerank a set of results given the following document prior:
#
# p(D) = p(R=1 | D) = c(R=1,S)/c(R=1)
#
# Uses training data to estimate the prior probability of a document
# being relevant given the source (repository).
#

#$exp = $ARGV[1];


# Get the repository for each document 
# 1,arrayexpress,18,108
my %prior;
my %repos;
open(F, "data/biocaddie-doc-repo.out");
while (<F>) {
   chomp();
   my ($docno, $repo, $tlen, $textlen) = split ",", $_;
   $repos{$docno} = $repo;
   $prior{$repo} = 0;
}


$epsilon=1;
# Read the qrels and calculate the source (repository) prior
my $nrel = 0;
open(F, "qrels/biocaddie.qrels.csv");
while (<F>) {
   chomp();
   #EA1,0,1330,0
   my ($query, $ord, $docno, $rel) = split ",", $_;
   $repo = $repos{$docno};
   if ($rel > 0) {
      $prior{$repo}++;
      $nrel++;
   }
}

for $repo (keys %prior) {
   $prior{$repo} = ($prior{$repo} + ($epsilon))/ ($nrel + ($epsilon * scalar(keys %prior)));
   print $repo . "=" . $prior{$repo} . "\n";
}


# Re-rank results using the source prior
#j EA1 Q0 577787 1 30.148 indri
open(R, $ARGV[0]);
my %results;
while(<R>) {
   chomp();
   my ($query, $ignore, $docno, $rank, $score, $run) = split " ",$_;
   $results{$query}{$docno} = $score; 
}

for $query (sort keys %results) {
   my %tmp;
   $i = 0;
   for $docno (sort {$results{$query}{$b} <=> $results{$query}{$a}} keys %{$results{$query}}) {
      if ($i == 1000) { last;}

      $score = $results{$query}{$docno};
      if ($tmp{$repo} eq "") {$tmp{$repo} = 0;}
      $tmp{$repo}++;
      $repo = $repos{$docno};
      $i++;
   }
   for $repo (keys %tmp) {
      $tmp{$repo} /= 1000;
      #print "$query $repo =". $tmp{$repo} . "\n";
   }

   my %rescore;
   for $docno (keys %{$results{$query}}) {
      $repo = $repos{$docno};
      $score = $results{$query}{$docno};
      if ($tmp{$repo} == "") { $p = 0.0001; }
      else { $p = $tmp{$repo}; }
      if ($ARGV[1]) {
         $rescore{$docno} = $score + exp($p);
      } else {
         $rescore{$docno} = $score * $p;
      }
   }

   $rank = 1;
   for $docno (sort {$rescore{$b} <=> $rescore{$a}} keys %rescore) {
   #for $docno (sort {$results{$query}{$b} <=> $results{$query}{$a}} keys %{$results{$query}}) {
   #   $score = $results{$query}{$docno};
      $rel = $qrels{$query}{$docno};
      $repo = $repos{$docno};
      $newscore = $rescore{$docno};
      $p = $tmp{$repo};
      print "$query Q0 $docno $rank $newscore exp5-$repo-$rel-$p\n";
   #   print "$query Q0 $docno $rank $score indri $repo $p $newscore\n";
      $rank++;
  #}
 }
}
