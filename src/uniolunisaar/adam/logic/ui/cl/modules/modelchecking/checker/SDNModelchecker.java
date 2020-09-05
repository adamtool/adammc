package uniolunisaar.adam.logic.ui.cl.modules.modelchecking.checker;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import uniol.apt.io.parser.ParseException;
import uniol.apt.module.exception.ModuleException;
import uniolunisaar.adam.ds.logics.ltl.flowltl.RunLTLFormula;
import uniolunisaar.adam.logic.parser.logics.flowltl.FlowLTLParser;
import uniolunisaar.adam.ds.modelchecking.results.LTLModelCheckingResult;
import uniolunisaar.adam.exceptions.ExternalToolException;
import uniolunisaar.adam.ds.petrinetwithtransits.PetriNetWithTransits;
import uniolunisaar.adam.tools.Logger;
import uniolunisaar.adam.exceptions.ProcessNotStartedException;
import uniolunisaar.adam.tools.Tools;
import uniolunisaar.adam.ds.ui.cl.parameters.IOParameters;
import uniolunisaar.adam.logic.ui.cl.modules.AbstractSimpleModule;
import uniolunisaar.adam.data.ui.cl.parameters.modelchecking.CircuitFlowLTLModelcheckingParameters;
import uniolunisaar.adam.data.ui.cl.parameters.modelchecking.CircuitLTLModelcheckingParameters;
import uniolunisaar.adam.ds.logics.ltl.ILTLFormula;
import uniolunisaar.adam.ds.modelchecking.settings.ltl.AdamCircuitFlowLTLMCSettings;
import uniolunisaar.adam.ds.modelchecking.settings.ltl.AdamCircuitLTLMCSettings;
import uniolunisaar.adam.ds.modelchecking.statistics.AdamCircuitLTLMCStatistics;
import uniolunisaar.adam.exceptions.ui.cl.CommandLineParseException;
import uniolunisaar.adam.generators.pnwt.SDNCreator;
import uniolunisaar.adam.logic.modelchecking.ltl.circuits.ModelCheckerFlowLTL;
import uniolunisaar.adam.logic.modelchecking.ltl.circuits.ModelCheckerLTL;
import uniolunisaar.adam.util.PNWTTools;
import uniolunisaar.adam.util.modelchecking.sdn.SDNFormelCreator;

/**
 * @author Manuel Gieseking
 */
public class SDNModelchecker extends AbstractSimpleModule {

    private static final String name = "mc_sdn";
    private static final String descr = "Modelchecking Software Defined Networks with concurrent updates.";

    private static final String PARAMETER_FORMULA = "f";
    private static final String PARAMETER_STD_PROP = "c";
    private static final String PARAMETER_UPDATE = "u";
    private static final String PARAMETER_OPTIMIZE_CONNECTIONS = "optCon";

    private static enum CheckProperties {
        connectivity,
        loopFreedom,
        weakLoopFreedom,
        dropFreedom,
        packetCoherence
    }

    @Override
    protected Map<String, Option> createOptions() {
        Map<String, Option> ops = super.createOptions();
        // IO
        ops.putAll(IOParameters.createOptions());
        // change the input description
        Option in = ops.get(IOParameters.getPARAMETER_INPUT());
        in.setDescription("The path to the input topology file.");
//        // change the output description
//        Option out = ops.get(IOParameters.getPARAMETER_OUTPUT());
//        out.setDescription("The path to an optional output folder.");

        // update
        OptionBuilder.hasArg();
        OptionBuilder.isRequired(true);
        OptionBuilder.withArgName("update");
        OptionBuilder.withDescription("The update of the topoloy which should be checked.");
        OptionBuilder.withLongOpt("update");
        ops.put(PARAMETER_UPDATE, OptionBuilder.create(PARAMETER_UPDATE));

        // update
//        OptionBuilder.withDescription("If set only the necessary connections of the"
//                + " topology are added. This means only those used in the initial configuration and"
//                + " those of the update.");
//        OptionBuilder.withLongOpt("opt-connections");
//        ops.put(PARAMETER_OPTIMIZE_CONNECTIONS, OptionBuilder.create(PARAMETER_OPTIMIZE_CONNECTIONS));

        // MC Parameters
        ops.putAll(new CircuitFlowLTLModelcheckingParameters().createOptions());

        return ops;
    }

    @Override
    protected Map<String, OptionGroup> createOptionGroups() {
        Map<String, OptionGroup> groups = super.createOptionGroups();

        OptionGroup group = new OptionGroup();
        group.setRequired(true);

        OptionBuilder.hasArg();
        OptionBuilder.withArgName("LTL | Flow-LTL formula");
        OptionBuilder.withDescription("The formula, either Flow-LTL or LTL, which should be checked.");
        OptionBuilder.withLongOpt("formula");
        OptionBuilder.withType(String.class);
        Option formula = OptionBuilder.create(PARAMETER_FORMULA);
        group.addOption(formula);

        OptionBuilder.hasArg();
        OptionBuilder.withArgName("property");
        String properties = Arrays.toString(CheckProperties.values());
        OptionBuilder.withDescription("The standard property to check. Possible values: " + properties.substring(1, properties.length() - 1).replace(",", " |"));
        OptionBuilder.withLongOpt("check");
        OptionBuilder.withType(CheckProperties.class);
        Option check = OptionBuilder.create(PARAMETER_STD_PROP);
        group.addOption(check);

        groups.put("mcInput", group);

        return groups;
    }

    @Override
    public void execute(CommandLine line) throws CommandLineParseException, FileNotFoundException, InterruptedException, IOException, ParseException, ProcessNotStartedException, ExternalToolException, Exception {
        super.execute(line);

        // LOAD Topology
        String input = IOParameters.getInput(line);
        String topology = Tools.readFile(input);
        String update = line.getOptionValue(PARAMETER_UPDATE);
        PetriNetWithTransits pnwt = SDNCreator.parse(topology, update, line.hasOption(PARAMETER_OPTIMIZE_CONNECTIONS));

        // GET THE FORMULA 
        RunLTLFormula f;
        ILTLFormula ltlF = null;
        if (line.hasOption(PARAMETER_FORMULA)) { // GIVEN BY THE COMMANDLINE
            String formula = line.getOptionValue(PARAMETER_FORMULA);
            try {
                f = FlowLTLParser.parse(pnwt, formula);
                if (f.getPhi() instanceof ILTLFormula) { // it is just an LTL formula given
                    ltlF = f.toLTLFormula();
                }
            } catch (ParseException pe) {
                throw new ModuleException(pe.getMessage() + ": " + pe.getCause().getMessage(), pe);
            }
        } else if (line.hasOption(PARAMETER_STD_PROP)) { // GIVEN A STANDARD PROPERTY
            String opt = line.getOptionValue(PARAMETER_STD_PROP); // todo: add the standard properties
            if (opt.equals(CheckProperties.connectivity.name())) {
                f = SDNFormelCreator.createConnectivity(pnwt);
            } else if (opt.equals(CheckProperties.dropFreedom.name())) {
                f = SDNFormelCreator.createDropFreedom(pnwt);
            } else if (opt.equals(CheckProperties.loopFreedom.name())) {
                f = SDNFormelCreator.createLoopFreedom(pnwt, false);
            } else if (opt.equals(CheckProperties.weakLoopFreedom.name())) {
                f = SDNFormelCreator.createLoopFreedom(pnwt, true);
            } else if (opt.equals(CheckProperties.packetCoherence.name())) {
                f = SDNFormelCreator.createPackageCoherence(pnwt);
            } else {
                throw new CommandLineParseException("The property '" + opt + "' is not a valid option for parameter '" + PARAMETER_STD_PROP + ".");
            }
        } else {
            throw new CommandLineParseException("A formula or a standard property needs to be provided.");
        }

        // HANDLE VERBOSE
        boolean verbose = line.hasOption(PARAMETER_VERBOSE);
        if (verbose) {
            String output = IOParameters.getOutput(line);
            PNWTTools.savePnwt2PDF(output + "_in", pnwt, false);
            CircuitLTLModelcheckingParameters.addLogger(line, verbose);
        }

        // GETTING THE MC PARAMETERS
        AdamCircuitLTLMCStatistics stats;
        LTLModelCheckingResult ret;
        String formulaSymbolString;
        if (ltlF != null) { // it was just an LTL formula
            AdamCircuitLTLMCSettings settings = CircuitLTLModelcheckingParameters.getMCSettings(line, verbose);
            stats = settings.getStatistics();
            ModelCheckerLTL mc = new ModelCheckerLTL(settings);
            ret = mc.check(pnwt, ltlF);
            formulaSymbolString = ltlF.toSymbolString();
        } else {
            AdamCircuitFlowLTLMCSettings settings = CircuitFlowLTLModelcheckingParameters.getMCSettings(line, verbose);
            stats = settings.getStatistics();
            ModelCheckerFlowLTL mc = new ModelCheckerFlowLTL(settings);
            ret = mc.check(pnwt, f);
            formulaSymbolString = f.toSymbolString();
        }

        if (stats != null) {
            Logger.getInstance().addMessage(stats.toString(), false);
        }
        if (ret.getSatisfied() == LTLModelCheckingResult.Satisfied.TRUE) {
            Logger.getInstance().addMessage("The net '" + pnwt.getName() + "' SATISFIES the formula '" + formulaSymbolString + "'", false, true);
        } else if (ret.getSatisfied() == LTLModelCheckingResult.Satisfied.FALSE) {
            Logger.getInstance().addMessage("The net '" + pnwt.getName() + "' does NOT SATISFY the formula '" + formulaSymbolString + "'", false, true);
            Logger.getInstance().addMessage("A counter examples is: " + ret.getCex().toString(), false, false);
        } else {
            Logger.getInstance().addMessage("The satisfiability of the net '" + pnwt.getName() + "' is UKNOWN for the formula '" + formulaSymbolString + "'."
                    + "Mainly this happens when the bound of a falsifier is not big enough.", false, true);
        }
    }

    @Override
    public String getDescr() {
        return descr;
    }

    @Override
    public String getName() {
        return name;
    }
}
