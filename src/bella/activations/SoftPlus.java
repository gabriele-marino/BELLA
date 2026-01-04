package bella.activations;

/**
 * SoftPlus activation function.
 * <p>
 * Applies element-wise:
 *     f(z) = log(1 + exp(z))
 */
public class SoftPlus extends ActivationFunction {

    @Override
    public void initAndValidate() {
        // No parameters to validate for SoftPlus
    }

    @Override
    public double apply(double z){
        return z > 0 ? z + Math.log1p(Math.exp(-z)) : Math.log1p(Math.exp(z));
    }
}
