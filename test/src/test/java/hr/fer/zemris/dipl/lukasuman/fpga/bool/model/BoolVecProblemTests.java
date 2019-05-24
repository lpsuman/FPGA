package hr.fer.zemris.dipl.lukasuman.fpga.bool.model;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanFunction;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanVector;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.IntArraySolution;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.Solution;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;
import org.junit.jupiter.api.Test;

import java.util.BitSet;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class BoolVecProblemTests {

    @Test
    void testTrim() {
        int[] redundantSolutionData = new int[]{
                3, 1, 0, 72,
                3, 4, 2, 28,
                0, 6, 2, 104,
                7, 2, 1, 9,
                8, 4, 2, 25,
                7, 8, 9, 73,
                10};
        int[] expectedTrimmedSolutionData = new int[]{
                3, 4, 2, 28,
                0, 5, 2, 104,
                6, 2, 1, 9,
                7, 4, 2, 25,
                6, 7, 8, 73,
                9};
        BooleanFunction func = new BooleanFunction(5, Utility.bitSetFromMask(
                0b00101100001001011000010111010101, 32));
        Solution<int[]> redundantSolution = new IntArraySolution(redundantSolutionData);

        BoolVecProblem problem = new BoolVecProblem(new BooleanVector(Collections.singletonList(func)), 3);
        problem.getClbController().setNumCLB(6);
        BitSet redundantIndices = Utility.bitSetFromMask(0b00000100000, 11);
        Solution<int[]> trimmedSolution = problem.trimmedBoolSolution(redundantSolution, redundantIndices);

        assertNotNull(trimmedSolution);
        assertArrayEquals(expectedTrimmedSolutionData, trimmedSolution.getData());
    }
}
