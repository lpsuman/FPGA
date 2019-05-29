package hr.fer.zemris.dipl.lukasuman.fpga.gui.action;

import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.awt.event.ActionEvent;

public class ShowMessageAction extends AbstractAppAction {

    private String messageLocalizationKey;

    public ShowMessageAction(JFPGA jfpga, String localizationKey, String messageLocalizationKey) {
        super(jfpga, localizationKey);
        this.messageLocalizationKey = Utility.checkIfValidString(messageLocalizationKey, "msg localization key");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String msg = jfpga.getFlp().getString(messageLocalizationKey);
        showInfoMsg(msg);
    }
}
