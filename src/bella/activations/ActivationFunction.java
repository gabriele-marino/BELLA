package bella.activations;

import beast.base.inference.CalculationNode;

import org.apache.commons.math3.linear.RealMatrix;

/**
 * Abstract base class for activation functions.
 * <p>
 * An activation function takes a matrix of pre-activation values
 * (typically denoted as z) and applies a non-linear transformation.
 * <p>
 * Concrete subclasses must implement the element-wise transformation.
 */
public abstract class ActivationFunction extends CalculationNode {

    /**
     * Apply the activation function element-wise to a single value.
     *
     * @param z input value
     * @return transformed value after applying the activation function
     */
    public abstract double apply(double z);

    /**
     * Apply the activation function to the input matrix.

     * Default implementation: applies the element-wise function to each entry.

     * @param z matrix of pre-activation values
     * @return transformed matrix after applying the activation function
     */
    public RealMatrix apply(RealMatrix z) {
        RealMatrix result = z.copy();
        for (int i = 0; i < z.getRowDimension(); i++) {
            for (int j = 0; j < z.getColumnDimension(); j++) {
                result.setEntry(i, j, apply(z.getEntry(i, j)));
            }
        }
        return result;
    }
}
