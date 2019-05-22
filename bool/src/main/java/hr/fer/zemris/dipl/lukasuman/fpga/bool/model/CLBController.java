package hr.fer.zemris.dipl.lukasuman.fpga.bool.model;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BoolFunc;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BoolVector;
import hr.fer.zemris.dipl.lukasuman.fpga.rng.IRNG;
import hr.fer.zemris.dipl.lukasuman.fpga.rng.RNG;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.util.HashSet;
import java.util.Set;

public class CLBController {

    private int sizeLUT;
    private int intsPerLUT;
    private int intsPerCLB;
    private int usedBitsInFirstInt;
    private int emptyBitsInFirstInt;

    private int numInputs;
    private int numCLBInputs;
    private int numCLB;

    private Set<CLBChangeListener> clbChangeListeners;

    public CLBController(int numInputs, int numCLBInputs, int numCLB) {
        this(numInputs, numCLBInputs);
        setNumCLB(numCLB);
    }

    public CLBController(BoolVector boolVector, int numCLBInputs) {
        this(boolVector.getNumInputs(), numCLBInputs);
        setNumCLB(estimateMaxNumCLBForVector(boolVector));
    }

    private CLBController(int numInputs, int numCLBInputs) {
        this.numInputs = numInputs;
        setNumCLBInputs(numCLBInputs);
    }

    public void setNumCLBInputs(int numCLBInputs) {
        Utility.checkLimit(Constants.NUM_CLB_INPUTS_LIMIT, numCLBInputs);

        if (numCLBInputs > numInputs) {
            throw new IllegalArgumentException(String.format(
                    "Number of CLB inputs (%d given) should be smaller than the number of boolean vector inputs (%d).",
                    numCLBInputs, numInputs));
        }

        int prevNumCLBInputs = this.numCLBInputs;
        this.numCLBInputs = numCLBInputs;
        sizeLUT = (int) Math.pow(2, numCLBInputs);
        intsPerLUT = sizeLUT / 32 + 1;
        intsPerCLB = numCLBInputs + intsPerLUT;
        usedBitsInFirstInt = sizeLUT - 32 * (intsPerLUT - 1);
        emptyBitsInFirstInt = 32 - usedBitsInFirstInt;

        if (clbChangeListeners != null) {
            clbChangeListeners.forEach(l -> l.numCLBInputsChanged(prevNumCLBInputs, this.numCLBInputs));
        }
    }

    public static int calcExtendedIndex(int inputCombination, int[] inputIDs) {
        return calcExtendedIndex(inputCombination, inputIDs, 0, inputIDs.length);
    }

    public static int calcExtendedIndex(int inputCombination, int[] inputIDs, int startIndex, int numInputs) {
        int extendedIndex = 0;
        for (int i = 0; i < numInputs; ++i) {
            if (testInputBit(inputCombination, numInputs, inputIDs[startIndex + i])) {
                extendedIndex++;
            }
            if (i < numInputs - 1) {
                extendedIndex <<= 1;
            }
        }
        return extendedIndex;
    }

    public static boolean testInputBit(int inputCombination, int numInputs, int index) {
        return Utility.testBitFromRight(inputCombination, numInputs - 1 - index);
    }

    public boolean readLUT(int[] data, int indexCLB, int extendedIndex) {
        int offset = calcLUTOffset(indexCLB);
        int bitOffset = extendedIndex + emptyBitsInFirstInt;
        offset += (bitOffset) / 32;
        return Utility.testBitFromLeft(data[offset], bitOffset % 32);
    }

    private int estimateMaxNumCLBForFunc(BoolFunc boolFunc) {
        Utility.checkNull(boolFunc, "boolean function");
        int numFuncInputs = boolFunc.getInputIDs().size();
        if (numCLBInputs >= numFuncInputs) {
            return 1;
        }

        return (int)((Math.pow(2, numFuncInputs - numCLBInputs + 1) - 1) * Constants.NUM_CLB_ESTIMATION_MULTIPLIER);
    }

    private int estimateMaxNumCLBForVector(BoolVector boolVector) {
        return boolVector.getBoolFunctions().stream()
                .mapToInt(this::estimateMaxNumCLBForFunc)
                .sum();
    }

    public void randomizeTable(int[] data, int blockIndex, IRNG random) {
        int offset = calcLUTOffset(blockIndex);

        for (int j = 0; j < getIntsPerLUT(); ++j) {
            if (j == 0) {
                data[offset + j] = random.nextInt(0,
                        1 << getUsedBitsInFirstInt());
            } else {
                data[offset + j] = random.nextInt();
            }
        }
    }

    public void randomizeTableBit(int[] data, int blockIndex, int bitIndex, IRNG random) {
        int offset = calcLUTOffset(blockIndex);

        int intIndex = bitIndex / 32;
        if (intIndex == 0) {
            data[offset] ^= 1 << (usedBitsInFirstInt - bitIndex - 1);
        } else {
            int indexInInt = (bitIndex - usedBitsInFirstInt) % 32;
            data[offset] ^= 1 << (31 - (indexInInt));
        }
    }

    public int calcCLBOffset(int index) {
        return index * intsPerCLB;
    }

    public int calcLUTOffset(int index) {
        return calcCLBOffset(index) + numCLBInputs;
    }

    public static void swapSingleData(int[] firstData, int[] secondData, int firstIndex, int secondIndex) {
        int temp = firstData[firstIndex];
        firstData[firstIndex] = secondData[secondIndex];
        secondData[secondIndex] = temp;
    }

    public static void swapSingleData(int[] data, int firstIndex, int secondIndex) {
        swapSingleData(data, data, firstIndex, secondIndex);
    }

    public void swapSingleCLB(int[] firstData, int[] secondData, int firstIndex, int secondIndex,
                                 boolean switchInputs, boolean switchLUT) {

        int firstOffset = calcCLBOffset(firstIndex);
        int secondOffset = calcCLBOffset(secondIndex);
        int numCLBInputs = getNumCLBInputs();

        if (switchInputs) {
            for (int i = 0; i < numCLBInputs; ++i) {
                swapSingleData(firstData, secondData, firstOffset + i, secondOffset + i);

                if (firstIndex != secondIndex) {
                    if (firstIndex > secondIndex) {
                        checkInput(secondData, secondIndex, i);
                    } else {
                        checkInput(firstData, firstIndex, i);
                    }
                }
            }
        }

        firstOffset += numCLBInputs;
        secondOffset += numCLBInputs;

        if (switchLUT) {
            for (int i = 0, m = getIntsPerLUT(); i < m; ++i) {
                swapSingleData(firstData, secondData, firstOffset + i, secondOffset + i);
            }
        }
    }

    public void swapSingleCLB(int[] firstData, int[] secondData, int firstIndex, int secondIndex) {
        swapSingleCLB(firstData, secondData, firstIndex, secondIndex, true, true);
    }

    public void swapSingleCLB(int[] firstData, int[] secondData, int index,
                                 boolean switchInputs, boolean switchLUT) {

        swapSingleCLB(firstData, secondData, index, index, switchInputs, switchLUT);
    }

    public void swapSingleCLB(int[] firstData, int[] secondData, int index) {
        swapSingleCLB(firstData, secondData, index, true, true);
    }

    public int calcRandomInput(int index) {
        return RNG.getRNG().nextInt(0, numInputs + index);
    }

    public void checkInput(int[] data, int blockIndex, int inputIndex) {
        int maxInputID = numInputs + blockIndex;
        int inputOffset = calcCLBOffset(blockIndex) + inputIndex;
        if (data[inputOffset] >= maxInputID) {
            data[inputOffset] = RNG.getRNG().nextInt(0, maxInputID);
        }
    }

    public int getNumCLBInputs() {
        return numCLBInputs;
    }

    public int getSizeLUT() {
        return sizeLUT;
    }

    public int getIntsPerLUT() {
        return intsPerLUT;
    }

    public int getBitsPerLUT() {
        return usedBitsInFirstInt + 32 * (getIntsPerLUT() - 1);
    }

    public int getIntsPerCLB() {
        return intsPerCLB;
    }

    public int getUsedBitsInFirstInt() {
        return usedBitsInFirstInt;
    }

    public int getEmptyBitsInFirstInt() {
        return emptyBitsInFirstInt;
    }

    public int getNumInputs() {
        return numInputs;
    }

    public int getNumCLB() {
        return numCLB;
    }

    public void setNumCLB(int numCLB) {
        Utility.checkLimit(Constants.NUM_CLB_LIMIT, numCLB);

        if (numCLB == this.numCLB) {
            return;
        }

        int prevNumCLB = this.numCLB;
        this.numCLB = numCLB;

        if (clbChangeListeners != null) {
            clbChangeListeners.forEach(l -> l.numCLBChanged(prevNumCLB, this.numCLB));
        }
    }

    public void addCLBChangeListener(CLBChangeListener clbChangeListener) {
        Utility.checkNull(clbChangeListener, "listener");
        if (clbChangeListeners == null) {
            clbChangeListeners = new HashSet<>();
        }
        clbChangeListeners.add(clbChangeListener);
    }

    public void removeCLBChangeListern(CLBChangeListener clbChangeListener) {
        Utility.checkNull(clbChangeListener, "listener");
        if (clbChangeListeners != null) {
            clbChangeListeners.remove(clbChangeListener);
        }
    }

    @Override
    public String toString() {
        return String.format("Number of CLBs: %d\nNumber of CLB inputs: %d\nSize of each block (integers): %d\n",
                numCLB, numCLBInputs, intsPerCLB);
    }
}
