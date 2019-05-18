package hr.fer.zemris.dipl.lukasuman.fpga.bool;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BoolFunc;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BoolFuncController;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BoolVector;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.*;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.solver.BoolSolver;

import java.util.ArrayList;
import java.util.List;

public class BoolMain {

    public static void main(String[] args) {
        List<BoolFunc> functions = new ArrayList<>();
        functions.add(BoolFuncController.generateFromMask(0b11101110111100011111111100011111, 5));
        functions.add(BoolFuncController.generateFromMask(0b00000000111100000001000100011110, 5));
        functions.add(BoolFuncController.generateRandomFunction(5));
        functions.add(BoolFuncController.generateRandomFunction(5));
        functions.add(BoolFuncController.generateRandomFunction(5));

        BoolVecProblem problem = new BoolVecProblem(new BoolVector(functions), 3);
        BoolSolver.solve(problem);
    }
}
