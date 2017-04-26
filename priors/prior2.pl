#!/usr/bin/perl

#
# Prior2: Decompose query likelihood into lexical and repository information
# Estimate p(D | S)
#

$exp = $ARGV[1];

# 1,arrayexpress,18,108
my %repos;
open(F, "data/biocaddie-doc-repo.out");
while (<F>) {
   chomp();
   my ($docno, $repo, $tlen, $textlen) = split ",", $_;
   $repos{$docno} = $repo;
}

my %qrels;
open(F, "qrels/biocaddie.qrels.csv");
while (<F>) {
#EA1,0,1330,0
   chomp();
   my ($query, $ord, $docno, $rel) = split ",", $_;
   $qrels{$query}{$docno} = $rel;
}


# EA1 Q0 577787 1 30.148 indri
my %results;
open(R, $ARGV[0]);
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
   #for $repo (keys %tmp) {
   #   $tmp{$repo} /= 1000;
   #   #print "$query $repo =". $tmp{$repo} . "\n";
   #}

   my %rescore;
   for $docno (keys %{$results{$query}}) {
      $repo = $repos{$docno};
      $score = $results{$query}{$docno};

      if ($tmp{$repo} == "") { $tmp{$repo} = 0 ; }
      $p = ($tmp{$repo} + 1) / (1000 + scalar(keys %tmp));

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
      print "$query Q0 $docno $rank $newscore exp4-$repo-$rel-$p\n";
   #   print "$query Q0 $docno $rank $score indri $repo $p $newscore\n";
      $rank++;
  }
}
