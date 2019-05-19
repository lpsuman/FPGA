package hr.fer.zemris.dipl.lukasuman.fpga.bool.func;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.AbstractNameHandler;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.io.Serializable;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;

public class BoolFunc extends AbstractNameHandler implements Serializable {

    private static final long serialVersionUID = -3337214107238850818L;

    private static final String DEFAULT_NAME = "BoolFunc";
    private static final String NAME_MSG = "boolean function's name";
    private static final String INPUT_IDS_MSG = "input IDs for function";

    private int numInputs;
    private List<String> inputIDs;
    private int numInputCombinations;
    private BitSet truthTable;

    public BoolFunc(String name, List<String> inputIDs, BitSet truthTable) {
        super(name);
        this.inputIDs = Utility.checkIfValidCollection(inputIDs, INPUT_IDS_MSG);
        Utility.checkLimit(Constants.NUM_FUNCTION_INPUTS_LIMIT, inputIDs.size());
        this.truthTable = Utility.checkNull(truthTable, "truth table");
        numInputs = inputIDs.size();
        numInputCombinations = (int) Math.pow(2, numInputs);

        if (truthTable.length() > (1 << inputIDs.size())) {
            throw new IllegalArgumentException(String.format(
                    "Not enough inputs (%d) for the given truth table (of min size %d).",
                    inputIDs.size(), truthTable.length()));
        }
    }

    public BoolFunc(List<String> inputIDs, BitSet truthTable) {
        this (DEFAULT_NAME, inputIDs, truthTable);
    }

    public BoolFunc(String name, int numInputs, BitSet truthTable) {
        this(name, BoolFuncController.generateDefaultInputIDs(numInputs), truthTable);
    }

    public BoolFunc(int numInputs, BitSet truthTable) {
        this(DEFAULT_NAME, numInputs, truthTable);
    }

    public List<String> getInputIDs() {
        return inputIDs;
    }

    public void setInputIDs(List<String> inputIDs) {
        Utility.checkIfValidCollection(inputIDs, INPUT_IDS_MSG);

        if (inputIDs.size() != this.inputIDs.size()) {
            throw new IllegalArgumentException(String.format("New list of input IDs (size %d) must contain the " +
                    "same number of inputs as this function (%d inputs).", inputIDs.size(), this.inputIDs.size()));
        }

        this.inputIDs = inputIDs;
    }

    public BitSet getTruthTable() {
        return truthTable;
    }

    public int getNumInputs() {
        return numInputs;
    }

    public int getNumInputCombinations() {
        return numInputCombinations;
    }

    @Override
    protected String getNameMessage() {
        return NAME_MSG;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoolFunc boolFunc = (BoolFunc) o;
        return truthTable.equals(boolFunc.truthTable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(truthTable);
    }
}
