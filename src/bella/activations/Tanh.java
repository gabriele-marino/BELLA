package bella.activations;

/**
 * Hyperbolic tangent (Tanh) activation function.
 * <p>
 * Computes element-wise:
 *     f(z) = tanh(z)
 */
public class Tanh extends ActivationFunction {

    @Override
    public void initAndValidate() {
        // No parameters to validate for Tanh
    }

    @Override
    public double apply(double z){
        return Math.tanh(z);
    }
}