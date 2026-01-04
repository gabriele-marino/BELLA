package bella.util;

import bella.activations.ActivationFunction;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

/**
 * Utility class for working with Multi-Layer Perceptron (MLP) objects.
 */
public class MLPUtil {
    // Prevent instantiation
    private MLPUtil() {}

    /**
     * Performs a forward pass through a single MLP layer with bias.
     * <p>
     * The input is augmented with a bias column of ones, then multiplied
     * by the weight matrix, and finally passed through the activation function.
     *
     * @param input      Input matrix of shape [nSamples × nFeatures].
     * @param weights    Weight matrix of shape [(nFeatures + 1) × nOutputs],
     *                   where the first row represents the bias.
     * @param activation Activation function to apply element-wise.
     * @return Output matrix after applying the linear transformation and activation.
     */
    public static RealMatrix layer_forward(RealMatrix input, RealMatrix weights, ActivationFunction activation) {
        int nRows = input.getRowDimension();
        int nCols = input.getColumnDimension();

        RealMatrix inputWithBias = MatrixUtils.createRealMatrix(nRows, nCols + 1);
        for (int i = 0; i < nRows; i++) {
            inputWithBias.setEntry(i, 0, 1.0);
        }
        inputWithBias.setSubMatrix(input.getData(), 0, 1);

        RealMatrix z = inputWithBias.multiply(weights);

        return activation.apply(z);
    }

    /**
     * Performs a full forward pass through a Multi-Layer Perceptron (MLP).
     *
     * @param input            Input matrix of shape [nSamples × nFeatures].
     * @param weightMatrices   Array of weight matrices for each layer.
     *                         Each matrix shape: [(nFeatures + 1) × nOutputs],
     *                         with the first row as bias.
     * @param hiddenActivation Activation function for hidden layers.
     * @param outputActivation Activation function for the output layer.
     * @return Output matrix after applying all layers and activations.
     */
    public static RealMatrix forward(
            RealMatrix input,
            RealMatrix[] weightMatrices,
            ActivationFunction hiddenActivation,
            ActivationFunction outputActivation
    ) {
        RealMatrix x = input;

        for (int i = 0; i < weightMatrices.length; i++) {
            ActivationFunction activation = (i == weightMatrices.length - 1)
                    ? outputActivation
                    : hiddenActivation;

            x = layer_forward(x, weightMatrices[i], activation);
        }

        return x;
    }
}
