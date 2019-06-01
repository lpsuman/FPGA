package hr.fer.zemris.dipl.lukasuman.fpga.gui.action.func;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BoolFuncController;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanVector;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.GUIConstants;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.AbstractAppAction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationKeys;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.awt.event.ActionEvent;
import java.util.function.Supplier;

public class GenerateRandomVectorAction extends AbstractAppAction {

    private Supplier<Integer> numInputsProvider;
    private Supplier<Integer> numFunctionsProvider;

    public GenerateRandomVectorAction(JFPGA jfpga, Supplier<Integer> numInputsProvider, Supplier<Integer>  numFunctionsProvider) {
        super(jfpga, LocalizationKeys.GENERATE_RANDOM_VECTOR_KEY);
        this.numInputsProvider = Utility.checkNull(numInputsProvider, "number of inputs provider");
        this.numFunctionsProvider = Utility.checkNull(numFunctionsProvider, "number of functions provider");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        BooleanVector randVector = BoolFuncController.generateRandomVector(numInputsProvider.get(), numFunctionsProvider.get());
        randVector.setName(GUIConstants.RANDOM_NAME);
        jfpga.getCurrentSession().getBooleanVectorController().addItem(randVector);
    }
}
