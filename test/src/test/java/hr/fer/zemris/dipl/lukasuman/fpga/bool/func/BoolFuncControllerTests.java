package hr.fer.zemris.dipl.lukasuman.fpga.bool.func;

import hr.fer.zemris.dipl.lukasuman.fpga.TestUtil;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BoolFuncControllerTests {

    private static BoolFunc INVARIANT_FUNC;
    private static List<Integer> EXPECTED_INDICES;
    private static BitSet EXPECTED_TRUTH_TABLE;
    private static List<String> EXPECTED_INPUT_IDS;

    @BeforeAll
    static void initFunc() {
        INVARIANT_FUNC = new BoolFunc(Arrays.asList("a", "b", "c", "d", "e"), Utility.bitSetFromMask(
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

        BoolFunc reducedFunction = BoolFuncController.removeInputs(INVARIANT_FUNC, redundantIndices);
        assertEquals(EXPECTED_INPUT_IDS, reducedFunction.getInputIDs());
        assertEquals(EXPECTED_TRUTH_TABLE, reducedFunction.getTruthTable());
    }
}
