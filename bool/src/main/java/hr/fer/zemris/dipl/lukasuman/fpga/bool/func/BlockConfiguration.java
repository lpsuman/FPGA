package hr.fer.zemris.dipl.lukasuman.fpga.bool.func;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.CLBController;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.solver.FuncToExpressionConverter;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.IntArraySolution;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.Solution;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Duplicateable;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

/**
 * This class represents a configuration of CLBs. It is used as a data storage and can't be evaluated by itself.
 */
public class BlockConfiguration implements Serializable, Duplicateable<BlockConfiguration> {

    private static final long serialVersionUID = 4470315848595252310L;

    private int numCLB;
    private int numCLBInputs;
    private int blockSize;
    private int[] data;

    private List<Integer> outputIndices;

    public BlockConfiguration(int numCLBInputs, int numCLB, int[] data, List<Integer> outputIndices) {
        this.data = Utility.checkNull(data, "CLB data");
        this.outputIndices = Utility.checkIfValidCollection(outputIndices, "list of output indices");
        this.blockSize = numCLBInputs + (int) Math.ceil(Math.pow(2, numCLBInputs) / 32.0);

        if (this.data.length % blockSize != 0) {
            throw new IllegalArgumentException(String.format(
                    "Data array size (%d) must be divisible by the block size (%d).", this.data.length, blockSize));
        }

        if (numCLB * blockSize != this.data.length) {
            throw new IllegalArgumentException(String.format(
                    "Invalid block configuration (%d blocks of size %d for array of size %d).",
                    numCLB, blockSize, this.data.length));
        }

        if (blockSize - numCLBInputs < 1) {
            throw new IllegalArgumentException(String.format(
                    "Invalid block configuration (%d inputs for blocks of size %d).", numCLBInputs, blockSize));
        }

        this.numCLB = numCLB;
        this.numCLBInputs = numCLBInputs;
    }

    public BlockConfiguration(BlockConfiguration other) {
        this(other.numCLBInputs, other.numCLB, Arrays.copyOf(other.data, other.data.length), new ArrayList<>(other.outputIndices));
    }

    public int getNumCLB() {
        return numCLB;
    }

    public int getNumCLBInputs() {
        return numCLBInputs;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public int[] getData() {
        return data;
    }

    public List<Integer> getOutputIndices() {
        return outputIndices;
    }

    public boolean isCompatibleWith(BlockConfiguration other) {
        return numCLBInputs == other.numCLBInputs;
    }

    private static void appendBlockUsage(StringBuilder sb, BitSet[] usage, int blockIndex) {
        sb.append(" |  ");
        for (BitSet bitSet : usage) {
            sb.append(bitSet.get(blockIndex) ? "1 " : "0 ");
        }
        sb.append(" | ");
    }

    public static void appendFormattedInputData(StringBuilder sb, List<String> inputIDs, BitSet[] blockUsage) {
        for (int i = 0, n = inputIDs.size(); i < n; i++) {
            if (blockUsage != null) {
                appendBlockUsage(sb, blockUsage, i);
            }
            sb.append(String.format(Constants.SOLUTION_PRINT_FORMAT + "%s\n", i, inputIDs.get(i)));
        }
    }

    public static void appendFormattedCLBData(StringBuilder sb, int numInputs, int numCLB, int numCLBInputs,
                                              int blockSize, int[] data, BitSet[] blockUsage) {

        for (int i = 0; i < numCLB; i++) {
            if (blockUsage != null) {
                appendBlockUsage(sb, blockUsage, i + numInputs);
            }
            sb.append(String.format(Constants.SOLUTION_PRINT_FORMAT, i + numInputs));

            for (int j = 0; j < numCLBInputs; ++j) {
                sb.append(String.format("%4d", data[i * blockSize + j]));
                if (j < numCLBInputs - 1) {
                    sb.append(' ');
                }
            }

//            sb.append('\n');
//            sb.append(String.format(Constants.SOLUTION_PRINT_FORMAT, i + boolVector.getNumInputs()));
            sb.append(String.format("%" + Constants.BOOL_VECTOR_PRINT_CELL_SIZE + "s", ""));
            int sizeOfLUT = blockSize - numCLBInputs;

            for (int j = 0; j < sizeOfLUT; ++j) {
                int intValue = data[i * blockSize + numCLBInputs + j];
                int numUsedBits = 32;
                if (j == 0) {
                    numUsedBits = (int) Math.pow(2, numCLBInputs) - 32 * (sizeOfLUT - 1);
                }
                sb.append(Utility.toBinaryString(intValue, numUsedBits));
            }

            if (numCLBInputs == 2) {
                sb.append(String.format("%" + Constants.BOOL_VECTOR_PRINT_CELL_SIZE + "s", ""));

                sb.append(FuncToExpressionConverter.getUnenclosedString(data[i * blockSize + 2],
                        Integer.toString(data[i * blockSize]),
                        Integer.toString(data[i * blockSize + 1])));
            }

            sb.append('\n');
        }
    }

    public static void appendFormattedCLBData(StringBuilder sb, CLBController clbController, int[] data, BitSet[] blockUsage) {
        appendFormattedCLBData(sb, clbController.getNumInputs(), clbController.getNumCLB(),
                clbController.getNumCLBInputs(), clbController.getIntsPerCLB(), data, blockUsage);
    }

    public static void appendFormattedOutputData(StringBuilder sb, List<Integer> outputIndices) {
        for (int i = 0, n = outputIndices.size(); i < n; i++) {
            sb.append(String.format("F%d at %4d", i, outputIndices.get(i)));

            if (i < n - 1) {
                sb.append('\n');
            }
        }
    }

    public static void appendFormattedOutputData(StringBuilder sb, int[] outputIndices, int startIndex,
                                                 BooleanVector booleanVector) {

        int funcIndex = 0;
        for (int i = startIndex, n = outputIndices.length; i < n; i++) {
            if (booleanVector != null) {
                sb.append(String.format("%s   at %4d", booleanVector.getBoolFunctions().get(i - startIndex).getName(), outputIndices[i]));
            } else {
                sb.append(String.format("F%d at %4d", funcIndex, outputIndices[i]));
            }
            funcIndex++;

            if (i < n - 1) {
                sb.append('\n');
            }
        }
    }

    public Solution<int[]> getAsFlatArray() {
        int[] flatData = new int[data.length + outputIndices.size()];
        System.arraycopy(data, 0, flatData, 0, data.length);

        for (int i = 0; i < outputIndices.size(); i++) {
            flatData[data.length + i] = outputIndices.get(i);
        }

        return new IntArraySolution(flatData);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("Block Configuration of %d CLBs with %d inputs per CLB:\n", numCLB, numCLBInputs));

        //TODO

        return sb.toString();
    }

    @Override
    public BlockConfiguration getDuplicate() {
        return new BlockConfiguration(this);
    }
}
