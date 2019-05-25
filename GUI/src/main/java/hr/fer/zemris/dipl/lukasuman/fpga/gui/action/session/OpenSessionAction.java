package hr.fer.zemris.dipl.lukasuman.fpga.gui.action.session;

import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.AbstractAppAction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationKeys;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class OpenSessionAction extends AbstractAppAction {

    private static final long serialVersionUID = -6117170662203616218L;

    /**Accelerator key (shortcut).*/
    private static final String ACC_KEY = "control O";

    /**Mnemonic key.*/
    private static final int MNEMONIC_KEY = KeyEvent.VK_O;

    public OpenSessionAction(JFPGA jfpga) {
        super(jfpga, LocalizationKeys.OPEN_KEY);
        setValues(ACC_KEY, MNEMONIC_KEY);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //TODO open session
    }
}
