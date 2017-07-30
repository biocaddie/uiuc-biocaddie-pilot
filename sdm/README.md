# Dependendence model

We've also included as a baseline Metlzer's sequential dependence model (SDM) and full dependence model (FDM) runs.

This directory contains scripts needed to run the sequential and full dependence baseline models.

Generate the SDM queries (uses ``dm.pl``). For example:
```bash
sdm/gensd.sh short test
```

Run the queries, also sweeping the Dirichlet mu:
```bash
sdm/runsd.sh short test | parallel -j <jobs> bash -c "{}"
```

You can also run the ``fd`` variants of these two scripts.

For more information, see:
Metzler, D. and Croft, W.B., [A Markov Random Field Model for Term Dependencies](http://dl.acm.org/citation.cfm?id=1076115), ACM SIGIR 2005.
