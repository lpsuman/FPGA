package hr.fer.zemris.dipl.lukasuman.fpga.bool.func;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.CLBController;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.util.*;

public class BoolVector {

    private List<BoolFunc> boolFunctions;
    private int numInputCombinations;
    private BitSet[] truthTable;
    private List<String> sortedInputIDs;

    public BoolVector(List<BoolFunc> boolFunctions) {
        this.boolFunctions = Utility.checkNull(boolFunctions, "list of boolean functions");
        Utility.checkLimit(Constants.NUM_FUNCTIONS_LIMIT, boolFunctions.size());
        boolFunctions.forEach(f -> Utility.checkNull(f, "boolean function for vector"));

        fillTable(boolFunctions);
    }

    private void fillTable(List<BoolFunc> boolFuncs) {
        Set<String> inputIDSet = new HashSet<>();
        boolFuncs.forEach(f -> inputIDSet.addAll(f.getInputIDs()));
        sortedInputIDs = new ArrayList<>(inputIDSet);
        Collections.sort(sortedInputIDs);

        numInputCombinations = (int) Math.pow(2, getNumInputs());
        truthTable = Utility.newBitSetArray(boolFunctions.size(), numInputCombinations);

        for (int inputCombination = 0; inputCombination < numInputCombinations; inputCombination++) {
            for (int j = 0; j < getNumFunctions(); ++j) {
                BoolFunc boolFunc = boolFuncs.get(j);
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
}
