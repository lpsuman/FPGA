package hr.fer.zemris.dipl.lukasuman.fpga.gui.action.solve;

import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.AbstractAppAction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.LocalizationKeys;

import java.awt.event.ActionEvent;

public class ClearOutputAction extends AbstractAppAction {

    public ClearOutputAction(JFPGA jfpga) {
        super(jfpga, LocalizationKeys.CLEAR_OUTPUT_KEY);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //TODO clear output action
    }
}
