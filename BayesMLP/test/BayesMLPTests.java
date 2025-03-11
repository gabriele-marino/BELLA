import bayesmlp.priors.BayesMLP;
import beast.base.inference.parameter.IntegerParameter;
import beast.base.inference.parameter.RealParameter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BayesMLPTests {

    private BayesMLP bayesMLP;
    private ArrayList<RealParameter> predictors;
    private ArrayList<RealParameter> weights;
    private IntegerParameter nOutputs;
    private Integer layers;
    private List<Integer> nodes;

    @BeforeEach
    void setUp() {
        // Define predictors
        predictors = new ArrayList<>();
        // nPredictors=2, nInstances=3
        predictors.add(new RealParameter("0.5 1.0 1.5"));
        predictors.add(new RealParameter("2.0 2.5 3.0"));

        // Define weights
        weights = new ArrayList<>();
        weights.add(new RealParameter("0.1 0.2 0.3 0.4")); // For first layer (nPredictors x nNodes)
        weights.add(new RealParameter("0.1 0.2 0.3 0.4")); // For first layer (nPredictors x nNodes)
        weights.add(new RealParameter("0.7 0.8")); // Output layer

        // Define outputs and layers
        nOutputs = new IntegerParameter("1");
        layers = 2; // One hidden layer
        nodes = Arrays.asList(2, 2); // One hidden layer with 2 nodes

        // Initialize BayesMLP instance
        bayesMLP = new BayesMLP();
        bayesMLP.predictorsInput.setValue(predictors, bayesMLP);
        bayesMLP.weightsInput.setValue(weights, bayesMLP);
        bayesMLP.layersInput.setValue(layers, bayesMLP);
        bayesMLP.nodesInput.setValue(nodes, bayesMLP);

        bayesMLP.initAndValidate();
    }

    @Test
    void testInitializationValidInputs() {
        assertNotNull(bayesMLP);
        assertEquals(2, bayesMLP.predictorsInput.get().size());
        assertEquals(2, bayesMLP.layersInput.get());
        assertEquals(2, bayesMLP.nodesInput.get().size());
        assertEquals(2, bayesMLP.nodesInput.get().get(0));
    }

    @Test
    void testInvalidPredictorDimension() {
        predictors.add(new RealParameter("4.0")); // Mismatched dimension
        bayesMLP.predictorsInput.setValue(predictors, bayesMLP);
        assertThrows(IllegalArgumentException.class, bayesMLP::initAndValidate);
    }

    @Test
    void testInvalidNodesLayerMismatch() {
        bayesMLP.layersInput.setValue(5, bayesMLP);
        assertThrows(IllegalArgumentException.class, bayesMLP::initAndValidate);
    }

    @Test
    void testGetDimension() {
        assertEquals(3, bayesMLP.getDimension());
    }

    @Test
    void testGetArrayValue() {
        for (int i = 0; i < bayesMLP.getDimension(); i++){
            assertNotNull(bayesMLP.getArrayValue(i));
            System.out.print(bayesMLP.getArrayValue(i) + " ");
        }
    }
}
