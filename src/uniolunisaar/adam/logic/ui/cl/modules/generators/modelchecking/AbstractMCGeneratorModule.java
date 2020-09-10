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
import uniolunisaar.adam.util.PNWTTools;
import uniolunisaar.adam.logic.ui.cl.modules.AbstractSimpleModule;

/**
 *
 * @author Manuel Gieseking
 */
public abstract class AbstractMCGeneratorModule extends AbstractSimpleModule {

    protected static final String PARAMETER_OUTPUT = "o";
    protected static final String PARAMETER_NO_PDF = "npdf";
    private int nbIntParameters = 0;

    @Override
    public Map<String, Option> createOptions() {
        Map<String, Option> options = super.createOptions();

        OptionBuilder.hasArg();
        OptionBuilder.withArgName("file");
        OptionBuilder.withDescription("The output path where the generated Petri net with flows should be saved.");
        OptionBuilder.isRequired();
        OptionBuilder.withLongOpt("output");
        options.put(PARAMETER_OUTPUT, OptionBuilder.create(PARAMETER_OUTPUT));

        OptionBuilder.withLongOpt("noPDF");
        OptionBuilder.withDescription("Does not create a pdf of the generated net.");
        options.put(PARAMETER_NO_PDF, OptionBuilder.create(PARAMETER_NO_PDF));
        return options;
    }

    void addIntParameter(Map<String, Option> options, String id, String desc) {
        ++nbIntParameters;
        OptionBuilder.hasArg();
        OptionBuilder.withArgName("numberOf_" + id);
        OptionBuilder.withDescription(desc);
        OptionBuilder.isRequired();
        OptionBuilder.withLongOpt("nb_" + id);
        OptionBuilder.withType(Number.class);
        String name = "nb" + nbIntParameters;
        options.put(name, OptionBuilder.create(name));
    }

    int getIntParameter(String id, CommandLine line) throws ParseException {
        return ((Number) line.getParsedOptionValue("nb_" + id)).intValue();
    }

    void save(PetriNetWithTransits net, CommandLine line) throws FileNotFoundException, ModuleException, IOException, InterruptedException {
        save(net, line, !line.hasOption(PARAMETER_NO_PDF));
    }

    void save(PetriNetWithTransits net, CommandLine line, boolean withPDF) throws FileNotFoundException, ModuleException, IOException, InterruptedException {
        String output = line.getOptionValue(PARAMETER_OUTPUT);

        PNWTTools.saveAPT(output, net, true, true);
        if (withPDF) {
            PNWTTools.savePnwt2PDF(output, net, true);
        }
    }
}
