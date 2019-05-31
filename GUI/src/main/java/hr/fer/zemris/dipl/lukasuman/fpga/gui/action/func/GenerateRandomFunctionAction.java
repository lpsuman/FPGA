package hr.fer.zemris.dipl.lukasuman.fpga.gui.action.func;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BoolFuncController;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.AbstractAppAction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationKeys;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.awt.event.ActionEvent;

public class GenerateRandomFunctionAction extends AbstractAppAction {

    private NumberInputsProvider numberInputsProvider;

    public GenerateRandomFunctionAction(JFPGA jfpga, NumberInputsProvider numberInputsProvider) {
        super(jfpga, LocalizationKeys.GENERATE_RANDOM_KEY);
        this.numberInputsProvider = Utility.checkNull(numberInputsProvider, "number of inputs provider");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        jfpga.getCurrentSession().getBooleanFunctionController()
                .addItem(BoolFuncController.generateRandomFunction(numberInputsProvider.getNumberInputs()));
    }
}
