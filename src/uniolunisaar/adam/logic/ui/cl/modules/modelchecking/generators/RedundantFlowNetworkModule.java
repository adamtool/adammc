package uniolunisaar.adam.logic.ui.cl.modules.modelchecking.generators;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import uniol.apt.module.exception.ModuleException;
import uniolunisaar.adam.ds.petrinetwithtransits.PetriNetWithTransits;
import uniolunisaar.adam.exceptions.ui.cl.CommandLineParseException;
import uniolunisaar.adam.generators.pnwt.RedundantNetwork;

/**
 *
 * @author Manuel Gieseking
 */
public class RedundantFlowNetworkModule extends AbstractMCGeneratorModule {

    private static final String name = "gen_mc_redundant_flow_network";
    private static final String descr = "Generates"
            + " a network which has two ways to the output. A update function can block one of the ways."
            + " This can be done in correct or incorrect ways."
            + " Saves the resulting net in APT and, if dot is executable, as pdf.";
    private static final String PARAMETER_NB_NODESU = "nodesU";
    private static final String PARAMETER_NB_NODESD = "nodesD";
    private static final String PARAMETER_VERSION = "nv";
    private static final String PARAMETER_CONNECTIVITY = "con";

    @Override
    public Map<String, Option> createOptions() {
        Map<String, Option> options = super.createOptions();
        addIntParameter(options, PARAMETER_NB_NODESU, "The desired number of node for the upper path (>= 1).");
        addIntParameter(options, PARAMETER_NB_NODESD, "The desired number of node for the lower path (>= 1).");

        OptionBuilder.hasArg();
        OptionBuilder.withArgName("version");
        OptionBuilder.withDescription("The desired version of the network (B - basic, U - update, M - mutex, C - correct).");
        OptionBuilder.isRequired();
        OptionBuilder.withLongOpt("version");
        options.put(PARAMETER_VERSION, OptionBuilder.create(PARAMETER_VERSION));

        OptionBuilder.withDescription("Adds the formula checking connectivity to the net inscription.");
        OptionBuilder.withLongOpt("connectivity");
        options.put(PARAMETER_CONNECTIVITY, OptionBuilder.create(PARAMETER_CONNECTIVITY));
        return options;
    }

    @Override
    public void execute(CommandLine line) throws IOException, InterruptedException, FileNotFoundException, ModuleException, ParseException, Exception {
        super.execute(line);
        int nb_nodesU = getIntParameter(PARAMETER_NB_NODESU, line);
        int nb_nodesD = getIntParameter(PARAMETER_NB_NODESD, line);
        String version = line.getOptionValue(PARAMETER_VERSION);

        PetriNetWithTransits net = null;
        if (version.equals("B")) {
            net = RedundantNetwork.getBasis(nb_nodesU, nb_nodesD);
        } else if (version.equals("U")) {
            net = RedundantNetwork.getUpdatingNetwork(nb_nodesU, nb_nodesD);
        } else if (version.equals("M")) {
            net = RedundantNetwork.getUpdatingStillNotFixedMutexNetwork(nb_nodesU, nb_nodesD);
        } else if (version.equals("C")) {
            net = RedundantNetwork.getUpdatingStillNotFixedMutexNetwork(nb_nodesU, nb_nodesD);
        } else {
            throw new CommandLineParseException("The version '" + version + "' is not a valid options.");
        }

        if (line.hasOption(PARAMETER_CONNECTIVITY)) {
            RedundantNetwork.addConnectivity(net);
        }

        save(net, line);
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
