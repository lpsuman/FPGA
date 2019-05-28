package hr.fer.zemris.dipl.lukasuman.fpga.gui.action.func;

import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.EditListAction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.func.BooleanFunctionController;

import javax.swing.*;

public class EditFuncInputListAction extends EditListAction<String> {

    private BooleanFunctionController booleanFunctionController;

    public EditFuncInputListAction(BooleanFunctionController booleanFunctionController) {
        this.booleanFunctionController = booleanFunctionController;
    }

    @Override
    protected void applyValueToModel(String value, DefaultListModel<String> model, int row) {
        int funcIndex = booleanFunctionController.getIndexSelectedFunction();

        if (funcIndex == -1) {
            return;
        }

        booleanFunctionController.changeFunctionInput(row, value);
        model.set(row, value);
    }

    @Override
    protected String getTextFromValue(String value) {
        return value;
    }
}
