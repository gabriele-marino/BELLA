
## 1. BayesMLP

The main neural network class that implements a multi-layer perceptron.

**Inputs:**
- `predictor` (required): One or more RealParameter objects containing predictor variables
- `weights` (required): RealParameter objects for each layer's weights
- `nodes` (optional): List of integers specifying hidden layer sizes
- `outNodes` (optional): Number of output nodes (default: 1)
- `activationFunctionHidden` (optional): Activation function for hidden layers (default: ReLU)
- `activationFunctionsOutput` (optional): Activation function for output layer (default: Sigmoid)

**Architecture:**

The neural network automatically determines its structure based on:
- Number of predictors (input dimension)
- Hidden layer configuration (`nodes` parameter)
- Output dimension (`outNodes` parameter)

Each layer includes a bias term. The weight dimensions are calculated as:
- **Hidden layer i**: `(n_inputs[i] + 1) × n_nodes[i]`
- **Output layer**: `(n_hidden_last + 1) × n_outputs`

**Predictor Normalization**

All predictors are automatically normalized to [0,1] using min-max normalization:

```
normalized = (value - min) / (max - min)
```

If all values are identical, they are set to 0.5.

**Weight Matrix Structure**

Weights are stored as flattened vectors but organized as matrices internally:

- **Row index**: Input node (including bias term at index 0)
- **Column index**: Output node

For a layer with 3 inputs and 2 outputs (plus bias):
```
Weight matrix shape: (4, 2)
Flattened weight vector length: 8

Matrix layout:
[bias→node0,  bias→node1,
 in0→node0,   in0→node1,
 in1→node0,   in1→node1,
 in2→node0,   in2→node1]
```

**Usage:**

```xml
<bella.BayesMLP id="samplingRate" nodes="10 5">
    <predictor spec="RealParameter" value="1.0 2.0 3.0"/>
    <predictor spec="RealParameter" value="0.5 1.5 2.5"/>
    <weights id="w1" spec="RealParameter" value="0.1" dimension="30"/>  <!--Layer 1. Dimensio is set automatically to 30: (2+1)*10 -->
    <weights id="w2" spec="RealParameter" value="0.1"dimension="55"/>  <!-- Layer 2. Dimensio is set automatically to 55: (10+1)*5 -->
    <weights id="w3" spec="RealParameter" value="0.1"dimension="6"/>   <!-- Output. Dimensio is set automatically to 6: (5+1)*1 -->
    <activationFunctionHidden spec="bella.ReLu"/>
    <activationFunctionsOutput spec="bella.Sigmoid"/>
</bella.BayesMLP>
```

## 2. Activation Functions

### ReLu (Rectified Linear Unit)
```
f(x) = max(0, x)
```

**Usage:**
```xml
<activationFunctionHidden spec="bella.ReLu"/>
```

**Properties:**
- Non-linear
- Output range: [0, ∞)
- Commonly used in hidden layers
- Helps avoid vanishing gradient problem

### Sigmoid
```
f(x) = lower + (upper - lower) / (1 + exp(-shape * (x - midpoint)))
```

**Inputs:**
- `lower` (optional): Lower bound (default: 0.0)
- `upper` (optional): Upper bound (default: 1.0)
- `shape` (optional): Steepness parameter (default: 1.0)
- `midpoint` (optional): Inflection point (default: 0.0)

**Usage:**
```xml
<activationFunctionsOutput spec="bella.Sigmoid" lower="0.0" upper="100.0">
    <shape spec="RealParameter" value="2.0"/>
    <midpoint spec="RealParameter" value="0.5"/>
</activationFunctionsOutput>
```

**Properties:**
- S-shaped curve
- Output range: [lower, upper]
- Smooth, differentiable
- Useful for output layers when values need to be bounded

### SoftPlus
```
f(x) = log(1 + exp(x))
```

**Usage:**
```xml
<activationFunctionHidden spec="bella.SoftPlus"/>
```

**Properties:**
- Smooth approximation of ReLU
- Output range: (0, ∞)
- Always positive
- Differentiable everywhere

### Tanh (Hyperbolic Tangent)
```
f(x) = tanh(x) = (exp(x) - exp(-x)) / (exp(x) + exp(-x))
```

**Usage:**
```xml
<activationFunctionHidden spec="bella.Tanh"/>
```

**Properties:**
- S-shaped curve
- Output range: (-1, 1)
- Zero-centered
- Often used in hidden layers

## 3. SkylineNodeTreeLogger

Enhanced tree logger that combines typed node information with skyline parameter values.

**Inputs:**
- All inputs from `TypedNodeTreeLogger` (from BDMM-Prime)
- `skylineParameter`: One or more SkylineVectorParameter objects to log
- `parameterization`: Parameterization object for time conversion
- `finalSampleOffset` (optional): Time offset for final sample (default: 0.0)
- `precision` (optional): Decimal places for logging (default: 6)

**Usage:**
```xml
<logger id="treelog" spec="bella.SkylineNodeTreeLogger"
        logEvery="1000" fileName="$(filebase).trees" tree="@tree">
    <skylineParameter idref="birthRateSV"/>
    <skylineParameter idref="samplingRateSV"/>
    <parameterization idref="parameterization"/>
    <finalSampleOffset spec="RealParameter" value="0.0"/>
    <precision value="4"/>
</logger>
```

**Output:**

The logger produces Newick trees with enhanced metadata at each node, including skyline parameter values at the node's time point.
