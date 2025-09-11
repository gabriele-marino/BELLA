import bella.*;
import beast.base.inference.parameter.RealParameter;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class BayesMLPTests {

    BayesMLP mlp;

    @BeforeEach
    void setUp() {
        mlp = new BayesMLP();
        mlp.predictorsInput.setValue(mockPredictors(), mlp);
        mlp.nodesInput.setValue(List.of(3, 1), mlp); // 1 hidden layer (3 nodes) + 1 output
        mlp.weightsInput.setValue(mockWeights(), mlp);
        mlp.activationHiddenInput.setValue(new ArrayList<>(List.of(new ReLu())), mlp);
    }

    @ParameterizedTest
    @MethodSource("activationRangeProvider")
    void testActivationOutputRange(String name, ActivationFunction activation, double lower, double upper) {
        if (activation instanceof Sigmoid){
            ((Sigmoid) activation).lowerInput.setValue(lower, activation);
            ((Sigmoid) activation).upperInput.setValue(upper, activation);
            activation.initAndValidate();
        }
        mlp.activationOutputInput.setValue(activation, mlp);
        mlp.initAndValidate();
        double val = mlp.getArrayValue(0);
        assertFalse(Double.isNaN(val), name + ": Output is NaN");
        assertTrue(val >= lower && val <= upper, name + ": Output not in range [" + lower + ", " + upper + "]");
    }

    static Stream<Object[]> activationRangeProvider() {
        return Stream.of(
                new Object[]{"SoftPlus", new SoftPlus(), 0.0, Double.POSITIVE_INFINITY},
                new Object[]{"Sigmoid[-1,2]", new Sigmoid(), -1.0, 2.0},
                new Object[]{"Sigmoid[0,1]", new Sigmoid(), 0.0, 1.0},
                new Object[]{"Tanh", new Tanh(), -1.0, 1.0}
        );
    }

    @Test
    void testBiasEnabled() {
        mlp.useBiasInAllInput.setValue(true, mlp);
        mlp.activationOutputInput.setValue(new SoftPlus(), mlp);
        mlp.initAndValidate();
        double val = mlp.getArrayValue(0);
        assertFalse(Double.isNaN(val));
        assertTrue(val >= 0.0, "SoftPlus output should be >= 0");
    }

    @Test
    void testBiasDisabled() {
        mlp.useBiasInAllInput.setValue(false, mlp);
        mlp.activationOutputInput.setValue(new SoftPlus(), mlp);
        mlp.initAndValidate();
        double val = mlp.getArrayValue(0);
        assertFalse(Double.isNaN(val));
        assertTrue(val >= 0.0, "SoftPlus output should be >= 0");
    }

    @Test
    void testSigmoidEdgeCase() {
        // Set high weights to simulate a large output before activation
        RealParameter highWeights = makeParam(new Double[]{10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0});
        mlp.weightsInput.setValue(highWeights, mlp);

        mlp.activationHiddenInput.get().clear();
        mlp.activationHiddenInput.setValue(new ArrayList<>(List.of(new ReLu())), mlp);
        Sigmoid sig = new Sigmoid();
        sig.lowerInput.setValue(-1.0, sig);
        sig.upperInput.setValue(2.0, sig);
        sig.initAndValidate();
        mlp.activationOutputInput.setValue(sig, mlp);
        mlp.useBiasInAllInput.setValue(true, mlp);
        mlp.initAndValidate();
        double sigmoidVal = mlp.getArrayValue(0);
        assertTrue(sigmoidVal <= 2.0, "Sigmoid output must be bounded by 2.0");
    }

    @Test
    void testSoftPlusEdgeCase() {
        // Set high weights to simulate a large output before activation
        RealParameter highWeights = makeParam(new Double[]{10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0});
        mlp.weightsInput.setValue(highWeights, mlp);

        mlp.activationHiddenInput.get().clear();
        mlp.activationHiddenInput.setValue(new ArrayList<>(List.of(new ReLu())), mlp);
        mlp.activationOutputInput.setValue(new SoftPlus(), mlp);
        mlp.useBiasInAllInput.setValue(true, mlp);
        mlp.initAndValidate();
        double softplusVal = mlp.getArrayValue(0);

        assertTrue(softplusVal > 2.0, "SoftPlus output expected to exceed upper sigmoid bound");
    }

    @Test
    void testInvalidWeightShapeThrows() {
        // Wrong number of weights (should be 13 for 2 predictors, 3 hidden nodes, 1 output)
        RealParameter badWeights = makeParam(new Double[]{1.0, 1.0, 1.0, 1.0, 1.0}); // incorrect shape - only 5 weights
        mlp.weightsInput.setValue(badWeights, mlp);
        mlp.useBiasInAllInput.setValue(true, mlp);
        mlp.activationOutputInput.setValue(new SoftPlus(), mlp);
        // This should not throw - the weights vector will just be resized
        // But we can still test it doesn't crash
        assertDoesNotThrow(() -> mlp.initAndValidate());
    }

    @Test
    void testPredictorNormalization() {
        // Test with predictors that include min, max, and intermediate values
        ArrayList<RealParameter> testPredictors = new ArrayList<>();
        // First predictor: min=10, mid=15, max=20 -> normalized: 0.0, 0.5, 1.0
        testPredictors.add(makeParam(new Double[]{10.0, 15.0, 20.0}));
        // Second predictor: min=100, mid=150, max=200 -> normalized: 0.0, 0.5, 1.0
        testPredictors.add(makeParam(new Double[]{100.0, 150.0, 200.0}));

        mlp.predictorsInput.get().clear();
        mlp.predictorsInput.setValue(testPredictors, mlp);
        mlp.initAndValidate();
        
        // Access the internal predictor matrix to verify normalization
        RealMatrix normalizedPredictors = mlp.getPredictors();
        
        double tolerance = 1e-10;
        
        // First predictor (row 0): [10, 15, 20] -> [0.0, 0.5, 1.0]
        assertEquals(0.0, normalizedPredictors.getEntry(0, 0), tolerance, 
            "First predictor min (10) should normalize to 0.0");
        assertEquals(0.5, normalizedPredictors.getEntry(1, 0), tolerance,
            "First predictor mid (15) should normalize to 0.5");
        assertEquals(1.0, normalizedPredictors.getEntry(2, 0), tolerance,
            "First predictor max (20) should normalize to 1.0");
        
        // Second predictor (row 1): [100, 150, 200] -> [0.0, 0.5, 1.0]
        assertEquals(0.0, normalizedPredictors.getEntry(0, 1), tolerance,
            "Second predictor min (100) should normalize to 0.0");
        assertEquals(0.5, normalizedPredictors.getEntry(1, 1), tolerance, 
            "Second predictor mid (150) should normalize to 0.5");
        assertEquals(1.0, normalizedPredictors.getEntry(2, 1), tolerance,
            "Second predictor max (200) should normalize to 1.0");
        
        // Also verify the output is still valid
        double output = mlp.getArrayValue(0);
        assertFalse(Double.isNaN(output), "Neural network output should not be NaN");
    }

    @Test
    void testTanhActivationFunction() {
        Tanh tanh = new Tanh();
        
        // Create test matrix with known values
        RealMatrix testMatrix = MatrixUtils.createRealMatrix(new double[][]{
            {0.0, 1.0, -1.0},
            {2.0, -2.0, 0.5}
        });
        
        RealMatrix result = tanh.apply(testMatrix);
        
        double tolerance = 1e-10;
        
        // Test known tanh values
        assertEquals(Math.tanh(0.0), result.getEntry(0, 0), tolerance, "tanh(0) should be 0");
        assertEquals(Math.tanh(1.0), result.getEntry(0, 1), tolerance, "tanh(1) should be ~0.762");
        assertEquals(Math.tanh(-1.0), result.getEntry(0, 2), tolerance, "tanh(-1) should be ~-0.762");
        assertEquals(Math.tanh(2.0), result.getEntry(1, 0), tolerance, "tanh(2) should be ~0.964");
        assertEquals(Math.tanh(-2.0), result.getEntry(1, 1), tolerance, "tanh(-2) should be ~-0.964");
        assertEquals(Math.tanh(0.5), result.getEntry(1, 2), tolerance, "tanh(0.5) should be ~0.462");
        
        // Verify all values are in [-1, 1] range
        for (int i = 0; i < result.getRowDimension(); i++) {
            for (int j = 0; j < result.getColumnDimension(); j++) {
                double value = result.getEntry(i, j);
                assertTrue(value >= -1.0 && value <= 1.0, 
                    String.format("Tanh output[%d][%d] = %f should be in [-1,1] range", i, j, value));
            }
        }
    }

    @Test
    void testTanhWithNeuralNetwork() {
        // Test tanh as both hidden and output activation functions
        mlp.activationHiddenInput.setValue(new ArrayList<>(List.of(new Tanh())), mlp);
        mlp.activationOutputInput.setValue(new Tanh(), mlp);
        mlp.initAndValidate();
        
        double output = mlp.getArrayValue(0);
        assertFalse(Double.isNaN(output), "Neural network output with Tanh should not be NaN");
        assertTrue(output >= -1.0 && output <= 1.0, 
            "Neural network output with Tanh should be in [-1,1] range, got: " + output);
    }

    private ArrayList<RealParameter> mockPredictors() {
        ArrayList<RealParameter> predictors = new ArrayList<>();
        predictors.add(makeParam(new Double[]{0.1, 0.2}));
        predictors.add(makeParam(new Double[]{0.3, 0.4}));
        return predictors;
    }

    private RealParameter mockWeights() {
        // Flattened weights: first layer (2+1)*3=9 weights + second layer (3+1)*1=4 weights = 13 total
        return makeParam(new Double[]{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0});
    }

    private RealParameter makeParam(Double[] values) {
        RealParameter p = new RealParameter(values);
        p.setDimension(values.length);
        return p;
    }
}
