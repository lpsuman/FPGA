package hr.fer.zemris.dipl.lukasuman.fpga.gui.action.func;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanFunction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.EditListAction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.func.BooleanFunctionController;

import javax.swing.*;

public class EditFuncNameListAction extends EditListAction<BooleanFunction> {

    private BooleanFunctionController booleanFunctionController;

    public EditFuncNameListAction(BooleanFunctionController booleanFunctionController) {
        this.booleanFunctionController = booleanFunctionController;
    }

    @Override
    protected void applyValueToModel(String value, DefaultListModel<BooleanFunction> model, int row) {
        booleanFunctionController.changeFunctionName(row, value);
    }

    @Override
    protected String getTextFromValue(BooleanFunction value) {
        return value.getName();
    }
}
