package hr.fer.zemris.dipl.lukasuman.fpga.gui.func;

import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationProvider;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import javax.swing.table.AbstractTableModel;

public abstract class MyAbstractTableModel extends AbstractTableModel {

    protected BooleanFunctionController booleanFunctionController;
    protected LocalizationProvider lp;
    private String[] columnNameKeys;
    protected String[] columnNames;
    private Double[] columnWidthPercentages;
    protected boolean displayIndices;

    public MyAbstractTableModel(BooleanFunctionController booleanFunctionController, LocalizationProvider lp,
                                String... columnNameKeys) {

        this.booleanFunctionController = Utility.checkNull(booleanFunctionController, "boolfunc controller");
        this.lp = Utility.checkNull(lp, "localization provider");

        if (columnNameKeys != null) {
            this.columnNameKeys = columnNameKeys;
            columnNames = new String[columnNameKeys.length];
            updateColumnNames();
            lp.addLocalizationListener(this::updateColumnNames);
        }

        displayIndices = true;
    }

    private void updateColumnNames() {
        for (int i = 0; i < columnNames.length; i++) {
            columnNames[i] = lp.getString(columnNameKeys[i]);
        }
        fireTableStructureChanged();
    }

    public void setDisplayIndices(boolean displayIndices) {
        if (displayIndices != this.displayIndices) {
            this.displayIndices = displayIndices;
            fireTableStructureChanged();
        }
    }

    protected void setColumnWidthPercentages(Double[] columnWidthPercentages) {
        this.columnWidthPercentages = Utility.checkIfValidArray(columnWidthPercentages, "width percentages");
    }

    public double getColumnWidthPercentage(int column) {
        if (columnWidthPercentages == null) {
            return 1.0 / getColumnCount();
        }

        double widthSum = 0.0;
        for (int i = 0; i < columnWidthPercentages.length; i++) {
            if (i == 0 && !displayIndices) {
                continue;
            }

            widthSum += columnWidthPercentages[i];
        }

        if (!displayIndices) {
            column += 1;
        }

        return columnWidthPercentages[column] / widthSum;
    }

    @Override
    public int getColumnCount() {
        if (!displayIndices) {
            return columnNames.length - 1;
        } else {
            return columnNames.length;
        }
    }

    @Override
    public String getColumnName(int column) {
        if (!displayIndices) {
            column += 1;
        }

        return columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (!displayIndices) {
            columnIndex += 1;
        }

        if (columnIndex == 1) {
            return String.class;
        } else {
            return Integer.class;
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
