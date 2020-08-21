package uniolunisaar.adam.data.ui.cl.parameters.modelchecking;

import java.io.FileNotFoundException;
import org.apache.commons.cli.CommandLine;
import uniolunisaar.adam.data.ui.cl.parameters.IOParameters;
import uniolunisaar.adam.data.ui.cl.parameters.IOParameters;
import uniolunisaar.adam.ds.modelchecking.output.AdamCircuitLTLMCOutputData;
import uniolunisaar.adam.ds.modelchecking.settings.AdamCircuitLTLMCSettings;
import uniolunisaar.adam.ds.modelchecking.statistics.AdamCircuitFlowLTLMCStatistics;
import uniolunisaar.adam.ds.modelchecking.statistics.AdamCircuitLTLMCStatistics;
import uniolunisaar.adam.exceptions.ui.cl.CommandLineParseException;

/**
 *
 * @author Manuel Gieseking
 */
public class CircuitLTLModelcheckingParameters extends CircuitModelcheckingParameters {

    public static AdamCircuitLTLMCSettings getMCSettings(CommandLine line, boolean verbose) throws CommandLineParseException, FileNotFoundException {
        // Settings
        AdamCircuitLTLMCSettings settings = new AdamCircuitLTLMCSettings();
        handleParameters(line, settings);

        // Outputdata
        String outputPath = IOParameters.getOutput(line);
        AdamCircuitLTLMCOutputData data = new AdamCircuitLTLMCOutputData(
                outputPath,
                CircuitModelcheckingParameters.saveCircuit(line),
                verbose);
        settings.setOutputData(data);

        // Statistics
        String statsOut = CircuitModelcheckingParameters.getStatisticsFile(line);
        AdamCircuitLTLMCStatistics stats;
        if (statsOut != null) {
            if (statsOut.isEmpty()) {
                stats = new AdamCircuitLTLMCStatistics();
            } else {
                stats = new AdamCircuitFlowLTLMCStatistics(statsOut);
            }
            settings.setStatistics(stats);
        }
        return settings;
    }
}
