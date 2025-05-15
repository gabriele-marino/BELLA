package bayesmlp;


import beast.base.inference.CalculationNode;
import org.apache.commons.math3.linear.RealMatrix;

public abstract class ActivationFunction extends CalculationNode {
    public abstract RealMatrix apply(RealMatrix z);

    public abstract String toString();
}
