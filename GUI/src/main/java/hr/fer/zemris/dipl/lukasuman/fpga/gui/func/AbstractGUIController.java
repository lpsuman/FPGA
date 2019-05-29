package hr.fer.zemris.dipl.lukasuman.fpga.gui.func;

import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.session.SessionController;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import javax.swing.*;

public class AbstractGUIController {

    protected JFPGA jfpga;
    protected SessionController parentSession;
    protected JPanel mainPanel;

    public AbstractGUIController(JFPGA jfpga, SessionController parentSession) {
        this.jfpga = Utility.checkNull(jfpga, "JFPGA");
        this.parentSession = Utility.checkNull(parentSession, "parent session");
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public JFPGA getJfpga() {
        return jfpga;
    }

    public SessionController getParentSession() {
        return parentSession;
    }
}
