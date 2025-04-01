# NNvsGLM

The aim of this project is to explore the use of neural networks (NNs) as an alternative to generalized linear models (GLMs) for the modeling of migration rates in multi-type birth-death (MTBD) models.

This repository is to be intended as a work in progress.

### Repository structure

```
├── BayesMLP # BEAST package for Bayesian MLP as rates prior. 
│   ├── examples
│   ├── lib
│   ├── src
│   │   ├── META-INF
│   │   └── bayesmlp
│   │       └── priors
│   └── test
├── data
└── testing #simulations for  initial testing of the implementation
    ├── out_figs #BDMM-Prime, GLM and BayesMLP comparison figures
    ├── out_logs #BDMM-Prime, GLM and BayesMLP comparison logs
    ├── remaster_sims # BD-sampling simulation
    └── run_xmls # xmls for BDMM-Prime, GLM and BayesMLP
```

### References

- [Valenzuela Agüí et al., A comprehensive study of the phylodynamics of SARS-CoV-2 in Europe, 2021](https://www.research-collection.ethz.ch/bitstream/handle/20.500.11850/609461/1/ValenzuelaCecilia_MasterThesis.pdf).
- [Hauffe et al., Trait-mediated speciation and human-driven extinctions in proboscideans revealed by unsupervised Bayesian neural networks](https://www.science.org/doi/full/10.1126/sciadv.adl2643)
