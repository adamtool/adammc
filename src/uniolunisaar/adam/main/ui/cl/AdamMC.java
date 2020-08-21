package uniolunisaar.adam.main.ui.cl;

import uniolunisaar.adam.logic.ui.cl.AdamUI;
import uniolunisaar.adam.logic.ui.cl.modules.modelchecking.ModulesMC;

/**
 *
 * @author Manuel Gieseking
 */
public class AdamMC {

    public static boolean debug = false;

    public static void main(String[] args) {
        AdamUI.main(args, new ModulesMC(), debug);
    }
}
