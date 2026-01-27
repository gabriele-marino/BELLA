## 💡 Highlights from the BELLA Paper

> 📑📢 The full BELLA manuscript is available on [COMING SOON](), and all experiments can be reproduced using the [companion code](https://github.com/gabriele-marino/BELLA-companion).

### 📈 Capturing nonlinear dynamics with BELLA

<p align="center">
  <img src="https://raw.githubusercontent.com/gabriele-marino/BELLA/main/figures/epi-multitype.png" width="100%;">
</p>

We tested BELLA on a simulation scenario involving the prediction of 20 migration rates across 5 populations under nonlinear predictor–parameter relationships. BELLA accurately captures these nonlinear patterns and outperforms both a generalized linear model (GLM) and predictor-agnostic baselines, which struggle when dynamics are nonlinear or only a few independent parameters drive variation.

### 🔍 Interpreting the BELLA model

<p align="center">
  <img src="https://raw.githubusercontent.com/gabriele-marino/BELLA/main/figures/fbd-2traits.png" width="100%;">
</p>

We assessed our interpretation framework using a macroevolutionary simulation in which speciation ($\lambda$, top row) and extinction ($\mu$, bottom row) rates varied through time and among four groups of species as a function of a binary trait evolving along the phylogeny. The analysis included four predictors: time and a relevant binary trait (red), as well as an unrelated time series and an additional binary trait with no effect on diversification (gray). Partial dependence plots (PDPs) recovered the marginal effects of the relevant predictors, capturing the expected temporal decrease in speciation and increase in extinction rates, as well as trait-dependent shifts in both parameters, while indicating negligible influence of the irrelevant predictors. These patterns were corroborated by SHAP feature importance analyses, which identified time and the relevant trait as the dominant contributors to model output. Median predicted rates through time across species groups closely matched the simulated phylodynamic regimes, confirming that BELLA accurately identifies relevant drivers of diversification while remaining robust to uninformative predictors.

### 🐒 Leveraging BELLA in macroevolutionary analyses

<p align="center">
  <img src="https://raw.githubusercontent.com/gabriele-marino/BELLA/main/figures/platyrrhine.png" width="100%;">
</p>

We used BELLA to model the macroevolutionary history of New World monkey phylogenies, estimating diversification rates as functions of time and body mass. PDPs revealed a strong non-additive interaction between temporal and trait-dependent effects on diversification. Specifically, small-bodied lineages showed a steady increase in diversification through time, whereas larger-bodied lineages experienced a pronounced mid-Miocene decline followed by a rebound toward the present. This complex interaction would have been difficult to detect using alternative phylodynamic models.

### 🦠 Leveraging BELLA in epidemiological analyses

<p align="center">
  <img src="https://raw.githubusercontent.com/gabriele-marino/BELLA/main/figures/eucovid.png" width="100%;">
</p>

We applied BELLA to estimate SARS-CoV-2 migration rates across countries during the early spread of the virus in China and Europe. Migration rates were modeled as a function of the number of flights between countries, normalized by the population size of the source country. We reconstructed the geographic spread of the outbreak, identifying Italy as the most probable location of the most recent common ancestor of the European clade, with Italy inferred as the principal exporter of viral lineages and Germany as the main importer during this phase of the pandemic. We further examined how BELLA and a GLM link migration rates to the normalized number of flights using PDPs. BELLA recovered a nonlinear relationship that flattens at high predictor values, indicating a saturation effect that cannot be accommodated by the GLM’s linear functional form.
