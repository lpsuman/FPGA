package hr.fer.zemris.dipl.lukasuman.fpga.bool.func;

import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.io.Serializable;
import java.util.List;

/**
 * This class represents a configuration of CLBs. It is used as a data storage and can't be evaluated by itself.
 */
public class BlockConfiguration implements Serializable {

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
}
