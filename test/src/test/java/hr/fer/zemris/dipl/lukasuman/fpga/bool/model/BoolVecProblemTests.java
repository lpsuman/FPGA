package hr.fer.zemris.dipl.lukasuman.fpga.bool.model;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BoolFuncController;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanFunction;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanVector;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.IntArraySolution;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.Solution;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;
import org.junit.jupiter.api.Test;

import java.util.BitSet;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class BoolVecProblemTests {

    private static final int NUM_FUNC_TO_TEST = 3;

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

    private static void testBrute(int numFuncInputs, int numCLBInputs) {
//        BooleanFunction controllers = BoolFuncController.generateFromMask(0b01011011, 3);
//        BooleanFunction controllers = BoolFuncController.generateFromMask(0b1011001000111010, 4);
//        BooleanFunction controllers = BoolFuncController.generateFromMask(0b00010010111011011011111101101100, 5);
        BooleanFunction func = BoolFuncController.generateRandomFunction(numFuncInputs);
        Solution<int[]> solution = BoolVecProblem.bruteSolve(func, numCLBInputs);

        assertNotNull(solution);

        BoolVecProblem problem = new BoolVecProblem(new BooleanVector(Collections.singletonList(func), false), numCLBInputs);
        CLBController clbController =problem.getClbController();
        clbController.setNumCLB((solution.getData().length - 1) / clbController.getIntsPerCLB());
        BoolVecEvaluator evaluator = new BoolVecEvaluator(problem);
        evaluator.setLogging(true);
        evaluator.evaluateSolution(solution, false);

//        System.out.println(evaluator.getLog());
//        System.out.println(problem.solutionToString(solution, evaluator.getBlockUsage()));

        assertEquals(Constants.FITNESS_SCALE, solution.getFitness());
    }

    private static void doSequenceTest(int numCLBInputs) {
        for (int i = 0; i < NUM_FUNC_TO_TEST; i++) {
            testBrute(numCLBInputs + 1 + i, numCLBInputs);
        }
    }

    @Test
    void testBruteSolve() {
//        testBrute(4, 3);
//        testBrute(5, 3);
//        testBrute(6, 3);
//        testBrute(5, 4);
//        testBrute(6, 4);
//        testBrute(7, 4);
//        testBrute(6, 5);
//        testBrute(7, 5);
//        testBrute(8, 5);
//        testBrute(7, 6);
//        testBrute(8, 6);
//        testBrute(9, 6);
//        testBrute(8, 7);
//        testBrute(9, 7);
//        testBrute(10, 7);
        doSequenceTest(3);
        doSequenceTest(4);
        doSequenceTest(5);
        doSequenceTest(6);
        doSequenceTest(7);
//        doSequenceTest(8);
    }

    @Test
    void testBruteSolveTwoInputs() {
        testBrute(3, 2);
        testBrute(4, 2);
        testBrute(5, 2);
        testBrute(6, 2);
        testBrute(7, 2);
    }
}
