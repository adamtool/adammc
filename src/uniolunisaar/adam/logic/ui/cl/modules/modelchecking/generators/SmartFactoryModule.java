package uniolunisaar.adam.logic.ui.cl.modules.modelchecking.generators;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import uniol.apt.module.exception.ModuleException;
import uniolunisaar.adam.ds.petrinetwithtransits.PetriNetWithTransits;
import uniolunisaar.adam.generators.pnwt.SmartFactory;

/**
 *
 * @author Manuel Gieseking
 */
public class SmartFactoryModule extends AbstractMCGeneratorModule {

    private static final String name = "gen_mc_smart_factory";
    private static final String descr = "Generates"
            + " a smart factory which can create 'nb_products' products. There\n"
            + " are 'nb_shared_machines' machines which are shared by all products, and\n"
            + "'nb_special_machines' machines which are just responsable for the\n"
            + " creation of one type of product."
            + " Saves the resulting net in APT and, if dot is executable, as pdf.";
    private static final String PARAMETER_NB_PRODUCTS = "nb_products";
    private static final String PARAMETER_NB_SHARED_MACHINES = "nb_shared_machines";
    private static final String PARAMETER_NB_SPECIFIC_MACHINES = "nb_specific_machines";

    @Override
    public Map<String, Option> createOptions() {
        Map<String, Option> options = super.createOptions();
        addIntParameter(options, PARAMETER_NB_PRODUCTS, "The desired number of products (>= 1).");
        addIntParameter(options, PARAMETER_NB_SHARED_MACHINES, "The desired number of shared machines.");
        addIntParameter(options, PARAMETER_NB_SPECIFIC_MACHINES, "The desired number of specific machines.");
        return options;
    }

    @Override
    public void execute(CommandLine line) throws IOException, InterruptedException, FileNotFoundException, ModuleException, ParseException, Exception {
        super.execute(line);
        int nb_products = getIntParameter(PARAMETER_NB_PRODUCTS, line);
        int nb_shared_machines = getIntParameter(PARAMETER_NB_SHARED_MACHINES, line);
        int nb_specific_machines = getIntParameter(PARAMETER_NB_SPECIFIC_MACHINES, line);

        PetriNetWithTransits net = SmartFactory.createFactory(nb_products, nb_shared_machines, nb_specific_machines);
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
