package hr.fer.zemris.dipl.lukasuman.fpga.bool.func;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.AbstractNameHandler;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.CLBController;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.io.Serializable;
import java.util.*;

public class BoolVector extends AbstractNameHandler implements Serializable {

    private static final long serialVersionUID = -7755696151775719987L;

    private static final String DEFAULT_NAME = "BoolVector";
    private static final String NAME_MSG = "boolean vector's name";

    private List<BoolFunc> boolFunctions;
    private int numInputCombinations;
    private BitSet[] truthTable;
    private List<String> sortedInputIDs;

    public BoolVector(String name, List<BoolFunc> boolFunctions) {
        super(name);
        this.boolFunctions = Utility.checkIfValidCollection(boolFunctions, "boolean functions for vector");
        Utility.checkLimit(Constants.NUM_FUNCTIONS_LIMIT, boolFunctions.size());

        fillTable(boolFunctions);
    }

    public BoolVector(List<BoolFunc> boolFunctions) {
        this(DEFAULT_NAME, boolFunctions);
    }

    private void fillTable(List<BoolFunc> boolFuncs) {
        Set<String> inputIDSet = new HashSet<>();
        List<BoolFunc> checkedFunctions = new ArrayList<>(boolFuncs.size());
        boolFuncs.forEach(f -> checkedFunctions.add(BoolFuncController.removeRedundantInputsIfAble(f)));
        checkedFunctions.forEach(f -> inputIDSet.addAll(f.getInputIDs()));
        sortedInputIDs = new ArrayList<>(inputIDSet);
        Collections.sort(sortedInputIDs);

        numInputCombinations = (int) Math.pow(2, getNumInputs());
        truthTable = Utility.newBitSetArray(boolFunctions.size(), numInputCombinations);

        for (int inputCombination = 0; inputCombination < numInputCombinations; inputCombination++) {
            for (int j = 0; j < getNumFunctions(); ++j) {
                BoolFunc boolFunc = checkedFunctions.get(j);
                List<String> inputIDs = boolFunc.getInputIDs();
                int[] inputIndexes = new int[inputIDs.size()];

                for (int i = 0, n = inputIndexes.length; i < n; ++i) {
                    inputIndexes[i] = sortedInputIDs.indexOf(inputIDs.get(i));
                }

                int extendedIndex = CLBController.calcExtendedIndex(inputCombination, inputIndexes);

                truthTable[j].set(inputCombination, boolFunc.getTruthTable().get(extendedIndex));
            }
        }
    }

    public List<BoolFunc> getBoolFunctions() {
        return boolFunctions;
    }

    public int getNumFunctions() {
        return truthTable.length;
    }

    public int getNumInputs() {
        return sortedInputIDs.size();
    }

    public int getNumInputCombinations() {
        return numInputCombinations;
    }

    public BitSet[] getTruthTable() {
        return truthTable;
    }

    public List<String> getSortedInputIDs() {
        return sortedInputIDs;
    }

    @Override
    protected String getNameMessage() {
        return NAME_MSG;
    }
}
