# Introduction

**BELLA: Bayesian Evolutionary Layered Learning Architectures**

BELLA is a BEAST2 package that implements a Bayesian multi-layer perceptron (neural network) designed for phylodynamic inference. The package relies on neural networks as flexible functions within BEAST2 analyses, particularly useful for modeling time-varying parameters, informed by external data (features).

This manual is dedicated to the usage of BELLA. If you interested in the source code, visit [https://github.com/gabriele-marino/BELLA](https://github.com/gabriele-marino/BELLA).

## Overview

BELLA provides a framework for incorporating neural networks into BEAST2 models. The primary use case is modeling complex, non-linear relationships between predictors and model parameters (e.g., sampling, transmission or diversification rates) in birth-death models.

### Key Features

- **Flexible architecture**: Support for arbitrary numbers of hidden layers with configurable node counts
- **Implemented activation functions**: ReLU, Sigmoid, SoftPlus, and Tanh
- **Automatic normalization**: Predictors are automatically normalized to [0,1] range
- **Integration with BDMM-Prime**: Seamless integration with skyline and/or type-dependent parameters in a birth-death-sampling model
- **Weight logging**: Built-in support for logging neural network weights during MCMC
- **Type-aware tree logging**: Enhanced tree logging with skyline parameter values at each node
