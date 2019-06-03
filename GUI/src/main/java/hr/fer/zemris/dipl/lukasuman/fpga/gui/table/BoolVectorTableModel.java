package hr.fer.zemris.dipl.lukasuman.fpga.gui.table;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanVector;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.controllers.AbstractGUIController;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationKeys;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.session.SessionController;

import java.util.ArrayList;
import java.util.List;

public class BoolVectorTableModel extends MyAbstractTableModel<BooleanVector> {

    private static final List<BooleanVector> DEFAULT_ITEMS = new ArrayList<>();
    private static final Double[] COLUMN_WIDTH_PERCENTAGES = new Double[]{1.0, 4.0, 1.0, 1.0};

    public BoolVectorTableModel(SessionController parentSession, AbstractGUIController parentController,
                                List<BooleanVector> booleanVectors) {

        super(parentSession, parentController, booleanVectors, LocalizationKeys.INDEX_KEY,
                LocalizationKeys.NAME_KEY, LocalizationKeys.INPUTS_KEY, LocalizationKeys.FUNCTIONS_KEY);

        setColumnWidthPercentages(COLUMN_WIDTH_PERCENTAGES);
    }

    @Override
    protected List<BooleanVector> getDefaultItems() {
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
            case 3:
                return items.get(rowIndex).getNumFunctions();
            default:
                throw new IllegalArgumentException("Invalid column index: " + columnIndex);
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        renameItem(aValue, rowIndex, columnIndex, parentSession.getBooleanVectorController());
    }
}
