package bella.activations;

import beast.base.core.Input;

/**
 * Sigmoid activation function with configurable lower and upper bounds,
 * shape (steepness), and midpoint.
 * <p>
 * Applies element-wise:
 *      f(z) = lower + (upper - lower) / (1 + exp(-shape * (z - midpoint)))
 */
public class Sigmoid extends ActivationFunction {

    public Input<Double> lowerInput = new Input<>(
            "lower",
            "Lower bound of the sigmoid output (minimum value). Default is 0.0.",
            0.0, Input.Validate.OPTIONAL);

    public Input<Double> upperInput = new Input<>(
            "upper",
            "Upper bound of the sigmoid output (maximum value). Default is 1.0.",
            1.0, Input.Validate.OPTIONAL);

    // Parameters that can be estimated via MCMC
    public Input<Double> shapeInput = new Input<>(
            "shape",
            "Shape (steepness) of the sigmoid curve. Larger values produce a steeper transition. "
                    + "Default is 1.0.",
            1.0, Input.Validate.OPTIONAL);

    public Input<Double> midpointInput = new Input<>(
        "midpoint",
        "Input value at which the sigmoid output reaches halfway between lower and upper. "
                + "Default is 0.0.",
        0.0, Input.Validate.OPTIONAL);

    private double lower;
    private double upper;
    private double shape;
    private double midpoint;

    @Override
    public void initAndValidate() {
        upper = upperInput.get();
        lower = lowerInput.get();
        if (lower >= upper) {
            throw new IllegalArgumentException(
                    String.format(
                            "Invalid sigmoid bounds: lower bound (%.3f) must be less than upper bound (%.3f).",
                            lower, upper
                    )
            );
        }

        shape = shapeInput.get();
        midpoint = midpointInput.get();
    }

    @Override
    public double apply(double z){
        return lower + (upper - lower) / (1 + Math.exp(-shape * (z - midpoint)));
    }
}
