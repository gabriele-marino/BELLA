<p align="center">
  <img src="https://raw.githubusercontent.com/gabriele-marino/BELLA/main/figures/logo.png" width="100%;"/>
</p>

---

![Version](https://img.shields.io/badge/version-0.1.0-brightgreen?style=flat-square)
[![BEAST 2.7](https://img.shields.io/badge/Powered%20by-BEAST%202.7-orange?style=flat-square)](https://www.beast2.org/)
[![Java version](https://img.shields.io/badge/Java-17%2B-blue?style=flat-square)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)

**Bayesian Evolutionary Layered Learning Architectures (BELLA)** is a [BEAST 2](https://www.beast2.org/) package that brings *unsupervised Bayesian neural networks* to phylodynamic inference, letting you map rich predictor sets (traits, time series, environmental covariates) to key parameters such as speciation, extinction, transmission, and migration rates — all learned jointly with the phylogeny via MCMC.

## 🗂️ Table of Contents

- [🧭🌐 Overview](#overview)
- [✨ Features](#features)
- [💡 Highlights from the BELLA Paper](#highlights-from-the-bella-paper)
  - [📈 Capturing nonlinear dynamics with BELLA](#capturing-nonlinear-dynamics-with-bella)
  - [🔍 Interpreting the BELLA model](#interpreting-the-bella-model)
  - [🐒 Leveraging BELLA in macroevolutionary analyses](#leveraging-bella-in-macroevolutionary-analyses)
  - [🦠 Leveraging BELLA in epidemiological analyses](#leveraging-bella-in-epidemiological-analyses)
- [⚙️ Installation](#installation)
- [🚀 Getting started](#getting-started)
- [📚 Documentation](#documentation)
- [📑 Citing BELLA](#citing-bella)
- [📬 Contact](#contact)

<a id="overview"></a>
## 🧭🌐 Overview

<p align="center">
  <img src="https://raw.githubusercontent.com/gabriele-marino/BELLA/main/figures/BELLA.png" width="100%;">
</p>

BELLA integrates time-calibrated phylogenies with predictor data—such as traits, environmental covariates, or time series—and prior knowledge on phylodynamic parameters and network weights. It uses a Bayesian neural network to flexibly map these predictors to key evolutionary or epidemiological parameters, like speciation, extinction, transmission, or migration rates, capturing complex nonlinear relationships that go beyond traditional GLMs and skyline models. All parameters, including the network weights, are estimated jointly within a Markov chain Monte Carlo framework. The resulting posterior distributions allow researchers not only to infer population dynamics but also to interpret how predictors influence them, using explainable AI tools.

<a id="features"></a>
## ✨ Features

- 🧠📈 **Nonlinear, highly flexible mappings** that model smooth, high-dimensional relationships between predictors and phylodynamic parameters, generalizing and extending GLMs and skyline models without restrictive linearity or piecewise-constant assumptions.
- 🧬🤖 **Unsupervised Bayesian neural networks** whose weights are inferred jointly with the phylogeny and evolutionary parameters by conditioning directly on the sequence likelihood—eliminating the need for external response variables or labeled training data.
- ⚖️🔒 **Built-in regularization** via weight priors that penalize overly complex functions, reducing overfitting while propagating uncertainty into phylodynamic estimates.
- 🔍🧩 **Explainable AI tooling** using partial dependence plots and SHAP-style attribution scores to quantify and visualize predictor effects on inferred phylodynamic parameters.
- ⚙️🐒🦠 **Deep BEAST 2 integration** enabling end-to-end Bayesian inference for epidemiological and macroevolutionary workflows within existing BEAST 2 pipelines.

<a id="highlights-from-the-bella-paper"></a>
## 💡 Highlights from the BELLA Paper

> 📑📢 The full BELLA manuscript is available on [COMING SOON](), and all experiments can be reproduced using the [companion code](https://github.com/gabriele-marino/BELLA-companion).

<a id="capturing-nonlinear-dynamics-with-bella"></a>
### 📈 Capturing nonlinear dynamics with BELLA

<p align="center">
  <img src="https://raw.githubusercontent.com/gabriele-marino/BELLA/main/figures/epi-multitype.png" width="100%;">
</p>

We tested BELLA on a simulation scenario involving the prediction of 20 migration rates across 5 populations under nonlinear predictor–parameter relationships. BELLA accurately captures these nonlinear patterns and outperforms both a generalized linear model (GLM) and predictor-agnostic baselines, which struggle when dynamics are nonlinear or only a few independent parameters drive variation.

<a id="interpreting-the-bella-model"></a>
### 🔍 Interpreting the BELLA model

<p align="center">
  <img src="https://raw.githubusercontent.com/gabriele-marino/BELLA/main/figures/fbd-2traits.png" width="100%;">
</p>

We assessed our interpretation framework using a macroevolutionary simulation in which speciation ($\lambda$, top row) and extinction ($\mu$, bottom row) rates varied through time and among four groups of species as a function of a binary trait evolving along the phylogeny. The analysis included four predictors: time and a relevant binary trait (red), as well as an unrelated time series and an additional binary trait with no effect on diversification (gray). Partial dependence plots (PDPs) recovered the marginal effects of the relevant predictors, capturing the expected temporal decrease in speciation and increase in extinction rates, as well as trait-dependent shifts in both parameters, while indicating negligible influence of the irrelevant predictors. These patterns were corroborated by SHAP feature importance analyses, which identified time and the relevant trait as the dominant contributors to model output. Median predicted rates through time across species groups closely matched the simulated phylodynamic regimes, confirming that BELLA accurately identifies relevant drivers of diversification while remaining robust to uninformative predictors.

<a id="leveraging-bella-in-macroevolutionary-analyses"></a>
### 🐒 Leveraging BELLA in macroevolutionary analyses

<p align="center">
  <img src="https://raw.githubusercontent.com/gabriele-marino/BELLA/main/figures/platyrrhine.png" width="100%;">
</p>

We used BELLA to model the macroevolutionary history of New World monkey phylogenies, estimating diversification rates as functions of time and body mass. PDPs revealed a strong non-additive interaction between temporal and trait-dependent effects on diversification. Specifically, small-bodied lineages showed a steady increase in diversification through time, whereas larger-bodied lineages experienced a pronounced mid-Miocene decline followed by a rebound toward the present. This complex interaction would have been difficult to detect using alternative phylodynamic models.

<a id="leveraging-bella-in-epidemiological-analyses"></a>
### 🦠 Leveraging BELLA in epidemiological analyses

<p align="center">
  <img src="https://raw.githubusercontent.com/gabriele-marino/BELLA/main/figures/eucovid.png" width="100%;">
</p>

We applied BELLA to estimate SARS-CoV-2 migration rates across countries during the early spread of the virus in China and Europe. Migration rates were modeled as a function of the number of flights between countries, normalized by the population size of the source country. We reconstructed the geographic spread of the outbreak, identifying Italy as the most probable location of the most recent common ancestor of the European clade, with Italy inferred as the principal exporter of viral lineages and Germany as the main importer during this phase of the pandemic. We further examined how BELLA and a GLM link migration rates to the normalized number of flights using PDPs. BELLA recovered a nonlinear relationship that flattens at high predictor values, indicating a saturation effect that cannot be accommodated by the GLM’s linear functional form.

<a id="installation"></a>
## ⚙️ Installation

BELLA is available as a package for [BEAST 2.7+ ](https://www.beast2.org/). You can install it via BEAUti as follows:

1. Open BEAUti.
2. Go to `File` -> `Manage Packages`.
3. Click `Package repositories`.
4. `Add URL` and enter:
```https://raw.githubusercontent.com/gabriele-marino/BELLA/main/package.xml```.
5. Click `OK` to add the repository.
6. Close the Package Repositories window. Return to the Package Manager window, scroll down to find `BELLA`, select it, and click Install/Upgrade.
7. You're now ready to use BELLA!

<a id="getting-started"></a>
## 🚀 Getting started

We provide several well-documented example configuration files in the [examples](https://github.com/gabriele-marino/BELLA/tree/main/examples) directory, covering a range of use cases. See the [examples README](https://github.com/gabriele-marino/BELLA/tree/main/examples/README.md) to get started!

If you’ve already run your analyses and want to postprocess the results, check out the [BELLA-companion](https://github.com/gabriele-marino/BELLA-companion) Python package, which includes helpful plotting tools like the ones shown above.

> ❗️ If you don't find what you're looking for, please [reach out](mailto:gabmarino.8601@email.com)—I’m always happy to help!

<a id="documentation"></a>
## 📖 Documentation

The full BELLA documentation is available [here](https://gabriele-marino.github.io/BELLA/). It includes detailed instructions on installation, configuration, and usage, as well as tutorials and examples to help you get started.

<a id="citing-bella"></a>
## 📑 Citing BELLA

If you use BELLA in your research, please cite the following preprint:

> COMING SOON

<a id="contact"></a>
## 📫 Contact

For questions, bug reports, or feature requests, please, consider opening an [issue on GitHub](https://github.com/gabriele-marino/BELLA/issues), or [contact me directly](mailto:gabmarino.8601@email.com).

For help with configuration files, don’t hesitate to reach out — I’m happy to assist!
