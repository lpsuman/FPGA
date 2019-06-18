package hr.fer.zemris.dipl.lukasuman.fpga.bool;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.BoolVecProblem;

import java.io.IOException;
import java.util.List;

public class ShowResults {

    public static void main(String[] args) {
        String filePath = BooleanOptimizer.getFolderPath() + "opt_2019-06-17T23_26_58.865476100_with_caching_30" + ".json";
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
        BooleanOptimizer.showAverageResults(optimizationResults, numTests);
    }
}
