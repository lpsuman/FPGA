package hr.fer.zemris.dipl.lukasuman.fpga.gui.session;

import hr.fer.zemris.dipl.lukasuman.fpga.gui.GUIController;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.func.BooleanFunctionController;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import javax.swing.*;
import java.awt.*;

public class SessionController implements GUIController {

    private SessionData sessionData;
    private JPanel mainPanel;
    private BooleanFunctionController booleanFunctionController;

    public SessionController(SessionData sessionData) {
        this.sessionData = Utility.checkNull(sessionData, "session data");
        loadSessionData();
        initGUI();
    }

    private void loadSessionData() {
        booleanFunctionController = new BooleanFunctionController(sessionData.getBoolFunctions());
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
}
