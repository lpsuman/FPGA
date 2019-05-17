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

    @Test
    void testBoolFunc() {
        TestUtil.argThrow(() -> new BoolFunc(null, firstTruthTable));
        TestUtil.argThrow(() -> new  BoolFunc(firstInputIDs, null));
        TestUtil.argThrow(() -> new  BoolFunc(new ArrayList<>(Arrays.asList(new String[500])), secondTruthTable));
        TestUtil.argThrow(() -> new  BoolFunc(firstInputIDs,thirdTruthTable));
        assertDoesNotThrow(() -> new BoolFunc(thirdInputIDs, firstTruthTable));

        BoolFunc func = new BoolFunc(firstInputIDs, firstTruthTable);
        assertEquals(func.getInputIDs(), firstInputIDs);
        assertEquals(func.getTruthTable(), firstTruthTable);
    }

    @Test
    void testBoolVector() {
        TestUtil.argThrow(() -> new BoolVector(null));
        TestUtil.argThrow(() -> new BoolVector(new ArrayList<>()));

        BoolFunc firstFunc = new BoolFunc(firstInputIDs, firstTruthTable);
        BoolFunc secondFunc = new BoolFunc(secondInputIDs, secondTruthTable);
        BoolFunc thirdFunc = new BoolFunc(thirdInputIDs, thirdTruthTable);
        List<BoolFunc> funcList = new ArrayList<>();
        funcList.add(firstFunc);
        funcList.add(secondFunc);
        funcList.add(null);

        TestUtil.argThrow(() -> new BoolVector(funcList));
        funcList.remove(funcList.size() - 1);
        funcList.add(thirdFunc);
        BoolVector boolVector = new BoolVector(funcList);

        assertEquals(boolVector.getNumFunctions(), 3);
        assertEquals(boolVector.getBoolFunctions(), funcList);
        assertEquals(boolVector.getNumInputs(), expectedInputIDs.size());
        assertEquals(boolVector.getNumInputCombinations(), (int) Math.pow(2, expectedInputIDs.size()));
        assertEquals(boolVector.getTruthTable().length, 3);
        assertEquals(boolVector.getSortedInputIDs(), expectedInputIDs);
    }
}
