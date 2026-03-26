package bella.activations;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Softplus activation function.
 */
public class SoftplusTest {

    @Test
    void testSoftplusBasic() {
        Softplus Softplus = new Softplus();

        double[][] data = {{-1.0, 0.0}, {1.0, 2.0}};
        RealMatrix input = MatrixUtils.createRealMatrix(data);

        RealMatrix output = Softplus.apply(input);

        // Basic checks: Softplus(-1) < Softplus(0) < Softplus(1)
        assertTrue(output.getEntry(0, 0) < output.getEntry(0, 1), "Softplus(-1) < Softplus(0)");
        assertTrue(output.getEntry(0, 1) < output.getEntry(1, 0), "Softplus(0) < Softplus(1)");

        // Check that all outputs are positive
        for (int i = 0; i < output.getRowDimension(); i++) {
            for (int j = 0; j < output.getColumnDimension(); j++) {
                assertTrue(output.getEntry(i, j) > 0, "Softplus output should be positive");
            }
        }
    }

    @Test
    void testSoftplusLargeInputs() {
        Softplus Softplus = new Softplus();

        double[][] data = {{1000.0, 500.0}, {-500.0, -1000.0}};
        RealMatrix input = MatrixUtils.createRealMatrix(data);

        RealMatrix output = Softplus.apply(input);

        // Large positive inputs: Softplus ~ x
        assertEquals(1000.0, output.getEntry(0, 0), 1e-6, "Softplus(1000) ~ 1000");
        assertEquals(500.0, output.getEntry(0, 1), 1e-6, "Softplus(500) ~ 500");

        // Large negative inputs: Softplus ~ 0
        assertEquals(0.0, output.getEntry(1, 0), 1e-6, "Softplus(-500) ~ 0");
        assertEquals(0.0, output.getEntry(1, 1), 1e-6, "Softplus(-1000) ~ 0");
    }

    @Test
    void testSoftplusMonotonicity() {
        Softplus Softplus = new Softplus();

        double[][] data = {{-2.0, -1.0, 0.0, 1.0, 2.0}};
        RealMatrix input = MatrixUtils.createRealMatrix(data);

        RealMatrix output = Softplus.apply(input);

        // Check that Softplus is strictly increasing
        for (int j = 1; j < output.getColumnDimension(); j++) {
            assertTrue(output.getEntry(0, j) > output.getEntry(0, j - 1),
                    String.format("Softplus should be increasing: %.6f !> %.6f",
                            output.getEntry(0, j), output.getEntry(0, j - 1)));
        }
    }

    @Test
    void testSoftplusZeroInput() {
        Softplus Softplus = new Softplus();

        double[][] data = {{0.0}};
        RealMatrix input = MatrixUtils.createRealMatrix(data);

        RealMatrix output = Softplus.apply(input);

        // Softplus(0) = log(2) ~ 0.693
        assertEquals(Math.log(2), output.getEntry(0, 0), 1e-6, "Softplus(0) should be log(2)");
    }
}
