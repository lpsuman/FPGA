package hr.fer.zemris.dipl.lukasuman.fpga.gui.action.func;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanFunction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.AbstractAppAction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.func.BooleanFunctionController;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationKeys;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.awt.event.ActionEvent;

public class DuplicateFunctionAction extends AbstractAppAction {

    private BooleanFunctionController booleanFunctionController;

    public DuplicateFunctionAction(JFPGA jfpga, BooleanFunctionController booleanFunctionController) {
        super(jfpga, LocalizationKeys.DUPLICATE_FUNCTION_KEY);
        this.booleanFunctionController = Utility.checkNull(booleanFunctionController, "boolfunc controller");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int indexSelectedFunc = booleanFunctionController.getIndexSelectedFunction();

        if (indexSelectedFunc < 0) {
            return;
        }

        booleanFunctionController.addBooleanFunction(
                new BooleanFunction(booleanFunctionController.getBooleanFunction(indexSelectedFunc)));
    }
}
