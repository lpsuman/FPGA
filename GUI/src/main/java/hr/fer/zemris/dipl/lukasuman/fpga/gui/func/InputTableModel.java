package hr.fer.zemris.dipl.lukasuman.fpga.gui.func;

import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationKeys;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationProvider;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

public class InputTableModel extends MyAbstractTableModel {

    private static List<String> DEFAULT_INPUT_IDS = Arrays.asList("a", "b", "c");
    private static Double[] COLUMN_WIDTH_PERCENTAGES = new Double[]{1.0, 4.0};

    private List<String> inputIDs;

    public InputTableModel(BooleanFunctionController booleanFunctionController, LocalizationProvider lp) {
        super(booleanFunctionController, lp, LocalizationKeys.INDEX_KEY, LocalizationKeys.INPUTS_KEY);
        setColumnWidthPercentages(COLUMN_WIDTH_PERCENTAGES);
        this.inputIDs = DEFAULT_INPUT_IDS;
    }

    public void setInputIDs(List<String> inputIDs) {
        this.inputIDs = Utility.checkIfValidCollection(inputIDs, "input IDs");
        fireTableDataChanged();
    }

    public void setDisplayIndices(boolean displayIndices) {
        if (displayIndices != this.displayIndices) {
            this.displayIndices = displayIndices;
            fireTableStructureChanged();
        }
    }

    @Override
    public int getRowCount() {
        return inputIDs.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (!displayIndices) {
            columnIndex += 1;
        }

        if (columnIndex == 0) {
            return rowIndex;
        } else {
            return inputIDs.get(rowIndex);
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (!isCellEditable(rowIndex, columnIndex)) {
            throw new IllegalArgumentException("Cell is not editable.");
        }

        String newInputID = aValue.toString();

        for (int i = 0; i < inputIDs.size(); i++) {
            if (i != rowIndex && newInputID.equals(inputIDs.get(i))) {
                JOptionPane.showMessageDialog(
                        booleanFunctionController.getJfpga(),
                        String.format(lp.getString(LocalizationKeys.INPUT_S_ALREADY_EXISTS_KEY), newInputID),
                        lp.getString(LocalizationKeys.ERROR_KEY),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        String oldInputID = inputIDs.get(rowIndex);

        if (!newInputID.equals(oldInputID)) {
            booleanFunctionController.changeFunctionInput(rowIndex, newInputID);
        }
    }
}
