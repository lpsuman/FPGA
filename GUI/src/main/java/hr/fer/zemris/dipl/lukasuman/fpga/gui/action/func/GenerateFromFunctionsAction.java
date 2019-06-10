package hr.fer.zemris.dipl.lukasuman.fpga.gui.action.func;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanFunction;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanVector;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanVectorException;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.AbstractAppAction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationKeys;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.function.Supplier;

public class GenerateFromFunctionsAction extends AbstractAppAction {

    private Supplier<List<BooleanFunction>> linkableFunctionsSupplier;
    private Supplier<List<BooleanFunction>> functionSupplier;

    public GenerateFromFunctionsAction(JFPGA jfpga, Supplier<List<BooleanFunction>> functionSupplier,
                                       Supplier<List<BooleanFunction>> linkableFunctionsSupplier) {
        super(jfpga, LocalizationKeys.GENERATE_FROM_FUNCTIONS_KEY);
        this.functionSupplier = Utility.checkNull(functionSupplier, "supplier of functions");
        this.linkableFunctionsSupplier = Utility.checkNull(linkableFunctionsSupplier, "supplier of linkable functions");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        List<BooleanFunction> functions = functionSupplier.get();
        if (functions == null || functions.isEmpty()) {
            jfpga.showWarningMsg(jfpga.getFlp().getString(LocalizationKeys.NO_FUNCTIONS_SELECTED_MSG_KEY));
            return;
        }

        BooleanVector newVector;
        try {
            newVector = new BooleanVector(functions, linkableFunctionsSupplier.get(),
                    false, functions.get(0).getName());
        } catch (BooleanVectorException exc) {
            jfpga.showErrorMsg(String.format(jfpga.getFlp().getString(LocalizationKeys.INPUT_S_IS_AMBIGUOUS_KEY), exc.getCauseTarget()));
            return;
        }

        newVector.setName(functions.get(0).getName());
        jfpga.getCurrentSession().getBooleanVectorController().addItem(newVector);
    }
}
