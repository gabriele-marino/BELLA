# BELLA documentation

<p align="center">
  <img src="../assets/logo.png" alt="BELLA logo" width="420"/>
</p>

Welcome to **BELLA** (Bayesian Evolutionary Layered Learning Architectures) 📚🧠. BELLA is a [BEAST 2](https://www.beast2.org/) package that brings *unsupervised Bayesian neural networks* to phylodynamic inference. It lets you map rich predictor sets (traits, time series, environmental covariates) to key parameters such as **speciation, extinction, transmission, and migration rates** - and estimate everything jointly with the phylogeny using MCMC.

## Why BELLA?

- **Flexible nonlinear mappings** 🧩: capture complex, smooth dynamics that go beyond GLMs and skyline models.
- **Unsupervised Bayesian neural networks** 🤖: no labeled targets needed; the network learns from the sequence likelihood.
- **Built-in regularization** ⚖️: weight priors discourage overfitting while propagating uncertainty.
- **Explainable AI tools** 🔍: partial dependence plots and SHAP-style scores help interpret predictor effects.
- **Deep BEAST integration** ⚙️: fits seamlessly into BEAST 2 pipelines for epidemiology and macroevolution.

## How BELLA fits into BEAST

If you are new to BEAST, think of it this way:

1. **BEAST** is a Bayesian engine that runs MCMC to estimate phylogenies and model parameters.
2. **BELLA** is a package that provides a neural-network component (`bella.BayesMLP`) that BEAST can call **inside** the model.
3. Your BEAST XML config file wires predictors -> BELLA network -> phylodynamic rates -> likelihood.

In short: **BELLA is a neural network node inside the BEAST model graph** 🧠➡️🌳.

## What you need to know before you start

- BELLA runs on **BEAST 2.7+** and requires **Java 17+**.
- You will work with **BEAST XML configuration files**. These files declare data, model components, priors, and MCMC settings.
- BELLA exposes a **Bayesian MLP** that is configured in the XML like any other BEAST component.

Ready? Head to the installation guide next! 🚀
