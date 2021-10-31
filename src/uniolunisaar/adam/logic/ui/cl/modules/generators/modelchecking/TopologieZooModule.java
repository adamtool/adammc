package uniolunisaar.adam.logic.ui.cl.modules.generators.modelchecking;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.ParseException;
import uniol.apt.module.exception.ModuleException;
import uniolunisaar.adam.ds.petrinetwithtransits.PetriNetWithTransits;
import uniolunisaar.adam.exceptions.ui.cl.CommandLineParseException;
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
    private static final String PARAMETER_STD_PROP = "c";

    @Override
    public Map<String, Option> createOptions() {
        Map<String, Option> options = super.createOptions();
        OptionBuilder.hasArg();
        OptionBuilder.withArgName("path");
        OptionBuilder.withDescription("The input file for the topology from which the Petri net with transits should be created.");
        OptionBuilder.isRequired();
        OptionBuilder.withLongOpt("input");
        options.put(PARAMETER_INPUT, OptionBuilder.create(PARAMETER_INPUT));

        OptionBuilder.hasArg();
        OptionBuilder.withArgName("property");

        String properties = Arrays.toString(SDNFormulaCreationModule.SDNProperties.values());
        OptionBuilder.withDescription("The standard property to generate. Possible values: " + properties.substring(1, properties.length() - 1).replace(",", " |"));
        OptionBuilder.withLongOpt("prop");
        OptionBuilder.withType(SDNFormulaCreationModule.SDNProperties.class);
        Option check = OptionBuilder.create(PARAMETER_STD_PROP);
        options.put(PARAMETER_STD_PROP, check);
        return options;
    }

    @Override
    public void execute(CommandLine line) throws IOException, InterruptedException, FileNotFoundException, ModuleException, ParseException, Exception {
        super.execute(line);

        PetriNetWithTransits net = null;
        if (line.hasOption(PARAMETER_STD_PROP)) { // GIVEN A STANDARD PROPERTY
            String opt = line.getOptionValue(PARAMETER_STD_PROP); // todo: add the standard properties
            if (opt.equals(SDNFormulaCreationModule.SDNProperties.connectivity.name())) {
                throw new CommandLineParseException("Not yet implemented.");
            } else if (opt.equals(SDNFormulaCreationModule.SDNProperties.dropFreedom.name())) {
                net = TopologyZoo.createTopologyFromFile(line.getOptionValue(PARAMETER_INPUT), false, true, false, false);
            } else if (opt.equals(SDNFormulaCreationModule.SDNProperties.loopFreedom.name())) {
                net = TopologyZoo.createTopologyFromFile(line.getOptionValue(PARAMETER_INPUT), true, false, false, false);
            } else if (opt.equals(SDNFormulaCreationModule.SDNProperties.weakLoopFreedom.name())) {
                net = TopologyZoo.createTopologyFromFile(line.getOptionValue(PARAMETER_INPUT), false, false, true, false);
            } else if (opt.equals(SDNFormulaCreationModule.SDNProperties.packetCoherence.name())) {
                net = TopologyZoo.createTopologyFromFile(line.getOptionValue(PARAMETER_INPUT), false, false, false, true);
            } else {
                throw new CommandLineParseException("The property '" + opt + "' is not a valid option for parameter '" + PARAMETER_STD_PROP + ".");
            }
        }
        if (net == null) {
            net = TopologyZoo.createTopologyFromFile(line.getOptionValue(PARAMETER_INPUT));
        }

        save(net, line, false);
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
