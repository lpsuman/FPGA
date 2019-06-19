package hr.fer.zemris.dipl.lukasuman.fpga.gui.action.session;

import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.AbstractAppAction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.LocalizationKeys;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.session.SessionController;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class DuplicateSessionAction extends AbstractAppAction {

    /**Accelerator key (shortcut).*/
    private static final String ACC_KEY = "ctrl D";

    /**Mnemonic key.*/
    private static final int MNEMONIC_KEY = KeyEvent.VK_D;

    public DuplicateSessionAction(JFPGA jfpga) {
        super(jfpga, LocalizationKeys.DUPLICATE_SESSION_KEY);
        setValues(ACC_KEY, MNEMONIC_KEY);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SessionController currentSession = jfpga.getCurrentSession();

        if (currentSession == null) {
            return;
        }

        ImageIcon icon = currentSession.isEdited() ? jfpga.getRedDiskette() : jfpga.getBlueDiskette();
        jfpga.createNewSession(currentSession.getSessionData(), icon);
    }
}
