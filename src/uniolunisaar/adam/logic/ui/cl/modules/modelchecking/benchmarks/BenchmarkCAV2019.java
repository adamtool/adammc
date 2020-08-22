package uniolunisaar.adam.logic.ui.cl.modules.modelchecking.benchmarks;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.io.parser.ParseException;
import uniol.apt.module.exception.ModuleException;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.exceptions.pg.NoStrategyExistentException;
import uniolunisaar.adam.exceptions.pg.NoSuitableDistributionFoundException;
import uniolunisaar.adam.logic.externaltools.modelchecking.Abc.VerificationAlgo;
import uniolunisaar.adam.ds.logics.ltl.flowltl.RunLTLFormula;
import uniolunisaar.adam.logic.parser.logics.flowltl.FlowLTLParser;
import uniolunisaar.adam.util.PNWTTools;
import uniolunisaar.adam.logic.modelchecking.ltl.circuits.ModelCheckerFlowLTL;
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
import uniolunisaar.adam.logic.transformers.pn2aiger.AigerRenderer;
import uniolunisaar.adam.util.benchmarks.modelchecking.BenchmarksMC;

/**
 *
 * @author Manuel Gieseking
 */
@Deprecated
public class BenchmarkCAV2019 extends AbstractSimpleModule {

    private static final String name = "benchCAV2019";
    private static final String descr = "Just for benchmark purposes. Model checks nets and formulas for CAV 2019.";

//    private static final String PARAMETER_FORMULA = "f";
    @Override
    protected Map<String, Option> createOptions() {
        Map<String, Option> options = super.createOptions();

        // Add IO
        options.putAll(IOParameters.createOptions());

        // Formula
//        OptionBuilder.hasArg();
//        OptionBuilder.isRequired();
//        OptionBuilder.withArgName("Flow-LTL formula");
//        OptionBuilder.withDescription("The Flow-LTL formula, which should be checked.");
//        OptionBuilder.withLongOpt("formula");
//        OptionBuilder.withType(String.class);
//        options.put(PARAMETER_FORMULA, OptionBuilder.create(PARAMETER_FORMULA));
        // Verifier Parameters 
        options.putAll(new CircuitFlowLTLModelcheckingParameters().createOptions());

        return options;
    }

    @Override
    public void execute(CommandLine line) throws IOException, InterruptedException, FileNotFoundException, ModuleException, NetNotSafeException, NoSuitableDistributionFoundException, NoStrategyExistentException, ParseException, CommandLineParseException, Exception {
        super.execute(line);
        String input = IOParameters.getInput(line);
        PetriNet net;
        net = Tools.getPetriNet(input);

        PetriNetWithTransits pnwt = PNWTTools.getPetriNetWithTransitsFromParsedPetriNet(net, false);
        String formula = (String) pnwt.getExtension("formula");
//        String formula = line.getOptionValue(PARAMETER_FORMULA);
        RunLTLFormula f = FlowLTLParser.parse(pnwt, formula);

        String output = IOParameters.getOutput(line);

        AdamCircuitFlowLTLMCStatistics stats = new AdamCircuitFlowLTLMCStatistics();
        BenchmarksMC.EDACC = true;
        Logger.getInstance().addMessageStream("edacc", System.out);
        Logger.getInstance().setSilent(true);
// ATTENTION: THIS COMMENTED LINES IN THIS METHODS HAD BEEN USED TO CALCULATE THE RESULTS. DUE TO RESTRUCTURING 
//            THOSE METHODS ARE NOT AVAILABLE ANYMORE. TO REPRODUCE THE RESULTS, THIS HAS TO BE ADAPTED. 
//            IT WAS ONLY POSSIBLE TO THAT TIME TO HAVE ONE VERIFIER (NO PARALLELISM)
//        VerificationAlgo algo = CircuitFlowLTLModelcheckingParameters.getVerificationAlgorithm(line);

//        String abcParameter = CircuitFlowLTLModelcheckingParameters.getABCParameters(line);
// ATTENTION: FURTHER RESTRUCTURING WAS NECESSARY, CANNOT ENSURE THAT THE PARAMETER FITS THE ORIGINAL ONE OF THE SUBMISSION!
//        AdamCircuitFlowLTLMCSettings settings = new AdamCircuitFlowLTLMCSettings();
////        settings.setAbcParameters(abcParameter);
////        if (algo != null) {
////            settings.setVerificationAlgo(algo);
////        }
////        settings.setMaximality(CircuitFlowLTLModelcheckingParameters.getMaximality(line));
//
//        AdamCircuitFlowLTLMCOutputData data = new AdamCircuitFlowLTLMCOutputData(
//                output,
//                false,
//                false,
//                false);
//        settings.setOutputData(data);
//        settings.setStatistics(stats);
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
        mc.check(pnwt, f);
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
