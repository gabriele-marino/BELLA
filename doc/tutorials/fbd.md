# Tutorial: FBD example

This tutorial walks through the **Fossilized Birth-Death (FBD)** example in `examples/FBD` and shows how BELLA predicts skyline speciation and extinction rates.

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


## Files you will use

- `examples/FBD/config.xml` - BEAST configuration with BELLA wired in.
- `examples/FBD/data.json` - values that fill the `$(...)` placeholders.
- `examples/FBD/tree.nwk` - fixed phylogenetic tree.
- `examples/FBD/birth_rate_predictor.csv` - predictor values for birth rate.

## Step 1: Review the JSON parameters

Open `examples/FBD/data.json` and check these values:

- `processLength`: total time span of the FBD process.
- `changeTimes`: boundaries of skyline bins (space-separated).
- `layersRange` and `nodes`: define the MLP architecture.
- `birthRatePredictorFile` and `deathRatePredictor`: predictor inputs.
- `birthRateLower/Upper`, `deathRateLower/Upper`: bounds for rates.

Important rule: the number of predictor values **must equal** `length(changeTimes) + 1`.

## Step 2: See how BELLA is used in the XML

Inside `config.xml`, BELLA is used twice - once for birth rates and once for death rates. Both are skyline parameters with the same time bins:

```xml
<birthRate id="birthRateSP" spec="SkylineVectorParameter" changeTimes="$(changeTimes)">
    <skylineValues id="birthRate" spec="bella.BayesMLP" nodes="$(nodes)">
        <predictor spec="RealParameterFromXSV" fileName="$(birthRatePredictorFile)"/>
        <plate var="n" range="$(layersRange)">
            <weights idref="birthRateW$(n)"/>
        </plate>
        <outputActivation spec="bella.activations.Sigmoid" lower="$(birthRateLower)" upper="$(birthRateUpper)"/>
    </skylineValues>
</birthRate>
```

The death-rate block is identical except the predictor is provided directly as a vector in `data.json`.

## Step 3: Run the analysis

From the repository root:

```bash
beast -DF FBD/data.json FBD/config.xml
```

## Step 4: Inspect outputs

The example writes:

- `FBD.log` - posterior, likelihood, priors, sampling rate, and skyline rates.

You can now plot skyline rates and interpret how BELLA mapped predictors to parameters 🎉.

## Customizing for your own data

Start by editing `data.json` first:

- adjust `changeTimes` to match your time bins
- replace the predictor file or predictor vector
- tune rate bounds and priors

If you need more flexibility, edit `config.xml` directly. It contains inline comments that point to the main BELLA sections.
