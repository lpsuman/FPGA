package hr.fer.zemris.dipl.lukasuman.fpga.bool.solver;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BoolFuncController;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.BoolVecEvaluator;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.BoolVecProblem;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.Solution;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SolverTests {

    @Test
    void testBruteSolve() {
        BoolVecProblem problem = new BoolVecProblem(BoolFuncController.generateRandomVector(5, 4), 3);
        BooleanSolver solver = new BooleanSolver(SolverMode.BRUTE, null);

        BoolVectorSolution fullSolution = solver.solve(problem);
        assertNotNull(fullSolution);
        BoolVecEvaluator evaluator = new BoolVecEvaluator(problem);
        problem.getClbController().setNumCLB(fullSolution.getBlockConfiguration().getNumCLB());
        Solution<int[]> arraySolution = fullSolution.getBlockConfiguration().getAsFlatArray();
        evaluator.evaluateSolution(arraySolution, false);

        assertEquals(Constants.FITNESS_SCALE, arraySolution.getFitness());
    }
}
