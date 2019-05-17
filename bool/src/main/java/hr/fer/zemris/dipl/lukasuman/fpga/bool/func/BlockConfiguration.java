package hr.fer.zemris.dipl.lukasuman.fpga.bool.func;

import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

public class BlockConfiguration {

    private int numCLB;
    private int numCLBInputs;
    private int blockSize;
    private int[] data;

    public BlockConfiguration(int numCLB, int numCLBInputs, int blockSize, int[] data) {
        Utility.checkNull(data, "CLB data");

        if (data.length % blockSize != 0) {
            throw new IllegalArgumentException(String.format(
                    "Data array size (%d) must be divisible by the block size (%d).", data.length, blockSize));
        }

        if (numCLB * blockSize != data.length) {
            throw new IllegalArgumentException(String.format(
                    "Invalid block configuration (%d blocks of size %d for array of size %d).",
                    numCLB, blockSize, data.length));
        }

        if (blockSize - numCLBInputs < 1) {
            throw new IllegalArgumentException(String.format(
                    "Invalid block configuration (%d inputs for blocks of size %d).", numCLBInputs, blockSize));
        }

        this.numCLB = numCLB;
        this.numCLBInputs = numCLBInputs;
        this.blockSize = blockSize;
        this.data = data;
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
}
