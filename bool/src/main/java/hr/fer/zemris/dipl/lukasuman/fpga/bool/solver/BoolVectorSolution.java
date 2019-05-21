package hr.fer.zemris.dipl.lukasuman.fpga.bool.solver;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.AbstractNameHandler;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BlockConfiguration;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BoolFunc;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BoolVector;
import hr.fer.zemris.dipl.lukasuman.fpga.rng.IRNG;
import hr.fer.zemris.dipl.lukasuman.fpga.rng.RNG;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class BoolVectorSolution extends AbstractNameHandler implements Serializable {

    private static final long serialVersionUID = 2274732589717742103L;

    private static final String DEFAULT_NAME = "VectorSolution";
    private static final String BLOCK_CONFIG_MSG = "block configuration";

    private BoolVector boolVector;
    private BlockConfiguration blockConfiguration;

    public BoolVectorSolution(BoolVector boolVector, BlockConfiguration blockConfiguration, String name) {
        super(name);
        this.boolVector = Utility.checkNull(boolVector, "boolean vector");
        this.blockConfiguration = Utility.checkNull(blockConfiguration, BLOCK_CONFIG_MSG);
    }

    public BoolVectorSolution(BoolVector boolVector, BlockConfiguration blockConfiguration) {
        this(boolVector, blockConfiguration, DEFAULT_NAME);
    }

    public BoolVector getBoolVector() {
        return boolVector;
    }

    public BlockConfiguration getBlockConfiguration() {
        return blockConfiguration;
    }

    public void setBlockConfiguration(BlockConfiguration blockConfiguration) {
        this.blockConfiguration = Utility.checkNull(blockConfiguration, BLOCK_CONFIG_MSG);
    }

    public boolean canBeConvertedToExpression() {
        return blockConfiguration.getNumCLBInputs() == 2;
    }

    public String getAsExpression(int indexBoolFunc) {
        if (!canBeConvertedToExpression()) {
            throw new UnsupportedOperationException(
                    "Only solutions with 2 CLB inputs can be converted to an expression.");
        }
        Utility.checkRange(indexBoolFunc, 0, boolVector.getBoolFunctions().size() - 1);

        int indexFuncOutputBlock = blockConfiguration.getOutputIndices().get((indexBoolFunc));
        String[] perBlockStrings = new String[boolVector.getNumInputs() + blockConfiguration.getNumCLB()];

        return recursiveBlockToString(indexFuncOutputBlock, perBlockStrings);
    }

    private String recursiveBlockToString(int indexCLB, String[] perBlockStrings) {
        int numInputs = boolVector.getNumInputs();
        if (indexCLB < numInputs) {
            return boolVector.getSortedInputIDs().get(indexCLB);
        }
        int[] data = blockConfiguration.getData();

        int inputA = data[(indexCLB - numInputs) * 3];
        String leftString = perBlockStrings[inputA];
        if (leftString == null) {
            leftString = recursiveBlockToString(inputA, perBlockStrings);
            perBlockStrings[inputA] = leftString;
        }

        int inputB = data[(indexCLB - numInputs) * 3 + 1];
        String rightString = perBlockStrings[inputB];
        if (rightString == null) {
            rightString = recursiveBlockToString(inputB, perBlockStrings);
            perBlockStrings[inputB] = rightString;
        }

        int truthTable = data[(indexCLB - numInputs) * 3 + 2];
        return FuncToExpressionConverter.getString(truthTable, leftString, rightString);
    }

    public static BoolVectorSolution mergeSolutions(List<BoolVectorSolution> solutions) {
        checkIfCompatible(solutions);
        int numSolutions = solutions.size();

        if (numSolutions == 1) {
            throw new IllegalArgumentException("At least two solutions are required for merging.");
        }

        List<BoolFunc> mergedBoolFuncs = solutions.stream()
                .map(solution -> solution.boolVector.getBoolFunctions())
                .flatMap(List::stream)
                .collect(Collectors.toList());
        BoolVector mergedVector = new BoolVector(mergedBoolFuncs);
        List<String> mergedInputIDs = mergedVector.getSortedInputIDs();

        int numCLBMerged = solutions.stream().mapToInt(solution -> solution.blockConfiguration.getNumCLB()).sum();
        int[] mergedData = new int[numCLBMerged];
        int currIndexInMergedData = 0;
        List<Integer> mergedOutputIndices = new ArrayList<>();

        Set<Integer> remainingSolutions = Utility.generateRangeSet(0, numSolutions);
        int[] currIndicesInSolutions = new int[numSolutions];
        int currentTier = 0;
        IRNG random = RNG.getRNG();

        while (!remainingSolutions.isEmpty()) {
            Iterator<Integer> remainingSolutionsIterator = remainingSolutions.iterator();

            while (remainingSolutionsIterator.hasNext()) {
                int indexCurrSolution = remainingSolutionsIterator.next();
                BlockConfiguration blockConfiguration = solutions.get(indexCurrSolution).blockConfiguration;
                int[] blockData = blockConfiguration.getData();
                int blockSize = blockConfiguration.getBlockSize();
                int blockOffset = currIndicesInSolutions[indexCurrSolution] * blockSize;

                while (true) {
                    boolean areInputsLowerTier = true;

                    for (int i = 0, n = blockConfiguration.getNumCLBInputs(); i < n; ++i) {
                        int input = blockData[blockOffset + i];
                        if (input > currentTier) {
                            areInputsLowerTier = false;
                            break;
                        }
                    }

                    if (!areInputsLowerTier) {
                        break;
                    }

                    //TODO gotta keep going :)
                }
            }

            currentTier++;
        }

        return null;
    }

    private static boolean checkIfCompatible(List<BoolVectorSolution> solutions) {
        Utility.checkIfValidCollection(solutions, "list of solutions");

        BoolVectorSolution firstSolution = solutions.get(0);

        for (int i = 1, n = solutions.size(); i < n; ++i) {
            if (!firstSolution.blockConfiguration.isCompatibleWith(solutions.get(i).blockConfiguration)) {
                return false;
            }
        }

        return true;
    }
}
