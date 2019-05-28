package hr.fer.zemris.dipl.lukasuman.fpga.bool.func;

import hr.fer.zemris.dipl.lukasuman.fpga.TestUtil;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FuncTests {

    private static final List<String> firstInputIDs = new ArrayList<>(Arrays.asList("a", "b", "c"));
    private static final BitSet firstTruthTable = Utility.bitSetFromMask(0b10101100, 8);

    private static final List<String> secondInputIDs = new ArrayList<>(Arrays.asList("b", "c", "d"));
    private static final BitSet secondTruthTable = Utility.bitSetFromMask(0b00101101, 8);

    private static final List<String> thirdInputIDs = new ArrayList<>(Arrays.asList("b", "c", "d", "e"));
    private static final BitSet thirdTruthTable = Utility.bitSetFromMask(0b1110110001101000, 16);

    private static final List<String> expectedInputIDs = new ArrayList<>(Arrays.asList("a", "b", "c", "d", "e"));

    private static final BitSet expectedFirstTruthTable = Utility.bitSetFromMask(
            0b11110000111100001111111100000000, 32);

    private static final BitSet expectedSecondTruthTable = Utility.bitSetFromMask(
            0b00001100111100110000110011110011, 32);

    private static final BitSet expectedThirdTruthTable = Utility.bitSetFromMask(
            0b11101100011010001110110001101000, 32);

    @Test
    void testBoolFunc() {
        TestUtil.argThrow(() -> new BooleanFunction(null, firstTruthTable));
        TestUtil.argThrow(() -> new BooleanFunction(firstInputIDs, null));
        TestUtil.argThrow(() -> new BooleanFunction(new ArrayList<>(Arrays.asList(new String[500])), secondTruthTable));
        TestUtil.argThrow(() -> new BooleanFunction(firstInputIDs,thirdTruthTable));
        assertDoesNotThrow(() -> new BooleanFunction(thirdInputIDs, firstTruthTable));

        BooleanFunction func = new BooleanFunction(firstInputIDs, firstTruthTable);
        assertEquals(func.getInputIDs(), firstInputIDs);
        assertEquals(func.getTruthTable(), firstTruthTable);
    }

    @Test
    void testBoolVector() {
        TestUtil.argThrow(() -> new BooleanVector((List<BooleanFunction>) null));
        TestUtil.argThrow(() -> new BooleanVector(new ArrayList<>()));

        BooleanFunction firstFunc = new BooleanFunction(firstInputIDs, firstTruthTable);
        BooleanFunction secondFunc = new BooleanFunction(secondInputIDs, secondTruthTable);
        BooleanFunction thirdFunc = new BooleanFunction(thirdInputIDs, thirdTruthTable);
        List<BooleanFunction> funcList = new ArrayList<>();
        funcList.add(firstFunc);
        funcList.add(secondFunc);
        funcList.add(null);

        TestUtil.argThrow(() -> new BooleanVector(funcList));
        funcList.remove(funcList.size() - 1);
        funcList.add(thirdFunc);
        BooleanVector boolVector = new BooleanVector(funcList);

        assertEquals(boolVector.getNumFunctions(), 3);
        assertEquals(boolVector.getBoolFunctions(), funcList);
        assertEquals(boolVector.getNumInputs(), expectedInputIDs.size());
        assertEquals(boolVector.getNumInputCombinations(), (int) Math.pow(2, expectedInputIDs.size()));
        assertEquals(boolVector.getTruthTable().length, 3);
        assertEquals(boolVector.getSortedInputIDs(), expectedInputIDs);

        assertEquals(boolVector.getTruthTable()[0], expectedFirstTruthTable);
        assertEquals(boolVector.getTruthTable()[1], expectedSecondTruthTable);
        assertEquals(boolVector.getTruthTable()[2], expectedThirdTruthTable);
    }
}
