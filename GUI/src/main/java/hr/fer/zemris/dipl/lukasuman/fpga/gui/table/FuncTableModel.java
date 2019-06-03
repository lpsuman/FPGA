package hr.fer.zemris.dipl.lukasuman.fpga.gui.table;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanFunction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.controllers.AbstractGUIController;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationKeys;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.session.SessionController;

import java.util.ArrayList;
import java.util.List;

public class FuncTableModel extends MyAbstractTableModel<BooleanFunction> {

    private static final List<BooleanFunction> DEFAULT_ITEMS = new ArrayList<>();
    private static final Double[] COLUMN_WIDTH_PERCENTAGES = new Double[]{1.0, 4.0, 1.0};

    public FuncTableModel(SessionController parentSession, AbstractGUIController parentController,
                          List<BooleanFunction> booleanFunctions) {

        super(parentSession, parentController, booleanFunctions,
                LocalizationKeys.INDEX_KEY, LocalizationKeys.NAME_KEY, LocalizationKeys.INPUTS_KEY);

        setColumnWidthPercentages(COLUMN_WIDTH_PERCENTAGES);
    }

    @Override
    protected List<BooleanFunction> getDefaultItems() {
        return DEFAULT_ITEMS;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (!displayIndices) {
            columnIndex += 1;
        }

        switch (columnIndex) {
            case 0:
                return rowIndex;
            case 1:
                return items.get(rowIndex).getName();
            case 2:
                return items.get(rowIndex).getNumInputs();
            default:
                throw new IllegalArgumentException("Invalid column index: " + columnIndex);
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        renameItem(aValue, rowIndex, columnIndex, parentSession.getBooleanFunctionController());
    }
}
