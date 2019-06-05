package hr.fer.zemris.dipl.lukasuman.fpga.bool.solver;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BlockConfiguration;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BoolFuncController;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanFunction;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanVector;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.BoolVecProblem;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.IntArraySolution;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BoolVecSolutionTests {

    @Test
    void testRemoveRedundant() {
        int[] redundantData = new int[]{
                0, 2, 0b0110,
                1, 3, 0b1101,
                4, 1, 0b0001,
                3, 0, 0b0010,
                2, 1, 0b1001,
                3, 0, 0b0010,
                1, 2, 0b1101,
                6, 7, 0b0100,
                8, 9, 0b0110,
                5, 0, 0b0111,
                1, 2, 0b1101,
                0, 13, 0b0001,
                11, 14, 12, 4, 10
        };

        int[] expectedData = new int[]{
                0, 2, 0b0110,
                1, 3, 0b1101,
                4, 1, 0b0001,
                3, 0, 0b0010,
                2, 1, 0b1001,
                1, 2, 0b1101,
                6, 7, 0b0100,
                6, 8, 0b0110,
                5, 0, 0b0111,
                0, 8, 0b0001,
                10, 12, 11, 4, 9
        };

        List<BooleanFunction> functions = new ArrayList<>();
        functions.add(BoolFuncController.generateFromMask(0b10001101, 3));
        functions.add(BoolFuncController.generateFromMask(0b00001101, 3));
        functions.add(BoolFuncController.generateFromMask(0b00011111, 3));
        functions.add(BoolFuncController.generateFromMask(0b11011110, 3));
        functions.add(BoolFuncController.generateFromMask(0b10001001, 3));
        BooleanVector vector = new BooleanVector(functions);
        BoolVecProblem problem = new BoolVecProblem(vector, 2);
        problem.getClbController().setNumCLB(12);

        BlockConfiguration redundantBlockConfig = problem.generateBlockConfiguration(new IntArraySolution(redundantData));
        BoolVectorSolution redundantSolution = new BoolVectorSolution(vector, redundantBlockConfig);

        BoolVectorSolution result = BoolVectorSolution.removeRedundantCLBs(redundantSolution);
        assertNotNull(result);
        assertArrayEquals(expectedData, result.getBlockConfiguration().getAsFlatArray().getData());
    }
}
