# Tutorials overview

Welcome to the BELLA tutorials ✅. This section is split into:

- A **general overview** of BEAST and BELLA.
- One **tutorial per example** in `examples/`.

If you are new to BEAST, start here before jumping into the example pages.

## BEAST in one minute

BEAST (Bayesian Evolutionary Analysis by Sampling Trees) runs **MCMC** to estimate phylogenies and model parameters. A BEAST analysis is defined by an **XML configuration file** that declares:

- data (trees, alignments, predictors)
- model components (priors, likelihoods, parameterizations)
- MCMC settings (operators, logging, chain length)

Think of the XML as a **model graph**: nodes compute values, and BEAST samples the parameters that feed those nodes.

## Where BELLA fits

BELLA provides a Bayesian neural network component that lives **inside** the BEAST model graph. In practice:

1. You declare predictors (trait vectors or time series).
2. BELLA (`bella.BayesMLP`) maps predictors -> rates.
3. Those rates feed a phylodynamic model (birth-death, migration, etc.).

This lets BEAST estimate **network weights and evolutionary parameters jointly**.

BELLA behaves like any other BEAST component:

- it is a **CalculationNode** (recomputes when its inputs change),
- a **Function** (outputs a vector of values),
- and a **Loggable** (it can write to BEAST logs).

## How a BELLA XML is structured

Below is the mental model for a BELLA-driven BEAST analysis:

```
Data + predictors + priors
        v
   BEAST XML
        v
      MCMC
        v
 Posterior logs
```

### 1) XML skeleton

Every BEAST XML starts with a `<beast>` tag and a `namespace` that includes BELLA and any extra packages you need.

### 2) Data inputs

You typically provide:

- a **tree** (often Newick)
- one or more **predictor vectors** (CSV or direct values)

### 3) State nodes (parameters to estimate)

In BEAST, **state nodes** are parameters sampled by MCMC. For BELLA, these are usually the **network weights**. Other parameters (like sampling rates, clock rates, or population sizes) can also be sampled.

Example:

```xml
<state id="state">
    <plate var="n" range="$(layersRange)">
        <stateNode spec="RealParameter" id="birthRateW$(n)" value="0"/>
        <stateNode spec="RealParameter" id="deathRateW$(n)" value="0"/>
    </plate>
    <stateNode spec="RealParameter" id="samplingRate" value="$(samplingRateInit)"/>
</state>
```

How to think about it:

- `layersRange` expands one state node **per layer connection**.
- BELLA expects **one weight parameter per connection** (hidden layers + 1).
- The weight parameters are flattened vectors; BELLA reshapes them internally.

### 4) BELLA wiring (predictors -> rates)

A BELLA MLP is defined in XML like this:

```xml
<skylineValues id="birthRate" spec="bella.BayesMLP" nodes="$(nodes)">
    <predictor spec="RealParameterFromXSV" fileName="$(birthRatePredictorFile)"/>
    <plate var="n" range="$(layersRange)">
        <weights idref="birthRateW$(n)"/>
    </plate>
    <outputActivation spec="bella.activations.Sigmoid" lower="$(birthRateLower)" upper="$(birthRateUpper)"/>
</skylineValues>
```

Key points:

- `nodes` defines **hidden layers** only. BELLA adds input and output layers automatically.
- The predictor list becomes a matrix. Each row is an observation (time bin), each column is a predictor.
- Use `outputActivation` to constrain the output range (Sigmoid is a common choice).
- If `normalize=true` (default), BELLA min-max normalizes predictor vectors to `[0, 1]`.

### 5) Time bins and skyline vectors

BELLA often predicts **skyline vectors**, where each element is a rate for a time bin.

If your XML contains:

```
changeTimes = "t1 t2 ..."
```

then the number of skyline values is:

```
length(changeTimes) + 1
```

So every predictor vector must have that exact length.

### 6) Activation functions (hidden + output layers)

BELLA supports multiple activation functions. You configure them in XML with `hiddenActivation` and `outputActivation`:

```xml
<skylineValues id="birthRate" spec="bella.BayesMLP" nodes="$(nodes)">
    <predictor spec="RealParameterFromXSV" fileName="$(birthRatePredictorFile)"/>
    <hiddenActivation spec="bella.activations.ReLU"/>
    <outputActivation spec="bella.activations.Sigmoid" lower="$(birthRateLower)" upper="$(birthRateUpper)"/>
    ...
</skylineValues>
```

Available activations:

- **ReLU**: `max(0, z)` (default for hidden layers)
- **Tanh**: `tanh(z)`
- **SoftPlus**: `log(1 + exp(z))` with a stable implementation
- **Identity**: `z` (no nonlinearity)
- **Sigmoid**: bounded logistic with parameters:
  - `lower` and `upper` to clamp the output range
  - `shape` (steepness) and `midpoint` (center)

Practical guidance:

- Use **Sigmoid** for outputs that must stay within bounds (rates, probabilities).
- Use **ReLU** or **Tanh** for hidden layers to allow nonlinear mappings.
- If you want linear outputs, set `outputActivation` to **Identity**.

### 7) Likelihood and priors

In BEAST, the **posterior** is a product of likelihood and prior terms:

```xml
<distribution id="posterior" spec="CompoundDistribution">
    <distribution id="likelihood" spec="CompoundDistribution">
        <!-- model likelihood here -->
    </distribution>
    <distribution id="prior" spec="CompoundDistribution">
        <!-- priors here -->
    </distribution>
</distribution>
```

BELLA weights are just parameters, so you place priors on them like any other RealParameter:

```xml
<Normal id="weightsPrior" mean="0" sigma="1"/>
<plate var="n" range="$(layersRange)">
    <distribution spec="Prior" x="@birthRateW$(n)" distr="@weightsPrior"/>
    <distribution spec="Prior" x="@deathRateW$(n)" distr="@weightsPrior"/>
</plate>
```

### 8) Operators (how MCMC moves)

Operators propose new parameter values so MCMC can explore the posterior. BELLA examples use Bactrian random walks on weights:

```xml
<plate var="n" range="$(layersRange)">
    <operator spec="BactrianRandomWalkOperator" parameter="@birthRateW$(n)" weight="30.0"/>
    <operator spec="BactrianRandomWalkOperator" parameter="@deathRateW$(n)" weight="30.0"/>
</plate>
```

Use higher weights for parameters that need more frequent updates. Tune operator weights and step sizes if mixing is poor.

### 9) Logging (including BELLA internals)

Logging controls what you see in output files. You can log:

- **posterior, likelihood, prior**
- **skyline rates**
- **BELLA outputs**
- **BELLA network weights**

Example logger:

```xml
<logger spec="Logger" fileName="FBD.log" logEvery="$(logEvery)" model="@posterior">
    <log idref="posterior"/>
    <log idref="prior"/>
    <log idref="likelihood"/>
    <log idref="birthRateSP"/>
    <log idref="deathRateSP"/>
    <log idref="birthRate"/>
    <log idref="deathRate"/>
</logger>
```

How BELLA logs its weights:

- `bella.BayesMLP` implements **Loggable**.
- On init, it prints headers like `W.LayerX[i][j]` for every weight matrix entry.
  - `X` is the layer index (1-based).
  - `i` is the input neuron index (including the bias row as index 0).
  - `j` is the output neuron index.
- During logging, it prints every weight value in order.

So if you add `<log idref="birthRate"/>` and `<log idref="deathRate"/>`, you will get the full network weights per MCMC sample in your log file.

### 10) JSON + `-DF` substitutions

BELLA examples use a `data.json` file that fills placeholders like `$(treeFile)` and `$(nodes)` at runtime.

Run with:

```bash
beast -DF path/to/data.json path/to/config.xml
```

This is the easiest way to tweak a BELLA configuration without editing the XML directly.

---

Next, choose a specific tutorial from the sidebar for a full, runnable example 🚀.
