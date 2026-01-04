package bella.util;

import beast.base.core.Function;
import beast.base.inference.parameter.RealParameter;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

/**
 * Utility class for working with RealParameter objects.
 * <p>
 * Provides common parameter transformations, such as normalization.
 */
public final class ParameterUtil {

    // Prevent instantiation
    private ParameterUtil() {}

    /**
     * Applies in-place min–max normalization to a {@link RealParameter},
     * scaling all values to the range [0, 1].
     * <p>
     * Formula: normalized = (value - min) / (max - min)
     * <p>
     * If all values are identical, they are set to 0.5.
     *
     * @param parameter the RealParameter to normalize (modified in place)
     */
    public static void minMaxNormalize(RealParameter parameter) {
        double[] values = parameter.getDoubleValues();
        double min = Arrays.stream(values).min().orElseThrow();
        double max = Arrays.stream(values).max().orElseThrow();
        double range = max - min;

        double[] normalized = Arrays.stream(values)
                .map(v -> range > 0.0 ? (v - min) / range : 0.5)
                .toArray();
        for (int i = 0; i < normalized.length; i++) {
            parameter.setValue(i, normalized[i]);
        }
    }

    /**
     * Converts a list of {@link RealParameter} objects into a {@link RealMatrix}.
     * <p>
     * Each RealParameter becomes a row of the matrix, and each value in the parameter
     * becomes a column entry.
     *
     * @param realParams list of RealParameters (all must have the same dimension)
     * @return a RealMatrix representing the values of the RealParameters
     * @throws IllegalArgumentException if the parameters have different dimensions
     */
    public static RealMatrix toRealMatrix(ArrayList<RealParameter> realParams) {
        int dim = realParams.get(0).getDimension();

        for (int i = 0; i < realParams.size(); i++) {
            RealParameter pred = realParams.get(i);
            if (pred.getDimension() != dim) {
                throw new IllegalArgumentException(
                        String.format(
                                "All parameters must have the same dimension. "
                                        + "Parameter 0 has dimension %d, but parameter %d has dimension %d.",
                                pred.getDimension(), i, dim
                        )
                );
            }
        }

        double[][] result = realParams.stream()
                .map(Function::getDoubleValues)
                .toArray(double[][]::new);
        return MatrixUtils.createRealMatrix(result);
    }
}
