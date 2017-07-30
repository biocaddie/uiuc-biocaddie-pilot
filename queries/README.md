# bioCADDIE Queries

The official bioCADDIE qrels and queries have been converted to Indri format in the ``qrels`` and ``queries`` directories. We provide both the original ``train`` and final ``test`` queries and qrels, as well as ``combined`` sets for ongoing research. We've also included ``stopped`` queries and manually shortened versions (``short``).

The original bioCADDIE Challenge submission used only the original queries.  The pilot project evaluation uses the shortened queries exclusively.

## Shortened queries

As part of our pilot work, we determined that the original queries provided for the bioCADDIE challenge were unrealistic, containing boilerplate phrasing such as ``find all X across all datasets``. For our pilot evaluation, we decided to shorten the queries to what we consider to be more realistic user queries.  Two team members independently created shortened queries from the original queries and compared outcomes to resolve any differences.  We did this because we felt that the original queries would reward systems that handle noisy queries, even if noisy queries are unusual in the DataMed system.

## Train and test collections

Also as part of the pilot work, we analyzed the [differences between the ``train`` and ``test`` queries and relevance judgments](https://opensource.ncsa.illinois.edu/confluence/display/NDS/Differences+between+train+and+test+query+scores). We were surprised to find that the training and test sets had remarkably different judgment depths.  As a result, our evaluations use the ``test`` set exclusively due to concerns that the training data may skew results. 
