package hr.fer.zemris.dipl.lukasuman.fpga.gui.session;

import hr.fer.zemris.dipl.lukasuman.fpga.gui.GUIController;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.func.BooleanFunctionController;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import javax.swing.*;
import java.awt.*;

public class SessionController implements GUIController {

    private JFPGA jfpga;
    private SessionData sessionData;
    private JLabel iconLabel;
    private boolean isEdited;
    private JPanel mainPanel;
    private BooleanFunctionController booleanFunctionController;

    public SessionController(SessionData sessionData, JFPGA jfpga, JLabel iconPanel) {
        this.sessionData = Utility.checkNull(sessionData, "session data");
        this.jfpga = Utility.checkNull(jfpga, "jfpga");
        this.iconLabel = Utility.checkNull(iconPanel, "icon panel");
        loadSessionData();
        initGUI();
    }

    private void loadSessionData() {
        booleanFunctionController = new BooleanFunctionController(sessionData.getBoolFunctions(), jfpga, this);
    }

    private void initGUI() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(booleanFunctionController.getMainPanel(), BorderLayout.WEST);
    }

    @Override
    public JPanel getMainPanel() {
        return mainPanel;
    }

    public SessionData getSessionData() {
        return sessionData;
    }

    public BooleanFunctionController getBooleanFunctionController() {
        return booleanFunctionController;
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
