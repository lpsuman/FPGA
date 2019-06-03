package hr.fer.zemris.dipl.lukasuman.fpga.gui.table;

import hr.fer.zemris.dipl.lukasuman.fpga.gui.controllers.AbstractGUIController;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.session.SessionController;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public abstract class MyAbstractTableModel<T> extends AbstractTableModel {

    protected SessionController parentSession;
    private AbstractGUIController parentController;
    protected List<T> items;
    private String[] columnNameKeys;
    protected String[] columnNames;
    private Double[] columnWidthPercentages;
    protected boolean displayIndices;

    public MyAbstractTableModel(SessionController parentSession, AbstractGUIController parentController,
                                List<T> items, String... columnNameKeys) {

        this.parentSession = Utility.checkNull(parentSession, "parent session");
        this.parentController = Utility.checkNull(parentController, "parent controller");

        if (items == null) {
            loadDefaultItems();
        } else {
            this.items = items;
        }

        if (columnNameKeys != null) {
            this.columnNameKeys = columnNameKeys;
            columnNames = new String[columnNameKeys.length];
            updateColumnNames();
            parentSession.getLocProv().addLocalizationListener(this::updateColumnNames);
        }

        displayIndices = true;
    }

    protected abstract List<T> getDefaultItems();

    public void loadDefaultItems() {
        setItems(getDefaultItems());
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        Utility.checkNull(items, "items");
        this.items = Utility.checkIfContainsNull(items, "items");
        fireTableDataChanged();
    }

    private void updateColumnNames() {
        for (int i = 0; i < columnNames.length; i++) {
            columnNames[i] = parentSession.getLocProv().getString(columnNameKeys[i]);
        }
        fireTableStructureChanged();
    }

    public boolean isDisplayIndices() {
        return displayIndices;
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
    public int getRowCount() {
        if (items == null) {
            throw new UnsupportedOperationException("List of items is null, getRowCount needs to be overridden.");
        }
        return items.size();
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
        if (!parentController.areItemsEditable() || items == getDefaultItems()) {
            return false;
        }

        if (!displayIndices) {
            columnIndex += 1;
        }

        return isColumnEditable(columnIndex);
    }

    protected boolean isColumnEditable(int columnIndex) {
        return columnIndex == 1;
    }

    protected void renameItem(Object aValue, int rowIndex, int columnIndex, AbstractGUIController controller) {
        if (!isCellEditable(rowIndex, columnIndex)) {
            throw new IllegalArgumentException("Cell is not editable.");
        }

        String newName = aValue.toString();
        String oldName = controller.getItem(rowIndex).getName();

        if (!newName.equals(oldName)) {
            controller.changeItemName(rowIndex, aValue.toString());
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }
}
