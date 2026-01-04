package bella.activations;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Sigmoid activation function.
 */
public class SigmoidTest {

    @Test
    void testSigmoidDefaultBounds() {
        Sigmoid sigmoid = new Sigmoid();
        // Default: lower=0.0, upper=1.0, shape=1.0, midpoint=0.0
        sigmoid.initAndValidate();

        double[][] data = {{-1.0, 0.0}, {1.0, 2.0}};
        RealMatrix input = MatrixUtils.createRealMatrix(data);

        RealMatrix output = sigmoid.apply(input);

        // Check expected values
        assertTrue(output.getEntry(0, 0) < 0.5, "sigmoid(-1) should be < 0.5");
        assertEquals(0.5, output.getEntry(0, 1), 1e-6, "sigmoid(0) should be ~0.5");
        assertTrue(output.getEntry(1, 0) > 0.5, "sigmoid(1) should be > 0.5");
        assertTrue(output.getEntry(1, 1) > 0.5, "sigmoid(2) should be > 0.5");
    }

    @Test
    void testSigmoidCustomBounds() {
        Sigmoid sigmoid = new Sigmoid();
        sigmoid.initByName(
                "lower", 1.0,
                "upper", 3.0
        );

        double[][] data = {{0.0, 1.0, 10.0}};
        RealMatrix input = MatrixUtils.createRealMatrix(data);
        RealMatrix output = sigmoid.apply(input);

        // Output should be within [1.0, 3.0]
        for (int i = 0; i < output.getRowDimension(); i++) {
            for (int j = 0; j < output.getColumnDimension(); j++) {
                double val = output.getEntry(i, j);
                assertTrue(val >= 1.0 && val <= 3.0,
                        "Sigmoid output out of bounds: " + val);
            }
        }
    }

    @Test
    void testSigmoidSteepnessAndMidpoint() {
        Sigmoid sigmoid = new Sigmoid();
        sigmoid.initByName(
                // Default: lower=0.0, upper=1.0
                "shape", 10.0,
                "midpoint", 1.0
        );

        double[][] data = {{0.0, 1.0, 2.0}};
        RealMatrix input = MatrixUtils.createRealMatrix(data);
        RealMatrix output = sigmoid.apply(input);

        // Very steep sigmoid: values below midpoint ~ lower bound
        assertTrue(output.getEntry(0, 0) <  0.01, "Value below midpoint should be near lower bound");

        // At midpoint ~ middle of bounds
        assertEquals(0.5, output.getEntry(0, 1), 0.05, "Value at midpoint should be ~middle of bounds");

        // Above midpoint ~ upper bound
        assertTrue(output.getEntry(0, 2) > 1 - 0.01, "Value above midpoint should be near upper bound");
    }
}
