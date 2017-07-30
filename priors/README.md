# Repository priors

These scripts will re-score an initial retrieval using the priors described in the paper.

### Prior 1: Using training data

First, get the repository for each qrel:
```bash
cd ~/biocaddie
scripts/repo.sh > /data/biocaddie/data/biocaddie-doc-repo.out
```

Next, rescore an initial retrieval:
```bash
priors/rescore1.sh <input> <output>
```

For example
```bash
priors/rescore1.sh output/dir/combined/short output/dir-prior1/combined/short
```



### Prior 2: Pseudo-feedback
Rescore an initial retrieval:
```bash
priors/rescore2.sh <input> <output>
```

For example
```bash
priors/rescore2.sh output/dir/combined/short output/dir-prior2/combined/short
```
