package bella;

import beast.base.core.Description;
import beast.base.core.Function;
import beast.base.core.Input;
import beast.base.core.Loggable;
import beast.base.inference.CalculationNode;
import beast.base.inference.parameter.RealParameter;

import bella.activations.ActivationFunction;
import bella.activations.ReLU;
import bella.activations.Sigmoid;
import bella.util.MLPUtil;
import bella.util.ParameterUtil;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;

@Description("Bayesian Multi-Layer Perceptron (MLP) with configurable hidden layers and activation functions.")
public class BayesMLP extends CalculationNode implements Function, Loggable {

    public Input<ArrayList<RealParameter>> predictorsInput = new Input<>(
            "predictor",
            "List of predictor parameters defining the input layer. "
                    + "Each predictor is a vector of values, where each value feeds a neuron in the first layer.",
            new ArrayList<>(), Input.Validate.REQUIRED);

    public Input<ArrayList<RealParameter>> weightsInput = new Input<>(
            "weights",
            "Weight parameters. Must contain one weight matrix per layer connection "
                    + "(n + 1 total, where n is the number of hidden layers).",
            new ArrayList<>(), Input.Validate.REQUIRED);

    public Input<ArrayList<Integer>> nodesInput = new Input<>(
            "nodes",
            "Number of neurons in each hidden layer. "
                    + "Example: [5, 3] defines two hidden layers with 5 and 3 neurons. "
                    + "Default is an empty list, corresponding to no hidden layers.",
            new ArrayList<>(), Input.Validate.OPTIONAL);

    public Input<ActivationFunction> hiddenActivationInput = new Input<>(
            "hiddenActivation",
            "Activation function applied to all hidden layers. Default is ReLU",
            new ReLU(), Input.Validate.OPTIONAL);

    public Input<ActivationFunction> outputActivationInput = new Input<>(
            "outputActivation",
            "Activation function applied to the output layer. Default is Sigmoid",
            new Sigmoid(), Input.Validate.OPTIONAL);

    public Input<Boolean> normalizeInput = new Input<>(
            "normalize",
            "Whether to apply min–max normalization to predictor values, "
                    + "scaling them to the range [0, 1] before they are passed to the network. "
                    + "Default is true.",
            true, Input.Validate.OPTIONAL);

    RealMatrix predictors; // Input predictors matrix of shape [predictorSize × nPredictors]
    List<Integer> nodes; // Number of neurons in each layer of the network, of length nHiddenLayers + 2
    ArrayList<RealParameter> weights; // Flattened weights for each layer
    RealMatrix[] weightMatrices; // Weight matrices for each layer, shaped [(nInputs + 1) × nOutputs]
    RealMatrix output;
    ActivationFunction hiddenActivation;
    ActivationFunction outputActivation;

    @Override
    public void initAndValidate() {
        try {
            // Attempt to convert the input to a RealMatrix and transpose it
            predictors = ParameterUtil.toRealMatrix(predictorsInput.get()).transpose();
        } catch (IllegalArgumentException e) {
            // Raise a new exception with additional context
            throw new IllegalArgumentException("Error converting predictors to RealMatrix. " +
                    "Check the input parameter sizes.", e);
        }
        if (normalizeInput.get()) {
            for (RealParameter predictor : predictorsInput.get()) {
                ParameterUtil.minMaxNormalize(predictor);
            }
        }

        nodes = nodesInput.get();
        nodes.add(0, predictors.getColumnDimension());
        nodes.add(1);
        weights = weightsInput.get();
        hiddenActivation = hiddenActivationInput.get();
        outputActivation = outputActivationInput.get();

        if (nodes.size() - 1 != weights.size())
            throw new IllegalArgumentException(
                    String.format(
                            "Invalid network architecture: expected one weight parameter per layer connection. "
                                    + "Found %d hidden layer definitions but %d weight parameter sets. "
                                    + "The number of weight parameters must equal the number of hidden layers plus one.",
                            nodes.size() - 2, weights.size()
                    )
            );

        weightMatrices = new RealMatrix[weights.size()];
        for (int i = 0; i < weights.size(); i++) {
            int nInput = nodes.get(i) + 1;  // Add 1 to account for bias node
            int nOutput = nodes.get(i + 1);
            weights.get(i).setDimension(nInput * nOutput);
            weightMatrices[i] = MatrixUtils.createRealMatrix(nInput, nOutput);
        }
        output = MLPUtil.forward(predictors, weightMatrices, hiddenActivation, outputActivation);
    }

    @Override
    public int getDimension() {
        return predictors.getRowDimension();
    }

    @Override
    public double getArrayValue(int n) {
        boolean needsRecalculation = false;

        for (int i = 0; i < weights.size(); i++) {
            double[] w = weights.get(i).getDoubleValues();
            int nOutput = nodes.get(i + 1);
            for (int j = 0; j < w.length; j++) {
                int row = j / nOutput;
                int col = j % nOutput;
                if (weightMatrices[i].getEntry(row, col) != w[j]) {
                    needsRecalculation = true;
                    weightMatrices[i].setEntry(row, col, w[j]);
                }
            }
        }

        if (needsRecalculation) {
            output = MLPUtil.forward(predictors, weightMatrices, hiddenActivation, outputActivation);
        }

        return output.getEntry(n,0);
    }

    /**
     * Generates column headers for network weight coefficients.
     *
     * <p>Header format:
     * <pre>
     * &lt;id&gt;W.Layer&lt;X&gt;[&lt;i&gt;][&lt;j&gt;]
     * </pre>
     *
     * <p>Where:
     * <ul>
     *   <li><b>id</b> – identifier of the network instance (as specified by the BEAST object ID)</li>
     *   <li><b>X</b> – layer index (1-based)</li>
     *   <li><b>i</b> – input neuron index, including the bias term (i = 0)</li>
     *   <li><b>j</b> – output neuron index</li>
     * </ul>
     */
    @Override
    public void init(PrintStream out) {
        String prefix = (getID() != null) ? getID() : "";

        for (int i = 0; i < weightMatrices.length; i++) {
            for (int j = 0; j < weightMatrices[i].getRowDimension(); j++) {
                for (int k = 0; k < weightMatrices[i].getColumnDimension(); k++) {
                    out.printf("%sW.Layer%d[%d][%d]\t", prefix, i + 1, j, k);
                }
            }
        }
    }

    @Override
    public void log(long sample, PrintStream out) {
        for (RealParameter layerWeights : weights) {
            for (Double w : layerWeights.getDoubleValues()) {
                out.print(w + "\t");
            }
        }
    }

    @Override
    public void close(PrintStream out) {
    }
}
