package bayesmlp;

import beast.base.core.Input;
import beast.base.inference.parameter.RealParameter;
import org.apache.commons.math3.linear.RealMatrix;

public class Sigmoid extends ActivationFunction {

    public Input<Double> lowerInput = new Input<>("lower",
            "Lower bound for sigmoid scaling. Default 0.0.", 0.0);

    public Input<Double> upperInput = new Input<>("upper",
            "Upper bound for sigmoid scaling. Default 1.0.", 1.0);

    public Input<RealParameter> sInput = new Input<>("shape",
            "Shape (steepness). Larger values mean transition is steeper. Default 1.0.",
            new RealParameter("1.0"));

    public Input<RealParameter> midInput = new Input<>("midpoint",
            "Midpoint: the value of input at which the output is halfway between lower and upper. Default 0.0.",
            new RealParameter("0.0"));





    private double lower;
    private double upper;

    private RealParameter s; //shape (steepness)
    private RealParameter mid; // midpoint: the value of z at which the output is halfway between lower and upper


    @Override
    public void initAndValidate() {
        upper = upperInput.get().doubleValue();
        lower = lowerInput.get().doubleValue();
        if (lower >= upper){
            throw new IllegalArgumentException("Lower bound should not be less than upper bound.");
        }
        s = sInput.get();
        mid = midInput.get();
    }


    @Override
    public RealMatrix apply(RealMatrix z) {
        RealMatrix result = z.copy();
        double shape = s.getCurrent().getArrayValue();
        double midpoint = mid.getCurrent().getArrayValue();
        for (int i = 0; i < z.getRowDimension(); i++) {
            for (int j = 0; j < z.getColumnDimension(); j++) {
                double raw = z.getEntry(i, j);
                double scaled = lower + (upper - lower) / (1 + Math.exp(-shape * (raw - midpoint)));
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
