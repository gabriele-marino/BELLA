// Migrated from ND4J to Apache Commons Math
package bayesmlp;

import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.core.Log;
import beast.base.inference.CalculationNode;
import beast.base.inference.parameter.RealParameter;
import org.apache.commons.math3.linear.*;
import beast.base.core.Function;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Description("Designed to be used within Beast2. This is a version without ND4J, using Apache Commons Math.")
public class BayesMLP extends CalculationNode implements Function {

    public Input<ArrayList<RealParameter>> predictorsInput = new Input<>("predictor", "Predictors", new ArrayList<>(), Input.Validate.REQUIRED);
    public Input<ArrayList<RealParameter>> weightsInput = new Input<>("weights", "Weights", new ArrayList<>(), Input.Validate.REQUIRED);
    public Input<Boolean> useBiasInAllInput = new Input<>("useBiasInAll", "Bias for all layers?", true, Input.Validate.OPTIONAL);
    public Input<List<Integer>> nodesInput = new Input<>("nodes", "Layer nodes", new ArrayList<>(), Input.Validate.REQUIRED);
    public Input<ArrayList<ActivationFunction>> activationHiddenInput = new Input<>("activationFunctionsHidden",
            "Activation functions for hidden layers. Can only be empty if there are no hidden layers." +
                    "If exactly one is supplied, all hidden layers use this function." +
                    "If the list size is larger, one activation function per layer must be supplied (they may be the same)." +
                    "Default: relu for all hidden layers", new ArrayList<>(),
            Input.Validate.REQUIRED);

    public Input<ActivationFunction> activationOutputInput = new Input<>("activationFunctionsOutput",
            "Activation functions for the output layer." +
                    "Default: softplus.", new SoftPlus(), Input.Validate.REQUIRED);


    RealMatrix predictors;
    int nonOutputBiasTerm = 1;
    ArrayList<RealParameter> weights;
    int parameterSize;
    int nPredictor;
    int nHiddenLayers;
    RealMatrix[] weightMatrices;
    RealMatrix output;
    boolean needsRecalculation = true;
    int[][] shapes;
    ArrayList<ActivationFunction> activationFunctionsHidden;
    ActivationFunction DEFAULT_ACTIVATION_FUNCTION_HIDDEN = new ReLu();
    ActivationFunction activationFunctionOutput;

    @Override
    public void initAndValidate() {
        nPredictor = predictorsInput.get().size();
        parameterSize = predictorsInput.get().get(0).getDimension();

        if (!useBiasInAllInput.get())
            nonOutputBiasTerm = 0;

        for (RealParameter pred : predictorsInput.get()) {
            if (parameterSize != pred.getDimension()) {
                throw new IllegalArgumentException("Predictor dimension mismatch.");
            }
        }

        List<Integer> nodes = nodesInput.get();
        weights = weightsInput.get();
        if (nodes.size() == 0)
            throw new IllegalArgumentException("Nodes must include at least output layer.");
        if (nodes.size() != weights.size())
            throw new IllegalArgumentException("Weights and nodes dimension mismatch.");
        nHiddenLayers = nodes.size() - 1;

        activationFunctionsHidden = activationHiddenInput.get();
        if (activationFunctionsHidden.size()==0){
            Log.warning("No hidden layer activation function provided. Using " +
                    DEFAULT_ACTIVATION_FUNCTION_HIDDEN.toString() + ".");
            activationFunctionsHidden.add(DEFAULT_ACTIVATION_FUNCTION_HIDDEN);
        }
        if (activationFunctionsHidden.size() == 1 && nHiddenLayers > 1){
            Log.info.println("All hidden layers use activation function: " +
                    activationFunctionsHidden.get(0).toString() + ".");
            for (int i=1; i<nHiddenLayers; i++){
                activationFunctionsHidden.add(activationFunctionsHidden.get(0));
            }
        } else {
            if (activationFunctionsHidden.size() > 1 && activationFunctionsHidden.size() != nHiddenLayers)
                throw new IllegalArgumentException("Number of activation functions for hidden layers" +
                        " is larger than one but not equal to number of hidden layers.");

            for (int i=0; i<nHiddenLayers;i++)
                Log.info("Activation function for hidden layer " + i + ": " +
                        activationFunctionsHidden.get(i).toString() + ".");
        }

        activationFunctionOutput = activationOutputInput.get(); // required so can't be null, no check needed
        Log.info.println("Output layer activation function: " + activationFunctionOutput.toString() + ".");

        predictors = MatrixUtils.createRealMatrix(convertToDoubleArray(predictorsInput.get())).transpose();


        if (nHiddenLayers == 0) {
            weights.get(0).setDimension(nPredictor + 1);
        } else {
            for (int i = 0; i < nHiddenLayers; i++) {
                int inputDim = (i == 0 ? nPredictor : nodes.get(i - 1)) + nonOutputBiasTerm;
                weights.get(i).setDimension(inputDim * nodes.get(i));
            }
            weights.get(nHiddenLayers).setDimension(nodes.get(nHiddenLayers - 1) + 1);
        }
        initializeMatrices();
    }

    private void initializeMatrices() {
        weightMatrices = new RealMatrix[nHiddenLayers + 1];
        shapes = new int[nHiddenLayers + 1][2];
        for (int i = 0; i <= nHiddenLayers; i++) {
            shapes[i] = getLayerShape(i);
            weightMatrices[i] = MatrixUtils.createRealMatrix(shapes[i][0], shapes[i][1]);
            double[] vals = weights.get(i).getDoubleValues();
            for (int r = 0, k = 0; r < shapes[i][0]; r++) {
                for (int c = 0; c < shapes[i][1]; c++) {
                    weightMatrices[i].setEntry(r, c, vals[k++]);
                }
            }
        }
    }

    private int[] getLayerShape(int layer) {
        if (nHiddenLayers == 0) return new int[]{nPredictor + 1, nodesInput.get().get(0)};
        if (layer == 0) return new int[]{nPredictor + nonOutputBiasTerm, nodesInput.get().get(0)};
        if (layer == nHiddenLayers) return new int[]{nodesInput.get().get(layer - 1) + 1, 1};
        return new int[]{nodesInput.get().get(layer - 1) + nonOutputBiasTerm, nodesInput.get().get(layer)};
    }

    @Override
    public int getDimension() {
        return parameterSize;
    }

    @Override
    public double getArrayValue(int i) {
        checkAndUpdateWeights();
        if (needsRecalculation) {
            recalculate();
            needsRecalculation = false;
        }
        return output.getEntry(i,0);
    }

    private void checkAndUpdateWeights() {
        boolean updated = false;
        for (int i = 0; i < weights.size(); i++) {
            double[] w = weights.get(i).getDoubleValues();
            int k = 0;
            for (int r = 0; r < weightMatrices[i].getRowDimension(); r++) {
                for (int c = 0; c < weightMatrices[i].getColumnDimension(); c++) {
                    if (weightMatrices[i].getEntry(r, c) != w[k]) {
                        updated = true;
                        weightMatrices[i].setEntry(r, c, w[k]);
                    }
                    k++;
                }
            }
        }
        if (updated) needsRecalculation = true;
    }

    private void recalculate() {
        if (nHiddenLayers == 0) {
            output = runLayer(predictors, weightMatrices[nHiddenLayers], activationFunctionOutput);
            return;
        }

        RealMatrix layerOut = predictors;
        for (int l = 0; l < nHiddenLayers; l++) {
            layerOut = runLayer(layerOut, weightMatrices[l], activationFunctionsHidden.get(l));
        }
        output = runLayer(layerOut, weightMatrices[nHiddenLayers], activationFunctionOutput);
    }





    private RealMatrix runLayer(RealMatrix input, RealMatrix weights, ActivationFunction activation) {
        if (input.getColumnDimension() == weights.getRowDimension()) {
            return activation.apply(input.multiply(weights));
        } else {
            RealMatrix weightsPart = weights.getSubMatrix(1, weights.getRowDimension() - 1, 0, weights.getColumnDimension() - 1);
            RealMatrix bias = weights.getRowMatrix(0);
            RealMatrix z = input.multiply(weightsPart);
            for (int i = 0; i < z.getRowDimension(); i++) {
                for (int j = 0; j < z.getColumnDimension(); j++) {
                    z.addToEntry(i, j, bias.getEntry(0, j));
                }
            }
            return activation.apply(z);
        }
    }

    private static double[][] convertToDoubleArray(ArrayList<RealParameter> realParams) {
        int rows = realParams.size();
        int cols = realParams.get(0).getDimension();
        double[][] result = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = realParams.get(i).getValue(j);
            }
        }
        return result;
    }
} 