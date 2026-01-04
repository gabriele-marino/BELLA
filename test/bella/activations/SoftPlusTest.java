package bella.activations;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SoftPlus activation function.
 */
public class SoftPlusTest {

    @Test
    void testSoftPlusBasic() {
        SoftPlus softPlus = new SoftPlus();

        double[][] data = {{-1.0, 0.0}, {1.0, 2.0}};
        RealMatrix input = MatrixUtils.createRealMatrix(data);

        RealMatrix output = softPlus.apply(input);

        // Basic checks: SoftPlus(-1) < SoftPlus(0) < SoftPlus(1)
        assertTrue(output.getEntry(0, 0) < output.getEntry(0, 1), "SoftPlus(-1) < SoftPlus(0)");
        assertTrue(output.getEntry(0, 1) < output.getEntry(1, 0), "SoftPlus(0) < SoftPlus(1)");

        // Check that all outputs are positive
        for (int i = 0; i < output.getRowDimension(); i++) {
            for (int j = 0; j < output.getColumnDimension(); j++) {
                assertTrue(output.getEntry(i, j) > 0, "SoftPlus output should be positive");
            }
        }
    }

    @Test
    void testSoftPlusLargeInputs() {
        SoftPlus softPlus = new SoftPlus();

        double[][] data = {{1000.0, 500.0}, {-500.0, -1000.0}};
        RealMatrix input = MatrixUtils.createRealMatrix(data);

        RealMatrix output = softPlus.apply(input);

        // Large positive inputs: SoftPlus ~ x
        assertEquals(1000.0, output.getEntry(0, 0), 1e-6, "SoftPlus(1000) ~ 1000");
        assertEquals(500.0, output.getEntry(0, 1), 1e-6, "SoftPlus(500) ~ 500");

        // Large negative inputs: SoftPlus ~ 0
        assertEquals(0.0, output.getEntry(1, 0), 1e-6, "SoftPlus(-500) ~ 0");
        assertEquals(0.0, output.getEntry(1, 1), 1e-6, "SoftPlus(-1000) ~ 0");
    }

    @Test
    void testSoftPlusMonotonicity() {
        SoftPlus softPlus = new SoftPlus();

        double[][] data = {{-2.0, -1.0, 0.0, 1.0, 2.0}};
        RealMatrix input = MatrixUtils.createRealMatrix(data);

        RealMatrix output = softPlus.apply(input);

        // Check that SoftPlus is strictly increasing
        for (int j = 1; j < output.getColumnDimension(); j++) {
            assertTrue(output.getEntry(0, j) > output.getEntry(0, j - 1),
                    String.format("SoftPlus should be increasing: %.6f !> %.6f",
                            output.getEntry(0, j), output.getEntry(0, j - 1)));
        }
    }

    @Test
    void testSoftPlusZeroInput() {
        SoftPlus softPlus = new SoftPlus();

        double[][] data = {{0.0}};
        RealMatrix input = MatrixUtils.createRealMatrix(data);

        RealMatrix output = softPlus.apply(input);

        // SoftPlus(0) = log(2) ~ 0.693
        assertEquals(Math.log(2), output.getEntry(0, 0), 1e-6, "SoftPlus(0) should be log(2)");
    }
}
