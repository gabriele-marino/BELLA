package bayesmlp;

import beast.base.core.Input;
import org.apache.commons.math3.linear.RealMatrix;

public class Sigmoid extends ActivationFunction {

    public Input<Double> lowerInput = new Input<>("lower",
            "Lower bound for sigmoid scaling", 0.0);

    public Input<Double> upperInput = new Input<>("upper",
            "Upper bound for sigmoid scaling", 1.0);

    public Input<Double> sInput = new Input<>("shape",
            "Shape (steepness). Larger values mean transition is steeper.", 1.0);

    public Input<Double> midInput = new Input<>("midpoint",
            "Midpoint: the value of input at which the output is halfway between lower and upper",
            0.0);





    private double lower;
    private double upper;

    private double s; //shape (steepness)
    private double mid; // midpoint: the value of z at which the output is halfway between lower and upper


    @Override
    public void initAndValidate() {
        upper = upperInput.get().doubleValue();
        lower = lowerInput.get().doubleValue();
        if (lower >= upper){
            throw new IllegalArgumentException("Lower bound should not be less than upper bound.");
        }

    }


    @Override
    public RealMatrix apply(RealMatrix z) {
        RealMatrix result = z.copy();
        for (int i = 0; i < z.getRowDimension(); i++) {
            for (int j = 0; j < z.getColumnDimension(); j++) {
                double raw = z.getEntry(i, j);
                double scaled = lower + (upper - lower) / (1 + Math.exp(-s * (raw - mid)));
                result.setEntry(i, j, scaled);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "sigmoid in the interval [" + lower + "," + upper + "]";
    }
}
