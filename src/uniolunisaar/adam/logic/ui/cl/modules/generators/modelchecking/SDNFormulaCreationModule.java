package uniolunisaar.adam.logic.ui.cl.modules.generators.modelchecking;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.io.parser.ParseException;
import uniolunisaar.adam.ds.logics.ltl.flowltl.RunLTLFormula;
import uniolunisaar.adam.exceptions.ExternalToolException;
import uniolunisaar.adam.ds.petrinetwithtransits.PetriNetWithTransits;
import uniolunisaar.adam.exceptions.ProcessNotStartedException;
import uniolunisaar.adam.tools.Tools;
import uniolunisaar.adam.ds.ui.cl.parameters.IOParameters;
import uniolunisaar.adam.logic.ui.cl.modules.AbstractSimpleModule;
import uniolunisaar.adam.exceptions.ui.cl.CommandLineParseException;
import uniolunisaar.adam.util.PNWTTools;
import uniolunisaar.adam.util.modelchecking.sdn.SDNFormelCreator;

/**
 * @author Manuel Gieseking
 */
public class SDNFormulaCreationModule extends AbstractSimpleModule {

    private static final String name = "gen_sdn_formula";
    private static final String descr = "Generates formulas for the standard properties of Petri net with transits encoding an SDN."
            + " The places and transitions must be annotated propertly.";

    private static final String PARAMETER_STD_PROP = "c";

    static enum SDNProperties {
        connectivity,
        loopFreedom,
        weakLoopFreedom,
        dropFreedom,
        packetCoherence
    }

    @Override
    protected Map<String, Option> createOptions() {
        Map<String, Option> ops = super.createOptions();
        // IO
        ops.putAll(IOParameters.createOptions());

        OptionBuilder.hasArg();
        OptionBuilder.withArgName("property");

        String properties = Arrays.toString(SDNProperties.values());
        OptionBuilder.withDescription("The standard property to generate. Possible values: " + properties.substring(1, properties.length() - 1).replace(",", " |"));
        OptionBuilder.withLongOpt("prop");
        OptionBuilder.withType(SDNProperties.class);
        Option check = OptionBuilder.create(PARAMETER_STD_PROP);
        ops.put(PARAMETER_STD_PROP, check);

        return ops;
    }

    @Override
    public void execute(CommandLine line) throws CommandLineParseException, FileNotFoundException, InterruptedException, IOException, ParseException, ProcessNotStartedException, ExternalToolException, Exception {
        super.execute(line);

        // LOAD NET
        String input = IOParameters.getInput(line);
        PetriNet net = Tools.getPetriNet(input);
        PetriNetWithTransits pnwt = PNWTTools.getPetriNetWithTransitsFromParsedPetriNet(net, false);

        boolean verbose = line.hasOption(PARAMETER_VERBOSE);

        RunLTLFormula f = null;
        if (line.hasOption(PARAMETER_STD_PROP)) { // GIVEN A STANDARD PROPERTY
            String opt = line.getOptionValue(PARAMETER_STD_PROP); // todo: add the standard properties
            if (opt.equals(SDNProperties.connectivity.name())) {
                f = SDNFormelCreator.createConnectivity(pnwt);
            } else if (opt.equals(SDNProperties.dropFreedom.name())) {
                f = SDNFormelCreator.createDropFreedom(pnwt);
            } else if (opt.equals(SDNProperties.loopFreedom.name())) {
                f = SDNFormelCreator.createLoopFreedom(pnwt, false);
            } else if (opt.equals(SDNProperties.weakLoopFreedom.name())) {
                f = SDNFormelCreator.createLoopFreedom(pnwt, true);
            } else if (opt.equals(SDNProperties.packetCoherence.name())) {
                f = SDNFormelCreator.createPackageCoherence(pnwt);
            } else {
                throw new CommandLineParseException("The property '" + opt + "' is not a valid option for parameter '" + PARAMETER_STD_PROP + ".");
            }
        }
        System.out.println(f.toString());
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
