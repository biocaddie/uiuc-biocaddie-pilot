#!/usr/bin/env Rscript
args = commandArgs(trailingOnly=TRUE)

options(digits=4)
file <- args[1]

metricData <- read.table(file, header=T,sep="\t")
var_calculation <- function(row) var(row)
metricData$variance <- apply(metricData[,-1],1,var_calculation)
metricData
#qrels_stats <- aggregate(Docno ~ Rel*Query , data = qrelsData, length)
#colnames(qrels_stats) <- c("Rel","Query","Doccount")
#qrels_stats 

