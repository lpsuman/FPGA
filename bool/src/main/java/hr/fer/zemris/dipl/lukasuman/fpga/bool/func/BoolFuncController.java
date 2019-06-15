package hr.fer.zemris.dipl.lukasuman.fpga.bool.func;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.BoolVecProblem;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.BoolExpression;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.solver.BoolVectorSolution;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.Solution;
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

    public static List<BooleanFunction> generateRandomFunctions(int numInputs, int numFunctions) {
        List<BooleanFunction> functions = new ArrayList<>();
        for (int i = 0; i < numFunctions; i++) {
            functions.add(generateRandomFunction(numInputs));
        }
        return functions;
    }

    public static BooleanFunction generateRandomFunction(List<String> inputIDs, int numInputs) {
        int numInputCombinations = (int) Math.pow(2, numInputs);
        BitSet bitSet = RNG.getRNG().nextBitSet(numInputCombinations);
        return new BooleanFunction(inputIDs, bitSet, DEFAULT_FUNC_NAME);
    }

    public static BooleanVector generateRandomVector(int numFunctionInputs, int numFunctions) {
        return new BooleanVector(generateRandomFunctions(numFunctionInputs, numFunctions));
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

    public static BooleanFunction generateFromText(String text) {
        Utility.checkIfValidString(text, "text");
        String funcName = null;
        List<String> words = new ArrayList<>();

        int equalsSignIndex = text.indexOf('=');
        if (equalsSignIndex != -1) {
            funcName = text.substring(0, equalsSignIndex).trim();
            words.addAll(Utility.breakIntoWords(text.substring(equalsSignIndex + 1).trim()));
        } else {
            words.addAll(Utility.breakIntoWords(text));
        }

        if (words.isEmpty()) {
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

        if (funcName != null) {
            return new BooleanFunction(inputIDs, bitSet, funcName);
        } else {
            return new BooleanFunction(inputIDs, bitSet);
        }
    }

    public static int[] bitSetToArray(BitSet bitSet, int startIndex, int endIndex, int repeat) {
        int sizeOfTruthTable = (endIndex - startIndex) * repeat;
        int[] truthTable = new int[(int) Math.ceil(sizeOfTruthTable / 32.0)];
        int offset = Math.max(0, 32 - sizeOfTruthTable);

        for (int i = 0; i < repeat; i++) {
            for (int j = startIndex; j < endIndex; j++) {
                if (bitSet.get(j)) {
                    int offsetInt = offset / 32;
                    int offsetBit = offset % 32;
                    truthTable[offsetInt] |= 1 << (31 - offsetBit);
                }

                offset++;
            }
        }

        return truthTable;
    }

    public static int[] bitSetToArray(BooleanFunction func) {
        return bitSetToArray(func.getTruthTable(), 0, func.getNumInputCombinations(), 1);
    }

    public static BooleanVector generateSolvable(int numInputs, int numFunctions, int numCLBInputs, int numCLB, boolean canBeRedundant) {
        BoolVecProblem problem = new BoolVecProblem(BoolFuncController.generateRandomVector(numInputs, numFunctions), numCLBInputs);
        problem.getClbController().setNumCLB(numCLB);

        while (true) {
            BlockConfiguration blockConfiguration;
            List<BitSet> fullCLBOutputs;

            outer:
            while (true) {
                blockConfiguration = problem.generateBlockConfiguration(problem.get());
                fullCLBOutputs = BoolVectorSolution.getFullCLBOutputs(problem.getBoolVector(), blockConfiguration);

                for (BitSet fullCLBOutput : fullCLBOutputs) {
                    if (fullCLBOutput.cardinality() == 0) {
                        continue outer;
                    } else {
                        BitSet flipped = (BitSet) fullCLBOutput.clone();
                        flipped.flip(0, problem.getBoolVector().getNumInputCombinations());
                        if (flipped.cardinality() == 0) {
                            continue outer;
                        }
                    }
                }

                break;
            }

            List<Integer> funcOutCLB = new ArrayList<>();
            List<BooleanFunction> functions = new ArrayList<>();
            int numCLBInSolution = fullCLBOutputs.size();
            int indexLastUsed = numCLBInSolution;

            for (int i = 0; i < numFunctions; i++) {
                int truthTableIndex;
                if (indexLastUsed == 0) {
                    truthTableIndex = RNG.getRNG().nextInt(0, numCLBInSolution);
                } else {
                    truthTableIndex = indexLastUsed - 1;
                    indexLastUsed--;
                }
                funcOutCLB.add(truthTableIndex + numInputs);
                functions.add(new BooleanFunction(numInputs, fullCLBOutputs.get(truthTableIndex)));
            }
            BooleanVector solvableVector = new BooleanVector(functions);

            if (!canBeRedundant) {
                for (int i = 0; i < numFunctions; i++) {
                    blockConfiguration.getOutputIndices().set(i, funcOutCLB.get(i));
                }
                BoolVectorSolution solution = new BoolVectorSolution(solvableVector, blockConfiguration);
                BoolVectorSolution checkedSolution = BoolVectorSolution.removeRedundantCLBs(solution);

                if (checkedSolution.getBlockConfiguration().getNumCLB() != solution.getBlockConfiguration().getNumCLB()) {
                    continue;
                }
            }

            return solvableVector;
        }
    }
}
