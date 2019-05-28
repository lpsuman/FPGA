package hr.fer.zemris.dipl.lukasuman.fpga.gui.func;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.AbstractNameHandler;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanFunction;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanVector;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationKeys;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationProvider;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import javax.swing.table.AbstractTableModel;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    private LocalizationProvider lp;
    private String indexColumnName;
    private boolean displayIndices;

    public TruthTableModel(LocalizationProvider lp) {
        this.lp = Utility.checkNull(lp, "localization provider");
        updateColumnNames();
        lp.addLocalizationListener(this::updateColumnNames);
        loadDefaultData();
        displayIndices = true;
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

    public void setData(BooleanVector booleanVector) {
        List<String> outputIDs = booleanVector.getBoolFunctions().stream()
                .map(AbstractNameHandler::getName)
                .collect(Collectors.toList());
        setData(booleanVector.getSortedInputIDs(), outputIDs, booleanVector.getTruthTable());
    }

    public void setData(List<String> inputIDs, String outputName, BitSet truthTable) {
        BitSet[] truthTableArray = new BitSet[1];
        truthTableArray[0] = truthTable;
        setData(inputIDs, Collections.singletonList(outputName), truthTableArray);
    }

    public void setData(BooleanFunction booleanFunction) {
        setData(booleanFunction.getInputIDs(), booleanFunction.getName(), booleanFunction.getTruthTable());
    }

    private void loadDefaultData() {
        setData(DEFAULT_INPUT_IDS, DEFAULT_OUTPUT_IDS, DEFAULT_TRUTH_TABLES);
    }

    private void updateColumnNames() {
        indexColumnName = lp.getString(LocalizationKeys.INDEX_KEY);
        fireTableStructureChanged();
    }

    public void setDisplayIndices(boolean displayIndices) {
        if (displayIndices != this.displayIndices) {
            this.displayIndices = displayIndices;
            fireTableStructureChanged();
        }
    }

    public List<String> getInputIDs() {
        return inputIDs;
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
        int columnCount = getNumInputs() + getNumOutputs();

        if (displayIndices) {
            columnCount += 1;
        }

        return columnCount;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (displayIndices) {
            if (columnIndex == 0) {
                return rowIndex;
            }
            columnIndex -= 1;
        }

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

        if (displayIndices) {
            columnIndex -= 1;
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
        if (displayIndices) {
            if (column == 0) {
                return indexColumnName;
            }
            column -= 1;
        }

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
        if (truthTables.length > 1) {
            return false;
        }

        if (displayIndices) {
            columnIndex -= 1;
        }

        return columnIndex >= getNumInputs();
    }
}
