package hr.fer.zemris.dipl.lukasuman.fpga.gui.func;

import hr.fer.zemris.dipl.lukasuman.fpga.gui.GUIUtility;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.JPanelPair;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationProvider;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.session.SessionController;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.table.MyAbstractTableModel;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.table.MyJTable;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.table.TableItemListener;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Nameable;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractGUIController<T extends Nameable> {

    protected SessionController parentSession;
    private List<T> allItems;
    private List<T> items;
    protected JPanel mainPanel;

    protected MyAbstractTableModel<T> itemTableModel;
    protected MyJTable itemTable;

    public AbstractGUIController(SessionController parentSession, List<T> items) {
        this.parentSession = Utility.checkNull(parentSession, "parent session");
        this.items = Utility.checkNull(items, "list of items");
        allItems = items;

        loadData();
        initGUI();
    }

    protected abstract void loadData();

    protected void initGUI() {
        JPanelPair outerAndInnerPair = GUIUtility.putGridBagInBorderCenter();
        mainPanel = outerAndInnerPair.getUpperPanel();

        setupGUI();

        mainPanel = outerAndInnerPair.getLowerPanel();
        addResizeAndLocalizationListener(itemTable, itemTableModel);
    }

    protected void addResizeAndLocalizationListener(JTable table, MyAbstractTableModel tableModel) {
        getJfpga().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                GUIUtility.resizeColumns(table, tableModel);
            }
        });
        getLocProv().addLocalizationListener(() -> GUIUtility.resizeColumns(table, tableModel));
    }

    protected abstract void setupGUI();

    public SessionController getParentSession() {
        return parentSession;
    }

    public JFPGA getJfpga() {
        return parentSession.getJfpga();
    }

    public LocalizationProvider getLocProv() {
        return getJfpga().getFlp();
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public List<T> getAllItems() {
        return allItems;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = Utility.checkNull(items, "list of items");
        itemTableModel.setItems(this.items);
        itemTableModel.fireTableDataChanged();

        if (getTableItemListeners() != null) {
            getTableItemListeners().forEach(TableItemListener::itemListChanged);
        }

        parentSession.setEdited(true);
    }

    public int getNumItems() {
        return getItems().size();
    }

    public int getIndexSelectedItem() {
        return itemTable.getSelectedRow();
    }

    public int[] getIndicesSelectedItems() {
        return itemTable.getSelectedRows();
    }

    public T getSelectedItem() {
        return getItem(getIndexSelectedItem());
    }

    public List<T> getSelectedItems() {
        List<T> selectedItems = new ArrayList<>();

        for (int selectedIndex : getIndicesSelectedItems()) {
            selectedItems.add(getItem(selectedIndex));
        }

        return selectedItems;
    }

    public T getItem(int index) {
        Utility.checkRange(index, 0, getNumItems() - 1);
        return getItems().get(index);
    }

    public void setItem(int index, T newItem) {
        Utility.checkRange(index, 0, getNumItems() - 1);
        getItems().set(index, newItem);
    }

    protected abstract Collection<? extends TableItemListener<T>> getTableItemListeners();

    public void addItem(T newItem, int index) {
        Utility.checkNull(newItem, "new item");
        Utility.checkRange(index, 0, getNumItems());
        getItems().add(index, newItem);
        itemTableModel.fireTableRowsInserted(index, index);

        if (getTableItemListeners() != null) {
            getTableItemListeners().forEach(l -> l.itemAdded(newItem, index));
        }

        parentSession.setEdited(true);
    }

    public void addItem(T newItem) {
        addItem(newItem, getNumItems());
    }

    public void removeItem(int index) {
        Utility.checkRange(index, 0, getNumItems() - 1);
        boolean selectPrevious = index > 0;
        boolean selectNext = index < getNumItems() - 1;
        T removed = getItems().remove(index);
        itemTableModel.fireTableRowsDeleted(index, index);

        if (getTableItemListeners() != null) {
            getTableItemListeners().forEach(l -> l.itemRemoved(removed, index));
        }

        if (selectNext) {
            itemTable.changeSelection(index, 0, false, false);
//            itemTable.setRowSelectionInterval(index, index);
        } else if (selectPrevious) {
            itemTable.changeSelection(index - 1, 0, false, false);
//            itemTable.setRowSelectionInterval(index - 1, index - 1);
        }

        parentSession.setEdited(true);
    }

    public void changeItemName(int index, String newName) {
        Utility.checkRange(index, 0, getNumItems() - 1);
        Utility.checkIfValidString(newName, "new boolean function's name");
        T item = getItem(index);
        String oldName = item.getName();
        item.setName(newName);
        itemTableModel.fireTableRowsUpdated(index, index);

        if (getTableItemListeners() != null) {
            getTableItemListeners().forEach(l -> l.itemRenamed(item, index, oldName));
        }

        parentSession.setEdited(true);
    }
}
