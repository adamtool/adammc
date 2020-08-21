package uniolunisaar.adam.logic.ui.cl.modules.exporter.modelchecking;

import uniolunisaar.adam.logic.ui.cl.modules.exporter.AbstractExporter;
import uniolunisaar.adam.logic.ui.cl.modules.Modules;
import uniolunisaar.adam.logic.ui.cl.modules.modelchecking.ModulesMC;

/**
 *
 * @author Manuel Gieseking
 */
public class ExporterMC extends AbstractExporter {

    @Override
    protected Modules getModules() {
        return new ModulesMC();
    }
   
}
