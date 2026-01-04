package bella;

import beast.base.core.Description;
import beast.base.core.Function;
import beast.base.core.Input;
import beast.base.evolution.tree.Node;
import beast.base.inference.parameter.RealParameter;

import bdmmprime.mapping.TypedNodeTreeLogger;
import bdmmprime.parameterization.Parameterization;
import bdmmprime.parameterization.SkylineVectorParameter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Description("Logs a typed node tree, annotating each node with the values of "
        + "one or more skyline vector parameters evaluated at the node time and type.")
public class SkylineNodeTreeLogger extends TypedNodeTreeLogger {

    public Input<Parameterization> parameterizationInput = new Input<>(
            "parameterization",
            "Parameterization used to convert node heights to times at which skyline parameters are evaluated.",
            Input.Validate.REQUIRED);

    public Input<List<SkylineVectorParameter>> skylineParametersInput = new Input<>(
            "skylineParameter",
            "One or more skyline vector parameters whose values are annotated at each node of the tree.",
            new ArrayList<>(), Input.Validate.REQUIRED);

    public Input<Function> finalSampleOffsetInput = new Input<>(
            "finalSampleOffset",
            "Optional time offset between the final sample and the end of the birth–death process, "
                    + "used when converting node heights to absolute times. Default is 0.",
            new RealParameter("0.0"), Input.Validate.OPTIONAL);

    public Input<Integer> precisionInput = new Input<>(
            "precision",
            "Number of decimal places used when logging skyline parameter values. Default is 6.",
            6, Input.Validate.OPTIONAL);

    private List<SkylineVectorParameter> skylineParameters;
    private Parameterization parameterization;
    private double finalSampleOffset;
    private int precision;

    @Override
    public void initAndValidate() {
        super.initAndValidate();
        skylineParameters = skylineParametersInput.get();
        parameterization = parameterizationInput.get();
        finalSampleOffset = finalSampleOffsetInput.get().getArrayValue();
        precision = precisionInput.get() ;
    }

    @Override
    public String getStrippedNewick(Node node) {
        for (Node n : node.getAllChildNodesAndSelf()) {
            double nodeTime = parameterization.getNodeTime(n, finalSampleOffset);

            Object nodeType = n.getMetaData("type");
            int typeIndex = nodeType != null ? (Integer) nodeType : 0;

            for (int i = 0; i < skylineParameters.size(); i++) {
                SkylineVectorParameter param = skylineParameters.get(i);
                String paramName = param.getID() != null ? param.getID() : "param" + i;

                double value = param.getValuesAtTime(nodeTime)[typeIndex];
                BigDecimal rounded = new BigDecimal(value)
                        .setScale(precision, RoundingMode.HALF_UP);
                n.setMetaData(paramName, rounded);
            }

            n.metaDataString = n.getMetaDataNames().stream()
                    .map(k -> k + "=" + n.getMetaData(k))
                    .collect(Collectors.joining(","));
        }

        return super.getStrippedNewick(node);
    }
}