package hr.fer.zemris.dipl.lukasuman.fpga.bool.model;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanVector;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.evaluator.AbstractLoggingEvaluator;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.Solution;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.util.Arrays;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.Queue;

public class BoolVecEvaluator extends AbstractLoggingEvaluator<int[]> implements CLBChangeListener {

    private BoolVecProblem problem;
    private boolean useStructureFitness;

    private BitSet perCLBOutputs;
    private int[][] numMatchingOutputs;
    private int[] bestMatchingCounts;
    private BitSet[] blockUsage;
    private BitSet unusedBlocks;

    public BoolVecEvaluator(BoolVecProblem problem, boolean useStructureFitness) {
        this.problem = problem;
        this.useStructureFitness = useStructureFitness;
        getController().addCLBChangeListener(this);
        bestMatchingCounts = new int[getVector().getNumFunctions()];
        updateDataStructures();
    }

    public BoolVecEvaluator(BoolVecProblem problem) {
        this(problem, Constants.USE_STRUCTURE_FITNESS);
    }

    private void updateDataStructures() {
        int numCLB = getController().getNumCLB();
        perCLBOutputs = new BitSet(numCLB);

        int numFunctions = getVector().getNumFunctions();
        numMatchingOutputs = new int[numCLB][numFunctions];

        int numInputs = getController().getNumInputs();
        blockUsage = Utility.newBitSetArray(numFunctions, numInputs + numCLB);
        unusedBlocks = new BitSet(numInputs + numCLB);
    }

    @Override
    public double evaluateSolution(Solution<int[]> solution, boolean allowTermination) {
        int[] data = solution.getData();
        int numFunctions = getVector().getNumFunctions();
        int numCLB = getController().getNumCLB();
        int numInputCombinations = getVector().getNumInputCombinations();

        calculateNumMatchingOutputs(data, numFunctions, numCLB, numInputCombinations);
        int numPerfectMatching = calculateBestMatchingOutputs(data, numFunctions, numCLB, numInputCombinations);

        if (useStructureFitness || enableLogging) {
            calcBlockUsage(data, numFunctions, numCLB);
        }

        double fitness = 0.0;

        if (numPerfectMatching == numFunctions) {
            if (allowTermination) {
                notifyTerminationListeners();
            }

            fitness = Constants.FITNESS_SCALE;
        } else {
            for (int i = 0; i < numFunctions; i++) {
                fitness += bestMatchingCounts[i];
            }

            if (useStructureFitness) {
                double structureFitness = calcStructureFitness(numFunctions, numCLB);
                if (Double.isNaN(structureFitness) || structureFitness < 0.0 || structureFitness >= 1.0) {
                    throw new IllegalStateException("Invalid structure fitness.");
                }
                fitness += structureFitness;
            }

            fitness /= (numFunctions * numInputCombinations);
            fitness *= Constants.FITNESS_SCALE;
        }

//        if (Double.isNaN(fitness)) {
//            throw new IllegalStateException(String.format("Invalid fitness with %d functions and %d input combinations", numFunctions, numInputCombinations));
//        }

        solution.setFitness(fitness);
        notifyFitnessListeners(solution, false);
        numEvaluations++;
        return fitness;
    }

    private void calculateNumMatchingOutputs(int[] data, int numFunctions, int numCLB,
                                             int numInputCombinations) {

        for (int i = 0; i < numCLB; i++) {
            Arrays.fill(numMatchingOutputs[i], 0);
        }

        for (int i = 0, n = getController().getNumInputs() + numCLB; i < n; i++) {
            if (i == getController().getNumInputs()) {
                logPadding();
            }

            final int iFinal = i;
            log(() -> Utility.paddedString(Integer.toString(iFinal), Constants.BOOL_VECTOR_PRINT_CELL_SIZE));
        }

        logPadding();

        for (int i = 0; i < numFunctions; i ++) {
            final int iFinal = i;
            log(() -> Utility.paddedString("F" + iFinal, Constants.BOOL_VECTOR_PRINT_CELL_SIZE));
        }

        log(() -> "\n\n");

        for (int inputCombination = 0; inputCombination < numInputCombinations; inputCombination++) {
            final int inputCombinationFinal = inputCombination;

            log(() -> Utility.toBinaryString(inputCombinationFinal, getVector().getNumInputs(),
                    Constants.BOOL_VECTOR_PRINT_CELL_SIZE - 1));

            logPadding();

            for (int k = 0; k < numCLB; ++k) {
                boolean outputCLB = calcCLBOutput(inputCombination, data, k);
                perCLBOutputs.set(k, outputCLB);

                log(() -> Utility.paddedChar(outputCLB ? '1' : '0', Constants.BOOL_VECTOR_PRINT_CELL_SIZE));

                for (int j = 0; j < numFunctions; ++j) {
                    if (outputCLB == getVector().getTruthTable()[j].get(inputCombination)) {
                        numMatchingOutputs[k][j]++;
                    }
                }
            }

            logPadding();

            for (int j = 0; j < numFunctions; ++j) {
                final int jFinal = j;
                log(() -> Utility.paddedChar(getVector().getTruthTable()[jFinal].get(inputCombinationFinal) ? '1' : '0',
                        Constants.BOOL_VECTOR_PRINT_CELL_SIZE));
            }

            log(() -> "\n");
        }
    }

    private void logPadding() {
        log(() -> Utility.paddedString(Constants.BOOL_VECTOR_PRINT_SEPARATOR, Constants.BOOL_VECTOR_PRINT_CELL_SIZE));
    }

    private int calculateBestMatchingOutputs(int[] data, int numFunctions, int numCLB, int numInputCombinations) {
        int numPerfectMatching = 0;

        for (int i = 0; i < numFunctions; i++) {
            int indexBestMatchingOutput = -1;
            bestMatchingCounts[i] = Integer.MIN_VALUE;

            for (int j = 0; j < numCLB; ++j) {
                if (numMatchingOutputs[j][i] > bestMatchingCounts[i]) {
                    bestMatchingCounts[i] = numMatchingOutputs[j][i];
                    indexBestMatchingOutput = j + getVector().getNumInputs();
                }
            }

            if (bestMatchingCounts[i] == numInputCombinations) {
                numPerfectMatching++;
            }


            data[numCLB * getController().getIntsPerCLB() + i] = indexBestMatchingOutput;
        }

        return numPerfectMatching;
    }

    private double calcStructureFitness(int numFunctions, int numCLB) {
        double fitness = 0.0;

        for (int i = 0; i < numCLB; i++) {
            int numFuncUsingCurrBlock = 0;

            for (int j = 0; j < numFunctions; ++j) {
                if (blockUsage[j].get(i)) {
                    numFuncUsingCurrBlock++;
                }
            }

            if (numFuncUsingCurrBlock == 0) {
                fitness += 1.0;
            } else if (numFuncUsingCurrBlock > 1) {
                fitness += Math.pow(numFuncUsingCurrBlock, 2) / Math.pow(numFunctions, 2);
            }
        }

        return (fitness / numCLB) * Constants.STRUCTURE_FITNESS_SCALE;
    }

    private void calcBlockUsage(int[] data, int numFunctions, int numCLB) {
        int numInputs = getController().getNumInputs();
        unusedBlocks.set(0, numInputs + numCLB);

//        if (enableLogging) {
//            System.out.println(String.format("Num of functions: %d\nNum of inputs: %d\nNum of CLB: %d", numFunctions, numInputs, numCLB));
//        }

        for (int i = 0; i < numFunctions; i++) {
            blockUsage[i].clear();
            int indexBestMatchingOutput = data[numCLB * getController().getIntsPerCLB() + i];

            Queue<Integer> queue = new LinkedList<>();
            queue.add(indexBestMatchingOutput);
            blockUsage[i].set(indexBestMatchingOutput);

            while(!queue.isEmpty()) {
                int indexCLB = queue.poll();

//                if (enableLogging) {
//                    System.out.println("Popped:  " + indexCLB);
//                }

                if (indexCLB < numInputs) {
                    continue;
                }

                int offsetCLB = getController().calcCLBOffset(indexCLB - numInputs);

//                if (enableLogging) {
//                    System.out.println("CLB offset: " + offsetCLB);
//                }

                for (int j = 0, m = getController().getNumCLBInputs(); j < m; ++j) {
                    int input = data[offsetCLB + j];

//                    if (enableLogging) {
//                        System.out.println("Input:    " + input);
////                        System.out.println(blockUsage[i]);
//                    }

                    if (!blockUsage[i].get(input)) {
                        queue.add(input);
                        blockUsage[i].set(input);

//                        if (enableLogging) {
//                            System.out.println("Pushed:   " + (input));
//                        }
                    }
                }
            }

//            if (enableLogging) {
//                System.out.println(String.format("F%d unused inputs and blocks: %s", i, blockUsage[i]));
//            }


            if (i == 0) {
                unusedBlocks.xor(blockUsage[i]);
            } else {
                unusedBlocks.andNot(blockUsage[i]);
            }
        }
    }

    private boolean calcCLBOutput(int inputCombination, int[] data, int indexCLB) {
        int offset = getController().calcCLBOffset(indexCLB);
        int extendedIndex = 0;
        int numInputs = getVector().getNumInputs();

        for (int i = 0; i < getController().getNumCLBInputs(); i++) {
            int inputID = data[offset + i];

            if (inputID >= numInputs) {
                if (perCLBOutputs.get(inputID - numInputs)) {
                    extendedIndex++;
                }
            } else {
                if (CLBController.testInputBit(inputCombination, numInputs, inputID)) {
                    extendedIndex++;
                }
            }

            if (i < getController().getNumCLBInputs() - 1) {
                extendedIndex <<= 1;
            }
        }

        return getController().readLUT(data, indexCLB, extendedIndex);
    }

    private CLBController getController() {
        return problem.getClbController();
    }

    private BooleanVector getVector() {
        return problem.getBoolVector();
    }

    public BitSet[] getBlockUsage() {
        return blockUsage;
    }

    public BitSet getUnusedBlocks() {
        return unusedBlocks;
    }

    @Override
    public void numCLBInputsChanged(int prevNumCLBInputs, int newNumCLBInputs) {
        // do nothing
    }

    @Override
    public void numCLBChanged(int prevNumCLB, int newNumCLB) {
        updateDataStructures();
    }
}
