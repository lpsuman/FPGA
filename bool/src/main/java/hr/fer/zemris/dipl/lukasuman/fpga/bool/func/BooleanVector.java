package hr.fer.zemris.dipl.lukasuman.fpga.bool.func;

import hr.fer.zemris.dipl.lukasuman.fpga.util.AbstractNameHandler;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.CLBController;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Duplicateable;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.io.Serializable;
import java.util.*;

/**
 * This class represents a vector of boolean functions. Multiple boolean functions are joined in the following way:
 * <ul>
 *     <li>their input IDs are merged into a single set without duplications (same inputs overlap)</li>
 *     <li>their truth tables are put into a matrix</li>
 *     <li>functions which don't use all of the variables have shorter truth tables than required so their tables
 *     are copied as required</li>
 * </ul>
 */
public class BooleanVector extends AbstractNameHandler implements Serializable, Duplicateable<BooleanVector> {

    private static final long serialVersionUID = -7755696151775719987L;

    private static final String DEFAULT_NAME = "BooleanVector";

    private List<BooleanFunction> boolFunctions;
    private int numInputCombinations;
    private BitSet[] truthTable;
    private List<String> sortedInputIDs;

    public BooleanVector(List<BooleanFunction> boolFunctions, boolean removeRedundantInputs, String name) {
        super(name);
        this.boolFunctions = Utility.checkIfValidCollection(boolFunctions, "boolean functions for vector");
        updateTable(removeRedundantInputs);
    }

    public BooleanVector(List<BooleanFunction> boolFunctions, boolean removeRedundantInputs) {
        this(boolFunctions, removeRedundantInputs, DEFAULT_NAME);
    }

    public BooleanVector(List<BooleanFunction> boolFunctions, String name) {
        this(boolFunctions, false, name);
    }

    public BooleanVector(List<BooleanFunction> boolFunctions) {
        this(boolFunctions, false);
    }

    public BooleanVector(BooleanFunction func) {
        this(Collections.singletonList(func), func.getName());
    }

    public BooleanVector(BooleanVector other) {
        super(Utility.checkNull(other, "boolean vector").getName());
        boolFunctions = new ArrayList<>(other.boolFunctions);
        numInputCombinations = other.numInputCombinations;

        truthTable = new BitSet[other.truthTable.length];
        for (int i = 0; i < truthTable.length; i++) {
            truthTable[i] = (BitSet) other.truthTable[i].clone();
        }

        sortedInputIDs = new ArrayList<>(other.sortedInputIDs);
    }

    public void updateTable(boolean removeRedundantInputs) {
        Utility.checkLimit(Constants.NUM_FUNCTIONS_LIMIT, boolFunctions.size());
        Set<String> inputIDSet = new HashSet<>();
        List<BooleanFunction> checkedFunctions = new ArrayList<>(boolFunctions.size());

        if (removeRedundantInputs) {
            boolFunctions.forEach(f -> checkedFunctions.add(BoolFuncController.removeRedundantInputsIfAble(f)));
        } else {
            checkedFunctions.addAll(boolFunctions);
        }

        checkedFunctions.forEach(f -> inputIDSet.addAll(f.getInputIDs()));
        sortedInputIDs = new ArrayList<>(inputIDSet);
        Collections.sort(sortedInputIDs);

        int numInputs = getNumInputs();
        numInputCombinations = (int) Math.pow(2, numInputs);
        truthTable = Utility.newBitSetArray(boolFunctions.size(), numInputCombinations);

        for (int inputCombination = 0; inputCombination < numInputCombinations; inputCombination++) {
            for (int j = 0; j < getNumFunctions(); ++j) {
                BooleanFunction boolFunc = checkedFunctions.get(j);
                List<String> inputIDs = boolFunc.getInputIDs();
                int[] inputIndexes = new int[inputIDs.size()];

                for (int i = 0, n = inputIndexes.length; i < n; i++) {
                    inputIndexes[i] = sortedInputIDs.indexOf(inputIDs.get(i));
                }

                int extendedIndex = CLBController.calcExtendedIndex(inputCombination, numInputs, inputIndexes);

                truthTable[j].set(inputCombination, boolFunc.getTruthTable().get(extendedIndex));
            }
        }
    }

    public List<BooleanFunction> getBoolFunctions() {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BooleanVector that = (BooleanVector) o;
        return numInputCombinations == that.numInputCombinations &&
                boolFunctions.equals(that.boolFunctions) &&
                Arrays.equals(truthTable, that.truthTable) &&
                sortedInputIDs.equals(that.sortedInputIDs);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(boolFunctions, numInputCombinations, sortedInputIDs);
        result = 31 * result + Arrays.hashCode(truthTable);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("Boolean Vector, name={%s}, with functions:\n", getName()));
        boolFunctions.forEach(func -> sb.append(func).append('\n'));
        sb.append("Combined inputs = {");
        Utility.appendStringList(sb, sortedInputIDs);
        sb.append("}\nCombined truth tables:\n");
        for (BitSet bitSet : truthTable) {
            Utility.appendBitSet(sb, bitSet, numInputCombinations);
            sb.append('\n');
        }
        sb.setLength(sb.length() - 1);

        return sb.toString();
    }

    @Override
    public BooleanVector getDuplicate() {
        return new BooleanVector(this);
    }
}
