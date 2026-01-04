package bella.activations;

/**
 * Rectified Linear Unit (ReLU) activation function.
 * <p>
 * Applies element-wise:
 *      f(z) = max(0, z)
 */
public class ReLU extends ActivationFunction {

    @Override
    public void initAndValidate() {
        // No parameters to validate for ReLU
    }

    @Override
    public double apply(double z){
        return Math.max(0.0, z);
    }
}
