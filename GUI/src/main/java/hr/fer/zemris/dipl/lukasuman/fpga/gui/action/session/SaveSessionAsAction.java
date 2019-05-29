package hr.fer.zemris.dipl.lukasuman.fpga.gui.action.session;

import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.AbstractAppAction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationKeys;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class SaveSessionAsAction extends AbstractAppAction {

    /**Accelerator key (shortcut).*/
    private static final String ACC_KEY = "control shift S";

    /**Mnemonic key.*/
    private static final int MNEMONIC_KEY = KeyEvent.VK_Z;

    public SaveSessionAsAction(JFPGA jfpga) {
        super(jfpga, LocalizationKeys.SAVE_AS_KEY);
        setValues(ACC_KEY, MNEMONIC_KEY);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ActionEvent e2 = new ActionEvent(e.getSource(), e.getID(), LocalizationKeys.SAVE_SESSION_AS_KEY);
        jfpga.getSaveSessionAction().actionPerformed(e2);
    }
}
