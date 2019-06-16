package hr.fer.zemris.dipl.lukasuman.fpga.bool;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.BoolVecProblem;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.solver.BooleanSolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ShowResults {

    public static void main(String[] args) {
        String filePath = BooleanOptimizer.getFolderPath() + "opt_2019-06-17T00_31_34.619335600_individual_subtree_30" + ".json";
        int numTests = Integer.parseInt(filePath.substring(0, filePath.lastIndexOf('.')).substring(filePath.lastIndexOf('_') + 1));
        List<BooleanOptimizer.OptimizationRunResult> optimizationResults;

        try {
            optimizationResults = MyGson.readFromJsonAsList(filePath, BooleanOptimizer.OptimizationRunResult[].class);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        BoolVecProblem problem = BooleanOptimizer.getTestProblem();
        BooleanOptimizer.showResults(problem, optimizationResults);
        int numSetups = optimizationResults.size() / numTests;

        for (int i = 0; i < numSetups; i++) {
            List<List<BooleanSolver.RunResults>> results = new ArrayList<>();
            for (int j = 0; j < numTests; j++) {
                results.add(optimizationResults.get(i * numTests + j).getRunResults());
            }
            System.out.println(optimizationResults.get(i * numTests).getSetup());
            BooleanSolver.printPerFuncResultsMulti(results);
        }
    }
}
