package hr.fer.zemris.dipl.lukasuman.fpga.gui.action.func;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BoolFuncController;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanFunction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.AbstractAppAction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationKeys;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.awt.event.ActionEvent;
import java.util.Collections;

public class GenerateFromTextAction extends AbstractAppAction {

    private TextProvider textProvider;

    public GenerateFromTextAction(JFPGA jfpga, TextProvider textProvider) {

        super(jfpga, LocalizationKeys.GENERATE_FROM_TEXT_KEY);
        this.textProvider = Utility.checkNull(textProvider, "text provider");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String text = textProvider.getText();
        BooleanFunction newFunc;

        try {
            newFunc = BoolFuncController.generateFromText(Collections.singletonList(text));
        } catch (IllegalArgumentException exc) {
            showErrorMsg(exc.getMessage());
            return;
        }

        jfpga.getCurrentSession().getBooleanFunctionController().addItem(newFunc);
    }
}
