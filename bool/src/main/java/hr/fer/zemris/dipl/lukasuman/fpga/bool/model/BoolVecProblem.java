package hr.fer.zemris.dipl.lukasuman.fpga.bool.model;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BlockConfiguration;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BoolFunc;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BoolFuncController;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BoolVector;
import hr.fer.zemris.dipl.lukasuman.fpga.rng.IRNG;
import hr.fer.zemris.dipl.lukasuman.fpga.rng.RNG;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;
import jdk.jshell.execution.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.function.Supplier;

public class BoolVecProblem implements Supplier<BoolVecSolution> {

    private BoolVector boolVector;
    private CLBController clbController;

    public BoolVecProblem(BoolVector boolVector, int numCLBInputs) {
        this.boolVector = Utility.checkNull(boolVector, "boolean vector");
        clbController = new CLBController(boolVector, numCLBInputs);
    }

    @Override
    public BoolVecSolution get() {
        int numCLB = clbController.getNumCLB();
        int[] data = new int[numCLB * clbController.getIntsPerCLB() + boolVector.getNumFunctions()];
        IRNG random = RNG.getRNG();

        for (int i = 0; i < numCLB; ++i) {
            int offset = clbController.calcCLBOffset(i);
            int numCLBInputs = clbController.getNumCLBInputs();

            for (int j = 0; j < numCLBInputs; ++j) {
                data[offset + j] = clbController.calcRandomInput(i);
            }

            clbController.randomizeTable(data, i, random);
        }

        int numFunctions = boolVector.getNumFunctions();
        for (int i = 0; i < numFunctions; ++i) {
            data[data.length - numFunctions + i] = random.nextInt(0, boolVector.getNumInputs() + numCLB);
        }

        return new BoolVecSolution(data);
    }

    public static BoolVecProblem generateRandomProblem(int numFunctions, int numInputs, int numCLBInputs) {
        List<BoolFunc> boolFuncs = new ArrayList<>();
        for (int i = 0; i < numFunctions; ++i) {
            boolFuncs.add(BoolFuncController.generateRandomFunction(numInputs));
        }

        return new BoolVecProblem(new BoolVector(boolFuncs), numCLBInputs);
    }

    public String solutionToString(BoolVecSolution solution, BitSet[] blockUsage) {
        Utility.checkNull(solution, "solution");
        int[] data = solution.getData();
        int sizeCLB = clbController.getIntsPerCLB();
        int numCLB = (data.length - boolVector.getNumFunctions()) / sizeCLB;
        List<String> sortedIDs = boolVector.getSortedInputIDs();
        StringBuilder sb = new StringBuilder();

        for (int i = 0, n = sortedIDs.size(); i < n; ++i) {
            if (blockUsage != null) {
                printBlockUsage(sb, blockUsage, i);
            }
            sb.append(String.format(Constants.SOLUTION_PRINT_FORMAT + "%s\n", i, sortedIDs.get(i)));
        }

        for (int i = 0; i < numCLB; ++i) {
            if (blockUsage != null) {
                printBlockUsage(sb, blockUsage, i + boolVector.getNumInputs());
            }
            sb.append(String.format(Constants.SOLUTION_PRINT_FORMAT, i + boolVector.getNumInputs()));
            int numCLBInputs = clbController.getNumCLBInputs();

            for (int j = 0; j < numCLBInputs; ++j) {
                sb.append(String.format("%4d", data[i * sizeCLB + j]));
                if (j < numCLBInputs - 1) {
                    sb.append(' ');
                }
            }

//            sb.append('\n');
//            sb.append(String.format(Constants.SOLUTION_PRINT_FORMAT, i + boolVector.getNumInputs()));
            sb.append("    ");

            for (int j = 0; j < clbController.getIntsPerLUT(); ++j) {
                int intValue = data[i * sizeCLB + numCLBInputs + j];
                int numUsedBits = 32;
                if (j == 0) {
                    numUsedBits = clbController.getUsedBitsInFirstInt();
                }
                sb.append(toBinaryString(intValue, numUsedBits));
            }

            sb.append('\n');
        }

        for (int i = 0; i < boolVector.getNumFunctions(); ++i) {
            sb.append(String.format("F%d at %4d", i, data[numCLB * sizeCLB + i]));
            sb.append('\n');
        }

        return sb.toString();
    }

    private void printBlockUsage(StringBuilder sb, BitSet[] usage, int blockIndex) {
        sb.append(" |  ");
        for (BitSet bitSet : usage) {
            sb.append(bitSet.get(blockIndex) ? "1 " : "0 ");
        }
        sb.append(" | ");
    }

    public static String toBinaryString(int value, int numBits) {
        return String.format("%" + numBits + "s", Integer.toBinaryString(value)).replace(' ', '0');
    }

    public static String toBinaryString(int value, int numBits, int numSpaces) {
        StringBuilder sb = new StringBuilder();
        String strWithoutSpaces = toBinaryString(value, numBits);
        if (numSpaces < 1) {
            return strWithoutSpaces;
        }

        for (int i = 0, n = strWithoutSpaces.length(); i < n; ++i) {
            sb.append(Utility.paddedChar(strWithoutSpaces.charAt(i), Constants.BOOL_VECTOR_PRINT_CELL_SIZE));
        }

        return sb.toString();
    }

    public BlockConfiguration generateBlockConfiguration(BoolVecSolution solution) {
        Utility.checkNull(solution, "solution");
        int numCLB = clbController.getNumCLB();
        int numCLBInputs = clbController.getNumCLBInputs();
        int blockSize = clbController.getIntsPerCLB();
        int numFunctions = boolVector.getNumFunctions();
        int[] solutionData = solution.getData();

        if (solutionData.length != numCLB * blockSize + numFunctions) {
            throw new IllegalArgumentException(String.format("Invalid solution (data size %d) for configuration:\n%s",
                    solutionData.length, clbController));
        }

        int[] newData = Arrays.copyOf(solutionData, numCLB * blockSize);
        List<Integer> outputIndices = new ArrayList<>();

        for (int i = 0; i < numFunctions; i++) {
            outputIndices.add(solutionData[numCLB * blockSize + i]);
        }

        return new BlockConfiguration(numCLB, numCLBInputs, blockSize, newData, outputIndices);
    }

    public BoolVector getBoolVector() {
        return boolVector;
    }

    public CLBController getClbController() {
        return clbController;
    }
}