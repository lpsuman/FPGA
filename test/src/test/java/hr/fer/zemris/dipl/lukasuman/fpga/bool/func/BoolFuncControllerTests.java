package hr.fer.zemris.dipl.lukasuman.fpga.bool.func;

import hr.fer.zemris.dipl.lukasuman.fpga.TestUtil;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BoolFuncControllerTests {

    private static BooleanFunction INVARIANT_FUNC;
    private static List<Integer> EXPECTED_INDICES;
    private static BitSet EXPECTED_TRUTH_TABLE;
    private static List<String> EXPECTED_INPUT_IDS;

    @BeforeAll
    static void initFunc() {
        INVARIANT_FUNC = new BooleanFunction(Arrays.asList("a", "b", "c", "d", "e"), Utility.bitSetFromMask(
                0b10100101101011111010010110101111, 32));
        EXPECTED_INDICES = Arrays.asList(0, 3);
        EXPECTED_TRUTH_TABLE = Utility.bitSetFromMask(0b10011011, 8);
        EXPECTED_INPUT_IDS = Arrays.asList("b", "c", "e");
    }

    @Test
    void testUnused() {
        TestUtil.argThrow(() -> BoolFuncController.checkIfInputMatters(null));

        List<Integer> redundantIndices = BoolFuncController.checkIfInputMatters(INVARIANT_FUNC);
        assertEquals(redundantIndices, EXPECTED_INDICES);

        BooleanFunction reducedFunction = BoolFuncController.removeInputs(INVARIANT_FUNC, redundantIndices);
        assertEquals(EXPECTED_INPUT_IDS, reducedFunction.getInputIDs());
        assertEquals(EXPECTED_TRUTH_TABLE, reducedFunction.getTruthTable());
    }

    @Test
    void testGeneratingFromText() {
        List<String> invalidTexts = new ArrayList<>();
        invalidTexts.add(null);
        invalidTexts.add("");
        invalidTexts.add("a1010");
        invalidTexts.add("a 0101");
        invalidTexts.add("100 a b 0101");
        invalidTexts.add("4 a b c 0101010101010101");
        invalidTexts.add("3 a b c d1010110");
        invalidTexts.add("1 0 1");
        invalidTexts.add("a b 0 1 0 1 0");

        for (String invalidText : invalidTexts) {
            TestUtil.argThrow(() -> BoolFuncController.generateFromText(invalidText));
        }

        List<String> validTexts = new ArrayList<>();
        List<BooleanFunction> expected = new ArrayList<>();

        validTexts.add("1001");
        expected.add(BoolFuncController.generateFromMask(0b1001, 2));

        validTexts.add("a b 1010");
        expected.add(BoolFuncController.generateFromMask(0b1010, 2));

        validTexts.add("2\na\nb\n0000");
        expected.add(BoolFuncController.generateFromMask(0b0000, 2));

        validTexts.add("1 0 1 1");
        expected.add(BoolFuncController.generateFromMask(0b1011, 2));

        validTexts.add("5aasdf b33bb c5 ddddd 1010100101100101");
        expected.add(BoolFuncController.generateFromMask(0b1010100101100101, 4));

        validTexts.add("1a b c 00111100");
        expected.add(BoolFuncController.generateFromMask(0b00111100, 3));

        for (int i = 0; i < validTexts.size(); i++) {
//            System.out.println("Test  " + i);
            assertEquals(expected.get(i), BoolFuncController.generateFromText(validTexts.get(i)));
        }
    }

    @Test
    void testTruthTableToArray() {
        BooleanFunction func = BoolFuncController.generateFromMask(0b00010010111011011011111101101100, 5);
        int[] result = BoolFuncController.bitSetToArray(func);

        assertEquals(1, result.length);
        assertEquals(317570924, result[0]);
    }
}
