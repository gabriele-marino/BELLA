package bella.activations;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Tanh activation function.
 */
public class TanhTest {

    @Test
    void testTanhBasic() {
        Tanh tanh = new Tanh();

        double[][] data = {{-1.0, 0.0}, {1.0, 2.0}};
        RealMatrix input = MatrixUtils.createRealMatrix(data);
        RealMatrix output = tanh.apply(input);

        // Known outputs
        assertEquals(Math.tanh(-1.0), output.getEntry(0, 0), 1e-6, "tanh(-1)");
        assertEquals(Math.tanh(0.0), output.getEntry(0, 1), 1e-6, "tanh(0)");
        assertEquals(Math.tanh(1.0), output.getEntry(1, 0), 1e-6, "tanh(1)");
        assertEquals(Math.tanh(2.0), output.getEntry(1, 1), 1e-6, "tanh(2)");

        // Check output range [-1, 1]
        for (int i = 0; i < output.getRowDimension(); i++) {
            for (int j = 0; j < output.getColumnDimension(); j++) {
                double val = output.getEntry(i, j);
                assertTrue(val >= -1.0 && val <= 1.0,
                        "Tanh output should be in [-1, 1], got " + val);
            }
        }
    }

    @Test
    void testTanhMonotonicity() {
        Tanh tanh = new Tanh();

        double[][] data = {{-2.0, -1.0, 0.0, 1.0, 2.0}};
        RealMatrix input = MatrixUtils.createRealMatrix(data);
        RealMatrix output = tanh.apply(input);

        // Output should always increase with input
        for (int j = 1; j < output.getColumnDimension(); j++) {
            assertTrue(output.getEntry(0, j) > output.getEntry(0, j - 1),
                    String.format("Tanh should be increasing: %.6f !> %.6f",
                            output.getEntry(0, j), output.getEntry(0, j - 1)));
        }
    }

    @Test
    void testTanhLargeInputs() {
        Tanh tanh = new Tanh();

        double[][] data = {{1000.0, -1000.0}, {500.0, -500.0}};
        RealMatrix input = MatrixUtils.createRealMatrix(data);
        RealMatrix output = tanh.apply(input);

        // For very large inputs, tanh(x) ~ 1 or -1
        assertEquals(1.0, output.getEntry(0, 0), 1e-6, "tanh(1000) ~ 1");
        assertEquals(-1.0, output.getEntry(0, 1), 1e-6, "tanh(-1000) ~ -1");
        assertEquals(1.0, output.getEntry(1, 0), 1e-6, "tanh(500) ~ 1");
        assertEquals(-1.0, output.getEntry(1, 1), 1e-6, "tanh(-500) ~ -1");
    }

    @Test
    void testTanhZeroInput() {
        Tanh tanh = new Tanh();

        double[][] data = {{0.0}};
        RealMatrix input = MatrixUtils.createRealMatrix(data);
        RealMatrix output = tanh.apply(input);

        // Tanh(0) = 0
        assertEquals(0.0, output.getEntry(0, 0), 1e-6, "tanh(0) should be 0");
    }
}
