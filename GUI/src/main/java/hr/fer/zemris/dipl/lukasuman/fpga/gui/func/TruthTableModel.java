package hr.fer.zemris.dipl.lukasuman.fpga.gui.func;

import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import javax.swing.table.AbstractTableModel;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

public class TruthTableModel extends AbstractTableModel {

    private static final List<String> DEFAULT_INPUT_IDS = Arrays.asList("a", "b", "c", "d");
    private static final List<String> DEFAULT_OUTPUT_IDS = Arrays.asList("f1", "f2", "f3");
    private static final BitSet[] DEFAULT_TRUTH_TABLES = new BitSet[DEFAULT_OUTPUT_IDS.size()];

    static {
        for (int i = 0; i < DEFAULT_OUTPUT_IDS.size(); i++) {
            DEFAULT_TRUTH_TABLES[i] = new BitSet();
        }
    }

    private List<String> inputIDs;
    private int numInputCombinations;
    private List<String> outputIDs;
    private BitSet[] truthTables;

    public TruthTableModel() {
        loadDefaultData();
    }

    public void setData(List<String> inputIDs, List<String> outputIDs, BitSet[] truthTables) {
        Utility.checkIfValidCollection(inputIDs, "input IDs");
        Utility.checkIfValidCollection(outputIDs, "output IDs");
        Utility.checkIfValidArray(truthTables, "truth tables");

        if (outputIDs.size() != truthTables.length) {
            throw new IllegalArgumentException("Truth table model needs an output ID for each given truth table data");
        }

        int numInputCombinations = (int) Math.pow(2, inputIDs.size());

        for (BitSet truthTable : truthTables) {
            if (truthTable.length() > numInputCombinations) {
                throw new IllegalArgumentException(
                        "Truth table model was given a truth table data larger than the number of input combinations.");
            }
        }

        this.inputIDs = inputIDs;
        this.numInputCombinations = numInputCombinations;
        this.outputIDs = outputIDs;
        this.truthTables = truthTables;

        fireTableStructureChanged();
    }

    public void setData(List<String> inputIDs, String outputName, BitSet truthTable) {
        BitSet[] truthTableArray = new BitSet[1];
        truthTableArray[0] = truthTable;
        setData(inputIDs, Collections.singletonList(outputName), truthTableArray);
    }

    private void loadDefaultData() {
        setData(DEFAULT_INPUT_IDS, DEFAULT_OUTPUT_IDS, DEFAULT_TRUTH_TABLES);
    }

    private int getNumInputs() {
        return inputIDs.size();
    }

    private int getNumOutputs() {
        return outputIDs.size();
    }

    @Override
    public int getRowCount() {
        return numInputCombinations;
    }

    @Override
    public int getColumnCount() {
        return getNumInputs() + getNumOutputs();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex < getNumInputs()) {
            if (Utility.testBitFromRight(rowIndex, getNumInputs() - 1 - columnIndex)) {
                return 1;
            } else {
                return 0;
            }
        } else {
            int indexTruthTable = columnIndex - getNumInputs();
            if (truthTables[indexTruthTable].get(rowIndex)) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (!isCellEditable(rowIndex, columnIndex)) {
            throw new IllegalArgumentException("Truth table can only modify its output columns.");
        }

        boolean value = ((Integer) aValue) != 0;
        int indexTruthTable = columnIndex - getNumInputs();
        boolean oldValue = truthTables[indexTruthTable].get(rowIndex);

        if (value != oldValue) {
            truthTables[indexTruthTable].set(rowIndex, value);
            fireTableCellUpdated(rowIndex, columnIndex);
        }
    }

    @Override
    public String getColumnName(int column) {
        if (column < getNumInputs()) {
            return inputIDs.get(column);
        } else {
            return outputIDs.get(column - getNumInputs());
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return Integer.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex >= getNumInputs();
    }
}
