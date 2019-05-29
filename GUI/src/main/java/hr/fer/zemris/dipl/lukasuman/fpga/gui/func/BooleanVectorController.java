package hr.fer.zemris.dipl.lukasuman.fpga.gui.func;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanVector;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.session.SessionController;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BooleanVectorController extends AbstractGUIController{

    private List<BooleanVector> booleanVectors;
    private Set<BooleanVectorListener> booleanVectorListeners;

    public BooleanVectorController(List<BooleanVector> booleanVectors, JFPGA jfpga, SessionController parentSession) {
        super(jfpga, parentSession);
        this.booleanVectors = Utility.checkNull(booleanVectors, "list of boolean vectors");

        loadData();
        initActions();
        initGUI();
    }

    private void loadData() {

    }

    private void initActions() {

    }

    private void initGUI() {

    }

    public void addBooleanVectorListener(BooleanVectorListener listener) {
        if (booleanVectorListeners == null) {
            booleanVectorListeners = new HashSet<>();
        }
        booleanVectorListeners.add(listener);
    }

    public void removeBooleanVectorListener(BooleanVectorListener listener) {
        if (booleanVectorListeners != null) {
            booleanVectorListeners.remove(listener);
        }
    }
}
