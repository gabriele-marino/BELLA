package bella.util;

import bella.activations.Identity;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MLPUtilTest {

    @Test
    void testLayerForward() {
        // Input data: 4 rows (samples) x 2 columns (features)
        double[][] inputData = {
                {7.0, 2.0},
                {3.0, 4.0},
                {5.0, 6.0},
        };
        RealMatrix input = MatrixUtils.createRealMatrix(inputData);

        // Weights: 3 rows (bias + 2 features) x 2 outputs
        double[][] weightsData = {
                {0.5, -0.5},  // bias
                {7.0, 2.0},   // weights for feature 1
                {3.0, 4.0}    // weights for feature 2
        };
        RealMatrix weights = MatrixUtils.createRealMatrix(weightsData);

        RealMatrix output = MLPUtil.layer_forward(input, weights, new Identity());

        // Manually compute expected result
        double[][] expected = new double[3][2];
        // Row 0: z = input[0] * weightsPart + bias
        expected[0][0] = 0.5 + 7.0*7.0 + 3.0*2.0; // 0.5 + 1 + 6 = 7.5
        expected[0][1] = -0.5 + 2.0*7.0 + 4.0*2.0; // -0.5 + 2 + 8 = 9.5
        // Row 1
        expected[1][0] = 0.5 + 7.0*3.0 + 3.0*4.0; // 0.5 + 3 + 12 = 15.5
        expected[1][1] = -0.5 + 2.0*3.0 + 4.0*4.0; // -0.5 + 6 + 16 = 21.5
        // Row 2
        expected[2][0] = 0.5 + 7.0*5.0 + 3.0*6.0; // 0.5 + 1 + 6 = 7.5
        expected[2][1] = -0.5 + 2.0*5.0 + 4.0*6.0; // -0.5 + 2 + 8 = 9.5

        // Check each row
        for (int i = 0; i < expected.length; i++) {
            assertArrayEquals(expected[i], output.getRow(i), 1e-9);
        }
    }
}
