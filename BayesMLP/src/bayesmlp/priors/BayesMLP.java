package bayesmlp.priors;

import beast.base.core.Description;
import beast.base.core.Function;
import beast.base.core.Input;
import beast.base.inference.CalculationNode;
import beast.base.inference.parameter.RealParameter;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;
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

        public Input<Boolean> useBiasInAllInput = new Input<>("useBiasInAll",
                "Should we use bias term for all layers. If false, bias term is used only in output layer.",
                true, Input.Validate.OPTIONAL);

    public Input<List<Integer>> nodesInput = new Input<>("nodes",
            "Number of nodes in each hidden and output layer in GLM_ML.", new ArrayList<>(), Input.Validate.REQUIRED);


    INDArray predictors;

    int nonOutputBiasTerm = 1;

    ArrayList<RealParameter> weights;
    int parameterSize;
    int nPredictor;
    int nHiddenLayers; // parameter size as instances in the original example
    private INDArray[] weightMatrices;
    INDArray output;
    private boolean needsRecalculation = true;

    @Override
    public void initAndValidate() {
        nPredictor = predictorsInput.get().size();
        parameterSize = predictorsInput.get().get(0).getDimension();

        if (!useBiasInAllInput.get())
            nonOutputBiasTerm = 0; // If bias term is not used in all layers, then it is only used in the output layer


        for (RealParameter pred : predictorsInput.get()) {
            if (parameterSize != pred.getDimension()) {
                throw new IllegalArgumentException("GLM Predictors do not have the same dimension " +
                        parameterSize + "!=" + pred.getDimension());
            }
        }

        List<Integer> nodes = nodesInput.get();
        if (nodesInput.get().size() == 0)
            throw new IllegalArgumentException("Size of nodes vector must be at least one to note nodes in the output layer.");
        nHiddenLayers = nodes.size()-1;

        predictors = Nd4j.create(convertToDoubleArray(predictorsInput.get())).transpose();
        weights = weightsInput.get();


        Integer[] nWeights = new Integer[weights.size()];

        if (nHiddenLayers == 0) {
            weights.get(0).setDimension(nPredictor+1); // output always has bias term, hence +1
        } else {
            for (int i = 0; i < nHiddenLayers; i++) {
                if (i == 0) {
                    nWeights[i] = (nPredictor + nonOutputBiasTerm) * nodes.get(0);
                } else {
                    nWeights[i] = (nodes.get(i - 1) + nonOutputBiasTerm)* nodes.get(i);
                }
                weights.get(i).setDimension(nWeights[i]);
            }
            weights.get(nHiddenLayers).setDimension(nodes.get(nHiddenLayers - 1)+1); // output always has bias term, hence +1
        }
        initializeMatrices();
    }

    private void initializeMatrices() {
        weightMatrices = new INDArray[nHiddenLayers + 1];
        for (int i = 0; i <= nHiddenLayers; i++) {
            weightMatrices[i] = Nd4j.create(weights.get(i).getDoubleValues()).reshape(getLayerShape(i));
        }
    }

    private int[] getLayerShape(int layer) {
        if (nHiddenLayers == 0) return new int[]{nPredictor+1, nodesInput.get().get(0)}; // output always gets bias, so +1
        if (layer == 0) return new int[]{nPredictor+nonOutputBiasTerm, nodesInput.get().get(0)};
        if (layer == nHiddenLayers) return new int[]{nodesInput.get().get(layer - 1)+1, 1};// output always gets bias, so +1
        return new int[]{nodesInput.get().get(layer - 1)+nonOutputBiasTerm, nodesInput.get().get(layer)}; // bias in hidden layer only if nonOutputBiasTerm!=0
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
            if (weights.get(i).somethingIsDirty()) {
                    weightMatrices[i].assign(Nd4j.create(weights.get(i).getDoubleValues()));
                    updated = true;
            }
        }

        if (updated) {
            needsRecalculation = true;  // Trigger recalculation only if weights changed
        }
    }

    private void recalculate() {
        if (nHiddenLayers == 0) {
            output = runLayer(predictors, weightMatrices[nHiddenLayers], BayesMLP::softplus);
            return;
        }

        // Hidden layers
        for (int l = 0; l < nHiddenLayers; l++) {
            if (l==0){
                output = runLayer(predictors, weightMatrices[l], BayesMLP::relu);
            } else {
                output = runLayer(output, weightMatrices[l], BayesMLP::relu);
            }
        }

        // Output layer
        output = runLayer(output, weightMatrices[nHiddenLayers], BayesMLP::softplus);
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
        INDArray z;
        if (x1.columns() == x2.rows()) {
            z = x1.mmul(x2);
        } else {
            INDArray weights = x2.get(NDArrayIndex.interval(1, x2.rows()), NDArrayIndex.all()); //exclude bias
            z = x1.mmul(weights);
            INDArray bias = x2.getRow(0); // add bias
            z.addiRowVector(bias);
        }
        return (activationFunction != null) ? activationFunction.apply(z) : z;
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