package hr.fer.zemris.dipl.lukasuman.fpga.bool;

import com.google.gson.reflect.TypeToken;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanVector;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.BoolVecProblem;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.solver.BooleanSolver;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.solver.BooleanSolverConfig;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.solver.SolverMode;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.AnnealedThreadPoolConfig;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.ParallelGAConfig;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.operators.crossover.RandomizeCrossover;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.operators.mutation.RandomizeMutation;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.operator.OperatorStatistics;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Timer;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BooleanOptimizer {

    public static class OptimizationRunResult {
        private String setup;
        private List<BooleanSolver.RunResults> runResults;
        private List<OperatorStatistics> crossoverOperatorStatistics;
        private List<OperatorStatistics> mutationOperatorStatistics;

        public OptimizationRunResult(String setup, List<BooleanSolver.RunResults> runResults,
                                     List<OperatorStatistics> crossoverOperatorStatistics,
                                     List<OperatorStatistics> mutationOperatorStatistics) {
            this.setup = setup;
            this.runResults = runResults;
            this.crossoverOperatorStatistics = crossoverOperatorStatistics;
            this.mutationOperatorStatistics = mutationOperatorStatistics;
        }

        public String getSetup() {
            return setup;
        }

        public List<BooleanSolver.RunResults> getRunResults() {
            return runResults;
        }

        public List<OperatorStatistics> getCrossoverOperatorStatistics() {
            return crossoverOperatorStatistics;
        }

        public List<OperatorStatistics> getMutationOperatorStatistics() {
            return mutationOperatorStatistics;
        }
    }

    private static final int[][] FIXED_PARAMETERS = new int[][]{
            {10, 1000, 500}
    };

//    private static final int[][] FIXED_PARAMETERS = new int[][]{
//            {1, 50, 10000},
//            {1, 200, 2000},
//            {1, 200, 10000},
//            {10, 200, 400},
//            {10, 1000, 400}
//    };

    private static final int[] MAX_NUM_FAILS = new int[]{1, 5, 10};
    private static final int[] POPULATION_SIZES = new int[]{50, 200, 1000};
    private static final int[] MAX_GENERATIONS = new int[]{400, 2000, 10000};

    private static final int NUM_THREADS = 3;
    private static final int NUM_TESTS = 30;
    private static final boolean SOLVE_INDIVIDUALLY = false;
    private static final boolean USE_STATISTICS = false;
    private static final boolean ALLOW_PRINTING = false;

    public static void main(String[] args) {
//        int[][] params = getGridParameters();
        int[][] params = FIXED_PARAMETERS;

        BooleanSolverConfig solverConfig = new BooleanSolverConfig()
                .printOnlyBestSolution(true)
                .useStatistics(USE_STATISTICS)
                .printOnlyGlobalStatistics(true)
                .solveIndividually(SOLVE_INDIVIDUALLY);
        ArrayList<OptimizationRunResult> optimizationResults = new ArrayList<>();
        int totalNumSteps = params.length;

        Timer timer = new Timer();
        timer.start();

        for (int i = 0; i < totalNumSteps; i++) {
            int maxNumFails = params[i][0];
            int populationSize = params[i][1];
            int maxGenerations = params[i][2];

            for (int j = 0; j < NUM_TESTS; j++) {
                String setup = String.format("Step %2d/%2d (try %2d/%2d), fails=%3d, pop_size=%6d, max_gen=%6d",
                        i + 1, totalNumSteps, j + 1, NUM_TESTS, maxNumFails, populationSize, maxGenerations);
                System.out.println(setup);

                BooleanSolver solver = new BooleanSolver(SolverMode.FULL, null, solverConfig);
                solver.setMaxNumFails(maxNumFails);
                ParallelGAConfig gaConfig = new ParallelGAConfig()
                        .populationSize(populationSize)
                        .maxGenerations(maxGenerations);
                solver.setAlgorithmConfig(gaConfig);
                AnnealedThreadPoolConfig tpConfig = new AnnealedThreadPoolConfig()
                        .numThreads(NUM_THREADS);
                solver.setThreadPoolConfig(tpConfig);
                solver.setEnablePrinting(ALLOW_PRINTING);
                solver.solve(getTestProblem());

                optimizationResults.add(new OptimizationRunResult(setup, solver.getRunResults(),
                        solver.getCrossoverOperatorStatistics(), solver.getMutationOperatorStatistics()));
            }
        }

        LocalDateTime time = LocalDateTime.now();
        TypeToken<ArrayList<OptimizationRunResult>> listType = new TypeToken<>(){};
        try {
            MyGson.writeToJson(getFolderPath() + "opt_" + time.toString().replace(':', '_') + "_" + NUM_TESTS + ".json",
                    optimizationResults, listType.getRawType());
        } catch (IOException e) {
            e.printStackTrace();
        }

        showResults(getTestProblem(), optimizationResults);
        showAverageResults(optimizationResults, NUM_TESTS);

        System.out.println("Total elapsed time: " + timer.getElapsedTime() / 1000.0);
    }

    private static int[][] getGridParameters() {
        int totalNumSteps = MAX_NUM_FAILS.length * POPULATION_SIZES.length * MAX_GENERATIONS.length;
        int[][] params = new int[totalNumSteps][3];

        int step = 0;
        for (int maxNumFails : MAX_NUM_FAILS) {
            for (int populationSize : POPULATION_SIZES) {
                for (int maxGenerations : MAX_GENERATIONS) {
                    params[step][0] = maxNumFails;
                    params[step][1] = populationSize;
                    params[step][2] = maxGenerations;
                    step++;
                }
            }
        }

        return params;
    }

    public static BoolVecProblem getTestProblem() {
        BooleanVector adder3no;
        try {
            adder3no = MyGson.readFromJson(getFolderPath() + "adder3no.json", BooleanVector.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return new BoolVecProblem(adder3no, 3);
    }

    public static void showResults(BoolVecProblem problem, List<OptimizationRunResult> optimizationResults) {
        System.out.println("Showing results:\n");

        RandomizeCrossover<int[]> randomizeCrossover = BooleanSolver.generateRandomizeCrossover(problem.getClbController(), true);
        RandomizeMutation<int[]> randomizeMutation = BooleanSolver.generateRandomizeMutation(problem.getClbController(), Constants.OPERATOR_CHANCE_MULTIPLIER, true);

        List<OperatorStatistics> superGlobalCrossoverStatistics = null;
        List<OperatorStatistics> superGlobalMutationStatistics = null;

        for (OptimizationRunResult optimizationResult : optimizationResults) {
            System.out.println(optimizationResult.setup);
            BooleanSolver.printPerFuncResults(optimizationResult.runResults);

            if (superGlobalCrossoverStatistics == null) {
                superGlobalCrossoverStatistics = optimizationResult.crossoverOperatorStatistics;
                superGlobalMutationStatistics = optimizationResult.mutationOperatorStatistics;
            } else {
                OperatorStatistics.sumStatistics(superGlobalCrossoverStatistics, optimizationResult.crossoverOperatorStatistics);
                OperatorStatistics.sumStatistics(superGlobalMutationStatistics, optimizationResult.mutationOperatorStatistics);
            }
        }

        if (USE_STATISTICS) {
            System.out.println("\nSuper global operator statistics:");
            BooleanSolver.printStats(randomizeCrossover, superGlobalCrossoverStatistics,
                    randomizeMutation, superGlobalMutationStatistics);
        }
    }

    public static void showAverageResults(List<OptimizationRunResult> optimizationResults, int numTests) {
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

    public static String getFolderPath() {
        return Utility.getWorkingDir() + "/data/sessions/";
    }
}
