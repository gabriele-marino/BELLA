# Components (Java source walkthrough)

This section explains the BELLA Java code in detail so you can confidently configure and extend it 🧩. File paths are relative to the repository.

## Package map

- `src/bella/BayesMLP.java` - Bayesian MLP component used in BEAST XML.
- `src/bella/util/MLPUtil.java` - MLP forward-pass utilities.
- `src/bella/util/ParameterUtil.java` - parameter normalization and matrix conversion.
- `src/bella/activations/*.java` - activation functions (ReLU, Sigmoid, Tanh, SoftPlus, Identity).

## `bella.BayesMLP` (core model node)

`BayesMLP` is the main BELLA component you reference in a BEAST XML. It extends `CalculationNode` and implements `Function` and `Loggable`, which makes it:

- **A value-producing node** (`Function`) - it outputs a vector of predicted rates.
- **A recalculating node** (`CalculationNode`) - it recomputes only when needed.
- **A loggable node** (`Loggable`) - it can print its weights to the BEAST log.

### Inputs (BEAST XML attributes)

- `predictor` (required): list of `RealParameter` objects. Each parameter is a vector of predictor values.
  - All predictors **must have the same length**, because they form a matrix.
- `weights` (required): list of `RealParameter` objects, one per layer connection.
  - Each is a **flattened weight matrix** (row-major) for a single layer.
- `nodes` (optional): number of neurons in each hidden layer.
  - Example: `nodes="16 8"` means two hidden layers: 16 and 8 neurons.
  - If empty, the network has **no hidden layers**.
- `hiddenActivation` (optional): activation function for hidden layers. Default: ReLU.
- `outputActivation` (optional): activation function for output layer. Default: Sigmoid.
- `normalize` (optional): min-max normalize predictors to `[0, 1]`. Default: `true`.

### Network shape logic (important!)

When `initAndValidate()` runs, the class builds the full layer sizes:

```
input_size = number of predictors
output_size = 1
nodes list = [input_size, hidden_1, hidden_2, ..., output_size]
```

So if you pass `nodes="16 8"` and you have 3 predictors, the internal layer sizes are:

```
[3, 16, 8, 1]
```

That implies **3 weight matrices**:

- Layer 1: `(3 + 1) x 16` (bias included)
- Layer 2: `(16 + 1) x 8`
- Layer 3: `(8 + 1) x 1`

Because of this, the number of `weights` parameters must equal **hidden layers + 1**. If not, `initAndValidate()` throws an error.

### Predictor handling

- Predictors are converted to a matrix with `ParameterUtil.toRealMatrix()`.
- The matrix is **transposed** so it becomes:
  - rows = observations (e.g., time bins)
  - columns = predictors
- If `normalize=true`, each predictor is scaled to `[0, 1]` in place.

### Forward pass and caching

- The forward pass is computed via `MLPUtil.forward(...)`.
- In `getArrayValue(n)`, BELLA only recomputes the network **if any weight changed**.
- Outputs are stored as a matrix, and `getArrayValue(n)` returns the `n`th row (the predicted value for observation `n`).

### Logging behavior

- `init(PrintStream)` writes column headers for each weight, using:
  - `W.LayerX[i][j]` where X is the layer index.
- `log(...)` prints all weight values in order.

This means you can trace network weights over MCMC samples, just like any other BEAST parameter 📊.

## `bella.util.MLPUtil` (forward pass)

`MLPUtil` contains two static methods:

1. **`layer_forward(input, weights, activation)`**
   - Adds a **bias column of ones** to the input matrix.
   - Performs matrix multiplication with the layer weights.
   - Applies the activation function element-wise.

2. **`forward(input, weightMatrices, hiddenActivation, outputActivation)`**
   - Applies `layer_forward` across all layers.
   - Uses `hiddenActivation` for all but the last layer.
   - Uses `outputActivation` for the final layer.

This is a standard MLP forward pass with explicit bias handling.

## `bella.util.ParameterUtil` (helpers)

Two small but important utilities:

- **`minMaxNormalize(RealParameter)`**
  - Scales values into `[0, 1]`.
  - If all values are identical, they are set to `0.5` to avoid division by zero.
- **`toRealMatrix(ArrayList<RealParameter>)`**
  - Converts a list of equally-sized `RealParameter` objects into a matrix.
  - Throws a clear error if dimensions mismatch.

## Activation functions (`bella.activations`)

All activation functions extend `ActivationFunction`, which defines:

- `apply(double z)` - element-wise transform (must be implemented).
- `apply(RealMatrix z)` - loops over entries and applies the scalar method.

Concrete activations:

- **ReLU** (`ReLU.java`) 🔺: `max(0, z)`
- **Tanh** (`Tanh.java`) 🌊: `tanh(z)`
- **SoftPlus** (`SoftPlus.java`) 🧈: `log(1 + exp(z))`, numerically stable
- **Identity** (`Identity.java`) ➖: `z`
- **Sigmoid** (`Sigmoid.java`) 📈: bounded logistic with parameters:
  - `lower`, `upper` (output range)
  - `shape` (steepness)
  - `midpoint` (center point)

Sigmoid is the default for the output layer, which is handy when you want to keep rates within known bounds.

## Summary: data flows through BELLA like this

```
Predictor vectors
   v (optional normalization)
Matrix of observations x predictors
   v (bias + weights + activation)
BayesMLP output vector
   v
Phylodynamic rate parameter (e.g., skyline birth rate)
```

Understanding this flow will help you build correct BEAST XML configurations and interpret BELLA outputs 🔍.
