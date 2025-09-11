package bella;

import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.core.Log;
import beast.base.inference.CalculationNode;
import beast.base.inference.parameter.RealParameter;
import org.apache.commons.math3.linear.*;
import beast.base.core.Function;

import java.util.ArrayList;
import java.util.List;

@Description("Bayesian multi layer perceptron designed to be used within Beast2.")
public class BayesMLP extends CalculationNode implements Function {

    public Input<ArrayList<RealParameter>> predictorsInput = new Input<>("predictor", "Predictors", new ArrayList<>(), Input.Validate.REQUIRED);
    public Input<RealParameter> weightsInput = new Input<>("weights", "Flattened weights vector containing all layer weights sequentially", Input.Validate.REQUIRED);
    public Input<Boolean> useBiasInAllInput = new Input<>("useBiasInAll", "Bias for all layers?", true, Input.Validate.OPTIONAL);
    public Input<List<Integer>> nodesInput = new Input<>("nodes", "Hidden layer nodes", new ArrayList<>(), Input.Validate.REQUIRED);
    public Input<Integer> outputNodes = new Input<>("outNodes", "Output layer nodes", 1, Input.Validate.OPTIONAL);
    public Input<ArrayList<ActivationFunction>> activationHiddenInput = new Input<>("activationFunctionsHidden",
            "Activation functions for hidden layers. Can only be empty if there are no hidden layers." +
                    "If exactly one is supplied, all hidden layers use this function." +
                    "If the list size is larger, one activation function per layer must be supplied (they may be the same)." +
                    "Default: relu for all hidden layers", new ArrayList<>(),
            Input.Validate.OPTIONAL);

    public Input<ActivationFunction> activationOutputInput = new Input<>("activationFunctionsOutput",
            "Activation functions for the output layer." +
                    "Default: relu.", new ReLu(), Input.Validate.REQUIRED);


    RealMatrix predictors;
    int nonOutputBiasTerm = 1;
    RealParameter weights;
    int[] layerWeightOffsets; // Starting index for each layer's weights in the flattened vector
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
        nodes.add(outputNodes.get());
        weights = weightsInput.get();
        if (nodes.isEmpty())
            throw new IllegalArgumentException("Nodes must include at least output layer.");
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i)<=0)
                throw new IllegalArgumentException("Layer "+i+" has non positive number of nodes: "+nodes.get(i)+".");
        }

        nHiddenLayers = nodes.size() - 1;
        // Calculate total weights needed and layer offsets
        calculateWeightDimensionsAndOffsets(nodes);

        activationFunctionsHidden = activationHiddenInput.get();
        if (activationFunctionsHidden.isEmpty()) {
            Log.warning("No hidden layer activation function provided. Using " +
                    DEFAULT_ACTIVATION_FUNCTION_HIDDEN.toString() + ".");
            activationFunctionsHidden.add(DEFAULT_ACTIVATION_FUNCTION_HIDDEN);
        }
        if (activationFunctionsHidden.size() == 1 && nHiddenLayers > 1) {
            Log.info.println("All hidden layers use activation function: " +
                    activationFunctionsHidden.get(0).toString() + ".");
            for (int i = 1; i < nHiddenLayers; i++) {
                activationFunctionsHidden.add(activationFunctionsHidden.get(0));
            }
        } else {
            if (activationFunctionsHidden.size() > 1 && activationFunctionsHidden.size() != nHiddenLayers)
                throw new IllegalArgumentException("Number of activation functions for hidden layers" +
                        " is larger than one but not equal to number of hidden layers.");

            for (int i = 0; i < nHiddenLayers; i++)
                Log.info("Activation function for hidden layer " + i + ": " +
                        activationFunctionsHidden.get(i).toString() + ".");
        }

        activationFunctionOutput = activationOutputInput.get(); // required so can't be null, no check needed
        Log.info.println("Output layer activation function: " + activationFunctionOutput.toString() + ".");

        for (RealParameter pred : predictorsInput.get()) {
            normalizeRealParameter(pred);
        }

        predictors = MatrixUtils.createRealMatrix(convertToDoubleArray(predictorsInput.get())).transpose();


        // Dimension is set by calculateWeightDimensionsAndOffsets()
        initializeMatrices();
    }

    private void calculateWeightDimensionsAndOffsets(List<Integer> nodes) {
        layerWeightOffsets = new int[nHiddenLayers + 2]; // +1 for total size at end
        int totalWeights = 0;
        
        for (int i = 0; i <= nHiddenLayers; i++) {
            layerWeightOffsets[i] = totalWeights;
            int inputDim, outputDim;
            
            if (nHiddenLayers == 0) {
                // Only output layer
                inputDim = nPredictor + 1;
                outputDim = nodes.get(0);
            } else if (i == 0) {
                // First hidden layer
                inputDim = nPredictor + nonOutputBiasTerm;
                outputDim = nodes.get(0);
            } else if (i == nHiddenLayers) {
                // Output layer
                inputDim = nodes.get(i - 1) + 1;
                outputDim = 1;
            } else {
                // Hidden layers
                inputDim = nodes.get(i - 1) + nonOutputBiasTerm;
                outputDim = nodes.get(i);
            }
            
            totalWeights += inputDim * outputDim;
        }
        
        layerWeightOffsets[nHiddenLayers + 1] = totalWeights; // Store total size
        weights.setDimension(totalWeights);
    }

    private void initializeMatrices() {
        weightMatrices = new RealMatrix[nHiddenLayers + 1];
        shapes = new int[nHiddenLayers + 1][2];
        double[] allWeights = weights.getDoubleValues();
        
        for (int i = 0; i <= nHiddenLayers; i++) {
            shapes[i] = getLayerShape(i);
            weightMatrices[i] = MatrixUtils.createRealMatrix(shapes[i][0], shapes[i][1]);
            
            int offset = layerWeightOffsets[i];
            for (int r = 0, k = 0; r < shapes[i][0]; r++) {
                for (int c = 0; c < shapes[i][1]; c++) {
                    weightMatrices[i].setEntry(r, c, allWeights[offset + k]);
                    k++;
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
        double[] allWeights = weights.getDoubleValues();
        
        for (int i = 0; i <= nHiddenLayers; i++) {
            int offset = layerWeightOffsets[i];
            int k = 0;
            for (int r = 0; r < weightMatrices[i].getRowDimension(); r++) {
                for (int c = 0; c < weightMatrices[i].getColumnDimension(); c++) {
                    if (weightMatrices[i].getEntry(r, c) != allWeights[offset + k]) {
                        updated = true;
                        weightMatrices[i].setEntry(r, c, allWeights[offset + k]);
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
    
    // Public getter for testing purposes
    public RealMatrix getPredictors() {
        return predictors;
    }
    
    /**
     * Normalizes a RealParameter to the range [0,1] using min-max normalization.
     * Formula: normalized = (value - min) / (max - min)
     * 
     * @param parameter The RealParameter to normalize
     * @return A new RealParameter with values normalized to [0,1]
     */
    public static void normalizeRealParameter(RealParameter parameter) {
        double[] values = parameter.getDoubleValues();
        
        // Find min and max
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        for (double value : values) {
            if (value < min) min = value;
            if (value > max) max = value;
        }
        
        // Create normalized values
        double[] normalized = new double[values.length];
        double range = max - min;
        
        if (range > 0) {
            // Normal case: normalize to [0,1]
            for (int i = 0; i < values.length; i++) {
                normalized[i] = (values[i] - min) / range;
            }
        } else {
            // Edge case: all values are the same, set to 0.5
            for (int i = 0; i < values.length; i++) {
                normalized[i] = 0.5;
            }
        }

        for (int i = 0; i < normalized.length; i++) {
            parameter.setValue(i, normalized[i]);
        }
    }
} 