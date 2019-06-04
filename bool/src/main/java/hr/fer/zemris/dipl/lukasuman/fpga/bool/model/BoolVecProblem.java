package hr.fer.zemris.dipl.lukasuman.fpga.bool.model;

import hr.fer.zemris.dipl.lukasuman.fpga.util.AbstractNameHandler;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BlockConfiguration;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanFunction;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BoolFuncController;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanVector;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.IntArraySolution;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.Solution;
import hr.fer.zemris.dipl.lukasuman.fpga.rng.IRNG;
import hr.fer.zemris.dipl.lukasuman.fpga.rng.RNG;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.io.Serializable;
import java.util.*;
import java.util.function.Supplier;

public class BoolVecProblem extends AbstractNameHandler implements Supplier<Solution<int[]>>, Serializable {

    private static final long serialVersionUID = 8162221722673112143L;

    private static class MultiplexerData {
        private int numExcessInputs;
        private int numExtraInputs;
        private int numBranchingCLBs;
        private int numBitsInTable;
        private int sectorSize;
        private int[] data;

        public MultiplexerData(int numCLBInputs) {
            numExcessInputs = 0;
            numExtraInputs = 1;
            numBranchingCLBs = 2;

            while (true) {
                if (numExtraInputs + numBranchingCLBs == numCLBInputs) {
                    break;
                } else if (numExtraInputs + numBranchingCLBs > numCLBInputs) {
                    numExtraInputs--;
                    numBranchingCLBs /= 2;
                    break;
                }

                numExtraInputs++;
                numBranchingCLBs *= 2;
            }

            numExcessInputs = numCLBInputs - (numExtraInputs + numBranchingCLBs);
            numBitsInTable = (int) Math.pow(2, numCLBInputs);
            data = new int[(int) Math.ceil(numBitsInTable / 32.0)];
            sectorSize = (int) Math.pow(2, numExtraInputs + numBranchingCLBs);
            int perBranchingSectorSize = sectorSize / numBranchingCLBs;
            int numConsecutive = (int) Math.pow(2, numBranchingCLBs - 1);

            if (sectorSize < 32) {
                switch (numExcessInputs) {
                    case 0:
                        data[0] = 0b00110101;
                        break;
                    case 1:
                        data[0] = 0b0011010100110101;
                        break;
                    case 2:
                        data[0] = 0b00110101001101010011010100110101;
                        break;
                    default:
                        throw new IllegalStateException("Invalid number of excess inputs.");
                }
            } else {
                for (int i = 0; i < numBranchingCLBs; i++) {
                    for (int j = 0; j < perBranchingSectorSize / (2 * numConsecutive); j++) {
                        for (int k = 0; k < numConsecutive; k++) {
                            int bitIndex = i * perBranchingSectorSize + (2 * j + 1) * numConsecutive + k;
                            int indexIntegerInData = bitIndex / 32;
                            bitIndex %= 32;
                            data[indexIntegerInData] |= 1 << (31 - bitIndex);
                        }
                    }

                    numConsecutive /= 2;
                }

                for (int i = 0, n = (int) Math.pow(2, numExcessInputs) - 1; i < n; i++) {
                    System.arraycopy(data, 0, data, ((i + 1) * sectorSize / 32), sectorSize / 32);
                }
            }
        }
    }

    private static final String DEFAULT_NAME = "BoolProblem";

    private BooleanVector boolVector;
    private CLBController clbController;

    private List<Solution<int[]>> nextToSupplyList;
    private Solution<int[]> nextToSupply;
    private int indexCurrToSupply;

    public BoolVecProblem(BooleanVector boolVector, int numCLBInputs, String name) {
        super(name);
        this.boolVector = Utility.checkNull(boolVector, "boolean vector");
        clbController = new CLBController(boolVector, numCLBInputs);
    }

    public BoolVecProblem(BooleanVector boolVector, int numCLBInputs) {
        this(boolVector, numCLBInputs, DEFAULT_NAME);
    }

    public BoolVecProblem(BooleanFunction func, int numCLBInputs) {
        this(new BooleanVector(func), numCLBInputs);
    }

    public void setNextToSupply(Solution<int[]> solution) {
        Utility.checkNull(solution, "solution");
        this.nextToSupply = solution;
    }

    public void setNextToSupplyList(List<Solution<int[]>> solutions, int indexCurrToSupply) {
        this.nextToSupplyList = Utility.checkIfValidCollection(solutions, "list of solutions to supply next");
        this.indexCurrToSupply = Utility.checkRange(indexCurrToSupply, 0, solutions.size() - 1);
    }

    @Override
    public Solution<int[]> get() {
        if (nextToSupply != null) {
            Solution<int[]> result = nextToSupply;
            nextToSupply = null;
            return result;
        }

        if (nextToSupplyList != null) {
            Solution<int[]> result = nextToSupplyList.get(indexCurrToSupply);
            indexCurrToSupply++;

            if (indexCurrToSupply >= nextToSupplyList.size()) {
                nextToSupplyList = null;
            }
            return result;
        }

        int numCLB = clbController.getNumCLB();
        int[] data = new int[numCLB * clbController.getIntsPerCLB() + boolVector.getNumFunctions()];
        IRNG random = RNG.getRNG();

        for (int i = 0; i < numCLB; i++) {
            int offset = clbController.calcCLBOffset(i);
            int numCLBInputs = clbController.getNumCLBInputs();

            for (int j = 0; j < numCLBInputs; ++j) {
                data[offset + j] = clbController.calcRandomInput(i);
            }

            clbController.randomizeTable(data, i, random);
        }

        int numFunctions = boolVector.getNumFunctions();
        for (int i = 0; i < numFunctions; i++) {
            data[data.length - numFunctions + i] = random.nextInt(0, boolVector.getNumInputs() + numCLB);
        }

        return new IntArraySolution(data);
    }

    public static BoolVecProblem generateRandomProblem(int numFunctions, int numInputs, int numCLBInputs) {
        List<BooleanFunction> boolFuncs = new ArrayList<>();
        for (int i = 0; i < numFunctions; i++) {
            boolFuncs.add(BoolFuncController.generateRandomFunction(numInputs));
        }

        return new BoolVecProblem(new BooleanVector(boolFuncs), numCLBInputs);
    }

    private void appendSolution(StringBuilder sb, Solution<int[]> solution, BitSet[] blockUsage) {
        int[] data = solution.getData();
        List<String> sortedIDs = boolVector.getSortedInputIDs();

        BlockConfiguration.appendFormattedInputData(sb, sortedIDs, blockUsage);
        BlockConfiguration.appendFormattedCLBData(sb, clbController, data, blockUsage);
        BlockConfiguration.appendFormattedOutputData(sb, data, data.length - boolVector.getNumFunctions());
    }

    public String solutionToString(Solution<int[]> solution, BitSet[] blockUsage) {
        Utility.checkNull(solution, "solution");
        StringBuilder sb = new StringBuilder();

        appendSolution(sb, solution, blockUsage);

        return sb.toString();
    }

    public String getSolutionTestResults(Solution<int[]> solution, BoolVecEvaluator evaluator) {
        StringBuilder sb = new StringBuilder();

        evaluator.setLogging(true);
        evaluator.evaluateSolution(solution, false);
        sb.append(evaluator.getLog()).append('\n');
        evaluator.resetLog();
        evaluator.setLogging(false);
//        sb.append(solutionToString(solution, evaluator.getBlockUsage())).append('\n');

        appendSolution(sb, solution, evaluator.getBlockUsage());

        int numUnusedBlocks = evaluator.getUnusedCLBBlocks().cardinality();
        sb.append(String.format("\nThere were %d unused blocks.\n", numUnusedBlocks)).append('\n');

        return sb.toString();
    }

    private void checkIfValidSolution(Solution<int[]> solution) {
        Utility.checkNull(solution, "solution");
        int numCLB = clbController.getNumCLB();
        int blockSize = clbController.getIntsPerCLB();
        int numFunctions = boolVector.getNumFunctions();
        int[] solutionData = solution.getData();

        if (solutionData.length != numCLB * blockSize + numFunctions) {
            throw new IllegalArgumentException(String.format("Invalid solution (data size %d) for configuration:\n%s",
                    solutionData.length, clbController));
        }
    }

    public Solution<int[]> trimmedBoolSolution(Solution<int[]> solution, BitSet unusedBlocks) {
        checkIfValidSolution(solution);
        Utility.checkNull(unusedBlocks, "unused blocks");

        int[] solutionData = solution.getData();
        int numUnusedBlocks = unusedBlocks.cardinality();

        for (int i = 0; i < clbController.getNumInputs(); i++) {
            if (unusedBlocks.get(i)) {
                numUnusedBlocks--;
            }
        }

        if (numUnusedBlocks == 0) {
            return solution.duplicate();
        }

        int[] trimmedData = new int[solutionData.length - numUnusedBlocks * clbController.getIntsPerCLB()];
        int numInputs = clbController.getNumInputs();
        int numCLBInputs = clbController.getNumCLBInputs();
        int numCLB = clbController.getNumCLB();
        int[] newBlockIndices = new int[numCLB];
        int currIndexInTrimmedData = 0;

        for (int i = 0; i < numCLB; i++) {
            if (!unusedBlocks.get(numInputs + i)) {
                int indexBlockOriginal = clbController.calcCLBOffset(i);
                int indexBlockTrimmed = clbController.calcCLBOffset(currIndexInTrimmedData);

                for (int j = 0; j < numCLBInputs; ++j) {
                    int input = solutionData[indexBlockOriginal + j];
                    if (input >= numInputs) {
                        input = newBlockIndices[input - numInputs];
                    }
                    trimmedData[indexBlockTrimmed + j] = input;
                }

                System.arraycopy(solutionData, indexBlockOriginal + numCLBInputs,
                        trimmedData, indexBlockTrimmed + numCLBInputs, clbController.getIntsPerLUT());

                newBlockIndices[i] = currIndexInTrimmedData + numInputs;
                currIndexInTrimmedData++;
            }
        }

        int numFunctions = boolVector.getNumFunctions();

        for (int i = 0; i < numFunctions; i++) {
            int indexFunctionOutputBlock = clbController.calcCLBOffset(numCLB) + i;
            int funcOutputIndex = solutionData[indexFunctionOutputBlock];

            if (funcOutputIndex >= numInputs) {
                funcOutputIndex = newBlockIndices[funcOutputIndex - numInputs];
            }

            int indexInTrimmed = clbController.calcCLBOffset(currIndexInTrimmedData) + i;
            trimmedData[indexInTrimmed] = funcOutputIndex;
        }

        Solution<int[]> trimmedSolution = new IntArraySolution(trimmedData);
        trimmedSolution.setFitness(solution.getFitness());
        return trimmedSolution;
    }

    public BlockConfiguration generateBlockConfiguration(Solution<int[]> solution) {
        checkIfValidSolution(solution);

        int numCLB = clbController.getNumCLB();
        int blockSize = clbController.getIntsPerCLB();
        int[] solutionData = solution.getData();

        int[] newData = Arrays.copyOf(solutionData, numCLB * blockSize);
        List<Integer> outputIndices = new ArrayList<>();

        for (int i = 0; i < boolVector.getNumFunctions(); i++) {
            outputIndices.add(solutionData[numCLB * blockSize + i]);
        }

        int numCLBInputs = clbController.getNumCLBInputs();

        return new BlockConfiguration(numCLBInputs, numCLB, newData, outputIndices);
    }

    public BooleanVector getBoolVector() {
        return boolVector;
    }

    public CLBController getClbController() {
        return clbController;
    }

    public static BlockConfiguration bruteSolve(BooleanFunction func, int numCLBInputs) {
        Utility.checkNull(func, "boolean function");
        Utility.checkLimit(Constants.NUM_CLB_INPUTS_LIMIT, numCLBInputs);

        if (numCLBInputs == 2) {
            return bruteSolveForTwoInputs(func);
        }

        MultiplexerData multiplexerData = new MultiplexerData(numCLBInputs);
        int truthTableSize = func.getNumInputCombinations();
        int numCLBRatio;
        int depth = 0;
        int numCLBInDepth = 1;
        int numCLB = 1;

        while (true) {
            numCLBRatio = numCLBInDepth * multiplexerData.sectorSize / truthTableSize;

            if (numCLBRatio >= 1) {
                if (numCLBRatio != 1) {
                    numCLB -= (numCLB - 1) / numCLBRatio;
                }
                break;
            }

            depth++;
            numCLBInDepth *= multiplexerData.numBranchingCLBs;
            numCLB += numCLBInDepth;
        }

        CLBController clbController = new CLBController(func.getNumInputs(), numCLBInputs, numCLB);
        int[] data = new int[numCLB * clbController.getIntsPerCLB()];

        recursiveFill(clbController, multiplexerData, RNG.getRNG(), func.getTruthTable(), data, 0, depth, 1, numCLBRatio);

        return new BlockConfiguration(numCLBInputs, numCLB, data, Collections.singletonList(numCLB - 1));
    }

    private static void recursiveFill(CLBController clbController, MultiplexerData multiplexerData, IRNG random,
                                      BitSet truthTable, int[] data, int depth, int maxDepth, int treeIndex, int numCLBRatio) {

        int indexCLB = clbController.getNumCLB() - treeIndex;
        int offsetInData = clbController.calcCLBOffset(indexCLB);
        int numInputsToIgnore = multiplexerData.numExcessInputs;
        int numNonBranchingInputs = multiplexerData.numExtraInputs;
        int numBranchingCLB = multiplexerData.numBranchingCLBs;
        int numExtraInputsToIgnore = 0;

        if (depth == 0 && numCLBRatio != 1) {
            numExtraInputsToIgnore = (int)(Math.log(numCLBRatio) / Math.log(2));
            numInputsToIgnore += numExtraInputsToIgnore;
            numNonBranchingInputs -= numExtraInputsToIgnore;
            numBranchingCLB /= numCLBRatio;
        }

        for (int i = 0; i < numInputsToIgnore; i++) {
            data[offsetInData] = random.nextInt(0, clbController.getNumInputs());
            offsetInData++;
        }

        for (int i = 0; i < numNonBranchingInputs; i++) {
            if (depth == 0) {
                data[offsetInData] = i;
            } else {
                data[offsetInData] = depth * numNonBranchingInputs + i - numCLBRatio;
            }
            offsetInData++;
        }

        if (depth != maxDepth) {
            for (int i = 0; i < Math.max(1, numCLBRatio); i++) {
                int indexBranchingCLB = (treeIndex) * numBranchingCLB + 1;

                if (depth != 0 && numCLBRatio != 0) {
                    indexBranchingCLB -= 1 << numCLBRatio;
                }

                for (int j = 0; j < numBranchingCLB; j++) {
                    data[offsetInData] = clbController.getNumCLB() - indexBranchingCLB + clbController.getNumInputs();

                    if (i == 0) {
                        if (depth == 0) {
                            recursiveFill(clbController, multiplexerData, random, truthTable, data, depth + 1,
                                    maxDepth, indexBranchingCLB, numExtraInputsToIgnore);
                        } else {
                            recursiveFill(clbController, multiplexerData, random, truthTable, data, depth + 1,
                                    maxDepth, indexBranchingCLB, numCLBRatio);
                        }
                    }

                    offsetInData++;
                    indexBranchingCLB--;
                }
            }

            System.arraycopy(multiplexerData.data, 0, data, offsetInData, clbController.getIntsPerLUT());
        } else {
            int input = (depth + 1) * multiplexerData.numExtraInputs - numCLBRatio;

            for (int i = 0; i < multiplexerData.numBranchingCLBs; i++) {
                data[offsetInData] = input;
                offsetInData++;
                input++;
            }

            int startIndex = (clbController.getNumCLB() - treeIndex) * multiplexerData.sectorSize;
            int[] tableData = BoolFuncController.bitSetToArray(truthTable, startIndex,
                    startIndex + multiplexerData.sectorSize,
                    multiplexerData.numBitsInTable / multiplexerData.sectorSize);

            System.arraycopy(tableData, 0, data, offsetInData, tableData.length);
        }
    }

    private static BlockConfiguration bruteSolveForTwoInputs(BooleanFunction func) {
        BitSet truthTable = func.getTruthTable();
        int numFuncInputs = func.getNumInputs();
        int maxDepth = numFuncInputs - 2;
        int numCLBForNegations = numFuncInputs - 2;
        int numCLBForTableStorage = func.getNumInputCombinations() / 4;
        int numCLB = numCLBForNegations + numCLBForTableStorage + 3 * (int)(Math.pow(2, maxDepth) - 1);
        int[] data = new int[numCLB * 3];

        for (int i = 0; i < numFuncInputs - 2; i++) {
            int offsetCLB = i * 3;
            data[offsetCLB] = i;
            data[offsetCLB + 1] = i;
            data[offsetCLB + 2] = 0b1100;
        }

        int offset = numCLBForNegations * 3;
        for (int i = 0; i < numCLBForTableStorage; i++) {
            data[offset++] = numFuncInputs - 2;
            data[offset++] = numFuncInputs - 1;
            data[offset++] = BoolFuncController.bitSetToArray(truthTable, i * 4, (i + 1) * 4, 1)[0];

            data[offset - 1 + (numCLBForTableStorage + i / 2) * 3 - 1] = numFuncInputs + numCLBForNegations + i;
        }

        CLBController clbController = new CLBController(numFuncInputs, 2, numCLB);
        recursiveTwoInputFill(clbController, data, 0, maxDepth, 1, numCLB - 1);

        return new BlockConfiguration(2, numCLB, data, Collections.singletonList(numCLB - 1));
    }

    private static void recursiveTwoInputFill(CLBController clbController, int[] data,
                                              int depth, int maxDepth, int indexTree, int indexCLB) {

        int offsetData = indexCLB * 3 + 2;

        data[offsetData--] = 0b0111;
        data[offsetData--] = indexCLB - 1 + clbController.getNumInputs();
        data[offsetData--] = indexCLB - 2 + clbController.getNumInputs();

        int indexChildInTree;
        int indexChildCLB;

        for (int i = 0; i < 2; i++) {
            indexChildInTree = indexTree * 2 + i;
            data[offsetData--] = 0b0001;
            indexChildCLB = clbController.getNumCLB() - (indexChildInTree * 3) + 2;
            if (depth + 1 < maxDepth) {
                data[offsetData] = indexChildCLB + clbController.getNumInputs();
                recursiveTwoInputFill(clbController, data, depth + 1, maxDepth, indexChildInTree, indexChildCLB);
            }
            offsetData--;
            data[offsetData--] = depth + i * clbController.getNumInputs();
        }
    }
}
