package hr.fer.zemris.dipl.lukasuman.fpga.gui.func;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanFunction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationKeys;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationProvider;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.util.List;

public class FuncTableModel extends MyAbstractTableModel {

    private List<BooleanFunction> booleanFunctions;

    public FuncTableModel(List<BooleanFunction> booleanFunctions, BooleanFunctionController booleanFunctionController,
                          LocalizationProvider lp) {

        super(booleanFunctionController, lp, LocalizationKeys.INDEX_KEY,LocalizationKeys.NAME_KEY, LocalizationKeys.INPUTS_KEY);
        this.booleanFunctions = Utility.checkIfValidCollection(booleanFunctions, "boolean functions");
    }

    public List<BooleanFunction> getBooleanFunctions() {
        return booleanFunctions;
    }

    public void setBooleanFunctions(List<BooleanFunction> booleanFunctions) {
        Utility.checkIfValidCollection(booleanFunctions, "boolean functions");
        this.booleanFunctions = booleanFunctions;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return booleanFunctions.size();
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
                return booleanFunctions.get(rowIndex).getName();
            case 2:
                return booleanFunctions.get(rowIndex).getNumInputs();
            default:
                throw new IllegalArgumentException("Invalid column index: " + columnIndex);
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (!isCellEditable(rowIndex, columnIndex)) {
            throw new IllegalArgumentException("Cell is not editable.");
        }

        String newName = aValue.toString();
        String oldName = booleanFunctionController.getBooleanFunction(rowIndex).getName();

        if (!newName.equals(oldName)) {
            booleanFunctionController.changeFunctionName(rowIndex, aValue.toString());
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (!displayIndices) {
            columnIndex += 1;
        }

        return columnIndex == 1;
    }
}
