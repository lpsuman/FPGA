package hr.fer.zemris.dipl.lukasuman.fpga.bool.solver;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BlockConfiguration;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanFunction;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanVector;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MergingTests {

    @Test
    void testMerging() {
        String firstName = "F0";
        List<String> firstInputIDs = Arrays.asList("a", "b", "c", "d");
        BitSet firstTruthTable = Utility.bitSetFromMask(0b1011101000110110, 16);
        BooleanFunction firstFunc = new BooleanFunction(firstInputIDs, firstTruthTable, firstName);
        BooleanVector firstVector = new BooleanVector(Collections.singletonList(firstFunc));
        int[] firstData = new int[]{
                3, 1, 0b1101,
                0, 4, 0b1011,
                3, 1, 0b1110,
                5, 2, 0b0010,
                6, 7, 0b0110
        };
        BlockConfiguration firstConfiguration = new BlockConfiguration(2, 5, firstData, Collections.singletonList(8));
        BoolVectorSolution firstSolution = new BoolVectorSolution(firstVector, firstConfiguration);

        String secondName = "F1";
        List<String> secondInputIDs = Arrays.asList("c", "d", "e", "f");
        BitSet secondTruthTable = Utility.bitSetFromMask(0b1111010101110111, 16);
        BooleanFunction secondFunc = new BooleanFunction(secondInputIDs, secondTruthTable, secondName);
        BooleanVector secondVector = new BooleanVector(Collections.singletonList(secondFunc));
        int[] secondData = new int[]{
                0, 2, 0b0001,
                3, 4, 0b0111,
                0, 1, 0b0111,
                5, 6, 0b1011
        };
        BlockConfiguration secondConfiguration = new BlockConfiguration(2, 4, secondData, Collections.singletonList(7));
        BoolVectorSolution secondSolution = new BoolVectorSolution(secondVector, secondConfiguration);

        List<String> expectedMergedInputIDs = Arrays.asList("a", "b", "c", "d", "e", "f");
        int[] expectedMergedData = new int[]{
                3,  1,  0b1101,
                3,  1,  0b1110,
                2,  3,  0b0111,
                2,  4,  0b0001,
                0,  6,  0b1011,
                5,  9,  0b0111,
                10, 2,  0b0010,
                11, 8,  0b1011,
                7,  12, 0b0110,
        };
        List<Integer> expectedOutputIndices = Arrays.asList(8, 7);

        BoolVectorSolution mergedSolution = BoolVectorSolution.mergeSolutions(Arrays.asList(firstSolution, secondSolution));
        assertNotNull(mergedSolution);

        BooleanVector mergedVector = mergedSolution.getBoolVector();
        assertEquals(mergedVector.getNumInputCombinations(), 64);
        assertEquals(expectedMergedInputIDs, mergedVector.getSortedInputIDs());
        assertEquals(mergedVector.getBoolFunctions().get(0), firstFunc);
        assertEquals(mergedVector.getBoolFunctions().get(1), secondFunc);

        BlockConfiguration mergedBlockConfiguration = mergedSolution.getBlockConfiguration();
        assertEquals(mergedBlockConfiguration.getNumCLB(), 9);
        assertEquals(mergedBlockConfiguration.getNumCLBInputs(), 2);
        assertEquals(mergedBlockConfiguration.getBlockSize(), 3);
        assertArrayEquals(expectedMergedData, mergedBlockConfiguration.getData());

        assertEquals(expectedOutputIndices, mergedBlockConfiguration.getOutputIndices());

        System.out.println(mergedSolution);
    }
}
