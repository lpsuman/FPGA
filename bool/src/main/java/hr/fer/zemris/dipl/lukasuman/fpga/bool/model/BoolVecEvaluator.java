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
    private BooleanVector vector;
    private CLBController controller;
    private boolean useStructureFitness;
    private boolean saveCLBOutputs;

    private BitSet[] perCLBFullOutputs;
    private BitSet perCLBCurrentOutput;
    private int[][] numMatchingOutputs;
    private int[] bestMatchingCounts;
    private BitSet[] blockUsage;
    private BitSet unusedBlocks;

    public BoolVecEvaluator(BoolVecProblem problem, boolean useStructureFitness) {
        this.problem = Utility.checkNull(problem, "problem");
        this.vector = problem.getBoolVector();
        this.controller = problem.getClbController();
        this.useStructureFitness = useStructureFitness;
        controller.addCLBChangeListener(this);
        bestMatchingCounts = new int[vector.getNumFunctions()];
        updateDataStructures();
        saveCLBOutputs = false;
    }

    public BoolVecEvaluator(BoolVecProblem problem) {
        this(problem, Constants.USE_STRUCTURE_FITNESS);
    }

    private void updateDataStructures() {
        int numCLB = controller.getNumCLB();
        perCLBCurrentOutput = new BitSet(numCLB);

        int numFunctions = vector.getNumFunctions();
        numMatchingOutputs = new int[numCLB][numFunctions];

        int numInputs = controller.getNumInputs();
        blockUsage = Utility.newBitSetArray(numFunctions, numInputs + numCLB);
        unusedBlocks = new BitSet(numInputs + numCLB);
    }

    public void setSaveCLBOutputs(boolean saveCLBOutputs) {
        this.saveCLBOutputs = saveCLBOutputs;
    }

    @Override
    public double evaluateSolution(Solution<int[]> solution, boolean allowTermination) {
        int[] data = solution.getData();
        int numFunctions = vector.getNumFunctions();
        int numCLB = controller.getNumCLB();
        int numInputCombinations = vector.getNumInputCombinations();
        int numInputs = controller.getNumInputs();
        int numCLBInputs = controller.getNumCLBInputs();

        calculateNumMatchingOutputs(data, numFunctions, numCLB, numInputCombinations, numInputs, numCLBInputs);
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

    private void calculateNumMatchingOutputs(int[] data, int numFunctions, int numCLB, int numInputCombinations, int numInputs, int numCLBInputs) {
        if (saveCLBOutputs) {
            perCLBFullOutputs = Utility.newBitSetArray(numCLB, numInputCombinations);
        }

        if (numMatchingOutputs.length != numCLB) {
            updateDataStructures();
        }

        for (int i = 0; i < numCLB; i++) {
            Arrays.fill(numMatchingOutputs[i], 0);
        }

        if (enableLogging) {
            for (int i = 0, n = numInputs + numCLB; i < n; i++) {
                if (i == controller.getNumInputs()) {
                    logPadding();
                }

                final int iFinal = i;
                log(() -> Utility.paddedString(Integer.toString(iFinal), Constants.BOOL_VECTOR_PRINT_CELL_SIZE));
            }

            logPadding();

            for (int i = 0; i < numFunctions; i++) {
                final int iFinal = i;
                log(() -> Utility.paddedString("F" + iFinal, Constants.BOOL_VECTOR_PRINT_CELL_SIZE));
                //            log(() -> Utility.paddedString(vector.getBoolFunctions().get(iFinal).getName(), Constants.BOOL_VECTOR_PRINT_CELL_SIZE));
            }

            log(() -> "\n\n");
        }

        for (int inputCombination = 0; inputCombination < numInputCombinations; inputCombination++) {
            if (enableLogging) {
                final int inputCombinationFinal = inputCombination;
                log(() -> Utility.toBinaryString(inputCombinationFinal, vector.getNumInputs(),
                        Constants.BOOL_VECTOR_PRINT_CELL_SIZE - 1));
                logPadding();
            }
            BitSet[] truthTables = vector.getTruthTable();

            for (int k = 0; k < numCLB; ++k) {
                boolean outputCLB = calcCLBOutput(inputCombination, data, k, numInputs, numCLBInputs);
                perCLBCurrentOutput.set(k, outputCLB);

                if (saveCLBOutputs) {
                    perCLBFullOutputs[k].set(inputCombination, outputCLB);
                }

                if (enableLogging) log(() -> Utility.paddedChar(outputCLB ? '1' : '0', Constants.BOOL_VECTOR_PRINT_CELL_SIZE));

                for (int j = 0; j < numFunctions; ++j) {
                    if (outputCLB == truthTables[j].get(inputCombination)) {
                        numMatchingOutputs[k][j]++;
                    }
                }
            }

            if (enableLogging) {
                logPadding();
                for (int j = 0; j < numFunctions; ++j) {
                    final int inputCombinationFinal = inputCombination;
                    final int jFinal = j;
                    log(() -> Utility.paddedChar(truthTables[jFinal].get(inputCombinationFinal) ? '1' : '0',
                            Constants.BOOL_VECTOR_PRINT_CELL_SIZE));
                }

                log(() -> "\n");
            }
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
                    indexBestMatchingOutput = j + vector.getNumInputs();
                }
            }

            if (bestMatchingCounts[i] == numInputCombinations) {
                numPerfectMatching++;
            }


            data[numCLB * controller.getIntsPerCLB() + i] = indexBestMatchingOutput;
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
        int numInputs = controller.getNumInputs();
        unusedBlocks.set(0, numInputs + numCLB);

//        if (enableLogging) {
//            System.out.println(String.format("Num of functions: %d\nNum of inputs: %d\nNum of CLB: %d", numFunctions, numInputs, numCLB));
//        }

        for (int i = 0; i < numFunctions; i++) {
            blockUsage[i].clear();
            int indexBestMatchingOutput = data[numCLB * controller.getIntsPerCLB() + i];

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

                int offsetCLB = controller.calcCLBOffset(indexCLB - numInputs);

//                if (enableLogging) {
//                    System.out.println("CLB offset: " + offsetCLB);
//                }

                for (int j = 0, m = controller.getNumCLBInputs(); j < m; ++j) {
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

    private boolean calcCLBOutput(int inputCombination, int[] data, int indexCLB, int numInputs, int numCLBInputs) {
        int offset = controller.calcCLBOffset(indexCLB);
        int extendedIndex = 0;

        for (int i = 0; i < numCLBInputs; i++) {
            extendedIndex <<= 1;
            int inputID = data[offset + i];

            if (inputID >= numInputs) {
                if (perCLBCurrentOutput.get(inputID - numInputs)) {
                    extendedIndex++;
                }
            } else {
//                if (CLBController.testInputBit(inputCombination, numInputs, inputID)) {
                if ((inputCombination & (1 << (numInputs - 1 - inputID))) != 0) {
                    extendedIndex++;
                }
            }
        }

        return controller.readLUT(data, indexCLB, extendedIndex);
    }

    public BitSet[] getBlockUsage() {
        return blockUsage;
    }

    public BitSet getUnusedBlocks() {
        return unusedBlocks;
    }

    public BitSet getUnusedCLBBlocks() {
        BitSet result = (BitSet) unusedBlocks.clone();

        for (int i = 0; i < controller.getNumInputs(); i++) {
            result.set(i, false);
        }

        return result;
    }

    public BitSet[] getPerCLBFullOutputs() {
        return perCLBFullOutputs;
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
