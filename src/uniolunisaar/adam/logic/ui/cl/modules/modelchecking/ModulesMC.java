package uniolunisaar.adam.logic.ui.cl.modules.modelchecking;

import uniolunisaar.adam.logic.ui.cl.modules.AbstractModule;
import uniolunisaar.adam.logic.ui.cl.modules.Modules;
import uniolunisaar.adam.logic.ui.cl.modules.converter.modelchecking.Pnwt2Dot;
import uniolunisaar.adam.logic.ui.cl.modules.converter.modelchecking.Pnwt2Pdf;
import uniolunisaar.adam.logic.ui.cl.modules.converter.modelchecking.SDN2Dot;
import uniolunisaar.adam.logic.ui.cl.modules.converter.modelchecking.SDN2Pdf;
import uniolunisaar.adam.logic.ui.cl.modules.converter.petrinet.Pn2Pdf;
import uniolunisaar.adam.logic.ui.cl.modules.converter.petrinet.Pn2Unfolding;
import uniolunisaar.adam.logic.ui.cl.modules.modelchecking.checker.PetriNetWithTransitsModelchecker;
import uniolunisaar.adam.logic.ui.cl.modules.modelchecking.generators.RedundantFlowNetworkModule;
import uniolunisaar.adam.logic.ui.cl.modules.modelchecking.generators.RemoveNodeUpdateNetworkModule;
import uniolunisaar.adam.logic.ui.cl.modules.modelchecking.generators.TopologieZooModule;
import uniolunisaar.adam.logic.ui.cl.modules.modelchecking.checker.PetrinetModelchecker;
import uniolunisaar.adam.logic.ui.cl.modules.modelchecking.checker.SDNModelchecker;

/**
 *
 * @author Manuel Gieseking
 */
public class ModulesMC extends Modules {

    private static final AbstractModule[] modules = {
        // Converter
        new Pn2Pdf(),
        new Pn2Unfolding(),
        new Pnwt2Dot(),
        new Pnwt2Pdf(),
        new SDN2Dot(),
        new SDN2Pdf(),
        // Modelchecker
        new PetrinetModelchecker(),
        new PetriNetWithTransitsModelchecker(),
        new SDNModelchecker(),
        // Benchmark
        //        new BenchmarkTacas2018(),
        //        new BenchmarkCAV2019(),
        // Exporter
        //        new ExporterMC(),
        // Generators Model Checking
        new RemoveNodeUpdateNetworkModule(),
        new RedundantFlowNetworkModule(),
        new TopologieZooModule(), //        new SmartFactoryModule()
    };

    @Override
    public AbstractModule[] getModules() {
        return modules;
    }

}
