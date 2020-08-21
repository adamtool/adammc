package uniolunisaar.adam.logic.ui.cl.modules.converter.modelchecking;

import java.io.IOException;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import uniol.apt.io.parser.ParseException;
import uniolunisaar.adam.data.ui.cl.parameters.IOParameters;
import uniolunisaar.adam.ds.petrinetwithtransits.PetriNetWithTransits;
import uniolunisaar.adam.exceptions.ui.cl.CommandLineParseException;
import uniolunisaar.adam.generators.pnwt.SDNCreator;
import uniolunisaar.adam.logic.parser.sdn.SDNTopologyParser;
import uniolunisaar.adam.logic.ui.cl.modules.AbstractSimpleModule;
import uniolunisaar.adam.tools.Tools;
import uniolunisaar.adam.util.PNWTTools;

/**
 *
 * @author Manuel Gieseking
 */
public class SDN2Dot extends AbstractSimpleModule {

    private final String name = "sdn2dot";
    private final String descr = "Converts a Software Defined Networks topology (with an concurrent update)"
            + " to a Petri net with transits and saves this to a dot file.";

    private static final String PARAMETER_UPDATE = "u";
    private static final String PARAMETER_OPTIMIZE_CONNECTIONS = "optCon";

    @Override
    public Map<String, Option> createOptions() {
        Map<String, Option> options = super.createOptions();
        // Add IO
        options.putAll(IOParameters.createOptions());

        // Add optional update
        OptionBuilder.hasArg();
        OptionBuilder.withArgName("cup");
        OptionBuilder.withDescription("A concurrent update.");
        OptionBuilder.withLongOpt("update");
        options.put(PARAMETER_UPDATE, OptionBuilder.create(PARAMETER_UPDATE));

        OptionBuilder.withDescription("If set only the necessary connections of the"
                + " topology are added. This means only those used in the initial configuration and"
                + " those of the update.");
        OptionBuilder.withLongOpt("opt-connections");
        options.put(PARAMETER_OPTIMIZE_CONNECTIONS, OptionBuilder.create(PARAMETER_OPTIMIZE_CONNECTIONS));
        return options;
    }

    @Override
    public void execute(CommandLine line) throws IOException, CommandLineParseException, ClassNotFoundException, ParseException, Exception {
        super.execute(line);

        String inputTopology = Tools.readFile(IOParameters.getInput(line));

        PetriNetWithTransits pnwt;
        if (line.hasOption(PARAMETER_UPDATE)) {
            pnwt = SDNCreator.parse(inputTopology, line.getOptionValue(PARAMETER_UPDATE), line.hasOption(PARAMETER_OPTIMIZE_CONNECTIONS));
        } else {
            pnwt = SDNTopologyParser.parse(inputTopology, line.hasOption(PARAMETER_OPTIMIZE_CONNECTIONS));
        }
        PNWTTools.savePnwt2Dot(IOParameters.getOutput(line), pnwt, true);
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
