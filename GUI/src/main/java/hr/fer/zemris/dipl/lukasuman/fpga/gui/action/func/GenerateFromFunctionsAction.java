package hr.fer.zemris.dipl.lukasuman.fpga.gui.action.func;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanFunction;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanVector;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.AbstractAppAction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationKeys;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.function.Supplier;

public class GenerateFromFunctionsAction extends AbstractAppAction {

    private Supplier<List<BooleanFunction>> functionSupplier;

    public GenerateFromFunctionsAction(JFPGA jfpga, Supplier<List<BooleanFunction>> functionSupplier) {
        super(jfpga, LocalizationKeys.GENERATE_FROM_FUNCTIONS_KEY);
        this.functionSupplier = Utility.checkNull(functionSupplier, "supplier of functions");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        List<BooleanFunction> functions = functionSupplier.get();
        if (functions == null || functions.isEmpty()) {
            jfpga.showWarningMsg(jfpga.getFlp().getString(LocalizationKeys.NO_FUNCTIONS_SELECTED_MSG_KEY));
            return;
        }
        jfpga.getCurrentSession().getBooleanVectorController().addItem(new BooleanVector(functions));
    }
}
