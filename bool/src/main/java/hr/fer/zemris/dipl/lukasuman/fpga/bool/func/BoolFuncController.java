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
            if (func.getNumInputs() - redundantIndices.size() < Constants.NUM_FUNCTION_INPUTS_LIMIT.getLowerLimit()) {
                return func;
            }
            return removeInputs(func, redundantIndices);
        }
    }

    public static BooleanFunction generateFromExpression(BoolExpression boolExpression) {
        return new BooleanFunction(boolExpression.getInputIDs(), boolExpression.getTruthTable());
    }

    public static BooleanFunction generateFromText(List<String> text) {
        Utility.checkIfValidCollection(text, "text");
        List<String> words = Utility.breakIntoWords(text);
        if (words == null) {
            throw new IllegalArgumentException("Input text is empty.");
        }

        int numInputs = -1;

        try {
            String firstWord = words.get(0);
            if (firstWord.length() <= 2) {
                numInputs = Integer.parseInt(firstWord);

                if (numInputs == 0 || numInputs == 1) {
                    numInputs = -1;
                } else {
                    if (numInputs > Constants.NUM_FUNCTION_INPUTS_LIMIT.getUpperLimit()) {
                        throw new IllegalArgumentException(String.format(
                                "Too many inputs specified (%d while %d is maximum).",
                                numInputs, Constants.NUM_FUNCTION_INPUTS_LIMIT.getUpperLimit()));
                    }
                    words.remove(0);
                    if (words.isEmpty()) {
                        throw new IllegalArgumentException(String.format(
                                "%d inputs specified but there is nothing else in the input text.", numInputs));
                    }
                }
            }
        } catch (NumberFormatException ignored) {
        }

        List<String> inputIDs = new ArrayList<>();

        if (numInputs != -1) {
            for (int i = 0; i < numInputs; i++) {
                String word = words.remove(0);

                if (!Utility.isValidInputID(word)) {
                    throw new IllegalArgumentException(String.format(
                            "%d inputs specified but %d inputs detected (%s is not a valid input ID).",
                            numInputs, i, word));
                }

                if (words.isEmpty()) {
                    throw new IllegalArgumentException(String.format(
                            "Expected to read %d input IDs, but only %d were recognized.", numInputs, i + 1));
                }

                inputIDs.add(word);
            }

            if (Utility.isValidInputID(words.get(0))) {
                throw new IllegalArgumentException(String.format(
                        "Expected to read %d inputs, but %s is also a valid input ID.", numInputs, words.get(0)));
            }
        } else {
            while (true) {
                if (Utility.isValidInputID(words.get(0))) {
                    inputIDs.add(words.remove(0));

                    if (numInputs == -1) {
                        numInputs = 0;
                    }

                    numInputs++;

                    if (words.isEmpty()) {
                        throw new IllegalArgumentException(String.format(
                                "There are only input IDs (%d of them) in the input text. Last recognized input ID: %s",
                                inputIDs.size(), inputIDs.get(inputIDs.size() - 1)));
                    }
                } else {
                    break;
                }
            }
        }

        String joinedTruthTableString = String.join("", words);
        if (!Utility.isPowerOfTwo(joinedTruthTableString.length())) {
            throw new IllegalArgumentException("Truth table must have a power of two binary characters.");
        }
        int numExpectedInputs = Utility.lowestPowerOfTwo(joinedTruthTableString.length());

        if (numInputs == -1) {
            inputIDs = generateDefaultInputIDs(numExpectedInputs);
        } else if (numInputs != numExpectedInputs) {
            throw new IllegalArgumentException(String.format(
                    "%d inputs detected, but %d required for the given truth table %s.",
                    numInputs, numExpectedInputs, joinedTruthTableString));
        }

        if (inputIDs.size() < Constants.NUM_FUNCTION_INPUTS_LIMIT.getLowerLimit()) {
            throw new IllegalArgumentException(String.format("Not enough inputs (%d detected while %d is minimum).",
                    inputIDs.size(), Constants.NUM_FUNCTION_INPUTS_LIMIT.getLowerLimit()));
        }

        BitSet bitSet = Utility.bitSetFromString(joinedTruthTableString);

        if (bitSet == null) {
            throw new IllegalArgumentException("Invalid truth table format.");
        }

        return new BooleanFunction(inputIDs, bitSet);
    }
}
