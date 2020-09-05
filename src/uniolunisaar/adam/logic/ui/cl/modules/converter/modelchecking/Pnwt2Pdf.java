package uniolunisaar.adam.logic.ui.cl.modules.converter.modelchecking;

import java.io.IOException;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import uniol.apt.io.parser.ParseException;
import uniolunisaar.adam.ds.ui.cl.parameters.IOParameters;
import uniolunisaar.adam.exceptions.ui.cl.CommandLineParseException;
import uniolunisaar.adam.logic.ui.cl.modules.AbstractSimpleModule;
import uniolunisaar.adam.util.PNWTTools;

/**
 *
 * @author Manuel Gieseking
 */
public class Pnwt2Pdf extends AbstractSimpleModule {

    private static final String name = "pnwt2pdf";
    private static final String descr = "Converts a Petri net with transits"
            + " to a pdf file by using Graphviz (dot has to be executable).";

    @Override
    public Map<String, Option> createOptions() {
        Map<String, Option> options = super.createOptions();
        // Add IO
        options.putAll(IOParameters.createOptions());
        return options;
    }

    @Override
    public void execute(CommandLine line) throws IOException, ParseException, InterruptedException, CommandLineParseException, ClassNotFoundException, Exception {
        super.execute(line);
        PNWTTools.savePnwt2DotAndPDF(IOParameters.getInput(line), IOParameters.getOutput(line), false);
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
