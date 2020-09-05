package uniolunisaar.adam.logic.ui.cl.modules.generators.modelchecking;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import uniol.apt.module.exception.ModuleException;
import uniolunisaar.adam.ds.petrinetwithtransits.PetriNetWithTransits;
import uniolunisaar.adam.generators.pnwt.RedundantNetwork;
import uniolunisaar.adam.generators.pnwt.UpdatingNetwork;

/**
 *
 * @author Manuel Gieseking
 */
public class RemoveNodeUpdateNetworkModule extends AbstractMCGeneratorModule {

    private static final String name = "gen_mc_rm_node_update";
    private static final String descr = "Generates"
            + " a network which has an update function to detour exactly one node (the node is chosen randomly)."
            + " Saves the resulting net in APT and, if dot is executable, as pdf.";
    private static final String PARAMETER_NODES = "nodes";
    private static final String PARAMETER_CONNECTIVITY = "con";

    @Override
    public Map<String, Option> createOptions() {
        Map<String, Option> options = super.createOptions();
        addIntParameter(options, PARAMETER_NODES, "The desired number of node (>= 3).");

        OptionBuilder.withDescription("Adds the formula checking connectivity to the net inscription.");
        OptionBuilder.withLongOpt("connectivity");
        options.put(PARAMETER_CONNECTIVITY, OptionBuilder.create(PARAMETER_CONNECTIVITY));
        return options;
    }

    @Override
    public void execute(CommandLine line) throws IOException, InterruptedException, FileNotFoundException, ModuleException, ParseException, Exception {
        super.execute(line);
        int nb_nodes = getIntParameter(PARAMETER_NODES, line);

        PetriNetWithTransits net = UpdatingNetwork.create(nb_nodes, false); // todo: put the false to the parameters when finished.

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
