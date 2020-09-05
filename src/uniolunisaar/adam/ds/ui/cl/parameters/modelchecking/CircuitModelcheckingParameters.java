package uniolunisaar.adam.ds.ui.cl.parameters.modelchecking;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import uniolunisaar.adam.ds.circuits.CircuitRendererSettings;
import uniolunisaar.adam.ds.modelchecking.output.AdamCircuitLTLMCOutputData;
import uniolunisaar.adam.ds.modelchecking.settings.ltl.AdamCircuitMCSettings;
import uniolunisaar.adam.ds.modelchecking.settings.ltl.AdamCircuitMCSettings.Maximality;
import uniolunisaar.adam.ds.modelchecking.statistics.AdamCircuitLTLMCStatistics;
import uniolunisaar.adam.exceptions.ui.cl.CommandLineParseException;
import uniolunisaar.adam.logic.externaltools.modelchecking.Abc;
import uniolunisaar.adam.logic.externaltools.modelchecking.Abc.VerificationAlgo;
import uniolunisaar.adam.logic.externaltools.pnwt.AigToAig;
import uniolunisaar.adam.logic.externaltools.pnwt.AigToDot;
import uniolunisaar.adam.logic.transformers.pn2aiger.AigerRenderer;
import uniolunisaar.adam.tools.Logger;

/**
 *
 * @author Manuel Gieseking
 */
public class CircuitModelcheckingParameters {

    private static final String PARAMETER_VERIFIER = "veri";
    private static final String PARAMETER_STATISTICS = "stats";
    private static final String PARAMETER_ABC_PARAMETER = "p";
    private static final String PARAMETER_MAXIMALITY = "max";
    private static final String PARAMETER_CIRCUIT = "circ";
    private static final String PARAMETER_CIRCUIT_REDUCTION_SYS = "cr_sys";
    private static final String PARAMETER_CIRCUIT_REDUCTION_COM = "cr_com";
    private static final String PARAMETER_CIRCUIT_REDUCTION_ABC = "cr_abc";
    private static final String PARAMETER_CIRCUIT_PREPROCESSING = "pre";
    private static final String PARAMETER_CIRCUIT_ENCODING = "enc";
    private static final String PARAMETER_CIRCUIT_ATOMS = "atoms";
    private static final String PARAMETER_CIRCUIT_SEMANTICS = "sem";
    private static final String PARAMETER_MCHYPER_TOFORMULA = "noF";

    public Map<String, Option> createOptions() {
        Map<String, Option> options = new HashMap<>();
        // Verifier
        OptionBuilder.hasArg();
        OptionBuilder.withArgName("verifier");
        OptionBuilder.withDescription("The set of ver- and falsifieres which should be executed in parallel."
                + "Note that even parallel execution has some overhead. Input format: v_1 | ... | v_n with v_i from {IC3, INT, BMC, BMC2, BMC3}. Standard: IC3");
        OptionBuilder.withLongOpt("verifier");
        OptionBuilder.withType(String.class);
        options.put(PARAMETER_VERIFIER, OptionBuilder.create(PARAMETER_VERIFIER));

        // Verifier Parameters
        OptionBuilder.hasArg();
        OptionBuilder.withArgName("abcParameters");
        OptionBuilder.withDescription("Parameters for the verifier / falsifier for abc. Standard: no parameters.");
        OptionBuilder.withLongOpt("abcParameters");
        OptionBuilder.withType(String.class);
        options.put(PARAMETER_ABC_PARAMETER, OptionBuilder.create(PARAMETER_ABC_PARAMETER));

        // ModelcheckingStatistics
        OptionBuilder.hasArg();
        OptionBuilder.withArgName("file");
        OptionBuilder.withDescription("Calculates some statistics for the call. If file is the empty string it is printed to the output.");
        OptionBuilder.withLongOpt("statistics");
        options.put(PARAMETER_STATISTICS, OptionBuilder.create(PARAMETER_STATISTICS));

        // Outputing files
        OptionBuilder.withDescription("Saves the created circuit of the net as PDF. Attention: this could be really huge and dot could need lots of time!");
        OptionBuilder.withLongOpt("circuit");
        options.put(PARAMETER_CIRCUIT, OptionBuilder.create(PARAMETER_CIRCUIT));

        // Maximality in circuit        
        OptionBuilder.hasArg();
//        OptionBuilder.withArgName("");
//        OptionBuilder.withDescription("Calculate the interleaving maximality directly within the circuit.");
        OptionBuilder.withDescription("States which kind of maximality should be used. "
                + "Possible values:"
                + " IntC (interleaving calculated within the circuit) "
                + " | IntF (interleaving added to the formula) "
                + " | ConF (concurrent added to the formula) "
                + " | NONE. Standard: IntC.");
        OptionBuilder.withLongOpt("maximality");
        OptionBuilder.withType(String.class);
        options.put(PARAMETER_MAXIMALITY, OptionBuilder.create(PARAMETER_MAXIMALITY));

        // Encoding        
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("Encoding of the transitions in the circuit. Possible values: logEnc | expEnc. Standard: logEnc.");
        OptionBuilder.withLongOpt("encoding");
        OptionBuilder.withType(String.class);
        options.put(PARAMETER_CIRCUIT_ENCODING, OptionBuilder.create(PARAMETER_CIRCUIT_ENCODING));

        // Transition semantics        
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("Semantics of the atomic propositions 'transitions'. Possible values: ingoing | outgoing. Standard: outgoing.");
        OptionBuilder.withLongOpt("trans_semantics");
        OptionBuilder.withType(String.class);
        options.put(PARAMETER_CIRCUIT_SEMANTICS, OptionBuilder.create(PARAMETER_CIRCUIT_SEMANTICS));

        // Atomic propositoins
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("Which atomic propositions are allowed. Possible values: pl_tr | pl | fireability . Standard: pl_tr.");
        OptionBuilder.withLongOpt("atoms");
        OptionBuilder.withType(String.class);
        options.put(PARAMETER_CIRCUIT_ATOMS, OptionBuilder.create(PARAMETER_CIRCUIT_ATOMS));

        // Optimizations
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("Reduces the number of gates of the system's circuit."
                + " Possible values: G | G-EQCOM | G-I | G-I-EQCOM | G-I-EXTRA | G-I-EXTRA-EQCOM | NONE."
                + " Standard: NONE.");
        OptionBuilder.withLongOpt("red_gates_sys");
        options.put(PARAMETER_CIRCUIT_REDUCTION_SYS, OptionBuilder.create(PARAMETER_CIRCUIT_REDUCTION_SYS));

        OptionBuilder.hasArg();
        OptionBuilder.withDescription("Reduces the number of gates of the whole cirucit. That means it reduces the output of McHyper."
                + " Possible values: RX-G | RX-G-S | DS-G | DS-G-S | DS-G-S-EXTRA | NONE."
                + " Standard: NONE.");
        OptionBuilder.withLongOpt("red_gates_com");
        options.put(PARAMETER_CIRCUIT_REDUCTION_COM, OptionBuilder.create(PARAMETER_CIRCUIT_REDUCTION_COM));

        OptionBuilder.withDescription("Uses abc dfraig to reduce the circuit.");
        OptionBuilder.withLongOpt("red_abc");
        options.put(PARAMETER_CIRCUIT_REDUCTION_ABC, OptionBuilder.create(PARAMETER_CIRCUIT_REDUCTION_ABC));

        OptionBuilder.hasArg();
        OptionBuilder.withArgName("process");
        OptionBuilder.withDescription("Allows to excute any pre-process of abc before the actual veri- or falsifier is started.");
        OptionBuilder.withLongOpt("preProc");
        OptionBuilder.withType(String.class);
        options.put(PARAMETER_CIRCUIT_PREPROCESSING, OptionBuilder.create(PARAMETER_CIRCUIT_PREPROCESSING));

        OptionBuilder.withDescription("Does not write the formula to a file for giving it to MCHyper. This causes problems for huge formulas.");
        OptionBuilder.withLongOpt("noFile");
        options.put(PARAMETER_MCHYPER_TOFORMULA, OptionBuilder.create(PARAMETER_MCHYPER_TOFORMULA));
        return options;
    }

    public static void addLogger(CommandLine line, boolean verbose) throws CommandLineParseException, FileNotFoundException {
        Logger.getInstance().addMessageStream(AigToAig.LOGGER_AIGER_OUT, System.out);
        Logger.getInstance().addMessageStream(AigToAig.LOGGER_AIGER_ERR, System.err);
        Logger.getInstance().addMessageStream(AigToDot.LOGGER_AIGER_DOT_OUT, System.out);
        Logger.getInstance().addMessageStream(AigToDot.LOGGER_AIGER_DOT_ERR, System.err);
//        Logger.getInstance().addMessageStream(McHyper.LOGGER_MCHYPER_OUT, System.out);
//        Logger.getInstance().addMessageStream(McHyper.LOGGER_MCHYPER_ERR, System.err);
        Logger.getInstance().addMessageStream(Abc.LOGGER_ABC_OUT, System.out);
        Logger.getInstance().addMessageStream(Abc.LOGGER_ABC_ERR, System.err);
    }

    private static void handleOptimizations(CommandLine line, AdamCircuitMCSettings<? extends AdamCircuitLTLMCOutputData, ? extends AdamCircuitLTLMCStatistics> settings) {
        if (line.hasOption(PARAMETER_CIRCUIT_REDUCTION_SYS)) {
            String optimizations = line.getOptionValue(PARAMETER_CIRCUIT_REDUCTION_SYS);
            AigerRenderer.OptimizationsSystem optiSys = AigerRenderer.OptimizationsSystem.NONE;
            if (optimizations.equals("G")) {
                optiSys = AigerRenderer.OptimizationsSystem.NB_GATES;
            } else if (optimizations.equals("G-EQCOM")) {
                optiSys = AigerRenderer.OptimizationsSystem.NB_GATES_AND_EQCOM;
            } else if (optimizations.equals("G-I")) {
                optiSys = AigerRenderer.OptimizationsSystem.NB_GATES_AND_INDICES;
            } else if (optimizations.equals("G-I-EQCOM")) {
                optiSys = AigerRenderer.OptimizationsSystem.NB_GATES_AND_INDICES_AND_EQCOM;
            } else if (optimizations.equals("G-I-EXTRA")) {
                optiSys = AigerRenderer.OptimizationsSystem.NB_GATES_AND_INDICES_EXTRA;
            } else if (optimizations.equals("G-I-EXTRA-EQCOM")) {
                optiSys = AigerRenderer.OptimizationsSystem.NB_GATES_AND_INDICES_EXTRA_AND_EQCOM;
            }
            settings.setOptsSys(optiSys);
        }
        if (line.hasOption(PARAMETER_CIRCUIT_REDUCTION_COM)) {
            String optimizations = line.getOptionValue(PARAMETER_CIRCUIT_REDUCTION_COM);
            AigerRenderer.OptimizationsComplete optsComp = AigerRenderer.OptimizationsComplete.NONE;
            if (optimizations.equals("RX-G")) {
                optsComp = AigerRenderer.OptimizationsComplete.NB_GATES_BY_REGEX;
            } else if (optimizations.equals("RX-G-S")) {
                optsComp = AigerRenderer.OptimizationsComplete.NB_GATES_BY_REGEX_WITH_IDX_SQUEEZING;
            } else if (optimizations.equals("DS-G")) {
                optsComp = AigerRenderer.OptimizationsComplete.NB_GATES_BY_DS;
            } else if (optimizations.equals("DS-G-S")) {
                optsComp = AigerRenderer.OptimizationsComplete.NB_GATES_BY_DS_WITH_IDX_SQUEEZING;
            } else if (optimizations.equals("DS-G-S-EXTRA")) {
                optsComp = AigerRenderer.OptimizationsComplete.NB_GATES_BY_DS_WITH_IDX_SQUEEZING_AND_EXTRA_LIST;
            }
            settings.setOptsComp(optsComp);
        }
        if (line.hasOption(PARAMETER_CIRCUIT_REDUCTION_ABC)) {
            settings.setCircuitReductionABC(true);
        }
        if (line.hasOption(PARAMETER_CIRCUIT_PREPROCESSING)) {
            settings.setABCPreprocessingCommands(line.getOptionValue(PARAMETER_CIRCUIT_PREPROCESSING));
        }
    }

    private static Abc.VerificationAlgo[] getVerificationAlgorithm(CommandLine line) throws CommandLineParseException {
        String[] veris = line.getOptionValue(PARAMETER_VERIFIER).split("\\|");
        Abc.VerificationAlgo[] algos = new Abc.VerificationAlgo[veris.length];
        for (int i = 0; i < veris.length; i++) {
            String veri = veris[i];
            Abc.VerificationAlgo algo = null;
            if (veri.equals("IC3")) {
                algo = VerificationAlgo.IC3;
            } else if (veri.equals("INT")) {
                algo = VerificationAlgo.INT;
            } else if (veri.equals("BMC")) {
                algo = VerificationAlgo.BMC;
            } else if (veri.equals("BMC2")) {
                algo = VerificationAlgo.BMC2;
            } else if (veri.equals("BMC3")) {
                algo = VerificationAlgo.BMC3;
            } else {
                throw new CommandLineParseException("The value '" + veri + "' is no valid ver- or falsifier.");
            }
            algos[i] = algo;
        }
        return algos;
    }

    private static Maximality getMaximality(CommandLine line) throws CommandLineParseException {
        String maxi = line.getOptionValue(PARAMETER_MAXIMALITY);
        if (maxi.equals("IntC")) {
            return Maximality.MAX_INTERLEAVING_IN_CIRCUIT;
        } else if (maxi.equals("IntF")) {
            return Maximality.MAX_INTERLEAVING;
        } else if (maxi.equals("ConF")) {
            return Maximality.MAX_CONCURRENT;
        } else if (maxi.equals("NONE")) {
            return Maximality.MAX_NONE;
        } else {
            throw new CommandLineParseException("The value '" + maxi + "' is no suitable maximality key.");
        }
    }

    private static String getABCParameters(CommandLine line) {
        return line.getOptionValue(PARAMETER_ABC_PARAMETER);
    }

    static void handleParameters(CommandLine line, AdamCircuitMCSettings<? extends AdamCircuitLTLMCOutputData, ? extends AdamCircuitLTLMCStatistics> settings) throws CommandLineParseException {
        if (line.hasOption(PARAMETER_VERIFIER)) {
            settings.setVerificationAlgo(getVerificationAlgorithm(line));
        } else {
            settings.setVerificationAlgo(VerificationAlgo.IC3);
        }
        if (line.hasOption(PARAMETER_ABC_PARAMETER)) {
            String abcParameter = CircuitModelcheckingParameters.getABCParameters(line);
            settings.setAbcParameters(abcParameter);
        } else {
            settings.setAbcParameters("");
        }
        if (line.hasOption(PARAMETER_MAXIMALITY)) {
            Maximality max = CircuitModelcheckingParameters.getMaximality(line);
            settings.setMaximality(max);
        } else {
            settings.setMaximality(Maximality.MAX_INTERLEAVING_IN_CIRCUIT);
        }
        if (line.hasOption(PARAMETER_CIRCUIT_ENCODING)) {
            String encoding = line.getOptionValue(PARAMETER_CIRCUIT_ENCODING);
            if (encoding.equals("logEnc")) {
                settings.setTransitionEncoding(CircuitRendererSettings.TransitionEncoding.LOGARITHMIC);
            } else if (encoding.equals("expEnc")) {
                settings.setTransitionEncoding(CircuitRendererSettings.TransitionEncoding.EXPLICIT);
            } else {
                throw new CommandLineParseException("The value '" + encoding + "' is no suitable encoding key.");
            }
        }
        if (line.hasOption(PARAMETER_CIRCUIT_ATOMS)) {
            String atoms = line.getOptionValue(PARAMETER_CIRCUIT_ATOMS);
            if (atoms.equals("pl_tr")) {
                settings.setCircuitAtoms(CircuitRendererSettings.AtomicPropositions.PLACES_AND_TRANSITIONS);
            } else if (atoms.equals("pl")) {
                settings.setCircuitAtoms(CircuitRendererSettings.AtomicPropositions.PLACES);
            } else if (atoms.equals("fireability")) {
                settings.setCircuitAtoms(CircuitRendererSettings.AtomicPropositions.FIREABILITY);
            } else {
                throw new CommandLineParseException("The value '" + atoms + "' is no suitable encoding key.");
            }
        }
        if (line.hasOption(PARAMETER_CIRCUIT_SEMANTICS)) {
            String semantics = line.getOptionValue(PARAMETER_CIRCUIT_SEMANTICS);
            if (semantics.equals("ingoing")) {
                settings.setTransitionSemantics(CircuitRendererSettings.TransitionSemantics.INGOING);
            } else if (semantics.equals("outgoing")) {
                settings.setTransitionSemantics(CircuitRendererSettings.TransitionSemantics.OUTGOING);
            } else {
                throw new CommandLineParseException("The value '" + semantics + "' is no suitable encoding key.");
            }
        }
        if (line.hasOption(PARAMETER_MCHYPER_TOFORMULA)) {
            settings.setUseFormulaFileForMcHyper(false);
        }
        handleOptimizations(line, settings);
    }

    public static String getStatisticsFile(CommandLine line) {
        if (line.hasOption(PARAMETER_STATISTICS)) {
            return line.getOptionValue(PARAMETER_STATISTICS);
        } else {
            return null;
        }
    }

    public static boolean saveCircuit(CommandLine line) {
        return line.hasOption(PARAMETER_CIRCUIT);
    }
}
