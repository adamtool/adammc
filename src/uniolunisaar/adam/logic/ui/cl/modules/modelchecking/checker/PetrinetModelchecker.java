package uniolunisaar.adam.logic.ui.cl.modules.modelchecking.checker;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.analysis.bounded.BoundedResult;
import uniol.apt.io.parser.ParseException;
import uniol.apt.io.parser.impl.PnmlPNParser;
import uniol.apt.module.exception.ModuleException;
import uniolunisaar.adam.ds.logics.ltl.ILTLFormula;
import uniolunisaar.adam.ds.logics.ltl.flowltl.RunLTLFormula;
import uniolunisaar.adam.logic.parser.logics.flowltl.FlowLTLParser;
import uniolunisaar.adam.util.logics.FormulaCreator;
import uniolunisaar.adam.logic.modelchecking.ltl.circuits.ModelCheckerLTL;
import uniolunisaar.adam.ds.modelchecking.results.LTLModelCheckingResult;
import uniolunisaar.adam.exceptions.ExternalToolException;
import uniolunisaar.adam.tools.Logger;
import uniolunisaar.adam.exceptions.ProcessNotStartedException;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.tools.Tools;
import uniolunisaar.adam.ds.ui.cl.parameters.IOParameters;
import uniolunisaar.adam.logic.ui.cl.modules.AbstractSimpleModule;
import uniolunisaar.adam.data.ui.cl.parameters.modelchecking.CircuitLTLModelcheckingParameters;
import uniolunisaar.adam.ds.modelchecking.settings.ltl.AdamCircuitLTLMCSettings;
import uniolunisaar.adam.ds.modelchecking.statistics.AdamCircuitLTLMCStatistics;
import uniolunisaar.adam.exceptions.ui.cl.CommandLineParseException;
import uniolunisaar.adam.util.PNTools;

/**
 * @author Manuel Gieseking
 */
public class PetrinetModelchecker extends AbstractSimpleModule {

    private static final String name = "mc_pn";
    private static final String descr = "Modelchecking 1-bounded Petri nets with inhibitor arcs against LTL.";

    private static final String PARAMETER_FORMULA = "f";
//    private static final String PARAMETER_STD_PROP = "c";
    private static final String PARAMETER_PNML = "pnml";
    private static final String PARAMETER_CHECK_PRECOND = "cp";

    private static enum CheckProperties {
        deadlock,
        reversible,
        quasiLive,
        live,
    }

    @Override
    protected Map<String, Option> createOptions() {
        Map<String, Option> ops = super.createOptions();
        // IO
        ops.putAll(IOParameters.createOptions());
//        // change the output description
//        Option out = ops.get(IOParameters.getPARAMETER_OUTPUT());
//        out.setDescription("The path to an optional output folder.");

        OptionBuilder.withDescription("Allows to read the Petri net from the PNML format rather than the standard format.");
        ops.put(PARAMETER_PNML, OptionBuilder.create(PARAMETER_PNML));

        // MC Parameters
        ops.putAll(new CircuitLTLModelcheckingParameters().createOptions());

//        OptionBuilder.withDescription("Skips the tests like 1-bounded. Saves time,"
//                + " but should only be used if you are sure that your net fulfills "
//                + " all necessary preconditions!");
        OptionBuilder.withDescription("Checks preconditions like 1-bounded. Takes some time,"
                + " but should be used if you are not sure that your net fulfills "
                + " all necessary preconditions!");
        OptionBuilder.withLongOpt("check-precon");
        ops.put(PARAMETER_CHECK_PRECOND, OptionBuilder.create(PARAMETER_CHECK_PRECOND));
        return ops;
    }

    @Override
    protected Map<String, OptionGroup> createOptionGroups() {
        Map<String, OptionGroup> groups = super.createOptionGroups();

        OptionGroup group = new OptionGroup();
        group.setRequired(true);

        OptionBuilder.hasArg();
        OptionBuilder.isRequired();
        OptionBuilder.withArgName("LTL");
        OptionBuilder.withDescription("The formula which should be checked.");
        OptionBuilder.withLongOpt("formula");
        OptionBuilder.withType(String.class);
        Option formula = OptionBuilder.create(PARAMETER_FORMULA);
        group.addOption(formula);

//        OptionBuilder.hasArg();
//        OptionBuilder.withArgName("property");
//        String properties = Arrays.toString(CheckProperties.values());
//        OptionBuilder.withDescription("The standard property to check. Possible values: " + properties.substring(1, properties.length() - 1).replace(",", " |"));
//        OptionBuilder.withLongOpt("check");
//        OptionBuilder.withType(CheckProperties.class);
//        Option check = OptionBuilder.create(PARAMETER_STD_PROP);
//        group.addOption(check);
        groups.put("mcInput", group);

        return groups;
    }

    @Override
    public void execute(CommandLine line) throws CommandLineParseException, FileNotFoundException, InterruptedException, IOException, ParseException, ProcessNotStartedException, ExternalToolException, Exception {
        super.execute(line);

        // LOAD NET
        String input = IOParameters.getInput(line);
        PetriNet net;
        if (line.hasOption(PARAMETER_PNML)) {
            net = new PnmlPNParser().parseFile(input);
        } else {
            net = Tools.getPetriNet(input);
        }
        PNTools.annotateProcessFamilyID(net);

        // CHECK PRECONDITIONS
        if (line.hasOption(PARAMETER_CHECK_PRECOND)) {
            BoundedResult res = Tools.getBounded(net);
            if (!res.isSafe()) {
                throw new NetNotSafeException(res.unboundedPlace.toString(), res.getSequenceExceeding(1).toString());
            }
        } else {
            Logger.getInstance().addWarning("You decided to skip the tests. We cannot ensure that the net is safe.");
        }

        // GET THE FORMULA 
        ILTLFormula ltl;
        if (line.hasOption(PARAMETER_FORMULA)) { // GIVEN BY THE COMMANDLINE
            String formula = line.getOptionValue(PARAMETER_FORMULA);
            try {
                RunLTLFormula f = FlowLTLParser.parse(net, formula);
                if (!(f.getPhi() instanceof ILTLFormula)) {
                    throw new ModuleException("The formula '" + f.toSymbolString() + "' is not an LTL formula.");
                }
                ltl = f.toLTLFormula();
            } catch (ParseException pe) {
                throw new ModuleException(pe.getMessage() + ": " + pe.getCause().getMessage(), pe);
            }
//        } else if (line.hasOption(PARAMETER_STD_PROP)) { // ONLY GIVEN A STANDARD PROPERTY
//            String opt = line.getOptionValue(PARAMETER_STD_PROP);
//            if (opt.equals(CheckProperties.deadlock.name())) {
//                ltl = FormulaCreator.deadlock(net);
//            } else if (opt.equals(CheckProperties.reversible.name())) {
//                ltl = FormulaCreator.reversible(net);
//            } else if (opt.equals(CheckProperties.quasiLive.name())) {
//                ltl = FormulaCreator.quasiLive(net);
//            } else if (opt.equals(CheckProperties.live.name())) {
//                ltl = FormulaCreator.live(net);
//            } else {
//                throw new CommandLineParseException("The property '" + opt + "' is not a valid option for parameter '" + PARAMETER_STD_PROP + ".");
//            }
        } else {
            throw new CommandLineParseException("A formula or a standard property needs to be provided.");
        }

        // HANDLE VERBOSE
        boolean verbose = line.hasOption(PARAMETER_VERBOSE);
        if (verbose) {
            CircuitLTLModelcheckingParameters.addLogger(line, verbose);
        }

        // GETTING THE MC PARAMETERS
        AdamCircuitLTLMCSettings settings = CircuitLTLModelcheckingParameters.getMCSettings(line, verbose);
        AdamCircuitLTLMCStatistics stats = settings.getStatistics();
        ModelCheckerLTL mc = new ModelCheckerLTL(settings);
        LTLModelCheckingResult ret = mc.check(net, ltl);

        if (stats != null) {
            Logger.getInstance().addMessage(stats.toString(), false);
        }
        if (ret.getSatisfied() == LTLModelCheckingResult.Satisfied.TRUE) {
            Logger.getInstance().addMessage("The net '" + net.getName() + "' SATISFIES the formula '" + ltl.toSymbolString() + "'", false, true);
        } else if (ret.getSatisfied() == LTLModelCheckingResult.Satisfied.FALSE) {
            Logger.getInstance().addMessage("The net '" + net.getName() + "' does NOT SATISFY the formula '" + ltl.toSymbolString() + "'", false, true);
            Logger.getInstance().addMessage("A counter examples is: " + ret.getCex().toString(), false, false);
        } else {
            Logger.getInstance().addMessage("The satisfiability of the net '" + net.getName() + "' is UKNOWN for the formula '" + ltl.toSymbolString() + "'."
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
