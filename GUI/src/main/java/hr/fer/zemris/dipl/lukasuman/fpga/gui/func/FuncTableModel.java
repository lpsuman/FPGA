package hr.fer.zemris.dipl.lukasuman.fpga.gui.func;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanFunction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationKeys;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationProvider;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.session.SessionController;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.util.List;

public class FuncTableModel extends MyAbstractTableModel<BooleanFunction> {

    private static Double[] COLUMN_WIDTH_PERCENTAGES = new Double[]{1.0, 4.0, 1.0};

    public FuncTableModel(SessionController parentSession, List<BooleanFunction> booleanFunctions) {
        super(parentSession, booleanFunctions, LocalizationKeys.INDEX_KEY,LocalizationKeys.NAME_KEY, LocalizationKeys.INPUTS_KEY);
        setColumnWidthPercentages(COLUMN_WIDTH_PERCENTAGES);
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
        if (!isCellEditable(rowIndex, columnIndex)) {
            throw new IllegalArgumentException("Cell is not editable.");
        }

        BooleanFunctionController booleanFunctionController = parentSession.getBooleanFunctionController();
        String newName = aValue.toString();
        String oldName = booleanFunctionController.getItem(rowIndex).getName();

        if (!newName.equals(oldName)) {
            booleanFunctionController.changeItemName(rowIndex, aValue.toString());
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }
}
