package hr.fer.zemris.dipl.lukasuman.fpga.bool.model;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.AbstractNameHandler;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BlockConfiguration;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BoolFunc;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BoolFuncController;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BoolVector;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.IntArraySolution;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.Solution;
import hr.fer.zemris.dipl.lukasuman.fpga.rng.IRNG;
import hr.fer.zemris.dipl.lukasuman.fpga.rng.RNG;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BoolVecProblem extends AbstractNameHandler implements Supplier<Solution<int[]>> {

    private static final String DEFAULT_NAME = "BoolProblem";

    private BoolVector boolVector;
    private CLBController clbController;

    private List<Solution<int[]>> nextToSupplyList;
    Solution<int[]> nextToSupply;
    private int indexCurrToSupply;

    public BoolVecProblem(BoolVector boolVector, int numCLBInputs, String name) {
        super(name);
        this.boolVector = Utility.checkNull(boolVector, "boolean vector");
        clbController = new CLBController(boolVector, numCLBInputs);
    }

    public BoolVecProblem(BoolVector boolVector, int numCLBInputs) {
        this(boolVector, numCLBInputs, DEFAULT_NAME);
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
        List<BoolFunc> boolFuncs = new ArrayList<>();
        for (int i = 0; i < numFunctions; i++) {
            boolFuncs.add(BoolFuncController.generateRandomFunction(numInputs));
        }

        return new BoolVecProblem(new BoolVector(boolFuncs), numCLBInputs);
    }

    public String solutionToString(Solution<int[]> solution, BitSet[] blockUsage) {
        Utility.checkNull(solution, "solution");
        int[] data = solution.getData();
        int sizeCLB = clbController.getIntsPerCLB();
        int numCLB = (data.length - boolVector.getNumFunctions()) / sizeCLB;
        List<String> sortedIDs = boolVector.getSortedInputIDs();
        StringBuilder sb = new StringBuilder();



        return sb.toString();
    }

    public String getSolutionTestResults(Solution<int[]> solution, BoolVecEvaluator evaluator) {
        StringBuilder sb = new StringBuilder();

        evaluator.setLogging(true);
        evaluator.evaluateSolution(solution, false);
        sb.append(evaluator.getLog()).append('\n');
        evaluator.resetLog();
        evaluator.setLogging(false);
        sb.append(solutionToString(solution, evaluator.getBlockUsage())).append('\n');
        BitSet[] blockUsage = evaluator.getBlockUsage();

        BlockConfiguration.appendFormattedInputData(sb, boolVector.getSortedInputIDs(), blockUsage);
        BlockConfiguration.appendFormattedCLBData(sb, clbController, solution.getData(), blockUsage);
        List<Integer> outputIndices = Arrays.stream(solution.getData())
                .boxed()
                .collect(Collectors.toList());
        BlockConfiguration.appendFormattedOutputData(sb, outputIndices);

        int numUnusedBlocks = evaluator.getUnusedBlocks().cardinality();
        sb.append(String.format("There were %d unused blocks.\n", numUnusedBlocks)).append('\n');

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

    public BoolVector getBoolVector() {
        return boolVector;
    }

    public CLBController getClbController() {
        return clbController;
    }
}
