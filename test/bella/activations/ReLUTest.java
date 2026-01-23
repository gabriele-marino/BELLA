package bella.activations;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ReLU activation function.
 */
public class ReLUTest {

    @Test
    void testReLUPositiveValues() {
        ReLU relu = new ReLU();

        double[][] data = {{1.0, 2.0}, {3.0, 4.0}};
        RealMatrix input = MatrixUtils.createRealMatrix(data);

        RealMatrix output = relu.apply(input);

        assertArrayEquals(input.getData(), output.getData());
    }

    @Test
    void testReLUNegativeValues() {
        ReLU relu = new ReLU();

        double[][] data = {{-1.0, -2.0}, {-3.0, 4.0}};
        RealMatrix input = MatrixUtils.createRealMatrix(data);

        RealMatrix output = relu.apply(input);

        double[][] expected = {{0.0, 0.0}, {0.0, 4.0}};
        assertArrayEquals(expected, output.getData());
    }

    @Test
    void testReLUZeroValues() {
        ReLU relu = new ReLU();

        double[][] data = {{0.0, -0.1}, {0.1, 0.0}};
        RealMatrix input = MatrixUtils.createRealMatrix(data);

        RealMatrix output = relu.apply(input);

        double[][] expected = {{0.0, 0.0}, {0.1, 0.0}};
        assertArrayEquals(expected, output.getData());
    }
}
