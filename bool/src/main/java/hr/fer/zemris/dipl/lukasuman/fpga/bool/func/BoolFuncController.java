package hr.fer.zemris.dipl.lukasuman.fpga.bool.func;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.BoolExpression;
import hr.fer.zemris.dipl.lukasuman.fpga.rng.RNG;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * This class contains various static method for {@link BooleanFunction} manipulation.
 */
public class BoolFuncController {

    private static final String DEFAULT_FUNC_NAME = "func";

    public static List<String> generateDefaultInputIDs(int numInputs) {
        Utility.checkLimit(Constants.NUM_FUNCTION_INPUTS_LIMIT, numInputs);
        List<String> inputIDs = new ArrayList<>(numInputs);

        for (int i = 0; i < numInputs; i++) {
            inputIDs.add("" + (char)('a' + i));
        }

        return inputIDs;
    }

    public static BooleanFunction generateRandomFunction(int numInputs)  {
        return generateRandomFunction(BoolFuncController.generateDefaultInputIDs(numInputs), numInputs);
    }

    public static BooleanFunction generateRandomFunction(List<String> inputIDs, int numInputs) {
        int numInputCombinations = (int) Math.pow(2, numInputs);
        BitSet bitSet = RNG.getRNG().nextBitSet(numInputCombinations);
        return new BooleanFunction(inputIDs, bitSet, DEFAULT_FUNC_NAME);
    }

    public static BooleanFunction generateFromMask(int mask, int numInputs) {
        return new BooleanFunction(BoolFuncController.generateDefaultInputIDs(numInputs),
                Utility.bitSetFromMask(mask, (int) Math.pow(2, numInputs)), DEFAULT_FUNC_NAME);
    }

    public static int calcNumInputs(List<BooleanFunction> boolFunctions) {
        return boolFunctions.stream()
                .mapToInt(func -> func.getInputIDs().size())
                .max()
                .orElseThrow(NoSuchElementException::new);
    }

    public static List<Integer> checkIfInputMatters(BooleanFunction func) {
        Utility.checkNull(func, "boolean function");
        int numInputs = func.getNumInputs();
        BitSet truthTable = func.getTruthTable();
        int numInputCombinations = func.getNumInputCombinations();
        List<Integer> result = null;

        outer:
        for (int i = 0; i < numInputs; i++) {
            int numJumps = 1 << (i);
            int sizeOfJump = numInputCombinations / numJumps;
            int numConsecutive = 1 << (numInputs - 1 - i);

            for (int j = 0; j < numJumps; ++j) {
                for (int k = 0; k < numConsecutive; ++k) {
                    int inputCombination = j * sizeOfJump + k;
                    int complementCombination = inputCombination + sizeOfJump / 2;

                    if (truthTable.get(inputCombination) != truthTable.get(complementCombination)) {
                        continue outer;
                    }
                }
            }

            if (result == null) {
                result = new ArrayList<>();
            }

            result.add(i);
        }

        return result;
    }

    public static BooleanFunction removeInputs(BooleanFunction func, List<Integer> indicesInputsToRemove) {
        Utility.checkNull(func, "boolean function");
        if (indicesInputsToRemove == null || indicesInputsToRemove.isEmpty()) {
            return func;
        }

        int numInputs = func.getNumInputs();
        int newNumInputs = numInputs - indicesInputsToRemove.size();

        boolean[] isInputUsed = new boolean[numInputs];
        List<String> newInputIDs = new ArrayList<>(newNumInputs);

        for (int i = 0; i < numInputs; i++) {
            isInputUsed[i] = !indicesInputsToRemove.contains(i);
            if (isInputUsed[i]) {
                newInputIDs.add(func.getInputIDs().get(i));
            }
        }

        BitSet truthTable = func.getTruthTable();
        int newNumInputCombinations = (int) Math.pow(2, newNumInputs);
        BitSet newTruthTable = new BitSet(newNumInputCombinations);

        for (int inputCombination = 0; inputCombination < newNumInputCombinations; ++inputCombination) {
            int newInputCombination = 0;

            int currIndexInCombination = 0;
            int numShifted = 0;
            for (int i = numInputs - 1; i >= 0; --i) {
                if (isInputUsed[i]) {
                    newInputCombination |= (inputCombination & (1 << currIndexInCombination)) << numShifted;
                    currIndexInCombination++;
                } else {
                    numShifted++;
                }
            }

            newTruthTable.set(inputCombination, truthTable.get(newInputCombination));
        }

        return new BooleanFunction(newInputIDs, newTruthTable);
    }

    public static BooleanFunction removeRedundantInputsIfAble(BooleanFunction func) {
        List<Integer> redundantIndices = checkIfInputMatters(func);

        if (redundantIndices == null) {
            return func;
        } else {
            return removeInputs(func, redundantIndices);
        }
    }

    public static BooleanFunction generateFromExpression(BoolExpression boolExpression) {
        return new BooleanFunction(boolExpression.getInputIDs(), boolExpression.getTruthTable());
    }
}
