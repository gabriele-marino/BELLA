# Components

This section describes the main BELLA components you can use in your BEAST XML files. The core component is the [BayesMLP](#bella.BayesMLP), which implements a Bayesian multilayer perceptron (MLP) to map predictor variables to phylodynamic parameters. Additionally, BELLA includes several [ActivationFunction](#bella.activations)s that can be used within the MLP. These components rely on standard BEAST interfaces such as `CalculationNode`, `RealParameter`, `Function`, and `Loggable`, and are designed to be flexible and integrate seamlessly with BEAST 2's MCMC framework. 

<a id="bella.BayesMLP"></a>
## [bella.BayesMLP](#bella.BayesMLP)

[BayesMLP](#bella.BayesMLP) is the main BELLA component you reference in a BEAST XML. It extends `CalculationNode` and implements `Function` and `Loggable`, which makes it:

- A value-producing node (`Function`): it outputs a vector of predicted rates.
- A recalculating node (`CalculationNode`): it recomputes only when needed.
- A loggable node (`Loggable`): it can print its weights to the BEAST log.

A [BayesMLP](#bella.BayesMLP) represents a fully connected feedforward neural network (multilayer perceptron, MLP) with arbitrary hidden layers, used to map predictor variables to phylodynamic parameters.

It has the following BEAST XML attributes:

- `predictor` (required): list of `RealParameter` objects. Each parameter is a vector of predictor values. All predictors must have the same length, which defines the number of observations (e.g., time bins), and corresponds to the size of the output of the network.
- `weights` (required): list of `RealParameter` objects, one per layer connection. Each is a flattened weight matrix (row-major) for a single layer. The size of each weight matrix is determined internally by the number of neurons in the source and target layers, and is equal to $(\text{n_source} + 1) \times \text{n_target}$ (the +1 accounts for the bias term).
- `nodes` (optional): number of neurons in each hidden layer. For example, `nodes="16 8"` means two hidden layers: 16 and 8 neurons. Default is an empty list, corresponding to no hidden layers.
- `hiddenActivation` (optional): [activation function](#bella.activations) for hidden layers. Default: ReLU.
- `outputActivation` (optional): [activation function](#bella.activations) for the output layer. Default: Sigmoid.
- `normalize` (optional): Whether to apply min–max normalization to predictor values, scaling them to the range $[0, 1]$ before they are passed to the network. Default: `true`.

When a [BayesMLP](#bella.BayesMLP) object is initialized, the class builds the full layer sizes, using the number of predictors as the size of the input layer and 1 as the size of the output layer. So if you pass `nodes="16 8"` and you have 3 predictors, the internal layer sizes are: $[3, 16, 8, 1]$. That implies 3 weight matrices:

- Layer 1: $(3 + 1) \times 16$
- Layer 2: $(16 + 1) \times 8$
- Layer 3: $(8 + 1) \times 1$.

Because of this, the number of `weights` parameters must equal the number of hidden layers + 1. If not, an error is raised during initialization.

Predictor values are managed within the class as a matrix of size $(\text{num_observations} \times \text{num_predictors})$. Each row corresponds to one observation (e.g., a time bin), and each column to one predictor variable. When performing a forward pass, the entire matrix is processed at once, yielding an output vector of size $(\text{num_observations} \times 1)$. Thus, each observation gets its own predicted rate.

[BayesMLP](#bella.BayesMLP) implements the `Loggable` interface, which makes it possible to log the network weights during MCMC. When you add a [BayesMLP](#bella.BayesMLP) to the BEAST log, it will output one column per weight in the network, using the following format: `<id>W.Layer<X>[<i>][<j>]`, where:

- `<id>` is the BEAST object ID of the [BayesMLP](#bella.BayesMLP) instance.
- `<X>` is the layer index (1-based).
- `<i>` is the input neuron index (including bias term, so $i=0$ is bias).
- `<j>` is the output neuron index.

<a id="bella.activations"></a>
## [bella.activations](#bella.activations)

Activation functions are used within the [BayesMLP](#bella.BayesMLP) to introduce nonlinearity. BELLA provides several built-in activation functions that you can specify in your BEAST XML configuration:

- `bella.activations.Identity`: Identity activation function. Implements $f(z) = z$.
- `bella.activations.ReLU`: Rectified Linear Unit (ReLU) activation function. Implements $f(z) = \max(0, z)$.
- `bella.activations.Tanh`: Hyperbolic tangent activation function. Implements $f(z) = \tanh(z)$.
- `bella.activations.SoftPlus`: SoftPlus activation function. Implements $f(z) = \log(1 + \exp(z))$, with numerical stability.
- `bella.activations.Sigmoid`: Sigmoid activation function with customizable parameters. Implements a bounded logistic function defined by:
  $$
  f(z) = \text{lower} + \frac{\text{upper} - \text{lower}}{1 + \exp(-\text{shape} * (z - \text{midpoint}))}
  $$
  where $\text{lower}$, $\text{upper}$, $\text{shape}$, and $\text{midpoint}$ are parameters you can set in the XML.

The output activation function is a useful way to enforce particular behaviors on the network’s output, such as bounding rates within a specific range using the sigmoid function. The hidden activation function is typically set to ReLU because its non-saturating linear regime avoids compression of activity and supports a wide dynamic range.