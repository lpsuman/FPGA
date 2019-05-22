package hr.fer.zemris.dipl.lukasuman.fpga.bool;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BoolFunc;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BoolFuncController;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BoolVector;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.*;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.BoolExpression;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.lexer.BoolLexer;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing.parser.BoolParser;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.solver.BoolSolver;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.solver.BoolVectorSolution;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.util.ArrayList;
import java.util.List;

public class BoolMain {

    private static final boolean USE_NON_RANDOM_FUNCTIONS = true;
    private static final boolean SHOW_TWO_VARIABLE_FUNC_CONVERSION = true;
    private static final int NUM_FUNCTIONS = 5;
    private static final int NUM_FUNC_INPUTS = 5;
    private static final int NUM_CLB_INPUTS = 3;

    public static void main(String[] args) {
        List<BoolFunc> functions = new ArrayList<>();
        int numRemainingFunctions = NUM_FUNCTIONS;
        int numFuncInputs = NUM_FUNC_INPUTS;
        int numCLBInputs = NUM_CLB_INPUTS;

        if (SHOW_TWO_VARIABLE_FUNC_CONVERSION) {
            numRemainingFunctions = 0;
            numFuncInputs = 4;
            numCLBInputs = 2;

            if (USE_NON_RANDOM_FUNCTIONS) {
//                BoolParser parser = new BoolParser();
//                BoolExpression boolExpression = parser.parse(new BoolLexer(Utility.getInputStreamFromString(
//                        "(not a or c) xor (b and not a) or d")));
//                functions.add(BoolFuncController.generateFromExpression(boolExpression));
                functions.add(new BoolFunc(4, Utility.bitSetFromMask(0b1011101000110110, 16)));
            } else {
                functions.add(BoolFuncController.generateRandomFunction(numFuncInputs));
            }
        }

        if (USE_NON_RANDOM_FUNCTIONS && NUM_FUNC_INPUTS == 5 && numRemainingFunctions >= 2) {
            functions.add(BoolFuncController.generateFromMask(0b11101110111100011111111100011111, 5));
            functions.add(BoolFuncController.generateFromMask(0b00000000111100000001000100011110, 5));
            numRemainingFunctions -= 2;
        }

        for (int i = 0; i < numRemainingFunctions; ++i) {
            functions.add(BoolFuncController.generateRandomFunction(numFuncInputs));
        }

        BoolVecProblem problem = new BoolVecProblem(new BoolVector(functions), numCLBInputs);
        BoolSolver solver = new BoolSolver();
        BoolVectorSolution solution = solver.solve(problem);

        if (solution == null) {
            System.out.println("No merged solution.");
            return;
        }

        if (solution.canBeConvertedToExpression()) {
            BoolParser parser = new BoolParser();
            for (int i = 0; i < functions.size(); ++i) {
                String solutionString = solution.getAsExpression(i);
                System.out.println("F" + i + "\nSolution string:\n" + solutionString);
                BoolExpression boolExpression = parser.parse(new BoolLexer(Utility.getInputStreamFromString(solutionString)));
                parser.reset();
                System.out.println("Parsed into:\n" + boolExpression.toString());
            }
        }
    }
}
