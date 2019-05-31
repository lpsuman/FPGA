package hr.fer.zemris.dipl.lukasuman.fpga.gui.func;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanVector;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.session.SessionController;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BooleanVectorController extends AbstractGUIController<BooleanVector> {

    private Set<BooleanVectorListener> booleanVectorListeners;

    public BooleanVectorController(SessionController parentSession) {
        super(parentSession, parentSession.getSessionData().getBoolVectors());

        loadData();
        initGUI();
    }

    private void loadData() {

    }

    private void initGUI() {

    }

    @Override
    protected Collection<? extends TableItemListener<BooleanVector>> getTableItemListeners() {
        return booleanVectorListeners;
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
