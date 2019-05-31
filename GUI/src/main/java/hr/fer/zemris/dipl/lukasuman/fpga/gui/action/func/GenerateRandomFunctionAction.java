package hr.fer.zemris.dipl.lukasuman.fpga.gui.action.func;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BoolFuncController;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.AbstractAppAction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationKeys;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.awt.event.ActionEvent;
import java.util.function.Supplier;

public class GenerateRandomFunctionAction extends AbstractAppAction {

    private Supplier<Integer> numberInputsProvider;

    public GenerateRandomFunctionAction(JFPGA jfpga, Supplier<Integer> numberInputsProvider) {
        super(jfpga, LocalizationKeys.GENERATE_RANDOM_FUNCTION_KEY);
        this.numberInputsProvider = Utility.checkNull(numberInputsProvider, "number of inputs provider");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        jfpga.getCurrentSession().getBooleanFunctionController()
                .addItem(BoolFuncController.generateRandomFunction(numberInputsProvider.get()));
    }
}
