import bayesmlp.*;
import beast.base.inference.parameter.RealParameter;
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
                new Object[]{"Sigmoid[0,1]", new Sigmoid(), 0.0, 1.0}
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
        mlp.weightsInput.get().clear();
        ArrayList<RealParameter> weights = new ArrayList<>();
        weights.add(makeParam(new Double[]{10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0})); // (2+1)*3
        weights.add(makeParam(new Double[]{10.0, 10.0, 10.0, 10.0})); // (3+1)*1
        mlp.weightsInput.setValue(weights, mlp);

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
        mlp.weightsInput.get().clear();
        ArrayList<RealParameter> weights = new ArrayList<>();
        weights.add(makeParam(new Double[]{10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0})); // (2+1)*3
        weights.add(makeParam(new Double[]{10.0, 10.0, 10.0, 10.0})); // (3+1)*1
        mlp.weightsInput.setValue(weights, mlp);

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
        ArrayList<RealParameter> badWeights = new ArrayList<>();
        badWeights.add(makeParam(new Double[]{1.0, 1.0, 1.0, 1.0, 1.0})); // incorrect shape
        badWeights.add(makeParam(new Double[]{1.0, 1.0, 1.0, 1.0}));
        mlp.weightsInput.setValue(badWeights, mlp);
        mlp.useBiasInAllInput.setValue(true, mlp);
        mlp.activationOutputInput.setValue(new SoftPlus(), mlp);
        assertThrows(IllegalArgumentException.class, () -> mlp.initAndValidate());
    }


    private ArrayList<RealParameter> mockPredictors() {
        ArrayList<RealParameter> predictors = new ArrayList<>();
        predictors.add(makeParam(new Double[]{0.1, 0.2}));
        predictors.add(makeParam(new Double[]{0.3, 0.4}));
        return predictors;
    }

    private ArrayList<RealParameter> mockWeights() {
        ArrayList<RealParameter> weights = new ArrayList<>();
        weights.add(makeParam(new Double[]{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0}));  // (2+1) * 3
        weights.add(makeParam(new Double[]{1.0, 1.0, 1.0, 1.0}));  // (3+1) * 1
        return weights;
    }

    private RealParameter makeParam(Double[] values) {
        RealParameter p = new RealParameter(values);
        p.setDimension(values.length);
        return p;
    }
}
