package hr.fer.zemris.dipl.lukasuman.fpga.gui.table;

import hr.fer.zemris.dipl.lukasuman.fpga.gui.GUIConstants;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.GUIUtility;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

public class MyJTable extends JTable {

    private Map<Integer, ColumnClickedListener> columnClickedListeners;
    private MyAbstractTableModel tableModel;

    public MyJTable(MyAbstractTableModel tableModel) {
        super(tableModel);
        this.tableModel = tableModel;
        setMinimumColumnWidth(GUIConstants.MINIMUM_COLUMN_WIDTH);
        getModel().addTableModelListener(e -> setPreferredSize(getMinimumSize()));
        setDefaultRenderer(Integer.class, GUIUtility.CENTER_TABLE_CELL_RENDERER);
    }

    @Override
    public boolean editCellAt(int row, int column, EventObject e) {
        boolean result = super.editCellAt(row, column, e);
        selectAll(e);
        return result;
    }

    private void selectAll(EventObject e) {
        final Component editor = getEditorComponent();

        if (!(editor instanceof JTextComponent)) {
            return;
        }

        if (e == null) {
            ((JTextComponent) editor).selectAll();
            return;
        }

        if (e instanceof KeyEvent) {
            ((JTextComponent) editor).selectAll();
            return;
        }

        if (e instanceof ActionEvent) {
            ((JTextComponent) editor).selectAll();
            return;
        }

        if (e instanceof MouseEvent) {
            SwingUtilities.invokeLater(((JTextComponent) editor)::selectAll);
        }
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component component = super.prepareRenderer(renderer, row, column);
        int rendererWidth = component.getMinimumSize().width;
        TableColumn tableColumn = getColumnModel().getColumn(column);
        tableColumn.setMinWidth(Math.max(rendererWidth + getIntercellSpacing().width, tableColumn.getMinWidth()));
        return component;
    }

    public void setMinimumColumnWidth(int minColumnWidth) {
        for (int i = 0; i < getColumnModel().getColumnCount(); i++) {
            getColumnModel().getColumn(i).setMinWidth(minColumnWidth);
        }
    }

    public void applyMinSizeInScrollPane() {
        getParent().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(final ComponentEvent e) {
                setPreferredSize(getMinimumSize());
                if (getMinimumSize().width < getParent().getWidth()) {
                    setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
                } else {
                    setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                }
            }
        });
    }

    public void addColumnClickedListener(int columnIndex, ColumnClickedListener listener) {
        if (columnClickedListeners == null) {
            columnClickedListeners = new HashMap<>();

            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent mouseEvent) {
                    JTable table =(JTable) mouseEvent.getSource();
                    Point point = mouseEvent.getPoint();
                    int row = table.rowAtPoint(point);
                    int column = table.columnAtPoint(point);
                    ColumnClickedListener listener = columnClickedListeners.get(tableModel.displayIndices ? column : column + 1);

                    if (listener != null && mouseEvent.getClickCount() == 2 && table.getSelectedRow() != -1) {
                        listener.columnDoubleClicked(row);
                    }
                }
            });
        }

        columnClickedListeners.put(columnIndex, listener);
    }

    public void removeColumnClickedListener(ColumnClickedListener listener) {
        if (columnClickedListeners == null) {
            return;
        }

        columnClickedListeners.values().remove(listener);
    }
}
