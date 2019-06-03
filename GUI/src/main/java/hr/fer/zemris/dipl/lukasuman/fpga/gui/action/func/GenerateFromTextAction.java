package hr.fer.zemris.dipl.lukasuman.fpga.gui.action.func;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BoolFuncController;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanFunction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationKeys;

import java.util.Collections;
import java.util.function.Supplier;

public class GenerateFromTextAction extends AbstractGenerateFromTextAction {

    public GenerateFromTextAction(JFPGA jfpga, Supplier<String> textProvider) {
        super(jfpga, textProvider, LocalizationKeys.GENERATE_FROM_TEXT_KEY);
    }

    @Override
    protected void doAction(String text) {
        BooleanFunction newFunc;

        try {
            newFunc = BoolFuncController.generateFromText(Collections.singletonList(text));
        } catch (IllegalArgumentException exc) {
            jfpga.showErrorMsg(exc.getMessage());
            return;
        }

        jfpga.getCurrentSession().getBooleanFunctionController().addItem(newFunc);
    }
}
