package bella.activations;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class IdentityTest {

    @Test
    void testIdentity() {
        Identity identity = new Identity();
        double[][] inputData = {
                {1.0, -2.0, 3.5},
                {0.0, 4.0, -1.5}
        };
        RealMatrix input = MatrixUtils.createRealMatrix(inputData);

        RealMatrix output = identity.apply(input);

        assertArrayEquals(input.getData(), output.getData());
    }
}
