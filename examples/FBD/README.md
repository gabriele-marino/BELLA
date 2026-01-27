## FBD example – Fossilized birth-death analysis with BELLA

This example demonstrates how to use **BELLA** to estimate skyline speciation ($\lambda$) and extinction ($\mu$) rates for a fixed phylogenetic tree using a Fossilized Birth-Death (FBD) process.

### Overview

The analysis uses a fixed tree (`tree.nwk`) and applies BELLA to infer time-varying speciation and extinction rates. The rates are modeled as piece-wise constant (skyline) functions, with three time bins, and predicted using a small multilayer perceptron (MLP), with two hidden layers of 16 and 8 nodes, respectively. The birth rate ($\lambda$) is predicted using a continuous predictor provided in `birthRatePredictor.csv`, and the death rate ($\mu$) is predicted using a continuous predictor specified directly in the JSON data file.  

### Customizing for your own Analyses

To adapt this example to your own datasets, modify the following fields in `data.json`:

- **treeFile**: Path to your Newick tree file (`tree.nwk`).  
- **processLength**: Total length of the FBD process.  
- **changeTimes**: Boundaries of the time bins (space-separated). The number of time bins is `length(changeTimes) + 1`.  
- **samplingRateLower** and **samplingRateUpper**: Bounds for the uniform prior on the sampling rate through time.
- **samplingRateInit**: Initial value for the sampling rate through time. Must lie within the specified bounds.
- **layersRange**: Comma-separated list of MLP layers, e.g., `"0,1,2"`. For instance, `"0"` indicates just an input layer and output layer (no hidden layers), while `"0,1,2"` indicates two hidden layers.
- **nodes**: Space-separated number of nodes per hidden layer of the MLP, e.g., `"16 8"`. This must match the number of layers specified in `layersRange`. For example, for `layersRange` = `"0,1,2"` and `nodes` = `"16 8"`, the MLP will have an input layer whose size is determined by the number of predictors, and two hidden layers with 16 and 8 nodes respectively.
- **deathRatePredictor**: Predictor for the extinction rate; can be specified directly in JSON as a list of comma-separated values. The number of predictor values must match the number of time bins (`length(changeTimes) + 1`). 
- **birthRatePredictorFile**: CSV file containing predictors for the birth rate. Must also match the number of time bins. 
- **birthRateLower** and **birthRateUpper**: Lower and upper bounds for the birth rate logistic output activation function.  
- **deathRateLower** and **deathRateUpper**: Lower and upper bounds for the death rate logistic output activation function.  
- **rhoSampling**: Probability of sampling at the end of the process (present).  
- **chainLength**: Length of the MCMC chain.  
- **logEvery**: Frequency of logging.

If changing the `data.json` file does not provide enough flexibility for your analysis, you can also modify the BEAST XML configuration file (`config.xml`) directly. It contains comments to guide you through the relevant sections.

### Outputs

- **FBD.log** – MCMC traces for posterior, likelihood, prior, sampling rate, and skyline birth/death rates.
