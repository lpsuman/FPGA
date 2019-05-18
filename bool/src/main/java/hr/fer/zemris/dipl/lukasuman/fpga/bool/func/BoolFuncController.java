package hr.fer.zemris.dipl.lukasuman.fpga.bool.func;

import hr.fer.zemris.dipl.lukasuman.fpga.rng.RNG;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.NoSuchElementException;

public class BoolFuncController {

    private static final String DEFAULT_FUNC_NAME = "func";

    public static List<String> generateDefaultInputIDs(int numInputs) {
        Utility.checkLimit(Constants.NUM_FUNCTION_INPUTS_LIMIT, numInputs);
        List<String> inputIDs = new ArrayList<>(numInputs);

        for (int i = 0; i < numInputs; i++) {
            inputIDs.add("" + (char)('a' + i));
        }

        return inputIDs;
    }

    public static void initDefaultInputIDs(int[] inputIDs) {
        for (int i = 0, n = inputIDs.length; i < n; ++i) {
            inputIDs[i] = i;
        }
    }

    public static BoolFunc generateRandomFunction(int numInputs)  {
        return generateRandomFunction(BoolFuncController.generateDefaultInputIDs(numInputs), numInputs);
    }

    public static BoolFunc generateRandomFunction(List<String> inputIDs, int numInputs) {
        int numInputCombinations = (int) Math.pow(2, numInputs);
        BitSet bitSet = RNG.getRNG().nextBitSet(numInputCombinations);
        return new BoolFunc(DEFAULT_FUNC_NAME, inputIDs, bitSet);
    }

    public static BoolFunc generateFromMask(int mask, int numInputs) {
        return new BoolFunc(DEFAULT_FUNC_NAME, BoolFuncController.generateDefaultInputIDs(numInputs),
                Utility.bitSetFromMask(mask, (int) Math.pow(2, numInputs)));
    }

    public static int calcNumInputs(List<BoolFunc> boolFunctions) {
        return boolFunctions.stream()
                .mapToInt(func -> func.getInputIDs().size())
                .max()
                .orElseThrow(NoSuchElementException::new);
    }
}
