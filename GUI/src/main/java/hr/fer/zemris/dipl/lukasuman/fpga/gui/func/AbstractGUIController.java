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
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractGUIController<T extends Nameable> {

    protected SessionController parentSession;
    protected List<T> listOfItems;
    protected JPanel mainPanel;

    protected MyAbstractTableModel<T> itemTableModel;
    protected MyJTable itemTable;

    public AbstractGUIController(SessionController parentSession, List<T> listOfItems) {
        this.parentSession = Utility.checkNull(parentSession, "parent session");
        this.listOfItems = Utility.checkNull(listOfItems, "list of items");

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

    public List<T> getItems() {
        return itemTableModel.getItems();
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
        Utility.checkRange(index, 0, getNumItems());
        return getItems().get(index);
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
        boolean selectNext = index < getNumItems() - 1;
        T removed = getItems().remove(index);
        itemTableModel.fireTableRowsDeleted(index, index);

        if (getTableItemListeners() != null) {
            getTableItemListeners().forEach(l -> l.itemRemoved(removed, index));
        }

        if (selectNext) {
            itemTable.setRowSelectionInterval(index, index);
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
