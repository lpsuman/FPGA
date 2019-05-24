package hr.fer.zemris.dipl.lukasuman.fpga.bool.solver;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.AbstractNameHandler;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BlockConfiguration;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanFunction;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanVector;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class BoolVectorSolution extends AbstractNameHandler implements Serializable {

    private static final long serialVersionUID = 2274732589717742103L;

    private static final String DEFAULT_NAME = "VectorSolution";
    private static final String BLOCK_CONFIG_MSG = "block configuration";

    private BooleanVector boolVector;
    private BlockConfiguration blockConfiguration;

    public BoolVectorSolution(BooleanVector boolVector, BlockConfiguration blockConfiguration, String name) {
        super(name);
        this.boolVector = Utility.checkNull(boolVector, "boolean vector");
        this.blockConfiguration = Utility.checkNull(blockConfiguration, BLOCK_CONFIG_MSG);
    }

    public BoolVectorSolution(BooleanVector boolVector, BlockConfiguration blockConfiguration) {
        this(boolVector, blockConfiguration, DEFAULT_NAME);
    }

    public BooleanVector getBoolVector() {
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
        String recursiveResult = recursiveBlockToString(indexFuncOutputBlock, perBlockStrings);

        if (FuncToExpressionConverter.isEnclosedInParentheses(
                blockConfiguration.getData()[(indexFuncOutputBlock - boolVector.getNumInputs()) * 3 + 2])) {
            recursiveResult = recursiveResult.substring(1, recursiveResult.length() - 1);
        }

        return recursiveResult;
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
        if (!checkIfCompatible(solutions)) {
            throw new IllegalArgumentException("Can't merge incompatible solutions.");
        }
        int numSolutions = solutions.size();
        if (numSolutions == 1) {
            throw new IllegalArgumentException("At least two solutions are required for merging.");
        }

        List<BooleanFunction> mergedBoolFuncs = solutions.stream()
                .map(solution -> solution.boolVector.getBoolFunctions())
                .flatMap(List::stream)
                .collect(Collectors.toList());

        BooleanVector mergedVector = new BooleanVector(mergedBoolFuncs);
        List<String> mergedInputIDs = mergedVector.getSortedInputIDs();
        int numMergedInputs = mergedInputIDs.size();

        List<int[]> inputMappedData = mapCLBInputsToMerged(solutions, mergedInputIDs);
        List<int[]> indexBlockInMerged = new ArrayList<>();
        solutions.forEach(s -> indexBlockInMerged.add(new int[s.blockConfiguration.getNumCLB()]));
        for (int[] indices : indexBlockInMerged) {
            for (int i = 0; i < indices.length; i++) {
                indices[i] = -1;
            }
        }

        int blockSize = solutions.get(0).blockConfiguration.getBlockSize();
        int numCLBMerged = solutions.stream().mapToInt(solution -> solution.blockConfiguration.getNumCLB()).sum();
        int[] mergedData = new int[numCLBMerged * blockSize];
        int currIndexInMergedData = 0;

        Set<Integer> remainingSolutions = Utility.generateRangeSet(0, numSolutions);
        int[] currIndicesInSolutions = new int[numSolutions];
        int currentTier = 0;
        int numCLBInputs = solutions.get(0).blockConfiguration.getNumCLBInputs();


        while (!remainingSolutions.isEmpty()) {
            Iterator<Integer> remainingSolutionsIterator = remainingSolutions.iterator();

            while (remainingSolutionsIterator.hasNext()) {
                int indexCurrSolution = remainingSolutionsIterator.next();
                int[] blockData = inputMappedData.get(indexCurrSolution);
                int currIndexBlock = currIndicesInSolutions[indexCurrSolution];
                int leftMostNotUsed = -1;

                outer:
                while (true) {
                    int blockOffset = currIndexBlock * blockSize;
                    if (blockOffset >= blockData.length) {
                        break;
                    }

                    if (indexBlockInMerged.get(indexCurrSolution)[currIndexBlock] != -1) {
                        currIndexBlock++;
                        continue;
                    }

                    for (int i = 0; i < numCLBInputs; i++) {
                        int input = blockData[blockOffset + i];
                        if (input > currentTier) {
                            if (leftMostNotUsed == -1) {
                                leftMostNotUsed = currIndexBlock;
                            }
                            currIndexBlock++;
                            continue outer;
                        }
                    }

                    int mergedDataCurrBlockOffset = currIndexInMergedData * blockSize;

                    for (int i = 0; i < numCLBInputs; i++) {
                        int input = blockData[blockOffset + i];

                        if (input < numMergedInputs) {
                            mergedData[mergedDataCurrBlockOffset + i] = input;
                        } else {
                            mergedData[mergedDataCurrBlockOffset + i] =
                                    indexBlockInMerged.get(indexCurrSolution)[input - numMergedInputs] + numMergedInputs;
                        }
                    }

                    System.arraycopy(blockData, blockOffset + numCLBInputs, mergedData,
                            mergedDataCurrBlockOffset + numCLBInputs, blockSize - numCLBInputs);
                    indexBlockInMerged.get(indexCurrSolution)[currIndexBlock] = currIndexInMergedData;
                    currIndexInMergedData++;

                    currIndexBlock++;
                }

                if (leftMostNotUsed == -1) {
                    remainingSolutionsIterator.remove();
                } else {
                    currIndicesInSolutions[indexCurrSolution] = leftMostNotUsed;
                }
            }

            currentTier++;
        }

        List<Integer> mergedOutputIndices = new ArrayList<>();

        for (int i = 0; i < solutions.size(); i++) {
            BoolVectorSolution currSolution = solutions.get(i);
            List<Integer> outputIndices = currSolution.blockConfiguration.getOutputIndices();
            int deltaNumInputs = mergedInputIDs.size() - currSolution.boolVector.getNumInputs();

            for (Integer outputIndex : outputIndices) {
                mergedOutputIndices.add(indexBlockInMerged.get(i)[outputIndex + deltaNumInputs - numMergedInputs]);
            }
        }

        return new BoolVectorSolution(mergedVector,
                new BlockConfiguration(numCLBInputs, numCLBMerged, mergedData, mergedOutputIndices));
    }

    private static List<int[]> mapCLBInputsToMerged(List<BoolVectorSolution> solutions, List<String> mergedInputIDs) {
        Utility.checkIfValidCollection(solutions, "solutions");
        Utility.checkIfValidCollection(mergedInputIDs, "merged input IDs");
        List<int[]> result = new ArrayList<>();

        for (BoolVectorSolution solution : solutions) {
            List<String> inputIDs = solution.boolVector.getSortedInputIDs();
            int numInputs = inputIDs.size();
            int deltaNumInputs = mergedInputIDs.size() - numInputs;
            BlockConfiguration blockConfiguration = solution.blockConfiguration;
            int[] data = Arrays.copyOf(blockConfiguration.getData(), blockConfiguration.getData().length);
            int blockSize = blockConfiguration.getBlockSize();

            for (int i = 0, n = blockConfiguration.getNumCLB(); i < n; i++) {
                for (int j = 0, m = blockConfiguration.getNumCLBInputs(); j < m; ++j) {
                    int inputIndex = i * blockSize + j;

                    if (data[inputIndex] < numInputs) {
                        int indexInMerged = mergedInputIDs.indexOf(inputIDs.get(data[inputIndex]));
                        data[inputIndex] = indexInMerged;
                    } else {
                        data[inputIndex] += deltaNumInputs;
                    }
                }
            }

            result.add(data);
        }

        return result;
    }

    public static boolean checkIfCompatible(List<BoolVectorSolution> solutions) {
        try {
            Utility.checkIfValidCollection(solutions, "list of solutions");
        } catch (IllegalArgumentException e) {
            return false;
        }

        BoolVectorSolution firstSolution = solutions.get(0);

        for (int i = 1, n = solutions.size(); i < n; i++) {
            if (!firstSolution.blockConfiguration.isCompatibleWith(solutions.get(i).blockConfiguration)) {
                return false;
            }
        }

        return true;
    }
}
