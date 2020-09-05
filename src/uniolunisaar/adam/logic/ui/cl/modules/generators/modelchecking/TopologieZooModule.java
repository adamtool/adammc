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
import uniolunisaar.adam.generators.pnwt.TopologyZoo;

/**
 *
 * @author Manuel Gieseking
 */
public class TopologieZooModule extends AbstractMCGeneratorModule {

    private static final String name = "gen_topologie_zoo";
    private static final String descr = "Generates"
            + " a network from the topology given by the input file."
            + " Saves the resulting net in APT and, if dot is executable, as pdf.";
    private static final String PARAMETER_INPUT = "i";

    @Override
    public Map<String, Option> createOptions() {
        Map<String, Option> options = super.createOptions();
        OptionBuilder.hasArg();
        OptionBuilder.withArgName("path");
        OptionBuilder.withDescription("The input file for the topology from which the Petri game should be created.");
        OptionBuilder.isRequired();
        OptionBuilder.withLongOpt("input");
        options.put(PARAMETER_INPUT, OptionBuilder.create(PARAMETER_INPUT));
        return options;
    }

    @Override
    public void execute(CommandLine line) throws IOException, InterruptedException, FileNotFoundException, ModuleException, ParseException, Exception {
        super.execute(line);

        PetriNetWithTransits net = TopologyZoo.createTopologyFromFile(line.getOptionValue(PARAMETER_INPUT));
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
