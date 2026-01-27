# Tutorials

This section guides you from **zero BEAST knowledge** to running your first BELLA analysis ✅. The walkthroughs are based directly on the `examples/` folder.

## BEAST + BELLA config file walkthrough (from scratch)

A BEAST analysis is defined by an **XML configuration file**. BELLA is configured inside that XML like any other BEAST component. Below is the mental model you should keep in mind:

```
Data + predictors + priors
        v
   BEAST XML
        v
      MCMC
        v
 Posterior logs
```

### 1) Create the XML skeleton

Every BEAST XML starts with a `<beast>` tag. The `namespace` tells BEAST where to find classes (including BELLA and the extra packages used by examples):

```xml
<beast
    namespace="
    :beast.base.evolution.operator.kernel
    :beast.base.inference
    :beast.base.inference.distribution
    :beast.base.inference.operator.kernel
    :beast.base.inference.parameter
    :bdmmprime.distribution
    :bdmmprime.mapping
    :bdmmprime.parameterization
    :feast.fileio"
    version="2.5">
```

### 2) Load your tree and data

In the FBD example, you will reference:

- A **tree file** is used by the model (often Newick).
- A **predictor file** (CSV) contains the time series or trait values that drive BELLA.

Example from `examples/FBD/config.xml`:

```xml
<TreeFromNewickFile id="tree" fileName="$(treeFile)" IsLabelledNewick="true" adjustTipHeights='false'/>
```

The `$(treeFile)` placeholder is filled from a JSON file using BEAST's `-DF` option (see below).

### 3) Declare the MCMC state (parameters to estimate)

In BEAST, **state nodes** are parameters sampled by MCMC. For BELLA, you typically sample the **network weights** and any phylodynamic parameters like sampling rates.

```xml
<state id="state">
    <plate var="n" range="$(layersRange)">
        <stateNode spec="RealParameter" id="birthRateW$(n)" value="0"/>
        <stateNode spec="RealParameter" id="deathRateW$(n)" value="0"/>
    </plate>
    <stateNode spec="RealParameter" id="samplingRate" value="$(samplingRateInit)"/>
</state>
```

What is `layersRange`? It is a BEAST **plate** trick for repeating blocks. If you set `layersRange="0,1,2"`, BEAST will create 3 weight parameters: one per layer connection.

### 4) Wire BELLA into the model

The heart of BELLA is `bella.BayesMLP`. It maps predictors -> rates.

```xml
<skylineValues id="birthRate" spec="bella.BayesMLP" nodes="$(nodes)">
    <predictor spec="RealParameterFromXSV" fileName="$(birthRatePredictorFile)"/>
    <plate var="n" range="$(layersRange)">
        <weights idref="birthRateW$(n)"/>
    </plate>
    <outputActivation spec="bella.activations.Sigmoid" lower="$(birthRateLower)" upper="$(birthRateUpper)"/>
</skylineValues>
```

Key ideas:

- **Predictors** can be loaded from CSV (`RealParameterFromXSV`) or embedded directly as `RealParameter` values.
- **`nodes`** defines hidden layers; BELLA adds the input layer and output layer automatically.
- **`weights`** must match the number of layer connections.
- **`outputActivation`** is often a bounded Sigmoid so that rates stay in a sensible range.

### 5) Connect the BELLA output to a phylodynamic model

In the example, BELLA produces **time-varying skyline birth and death rates** for an FBD process:

```xml
<parameterization id="parameterization" spec="CanonicalParameterization" processLength="$(processLength)">
    <birthRate id="birthRateSP" spec="SkylineVectorParameter" changeTimes="$(changeTimes)">
        <skylineValues id="birthRate" spec="bella.BayesMLP" ... />
    </birthRate>
    <deathRate id="deathRateSP" spec="SkylineVectorParameter" changeTimes="$(changeTimes)">
        <skylineValues id="deathRate" spec="bella.BayesMLP" ... />
    </deathRate>
</parameterization>
```

The `changeTimes` list defines time bins. The number of skyline values is `length(changeTimes) + 1`, so **your predictor vectors must match that length**.

### 6) Add priors and MCMC operators

Weights need priors and proposal operators so the MCMC can explore them:

```xml
<Normal id="weightsPrior" mean="0" sigma="1"/>
<plate var="n" range="$(layersRange)">
    <distribution spec="Prior" x="@birthRateW$(n)" distr="@weightsPrior"/>
    <distribution spec="Prior" x="@deathRateW$(n)" distr="@weightsPrior"/>
</plate>

<plate var="n" range="$(layersRange)">
    <operator spec="BactrianRandomWalkOperator" parameter="@birthRateW$(n)" weight="30.0"/>
    <operator spec="BactrianRandomWalkOperator" parameter="@deathRateW$(n)" weight="30.0"/>
</plate>
```

### 7) Configure logging

Logging controls what you will see in output files. The example logs the posterior, rates, and the BELLA network outputs:

```xml
<logger spec="Logger" fileName="FBD.log" logEvery="$(logEvery)" model="@posterior">
    <log idref="posterior"/>
    <log idref="birthRateSP"/>
    <log idref="deathRateSP"/>
    <log idref="birthRate"/>
    <log idref="deathRate"/>
</logger>
```

### 8) Use `-DF` to fill values from JSON

BEAST can replace `$(...)` placeholders at runtime using `-DF`.

Example command (from the FBD example):

```bash
beast -DF FBD/data.json FBD/config.xml
```

That is why files like `examples/FBD/data.json` matter - they are the **parameter panel** for your XML.

---

## Tutorial: FBD example (from `examples/FBD`)

This tutorial shows a **Fossilized Birth-Death (FBD)** analysis where BELLA predicts skyline speciation and extinction rates.

### Files you will use

- `examples/FBD/config.xml` - BEAST configuration with BELLA wired in.
- `examples/FBD/data.json` - values that fill the `$(...)` placeholders.
- `examples/FBD/tree.nwk` - fixed phylogenetic tree.
- `examples/FBD/birth_rate_predictor.csv` - predictor values for birth rate.

### Step 1: Check the JSON parameters

Open `examples/FBD/data.json` and make sure the key values match your scenario:

- `processLength`: total time span of the FBD process.
- `changeTimes`: boundaries of skyline bins (space-separated).
- `layersRange` and `nodes`: define the MLP architecture.
- `birthRatePredictorFile` and `deathRatePredictor`: predictor inputs.
- `birthRateLower/Upper`, `deathRateLower/Upper`: bounds for rates.

Remember: the number of predictor values **must equal** `length(changeTimes) + 1`.

### Step 2: Understand how BELLA is used in the XML

Inside `config.xml`, both birth and death rates are **skyline parameters**, and their values are generated by a BELLA network (`bella.BayesMLP`).

- Birth rate uses a CSV predictor file (`RealParameterFromXSV`).
- Death rate uses a direct vector (`RealParameter`).
- Both use a Sigmoid output activation to keep rates bounded.

### Step 3: Run the analysis

From the `examples/` folder (or from repository root), run:

```bash
beast -DF FBD/data.json FBD/config.xml
```

### Step 4: Inspect outputs

The example writes:

- `FBD.log` - posterior, likelihood, priors, sampling rate, and skyline rates.

You can now plot skyline rates and interpret how BELLA mapped predictors to parameters 🎉.

---

If you want help adapting the XML to your own data, start by editing `data.json` first - it is the safest, most user-friendly entry point for BELLA configurations.
