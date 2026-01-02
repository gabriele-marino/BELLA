package bella;

import beast.base.core.Description;
import beast.base.core.Function;
import beast.base.core.Input;
import beast.base.evolution.tree.Node;
import beast.base.evolution.tree.Tree;
import bdmmprime.mapping.TypedNodeTreeLogger;
import bdmmprime.parameterization.Parameterization;
import bdmmprime.parameterization.SkylineVectorParameter;
import beast.base.inference.parameter.RealParameter;

import java.util.ArrayList;
import java.util.List;

/**
 * Logger which extends TypedNodeTreeLogger to additionally log values
 * from SkylineParameters at each node in the tree. For every node,
 * it gets the node's age, converts it to time using Parameterization,
 * and logs the parameter values at that time in the node metadata.
 *
 * @author ETH Zurich ugne.stolz@protonmail.com
 */
@Description("Logs typed node tree and one or more skyline vector parameters per node/time/type")
public class SkylineNodeTreeLogger extends TypedNodeTreeLogger {

    public Input<List<SkylineVectorParameter>> skylineParametersInput = new Input<>(
            "skylineParameter",
            "Skyline vector parameters to log at each node.",
            new ArrayList<>());

    public Input<Parameterization> parameterizationInput = new Input<>(
            "parameterization",
            "Parameterization object for time conversion.",
            Input.Validate.REQUIRED);

    public Input<Function> finalSampleOffsetInput = new Input<>("finalSampleOffset",
            "If provided, the difference in time between the final sample and the end of the BD process.",
            new RealParameter("0.0"));

    public Input<Integer> precisionInput = new Input<>(
            "precision",
            "Number of decimal places for parameter value logging (default: 6).",
            6);

    private List<SkylineVectorParameter> skylineParameters;
    private Parameterization parameterization;
    private int precision;

    @Override
    public void initAndValidate() {
        super.initAndValidate();
        skylineParameters = skylineParametersInput.get();
        parameterization = parameterizationInput.get();
        precision = precisionInput.get();
    }

    /**
     * Construct a newick representation of the given typed tree, but with
     * type-change nodes stripped away and skyline parameter values added
     * to node metadata.
     *
     * @param node root of typed tree.
     * @return newick representation.
     */
    @Override
    public String getStrippedNewick(Node node) {

        StringBuilder resultBuilder = new StringBuilder();

        Node topNode = node;

        while (node.getChildren().size()==1)
            node = node.getChild(0);

        if (!node.isLeaf()) {

            resultBuilder.append("(");
            boolean isFirst = true;
            for (Node child : node.getChildren()) {
                if (isFirst)
                    isFirst = false;
                else
                    resultBuilder.append(",");

                resultBuilder.append(getStrippedNewick(child));
            }

            resultBuilder.append(")");
        }

        if (node.getID() != null)
            resultBuilder.append(node.getNr()+Tree.taxaTranslationOffset);

        // Enhanced metadata with skyline parameter values
        StringBuilder metadataBuilder = new StringBuilder();
        metadataBuilder.append(node.metaDataString);

        // Add skyline parameter values for this node
        if (!skylineParameters.isEmpty()) {
            double nodeTime = parameterization.getNodeTime(node, finalSampleOffsetInput.get().getArrayValue());
            
            // Get node type for skyline parameter indexing
            Integer nodeType = getNodeType(node);
            
            for (int i = 0; i < skylineParameters.size(); i++) {
                SkylineVectorParameter param = skylineParameters.get(i);
                String paramName = param.getID() != null ? param.getID() : "param" + i;
                
                double[] values = param.getValuesAtTime(nodeTime);
                
                if (nodeType != null && nodeType < values.length) {
                    if (!metadataBuilder.isEmpty())
                        metadataBuilder.append(",");
                    metadataBuilder.append(paramName).append("=").append(formatValue(values[nodeType]));
                }
            }
        }

        resultBuilder.append("[&").append(metadataBuilder).append("]");

        double edgeLength = 0.0;
        if (topNode.getParent() != null)
            edgeLength = topNode.getParent().getHeight()-node.getHeight();

        resultBuilder.append(":").append(edgeLength);

        return resultBuilder.toString();
    }

    /**
     * Extract node type from node metadata. This assumes the node metadata
     * contains type information in a format like "type=0" or similar.
     *
     * @param node the node to extract type from
     * @return the node type as Integer, or null if not found
     */
    private Integer getNodeType(Node node) {
        if (node.metaDataString == null || node.metaDataString.isEmpty())
            return null;

        return (Integer) node.getMetaData("type");
    }

    /**
     * Format a double value to the specified precision.
     *
     * @param value the double value to format
     * @return formatted string representation of the value
     */
    private String formatValue(double value) {
        String format = "%." + precision + "f";
        return String.format(format, value);
    }
}