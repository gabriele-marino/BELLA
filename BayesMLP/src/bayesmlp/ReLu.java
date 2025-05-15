package bayesmlp;

import org.apache.commons.math3.linear.RealMatrix;

public class ReLu extends ActivationFunction {

    @Override
    public void initAndValidate() {

    }

    @Override
    public RealMatrix apply(RealMatrix z) {
        RealMatrix result = z.copy();
        for (int i = 0; i < z.getRowDimension(); i++) {
            for (int j = 0; j < z.getColumnDimension(); j++) {
                result.setEntry(i, j, Math.max(0, z.getEntry(i, j)));
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "relu";
    }


}
