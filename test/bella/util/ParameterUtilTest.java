package bella.util;

import beast.base.inference.parameter.RealParameter;

import java.util.ArrayList;

import org.apache.commons.math3.linear.RealMatrix;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ParameterUtilTest {

    @Test
    void testMinMaxNormalizeNormalCase() {
        RealParameter param = new RealParameter("1.0 2.0 3.0 4.0"); // values: [1,2,3,4]
        ParameterUtil.minMaxNormalize(param);

        double[] expected = {0.0, 0.333, 0.666, 1.0};
        double[] actual = param.getDoubleValues();

        assertArrayEquals(expected, actual, 1e-3, "Normalized values do not match expected [0,1] scaling.");
    }

    @Test
    void testMinMaxNormalizeAllEqualValues() {
        RealParameter param = new RealParameter("5.0 5.0 5.0");
        ParameterUtil.minMaxNormalize(param);

        double[] expected = {0.5, 0.5, 0.5};
        double[] actual = param.getDoubleValues();

        assertArrayEquals(expected, actual, 1e-9, "Degenerate case should map all values to 0.5.");
    }

    @Test
    void testMinMaxNormalizeSingleValue() {
        RealParameter param = new RealParameter("42.0");
        ParameterUtil.minMaxNormalize(param);

        double[] expected = {0.5};
        double[] actual = param.getDoubleValues();

        assertArrayEquals(expected, actual, 1e-9, "Single-element array should normalize to 0.5.");
    }

    @Test
    void testToRealMatrixNormalCase() {
        ArrayList<RealParameter> params = new ArrayList<>();
        params.add(new RealParameter("1.0 2.0 3.0"));
        params.add(new RealParameter("3.0 4.0 5.0"));

        RealMatrix matrix = ParameterUtil.toRealMatrix(params); // replace YourClass with actual class

        assertEquals(2, matrix.getRowDimension());
        assertEquals(3, matrix.getColumnDimension());
        assertArrayEquals(new double[]{1.0, 2.0, 3.0}, matrix.getRow(0));
        assertArrayEquals(new double[]{3.0, 4.0, 5.0}, matrix.getRow(1));
    }

}
