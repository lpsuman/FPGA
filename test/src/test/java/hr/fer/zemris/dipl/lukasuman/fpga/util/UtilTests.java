package hr.fer.zemris.dipl.lukasuman.fpga.util;

import hr.fer.zemris.dipl.lukasuman.fpga.TestUtil;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class UtilTests {

    private static final String TEST = "test";
    private static final int MASK = 0b1001;

    @Test
    void testCheckNull() {
        TestUtil.argThrow(() -> Utility.checkNull(null, null));

        String obj = "object";
        String returned = Utility.checkNull(obj, null);
        assertEquals(obj, returned);
        returned = Utility.checkNull(obj, TEST);
        assertEquals(obj, returned);
    }

    @Test
    void testCheckRange() {
        TestUtil.argThrow(() -> Utility.checkRange(-1, 0, 10));
        assertDoesNotThrow(() -> Utility.checkRange(0, 0, 10));
        assertDoesNotThrow(() -> Utility.checkRange(10, 0, 10));
        TestUtil.argThrow(() -> Utility.checkRange(11, 0, 10));
    }

    @Test
    void testCheckLimit() {
        ArgumentLimit<Integer> limit = new ArgumentIntervalLimit<>(TEST, 0, 10);
        TestUtil.argThrow(() -> Utility.checkLimit(limit, -1));
        assertEquals(Utility.checkLimit(limit, 0), 0);
        assertEquals(Utility.checkLimit(limit, 5), 5);
        assertEquals(Utility.checkLimit(limit, 10), 10);
        TestUtil.argThrow(() -> Utility.checkLimit(limit, 11));
    }

    @Test
    void testNewBitSetArray() {
        TestUtil.argThrow(() -> Utility.newBitSetArray(-1, 10));
        TestUtil.argThrow(() -> Utility.newBitSetArray(0, 10));
        TestUtil.argThrow(() -> Utility.newBitSetArray(100_000, 10));
        TestUtil.argThrow(() -> Utility.newBitSetArray(1, -1));
        TestUtil.argThrow(() -> Utility.newBitSetArray(1, Integer.MAX_VALUE));

        BitSet[] array1 = Utility.newBitSetArray(1, 10);
        BitSet[] array2 = Utility.newBitSetArray(3, 10);

        assertEquals(array1.length, 1);
        assertEquals(array2.length, 3);
        assertNotNull(array1[0]);

        for (int i = 0; i < 3; i++) {
            assertNotNull(array2[i]);
        }
    }

    private static void testBitSetForMask(BitSet bitSet) {
        assertNotNull(bitSet);
        assertEquals(bitSet.length(), 4);
        assertTrue(bitSet.get(0));
        assertFalse(bitSet.get(1));
        assertFalse(bitSet.get(2));
        assertTrue(bitSet.get(3));
    }

    @Test
    void testSetBitSet() {
        BitSet bitSet = new BitSet(4);
        TestUtil.argThrow(() -> Utility.setBitSet(null, 0, 0));
        TestUtil.argThrow(() -> Utility.setBitSet(bitSet, 0, -1));
        TestUtil.argThrow(() -> Utility.setBitSet(bitSet, 0, 33));

        Utility.setBitSet(bitSet, MASK, 4);
        testBitSetForMask(bitSet);
    }

    @Test
    void testBitSetFromMask() {
        TestUtil.argThrow(() -> Utility.bitSetFromMask(0, -1));
        TestUtil.argThrow(() -> Utility.bitSetFromMask(0, 33));

        testBitSetForMask(Utility.bitSetFromMask(MASK, 4));
    }

    @Test
    void testTestBit() {
        TestUtil.argThrow(() -> Utility.testBitFromLeft(0, -1));
        TestUtil.argThrow(() -> Utility.testBitFromLeft(0, 32));

        assertTrue(Utility.testBitFromRight(MASK, 0));
        assertFalse(Utility.testBitFromRight(MASK, 1));
        assertFalse(Utility.testBitFromRight(MASK, 2));
        assertTrue(Utility.testBitFromRight(MASK, 3));

        TestUtil.argThrow(() -> Utility.testBitFromRight(0, -1));
        TestUtil.argThrow(() -> Utility.testBitFromRight(0, 32));

        assertTrue(Utility.testBitFromLeft(MASK, 31));
        assertFalse(Utility.testBitFromLeft(MASK, 30));
        assertFalse(Utility.testBitFromLeft(MASK, 29));
        assertTrue(Utility.testBitFromLeft(MASK, 28));
    }

    @Test
    void testIntervalArgumentLimit() {
        TestUtil.argThrow(() -> new ArgumentIntervalLimit<Integer>(null, null, null));
        TestUtil.argThrow(() -> new ArgumentIntervalLimit<Integer>(TEST, 1, 0));

        ArgumentLimit<Integer> noLower = new ArgumentIntervalLimit<>(TEST, null, 10);
        ArgumentLimit<Integer> noUpper = new ArgumentIntervalLimit<>(TEST, 0, null);
        ArgumentLimit<Integer> both = new ArgumentIntervalLimit<>(TEST, 0, 10);

        assertDoesNotThrow(() -> Utility.checkLimit(noLower, -1));
        assertDoesNotThrow(() -> Utility.checkLimit(noLower, 10));
        TestUtil.argThrow(() -> Utility.checkLimit(noLower, 11));

        TestUtil.argThrow(() -> Utility.checkLimit(noUpper, -1));
        assertDoesNotThrow(() -> Utility.checkLimit(noUpper, 0));
        assertDoesNotThrow(() -> Utility.checkLimit(noUpper, 11));

        TestUtil.argThrow(() -> Utility.checkLimit(both, -1));
        assertDoesNotThrow(() -> Utility.checkLimit(both, 0));
        assertDoesNotThrow(() -> Utility.checkLimit(both, 10));
        TestUtil.argThrow(() -> Utility.checkLimit(both, 11));
    }

    @Test
    void testSubArraySearch() {
        int[] empty = new int[0];
        int[] haystack = new int[]{1, 2, 3, 1, 2, 3, 2, 4, 1};
        int[] needle = new int[]{2, 3, 2, 4};
        int expectedIndex = 4;
        int[] expectedPrefixArray = new int[]{0, 0, 1, 0};

        TestUtil.argThrow(() -> Utility.computePrefixArray(null));
        TestUtil.argThrow(() -> Utility.computePrefixArray(empty));

        int[] prefixArray = Utility.computePrefixArray(needle);
        assertTrue(Arrays.equals(prefixArray, expectedPrefixArray));

        TestUtil.argThrow(() -> Utility.findIndexOfSubArray(null, needle, null));
        TestUtil.argThrow(() -> Utility.findIndexOfSubArray(haystack, null, null));
        TestUtil.argThrow(() -> Utility.findIndexOfSubArray(haystack, needle, null));

        int index = Utility.findIndexOfSubArray(haystack, needle, prefixArray);
        assertEquals(index, expectedIndex);
    }

    @Test
    void testBreakIntoWords() {
        TestUtil.argThrow(() -> Utility.breakIntoWords((List<String>)null));
        TestUtil.argThrow(() -> Utility.breakIntoWords(new ArrayList<>()));
        assertNull(Utility.breakIntoWords(Collections.singletonList("")));

        List<String> text = Arrays.asList("abra", "kad abra", "bra");
        List<String> expected = Arrays.asList("abra", "kad", "abra", "bra");

        assertEquals(expected, Utility.breakIntoWords(text));
    }

    @Test
    void testBitSetFromString() {
        TestUtil.argThrow(() -> Utility.bitSetFromString(null));
        TestUtil.argThrow(() -> Utility.bitSetFromString(""));
        TestUtil.argThrow(() -> Utility.bitSetFromString("a"));
        TestUtil.argThrow(() -> Utility.bitSetFromString("a0"));
        TestUtil.argThrow(() -> Utility.bitSetFromString("0a"));
        TestUtil.argThrow(() -> Utility.bitSetFromString("a0a"));
        TestUtil.argThrow(() -> Utility.bitSetFromString("0a0"));
        TestUtil.argThrow(() -> Utility.bitSetFromString("010111031010"));

        BitSet result = Utility.bitSetFromString("1011001");
        BitSet expected = Utility.bitSetFromMask(0b1011001, 7);

        assertEquals(expected, result);
    }
}
