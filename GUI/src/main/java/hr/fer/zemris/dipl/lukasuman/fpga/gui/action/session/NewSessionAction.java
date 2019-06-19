package hr.fer.zemris.dipl.lukasuman.fpga.gui.action.session;

import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.AbstractAppAction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.LocalizationKeys;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.session.SessionData;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class NewSessionAction extends AbstractAppAction {

    /**Accelerator key (shortcut).*/
    private static final String ACC_KEY = "control N";

    /**Mnemonic key.*/
    private static final int MNEMONIC_KEY = KeyEvent.VK_N;

    public NewSessionAction(JFPGA jfpga) {
        super(jfpga, LocalizationKeys.NEW_KEY);
        setValues(ACC_KEY, MNEMONIC_KEY);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        jfpga.createNewSession(new SessionData(), jfpga.getBlueDiskette());
    }
}
