package hr.fer.zemris.dipl.lukasuman.fpga.gui.table;

public interface TableItemListener<T> {

    void itemAdded(T item, int indexInTable);
    void itemRemoved(T item, int indexInTable);
    void itemRenamed(T item, int indexInTable, String oldName);
    void itemListChanged();
}
