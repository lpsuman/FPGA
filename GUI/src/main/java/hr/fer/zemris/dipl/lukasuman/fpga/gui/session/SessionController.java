package hr.fer.zemris.dipl.lukasuman.fpga.gui.session;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanFunction;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanVector;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.GUIUtility;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.JPanelPair;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.func.BooleanFunctionAdapter;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.func.BooleanFunctionController;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.func.BooleanVectorController;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.func.BooleanVectorListener;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationProvider;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import javax.swing.*;
import java.awt.*;
import java.util.BitSet;
import java.util.List;

public class SessionController {

    private SessionData sessionData;
    private JLabel iconLabel;
    private boolean isEdited;
    private JPanel mainPanel;

    private JFPGA jfpga;
    private BooleanFunctionController booleanFunctionController;
    private BooleanVectorController booleanVectorController;

    public SessionController(SessionData sessionData, JFPGA jfpga, JLabel iconPanel) {
        this.sessionData = Utility.checkNull(sessionData, "session data");
        this.jfpga = Utility.checkNull(jfpga, "jfpga");
        this.iconLabel = Utility.checkNull(iconPanel, "icon panel");
        loadSessionData();
        initGUI();
    }

    private void loadSessionData() {
        booleanFunctionController = new BooleanFunctionController(this);
        booleanVectorController = new BooleanVectorController(this);

        booleanFunctionController.addBooleanFunctionListener(new BooleanFunctionAdapter() {
            @Override
            public void itemAdded(BooleanFunction item, int indexInTable) {
                updateVectorBeingEdited();
            }

            @Override
            public void itemRemoved(BooleanFunction item, int indexInTable) {
                updateVectorBeingEdited();
            }

            @Override
            public void itemListChanged() {
                if (booleanFunctionController.getItems() != booleanFunctionController.getAllItems()) {
                    booleanVectorController.stopEditingVector();
                }
            }

            @Override
            public void booleanFunctionInputsEdited(BooleanFunction booleanFunction, int index, List<String> oldInputs) {
                updateVectorBeingEdited();
            }

            @Override
            public void booleanFunctionTableEdited(BooleanFunction booleanFunction, int index, BitSet oldTable) {
                updateVectorBeingEdited();
            }
        });
    }

    private void updateVectorBeingEdited() {
        if (booleanFunctionController.getItems() != booleanFunctionController.getAllItems()) {
            booleanVectorController.updateVectorBeingEdited();
        }
    }

    private void initGUI() {
        JPanelPair outerAndInnerPair = GUIUtility.putGridBagInBorderCenter();
        mainPanel = outerAndInnerPair.getUpperPanel();

        GridBagConstraints gbc = GUIUtility.getGBC(0, 0, 0.3, 0.5);
        mainPanel.add(booleanFunctionController.getMainPanel(), gbc);

        gbc = GUIUtility.getGBC(1, 0, 0.1, 0.5);
        mainPanel.add(booleanVectorController.getMainPanel(), gbc);

        mainPanel = outerAndInnerPair.getLowerPanel();
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public SessionData getSessionData() {
        return sessionData;
    }

    public JFPGA getJfpga() {
        return jfpga;
    }

    public LocalizationProvider getLocProv() {
        return jfpga.getFlp();
    }

    public BooleanFunctionController getBooleanFunctionController() {
        return booleanFunctionController;
    }

    public BooleanVectorController getBooleanVectorController() {
        return booleanVectorController;
    }

    public boolean isEdited() {
        return isEdited;
    }

    public void setEdited(boolean edited) {
        isEdited = edited;
        if (isEdited) {
            iconLabel.setIcon(jfpga.getRedDiskette());
        } else {
            iconLabel.setIcon(jfpga.getBlueDiskette());
        }
    }

    public JLabel getIconLabel() {
        return iconLabel;
    }
}
