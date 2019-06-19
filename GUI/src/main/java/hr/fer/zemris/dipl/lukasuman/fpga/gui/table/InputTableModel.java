package hr.fer.zemris.dipl.lukasuman.fpga.gui.table;

import hr.fer.zemris.dipl.lukasuman.fpga.gui.controllers.AbstractGUIController;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.LocalizationKeys;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.session.SessionController;

import java.util.Arrays;
import java.util.List;

public class InputTableModel extends MyAbstractTableModel<String> {

    private static final List<String> DEFAULT_INPUT_IDS = Arrays.asList("a", "b", "c");
    private static final Double[] COLUMN_WIDTH_PERCENTAGES = new Double[]{1.0, 4.0};

    public InputTableModel(SessionController parentSession, AbstractGUIController parentController) {
        super(parentSession, parentController, null,
                LocalizationKeys.INDEX_KEY, LocalizationKeys.INPUTS_KEY);
        setColumnWidthPercentages(COLUMN_WIDTH_PERCENTAGES);
    }

    @Override
    protected List<String> getDefaultItems() {
        return DEFAULT_INPUT_IDS;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (!displayIndices) {
            columnIndex += 1;
        }

        if (columnIndex == 0) {
            return rowIndex;
        } else {
            return items.get(rowIndex);
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (!isCellEditable(rowIndex, columnIndex)) {
            throw new IllegalArgumentException("Cell is not editable.");
        }

        String newInputID = aValue.toString();

        for (int i = 0; i < items.size(); i++) {
            if (i != rowIndex && newInputID.equals(items.get(i))) {
                parentSession.getJfpga().showErrorMsg(String.format(parentSession.getLocProv()
                        .getString(LocalizationKeys.INPUT_S_ALREADY_EXISTS_KEY), newInputID));
                return;
            }
        }

        String oldInputID = items.get(rowIndex);

        if (!newInputID.equals(oldInputID)) {
            parentSession.getBooleanFunctionController().changeFunctionInput(rowIndex, newInputID);
        }
    }
}
