package bayesmlp.priors;

import beast.base.core.Description;
import beast.base.core.Function;
import beast.base.core.Input;
import beast.base.inference.CalculationNode;
import beast.base.inference.parameter.RealParameter;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.ops.transforms.Transforms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@Description("Designed to be used within Beast2." +
        "The weights are the parameters to be estimated. Training of weights should be implemented" +
        "The predictors are the input data. The number of outputs is the number of outputs in the output layer. " +
        "The number of layers is the number of hidden layers. " +
        "The nodes are the number of nodes in each hidden layer." +
        "The hidden layers use the ReLU activation function. The output layer uses the softplus activation function" )

public class BayesMLP extends CalculationNode implements Function {

    public  Input<ArrayList<RealParameter>>  predictorsInput = new Input<>("predictor",
            "One or more predictor for the GLM, e.g. numbers of flights between different locations",
            new ArrayList<>(), Input.Validate.REQUIRED);

    public Input<ArrayList<RealParameter>> weightsInput = new Input<>("weights",
            "GLM_ML weights for each layer (hidden and output).",new ArrayList<>(), Input.Validate.REQUIRED);

//    public Input<IntegerParameter> nOutputsInput = new Input<>("nOutputs",
//            "GLM_ML number of outputs the output layer. Default is 1.");
    public Input<Integer> layersInput = new Input<>("layers",
            "Number of hidden layers in GLM_ML. Default is 1.", 1);

    public Input<List<Integer>> nodesInput = new Input<>("nodes",
            "Number of nodes in each hidden layer in GLM_ML.", new ArrayList<>());



    // TODO Maybe have some of the below in the future
    // Different activation functions?
//    public Input<BooleanParameter> indicatorsInput = new Input<>("indicators",
//            "Indicators for predictor inclusion/exclusion in GLM.", Input.Validate.REQUIRED);
//    public Input<RealParameter> scaleFactorInput = new Input<>("scaleFactor",
//            "Scale factor.", new RealParameter("1.0"), Input.Validate.OPTIONAL);
//
//    public Input<RealParameter> errorInput = new Input<>("error",
//            "Error terms.", Input.Validate.OPTIONAL);
//
//    public Input<Boolean> transformInput = new Input<>("transform",
//            "Boolean value to log transform and scale predictors. Default true.", false,
//            Input.Validate.OPTIONAL);


    INDArray predictors;
    INDArray inputData;
    INDArray output;
    ArrayList<RealParameter> weights;
    int parameterSize, nPredictor; // parameter size as instances in the original example
    int nLayers;

    private INDArray[] weightMatrices;
    private boolean needsRecalculation = true;

    @Override
    public void initAndValidate() {
        nPredictor = predictorsInput.get().size();
        parameterSize = predictorsInput.get().get(0).getDimension();

        for (RealParameter pred : predictorsInput.get()) {
            if (parameterSize != pred.getDimension()) {
                throw new IllegalArgumentException("GLM Predictors do not have the same dimension " +
                        parameterSize + "!=" + pred.getDimension());
            }
        }

        List<Integer> nodes = nodesInput.get();
        nLayers = layersInput.get();

        if (nLayers >0 && nodes.size() != nLayers) {
            throw new IllegalArgumentException("GLM number of nodes do not match number of layers " +
                    nodes.size() + "!=" + nLayers);
        }

        predictors = Nd4j.create(convertToDoubleArray(predictorsInput.get())).transpose();
        weights = weightsInput.get();
//        nOutputs = nOutputsInput.get().getValue();
        Integer[] nWeights = new Integer[weights.size()];

        if (nLayers == 0) {
            weights.get(0).setDimension(nPredictor);
        } else {
            for (int i = 0; i < nLayers; i++) {
                if (i == 0) {
                    nWeights[i] = nPredictor * nodes.get(0);
                } else {
                    nWeights[i] = nodes.get(i - 1) * nodes.get(i);
                }
                weights.get(i).setDimension(nWeights[i]);
            }
            weights.get(nLayers).setDimension(nodes.get(nLayers - 1));
        }

        // Initialize weight matrices array
        weightMatrices = new INDArray[nLayers + 1];
        updateWeightMatrices();
    }

    @Override
    public int getDimension() {
        return parameterSize;
    }

    @Override
    public double getArrayValue(int i) {
        checkAndUpdateWeights();  // Ensure latest weights before computation
        if (needsRecalculation) {
            recalculate();
            needsRecalculation = false;
        }
        return output.getDouble(i);
    }

    private void checkAndUpdateWeights() {
        boolean updated = false;

        for (int i = 0; i < weights.size(); i++) {
            double[] w = weightMatrices[i].data().asDouble();
            boolean isEqual = Arrays.mismatch(w, weights.get(i).getDoubleValues()) == -1;
            if (!isEqual) {
                    weightMatrices[i].assign(Nd4j.create(weights.get(i).getDoubleValues()));
                    updated = true;
//                    break;
            }
//            if (weights.get(i).somethingIsDirty()) {
//                // Update the corresponding weight matrix
//                weightMatrices[i].assign(Nd4j.create(weights.get(i).getDoubleValues()));
//                updated = true;
//            }
        }

        if (updated) {
            needsRecalculation = true;  // Trigger recalculation only if weights changed
        }
    }

    private void recalculate() {
        if (nLayers == 0) {
            output = predictors.mmul(weightMatrices[0]);
            return;
        }

//        output = predictors.dup();  // Avoid modifying predictors directly

        // Hidden layers
        for (int l = 0; l < nLayers; l++) {
            if (l==0){
                output = runLayer(predictors, weightMatrices[l], BayesMLP::relu);
            } else {
                output = runLayer(output, weightMatrices[l], BayesMLP::relu);
            }
        }

        // Output layer
        output = runLayer(output, weightMatrices[nLayers], BayesMLP::softplus);
    }



    // TODO could also just use activation functions from:
    // https://github.com/deeplearning4j/deeplearning4j/blob/master/nd4j/nd4j-backends/nd4j-api-parent/nd4j-api/src/main/java/org/nd4j/linalg/activations/Activation.java

    private static INDArray softplus(INDArray z) {
        return Transforms.log(Transforms.exp(z).add(1), false);
    }

    private static INDArray relu(INDArray z) {
        return Transforms.max(z, 0);
    }

    private static INDArray runLayer(INDArray x1, INDArray x2, java.util.function.Function<INDArray, INDArray> activationFunction) {
        INDArray z = x1.mmul(x2);
        return (activationFunction != null) ? activationFunction.apply(z) : z;
    }

    private void updateWeightMatrices() {
        if (nLayers == 0) {
            weightMatrices[0] = Nd4j.create(weights.get(0).getDoubleValues()).reshape(nPredictor, 1);
        } else {
            for (int i = 0; i < nLayers; i++) {
                if (i == 0) {
                    weightMatrices[i] = Nd4j.create(weights.get(0).getDoubleValues()).reshape(nPredictor, nodesInput.get().get(0));
                } else {
                    weightMatrices[i] = Nd4j.create(weights.get(i).getDoubleValues()).reshape(nodesInput.get().get(i - 1), nodesInput.get().get(i));
                }
            }
            weightMatrices[nLayers] = Nd4j.create(weights.get(nLayers).getDoubleValues()).reshape(nodesInput.get().get(nLayers - 1), 1);
        }
    }


    // Helper converter
    private static double[][] convertToDoubleArray(ArrayList<RealParameter> realParams) {
        // Dimensions:
        int rows = realParams.size();
        int cols = realParams.get(0).getDimension(); // All must have the same dimension

        double[][] result = new double[rows][cols];

        // Fill the 2D array
        for (int i = 0; i < rows; i++) {
            RealParameter rp = realParams.get(i);
            for (int j = 0; j < cols; j++) {
                result[i][j] = rp.getValue(j);
            }
        }

        return result;
    }
}