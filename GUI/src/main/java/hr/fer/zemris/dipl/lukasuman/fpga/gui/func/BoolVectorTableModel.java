package hr.fer.zemris.dipl.lukasuman.fpga.gui.func;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanVector;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationKeys;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationProvider;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.util.List;

public class BoolVectorTableModel extends MyAbstractTableModel {

    private static Double[] COLUMN_WIDTH_PERCENTAGES = new Double[]{1.0, 4.0, 1.0, 1.0};

    private List<BooleanVector> booleanVectors;

    public BoolVectorTableModel(List<BooleanVector> booleanVectors, BooleanFunctionController booleanFunctionController,
                                LocalizationProvider lp) {

        super(booleanFunctionController, lp, LocalizationKeys.INDEX_KEY,LocalizationKeys.NAME_KEY, LocalizationKeys.INPUTS_KEY);
        setColumnWidthPercentages(COLUMN_WIDTH_PERCENTAGES);
        this.booleanVectors = Utility.checkIfValidCollection(booleanVectors, "boolean vectors");
    }

    public List<BooleanVector> getBooleanVectors() {
        return booleanVectors;
    }

    public void setBooleanVectors(List<BooleanVector> booleanVectors) {
        Utility.checkIfValidCollection(booleanVectors, "boolean vectors");
        this.booleanVectors = booleanVectors;
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return booleanVectors.size();
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
                return booleanVectors.get(rowIndex).getName();
            case 2:
                return booleanVectors.get(rowIndex).getNumInputs();
            case 3:
                return booleanVectors.get(rowIndex).getNumFunctions();
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
}
