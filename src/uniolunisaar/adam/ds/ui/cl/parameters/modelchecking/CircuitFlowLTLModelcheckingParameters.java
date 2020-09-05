package uniolunisaar.adam.ds.ui.cl.parameters.modelchecking;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import uniolunisaar.adam.ds.ui.cl.parameters.IOParameters;
import uniolunisaar.adam.ds.modelchecking.output.AdamCircuitFlowLTLMCOutputData;
import uniolunisaar.adam.ds.modelchecking.settings.ltl.AdamCircuitFlowLTLMCSettings;
import uniolunisaar.adam.ds.modelchecking.settings.ModelCheckingSettings;
import uniolunisaar.adam.ds.modelchecking.statistics.AdamCircuitFlowLTLMCStatistics;
import uniolunisaar.adam.exceptions.ui.cl.CommandLineParseException;
import static uniolunisaar.adam.ds.ui.cl.parameters.modelchecking.CircuitLTLModelcheckingParameters.handleParameters;

/**
 *
 * @author Manuel Gieseking
 */
public class CircuitFlowLTLModelcheckingParameters extends CircuitModelcheckingParameters {

    private static final String PARAMETER_TRANSFORMATION = "t";
    private static final String PARAMETER_FLOWLTL_STUCKING = "st";
    private static final String PARAMETER_FLOWLTL_APPROACH = "app";

    @Override
    public Map<String, Option> createOptions() {
        Map<String, Option> options = new HashMap<>(super.createOptions());
        // Outputing files
        OptionBuilder.withDescription("Saves the transformed net in APT format and, in the case that dot is executable, as PDF.");
        OptionBuilder.withLongOpt("trans");
        options.put(PARAMETER_TRANSFORMATION, OptionBuilder.create(PARAMETER_TRANSFORMATION));

        // stucking
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("Different formulas for the sequential approach to prevent runs from stucking in a subnet."
                + " Possible values: GFo | GFANDNpi | ANDGFNpi | GFoANDNpi."
                + " Standard: GFANDNpi.");
        OptionBuilder.withLongOpt("stuck");
        options.put(PARAMETER_FLOWLTL_STUCKING, OptionBuilder.create(PARAMETER_FLOWLTL_STUCKING));

        // approach
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("Chosing the sequential or parallel approach (with or without inhibitor arcs)."
                + " Possible values: seq | seqIn | par | parIn."
                + " Standard: parIn.");
        OptionBuilder.withLongOpt("approach");
        options.put(PARAMETER_FLOWLTL_APPROACH, OptionBuilder.create(PARAMETER_FLOWLTL_APPROACH));

        return options;
    }

    public static AdamCircuitFlowLTLMCSettings getMCSettings(CommandLine line, boolean verbose) throws CommandLineParseException, FileNotFoundException {
        // Outputdata
        String outputPath = IOParameters.getOutput(line);
        AdamCircuitFlowLTLMCOutputData data = new AdamCircuitFlowLTLMCOutputData(
                outputPath,
                CircuitLTLModelcheckingParameters.saveCircuit(line),
                CircuitFlowLTLModelcheckingParameters.saveTransformedNet(line),
                verbose);

        // Settings
        AdamCircuitFlowLTLMCSettings settings = new AdamCircuitFlowLTLMCSettings(data);
        handleParameters(line, settings);

        // stucking 
        if (line.hasOption(PARAMETER_FLOWLTL_STUCKING)) {
            String stuckInSubnet = line.getOptionValue(PARAMETER_FLOWLTL_STUCKING);
            if (stuckInSubnet.equals("GFo")) {
                settings.setStucking(AdamCircuitFlowLTLMCSettings.Stucking.GFo);
            } else if (stuckInSubnet.equals("ANDGFNpi")) {
                settings.setStucking(AdamCircuitFlowLTLMCSettings.Stucking.ANDGFNpi);
            } else if (stuckInSubnet.equals("GFANDNpi")) {
                settings.setStucking(AdamCircuitFlowLTLMCSettings.Stucking.GFANDNpi);
            } else if (stuckInSubnet.equals("GFoANDNpi")) {
                settings.setStucking(AdamCircuitFlowLTLMCSettings.Stucking.GFANDNpiAndo);
            }
        }

        // approach
        if (line.hasOption(PARAMETER_FLOWLTL_APPROACH)) {
            String approach = line.getOptionValue(PARAMETER_FLOWLTL_APPROACH);
            ModelCheckingSettings.Approach appr = ModelCheckingSettings.Approach.PARALLEL_INHIBITOR;
            if (approach.equals("seq")) {
                appr = ModelCheckingSettings.Approach.SEQUENTIAL;
            } else if (approach.equals("seqIn")) {
                appr = ModelCheckingSettings.Approach.SEQUENTIAL_INHIBITOR;
            } else if (approach.equals("par")) {
                appr = ModelCheckingSettings.Approach.PARALLEL;
            } else if (approach.equals("parIn")) {
                appr = ModelCheckingSettings.Approach.PARALLEL_INHIBITOR;
            } else {
                throw new RuntimeException("Not exceptable key for the approach " + approach);
            }
            settings.setApproach(appr);
        }

        // Statistics       
        String statsOut = CircuitModelcheckingParameters.getStatisticsFile(line);
        AdamCircuitFlowLTLMCStatistics stats;
        if (statsOut != null) {
            if (statsOut.isEmpty()) {
                stats = new AdamCircuitFlowLTLMCStatistics();
            } else {
                stats = new AdamCircuitFlowLTLMCStatistics(statsOut);
            }
            settings.setStatistics(stats);
        }
        return settings;
    }

    public static boolean saveTransformedNet(CommandLine line) {
        return line.hasOption(PARAMETER_TRANSFORMATION);
    }

}
