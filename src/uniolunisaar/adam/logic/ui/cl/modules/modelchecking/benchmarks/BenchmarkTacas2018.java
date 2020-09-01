package uniolunisaar.adam.logic.ui.cl.modules.modelchecking.benchmarks;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.io.parser.ParseException;
import uniol.apt.io.parser.impl.PnmlPNParser;
import uniol.apt.module.exception.ModuleException;
import uniolunisaar.adam.ds.logics.ltl.flowltl.RunLTLFormula;
import uniolunisaar.adam.logic.parser.logics.flowltl.FlowLTLParser;
import uniolunisaar.adam.util.PNWTTools;
import uniolunisaar.adam.logic.modelchecking.ltl.circuits.ModelCheckerFlowLTL;
import uniolunisaar.adam.ds.modelchecking.results.LTLModelCheckingResult;
import uniolunisaar.adam.ds.modelchecking.statistics.AdamCircuitFlowLTLMCStatistics;
import uniolunisaar.adam.ds.petrinetwithtransits.PetriNetWithTransits;
import uniolunisaar.adam.data.ui.cl.parameters.IOParameters;
import uniolunisaar.adam.tools.Logger;
import uniolunisaar.adam.tools.Tools;
import uniolunisaar.adam.logic.ui.cl.modules.AbstractSimpleModule;
import uniolunisaar.adam.data.ui.cl.parameters.modelchecking.CircuitFlowLTLModelcheckingParameters;
import uniolunisaar.adam.ds.circuits.CircuitRendererSettings;
import uniolunisaar.adam.ds.modelchecking.output.AdamCircuitFlowLTLMCOutputData;
import uniolunisaar.adam.ds.modelchecking.settings.ModelCheckingSettings;
import uniolunisaar.adam.ds.modelchecking.settings.ltl.AdamCircuitFlowLTLMCSettings;
import uniolunisaar.adam.ds.modelchecking.settings.ltl.AdamCircuitMCSettings;
import uniolunisaar.adam.exceptions.ui.cl.CommandLineParseException;
import uniolunisaar.adam.logic.externaltools.modelchecking.Abc.VerificationAlgo;
import uniolunisaar.adam.logic.transformers.pn2aiger.AigerRenderer;

/**
 *
 * @author Manuel Gieseking
 */
@Deprecated
public class BenchmarkTacas2018 extends AbstractSimpleModule {

    private static final String name = "benchTacas2018";
    private static final String descr = "Just for benchmark purposes. Model checks nets and formulas for tacas 2018.";

    private static final String PARAMETER_FORMULA = "f";
    private static final String PARAMETER_INTERN_OUTPUT = "io";

    @Override
    protected Map<String, Option> createOptions() {
        Map<String, Option> options = super.createOptions();

        // Add IO
        options.putAll(IOParameters.createOptions());

        OptionBuilder.withDescription("Standard is to read the file in the APT format. With this option we can directly read a file in PNML format.");
        options.put("pnml", OptionBuilder.create("pnml"));

        // Formula
        OptionBuilder.hasArg();
        OptionBuilder.isRequired();
        OptionBuilder.withArgName("Flow-LTL formula");
        OptionBuilder.withDescription("The Flow-LTL formula, which should be checked.");
        OptionBuilder.withLongOpt("formula");
        OptionBuilder.withType(String.class);
        options.put(PARAMETER_FORMULA, OptionBuilder.create(PARAMETER_FORMULA));

        // Verifier Parameters 
        options.putAll(new CircuitFlowLTLModelcheckingParameters().createOptions());

        // Add Benchmark specific ones
        OptionBuilder.hasArg();
        OptionBuilder.withArgName("file");
        OptionBuilder.withDescription("The path to the output file for the internal benchmark data.");
        OptionBuilder.withLongOpt("out_bench");
        options.put(PARAMETER_INTERN_OUTPUT, OptionBuilder.create(PARAMETER_INTERN_OUTPUT));

        return options;
    }

    @Override
    public void execute(CommandLine line) throws IOException, InterruptedException, FileNotFoundException, ModuleException, ParseException, CommandLineParseException, Exception {
        super.execute(line);
        String input = IOParameters.getInput(line);
        PetriNet net;
        if (line.hasOption("pnml")) {
            net = new PnmlPNParser().parseFile(input);
        } else {
            net = Tools.getPetriNet(input);
        }

        PetriNetWithTransits pnwt = PNWTTools.getPetriNetWithTransitsFromParsedPetriNet(net, false);
        String formula = line.getOptionValue(PARAMETER_FORMULA);
        RunLTLFormula f = FlowLTLParser.parse(pnwt, formula);

        String output = IOParameters.getOutput(line);

        AdamCircuitFlowLTLMCStatistics stats = null;
//        if (CircuitFlowLTLModelcheckingParameters.hasStatistics(line)) { todo: quickhack delted
        if (line.hasOption(PARAMETER_INTERN_OUTPUT)) {
            String output_bench = line.getOptionValue(PARAMETER_INTERN_OUTPUT);
            stats = new AdamCircuitFlowLTLMCStatistics(output_bench);
        } else {
            stats = new AdamCircuitFlowLTLMCStatistics();
        }
//        }

// ATTENTION: THIS COMMENTED LINES IN THIS METHODS HAD BEEN USED TO CALCULATE THE RESULTS. DUE TO RESTRUCTURING 
//            THOSE METHODS ARE NOT AVAILABLE ANYMORE. TO REPRODUCE THE RESULTS, THIS HAS TO BE ADAPTED. 
//            IT WAS ONLY POSSIBLE TO THAT TIME TO HAVE ONE VERIFIER (NO PARALLELISM)
//        VerificationAlgo algo = CircuitFlowLTLModelcheckingParameters.getVerificationAlgorithm(line);
//        String abcParameter = CircuitFlowLTLModelcheckingParameters.getABCParameters(line);
// ATTENTION: FURTHER RESTRUCTURING WAS NECESSARY, CANNOT ENSURE THAT THE PARAMETER FITS THE ORIGINAL ONE OF THE SUBMISSION!
//        AdamCircuitFlowLTLMCSettings settings = new AdamCircuitFlowLTLMCSettings();
//
////        settings.setAbcParameters(abcParameter);
////        if (algo != null) {
////            settings.setVerificationAlgo(algo);
////        }
////        settings.setMaximality(CircuitFlowLTLModelcheckingParameters.getMaximality(line));
//        AdamCircuitFlowLTLMCOutputData data = new AdamCircuitFlowLTLMCOutputData(
//                output,
//                false,
//                false,
//                false);
//        settings.setOutputData(data);
//        settings.setStatistics(stats);
//        
        AdamCircuitFlowLTLMCOutputData data = new AdamCircuitFlowLTLMCOutputData(
                output,
                false,
                false,
                false);
        AdamCircuitFlowLTLMCSettings settings = new AdamCircuitFlowLTLMCSettings(
                data,
                ModelCheckingSettings.Approach.SEQUENTIAL_INHIBITOR,
                AdamCircuitMCSettings.Maximality.MAX_INTERLEAVING_IN_CIRCUIT,
                AdamCircuitMCSettings.Stuttering.PREFIX_REGISTER,
                CircuitRendererSettings.TransitionSemantics.OUTGOING,
                CircuitRendererSettings.TransitionEncoding.EXPLICIT,
                CircuitRendererSettings.AtomicPropositions.PLACES_AND_TRANSITIONS,
                AigerRenderer.OptimizationsSystem.NONE, AigerRenderer.OptimizationsComplete.NONE,
                new VerificationAlgo[]{VerificationAlgo.IC3});

        settings.setStatistics(stats);

        ModelCheckerFlowLTL mc = new ModelCheckerFlowLTL(settings);

        LTLModelCheckingResult ret = mc.check(pnwt, f);
        Logger.getInstance().addMessage("The net '" + pnwt.getName() + "' satisfies the formula '" + f.toSymbolString() + "': " + ret.getSatisfied().toString(), false, true);
        if (stats != null) {
            if (line.hasOption(PARAMETER_INTERN_OUTPUT)) {
                stats.addResultToFile();
            } else {
                Logger.getInstance().addMessage(stats.toString(), false);
            }
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
