package bella.activations;

/**
 * Identity activation function.
 * <p>
 * Applies element-wise:
 *      f(z) = z
 */
public class Identity extends ActivationFunction {

    @Override
    public void initAndValidate() {
        // No parameters to validate for ReLU
    }

    @Override
    public double apply(double z){
        return z;
    }
}
