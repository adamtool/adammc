package uniolunisaar.adam.logic.ui.cl.modules.modelchecking.checker;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.analysis.bounded.BoundedResult;
import uniol.apt.io.parser.ParseException;
import uniol.apt.module.exception.ModuleException;
import uniolunisaar.adam.ds.logics.ltl.ILTLFormula;
import uniolunisaar.adam.ds.logics.ltl.flowltl.RunLTLFormula;
import uniolunisaar.adam.logic.parser.logics.flowltl.FlowLTLParser;
import uniolunisaar.adam.logic.modelchecking.ltl.circuits.ModelCheckerFlowLTL;
import uniolunisaar.adam.logic.modelchecking.ltl.circuits.ModelCheckerLTL;
import uniolunisaar.adam.ds.modelchecking.results.LTLModelCheckingResult;
import uniolunisaar.adam.exceptions.ExternalToolException;
import uniolunisaar.adam.ds.petrinetwithtransits.PetriNetWithTransits;
import uniolunisaar.adam.tools.Logger;
import uniolunisaar.adam.exceptions.ProcessNotStartedException;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.tools.Tools;
import uniolunisaar.adam.data.ui.cl.parameters.IOParameters;
import uniolunisaar.adam.logic.ui.cl.modules.AbstractSimpleModule;
import uniolunisaar.adam.data.ui.cl.parameters.modelchecking.CircuitLTLModelcheckingParameters;
import uniolunisaar.adam.data.ui.cl.parameters.modelchecking.CircuitFlowLTLModelcheckingParameters;
import uniolunisaar.adam.ds.modelchecking.settings.ltl.AdamCircuitFlowLTLMCSettings;
import uniolunisaar.adam.ds.modelchecking.settings.ltl.AdamCircuitLTLMCSettings;
import uniolunisaar.adam.ds.modelchecking.statistics.AdamCircuitLTLMCStatistics;
import uniolunisaar.adam.exceptions.ui.cl.CommandLineParseException;
import uniolunisaar.adam.util.PNWTTools;

/**
 * @author Manuel Gieseking
 */
public class PetriNetWithTransitsModelchecker extends AbstractSimpleModule {

    private static final String name = "mc_pnwt";
    private static final String descr = "Modelchecking Petri nets with transits against FlowLTL "
            + " or LTL.";

    private static final String PARAMETER_FORMULA = "f";
    private static final String PARAMETER_CHECK_PRECOND = "cp";

    @Override
    protected Map<String, Option> createOptions() {
        Map<String, Option> ops = super.createOptions();
        // IO
        ops.putAll(IOParameters.createOptions());
//        // change the output description
//        Option out = ops.get(IOParameters.getPARAMETER_OUTPUT());
//        out.setDescription("The path to an optional output folder.");

        // MC Parameters
        ops.putAll(new CircuitFlowLTLModelcheckingParameters().createOptions());

//        OptionBuilder.withDescription("Skips the tests like 1-bounded. Saves time,"
//                + " but should only be used if you are asure that your net fullfills "
//                + " all necessary preconditions!");
//        OptionBuilder.withLongOpt("skip");
//        ops.put(PARAMETER_SKIP, OptionBuilder.create(PARAMETER_SKIP));  
        OptionBuilder.withDescription("Checks preconditions like 1-bounded. Takes some time,"
                + " but should be used if you are not sure that your net fulfills "
                + " all necessary preconditions!");
        OptionBuilder.withLongOpt("check-precon");
        ops.put(PARAMETER_CHECK_PRECOND, OptionBuilder.create(PARAMETER_CHECK_PRECOND));

        OptionBuilder.hasArg();
        OptionBuilder.withArgName("LTL | Flow-LTL formula");
        OptionBuilder.withDescription("The formula, either Flow-LTL or LTL, which should be checked.");
        OptionBuilder.withLongOpt("formula");
        OptionBuilder.withType(String.class);
        ops.put(PARAMETER_FORMULA, OptionBuilder.create(PARAMETER_FORMULA));

        return ops;
    }

    @Override
    public void execute(CommandLine line) throws CommandLineParseException, FileNotFoundException, InterruptedException, IOException, ParseException, ProcessNotStartedException, ExternalToolException, Exception {
        super.execute(line);

        // LOAD NET
        String input = IOParameters.getInput(line);
        PetriNet net = Tools.getPetriNet(input);
        PetriNetWithTransits pnwt = PNWTTools.getPetriNetWithTransitsFromParsedPetriNet(net, false);

        // CHECK PRECONDITIONS
        if (line.hasOption(PARAMETER_CHECK_PRECOND)) {
            BoundedResult res = Tools.getBounded(pnwt);
            if (!res.isSafe()) {
                throw new NetNotSafeException(res.unboundedPlace.toString(), res.getSequenceExceeding(1).toString());
            }
        } else {
            Logger.getInstance().addWarning("You decided to skip the tests. We cannot ensure that the net is safe.");
        }

        // GET THE FORMULA 
        RunLTLFormula f = null;
        ILTLFormula ltlF = null;
        String formula = line.getOptionValue(PARAMETER_FORMULA);
        try {
            f = FlowLTLParser.parse(pnwt, formula);
            if (f.getPhi() instanceof ILTLFormula) { // it is just an LTL formula given
                ltlF = f.toLTLFormula();
            }
        } catch (ParseException pe) {
            throw new ModuleException(pe.getMessage() + ": " + pe.getCause().getMessage(), pe);
        }

        // HANDLE VERBOSE
        boolean verbose = line.hasOption(PARAMETER_VERBOSE);
        if (verbose) {
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
            stats.addResultToFile();
            // add ABC times to the file
            if (stats.getPath() != null) {
                try (BufferedWriter wr = new BufferedWriter(new FileWriter(stats.getPath(), true))) {
                    wr.append("\nAlgo:").append(ret.getAlgo().name());
                    wr.append("\nABC time:").append(String.valueOf(stats.getAbc_sec()));
                    wr.append("\nABC memory:").append(String.valueOf(stats.getAbc_mem()));
                }
            }
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
