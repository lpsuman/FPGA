package hr.fer.zemris.dipl.lukasuman.fpga.bool.solver;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BlockConfiguration;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanFunction;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanVector;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.ga.operators.crossover.AbstractBoolCrossover;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.ga.operators.crossover.IntervalBlockCrossover;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.ga.operators.crossover.SingleBlockCrossover;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.ga.operators.mutation.*;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.BoolVecEvaluator;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.BoolVecProblem;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.CLBChangeListener;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.CLBController;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.AnnealedThreadPool;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.GAThreadPool;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.ParallelGA;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.operators.crossover.RandomizeCrossover;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.operators.mutation.RandomizeMutation;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.algorithm.Algorithm;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.evaluator.AbstractEvaluator;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.evaluator.Evaluator;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.operator.OperatorStatistics;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.Solution;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Resetable;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Timer;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BooleanSolver implements Resetable, Serializable {

    private static final long serialVersionUID = 2558057486722850241L;

    private static class RunResults {
        private BoolVectorSolution result;
        private RandomizeCrossover<int[]> randomizeCrossover;
        private RandomizeMutation<int[]> randomizeMutation;
        private int numEvaluations;
        private int elapsedTime;

        public RunResults(BoolVectorSolution result, RandomizeCrossover<int[]> randomizeCrossover, RandomizeMutation<int[]> randomizeMutation, int numEvaluations, int elapsedTime) {
            this.result = result;
            this.randomizeCrossover = randomizeCrossover;
            this.randomizeMutation = randomizeMutation;
            this.numEvaluations = numEvaluations;
            this.elapsedTime = elapsedTime;
        }
    }

    private SolverMode solverMode;
    private Consumer<BoolVectorSolution> solutionConsumer;
    private ParallelGA<int[]> algorithm;
    private boolean shouldStop;
    private Solution<int[]> bestSolutionWithCurrentNumCLB;

    private int maxNumFails;
    private double noBestThresholdToStopTrying;
    private double bestExistsThresholdToStopTrying;
    private int maxNumBelowThresholdAttempts;
    private double skipIncreaseNumCLBFitnessThreshold;
    private double skipIncreaseNumCLBAmount;

    public BooleanSolver(SolverMode solverMode, Consumer<BoolVectorSolution> solutionConsumer) {
        this.solverMode = Utility.checkNull(solverMode, "solver mode");
        this.solutionConsumer = solutionConsumer;

        maxNumFails = Constants.DEFAULT_MAX_NUM_FAILS;
        noBestThresholdToStopTrying = Constants.DEFAULT_NO_BEST_THRESHOLD_TO_STOP_TRYING;
        bestExistsThresholdToStopTrying = Constants.DEFAULT_BEST_EXISTS_THRESHOLD_TO_STOP_TRYING;
        maxNumBelowThresholdAttempts = Constants.DEFAULT_MAX_NUM_BELOW_THRESHOLD_ATTEMPTS;
        skipIncreaseNumCLBFitnessThreshold = Constants.DEFAULT_SKIP_INCREASE_NUM_CLB_FITNESS_THRESHOLD;
        skipIncreaseNumCLBAmount = Constants.DEFAULT_SKIP_INCREASE_NUM_CLB_AMOUNT;
    }

    public BoolVectorSolution solve(BoolVecProblem problem) {
        shouldStop = false;
        List<BoolVectorSolution> perFunctionBestSolutions = doBruteSolve(problem);

        if (shouldStop) {
            if (perFunctionBestSolutions.size() == problem.getBoolVector().getNumFunctions()) {
                System.out.println("Warning, solver was forcibly stopped so the result might not be the best.");
            } else {
                System.out.println("Solver was forcibly stopped with no solution found.");
                return null;
            }
        } else {
            System.out.println("Found brute solutions.");
        }

        if (solverMode == SolverMode.BRUTE || shouldStop) {
            System.out.println("Returning brute solution as the result.");
            if (perFunctionBestSolutions.size() == 1) {
                return solverIsDone(perFunctionBestSolutions.get(0));
            } else {
                return solverIsDone(BoolVectorSolution.mergeSolutions(perFunctionBestSolutions));
            }
        }

        List<RunResults> perFuncResults = solveIndividually(problem);

        if (shouldStop) {
            if (perFuncResults.size() == problem.getBoolVector().getNumFunctions()) {
                System.out.println("Warning, solver was forcibly stopped so the result might not be the best.");
            } else {
                System.out.println("Solver was forcibly stopped with no solution found.");
                return null;
            }
        } else {
            System.out.println("Found solutions for individual functions.");
        }

        BoolVectorSolution solution = perFuncResults.get(0).result;


        if (problem.getBoolVector().getNumFunctions() == 1) {
            System.out.println("A vector with a single function was given, it's solution is the result.");
            return solverIsDone(solution);
        }

        List<BoolVectorSolution> perFuncSolutions = perFuncResults.stream()
                .map(result -> result.result)
                .collect(Collectors.toList());
        solution = BoolVectorSolution.mergeSolutions(perFuncSolutions);

        if (Constants.STOP_AFTER_MERGING || solverMode == SolverMode.FAST || shouldStop) {
            System.out.println("Returning the merge of individual functions' solutions.");
            return solverIsDone(solution);
        }

        int numCLBEstimation = perFuncResults.stream()
                .mapToInt(result -> result.result.getBlockConfiguration().getNumCLB())
                .sum() - 1;

        problem.getClbController().setNumCLB(numCLBEstimation);
        RunResults mergedRunResult = doARun(problem, true, false);

        if (mergedRunResult.result == null) {
            System.out.println("Couldn't find a better solution than the merge of individual function's solutions.");
            return solverIsDone(solution);
        } else {
            return solverIsDone(mergedRunResult.result);
        }
    }

    public void stop() {
        shouldStop = true;
        algorithm.stop();
    }

    private List<BoolVectorSolution> doBruteSolve(BoolVecProblem problem) {
        int numCLBInputs = problem.getClbController().getNumCLBInputs();
        List<BooleanFunction> functions = problem.getBoolVector().getBoolFunctions();

        List<BoolVectorSolution> perFunctionBestSolutions = new ArrayList<>();
        for (BooleanFunction function : functions) {
            BlockConfiguration bruteSolution = BoolVecProblem.bruteSolve(function, numCLBInputs);
            perFunctionBestSolutions.add(new BoolVectorSolution(new BooleanVector(function), bruteSolution));

            if (shouldStop) {
                break;
            }
        }

        return perFunctionBestSolutions;
    }

    private List<RunResults> solveIndividually(BoolVecProblem problem) {
        int numCLBInputs = problem.getClbController().getNumCLBInputs();
        List<BooleanFunction> functions = problem.getBoolVector().getBoolFunctions();

        List<RunResults> perFuncResults = new ArrayList<>();

        for (BooleanFunction function : functions) {
            BoolVecProblem singleFuncProblem = new BoolVecProblem(new BooleanVector(function), numCLBInputs);
            RunResults runResults = doARun(singleFuncProblem, true, true);

            if (runResults != null) {
                perFuncResults.add(runResults);
            }

            if (shouldStop) {
                break;
            }
        }

        List<OperatorStatistics> crossoverOperatorStatistics = perFuncResults.get(0).randomizeCrossover.getGlobalResults();
        List<OperatorStatistics> mutationOperatorStatistics = perFuncResults.get(0).randomizeMutation.getGlobalResults();
        for (int i = 1; i < functions.size(); i++) {
            OperatorStatistics.sumStatistics(crossoverOperatorStatistics, perFuncResults.get(i).randomizeCrossover.getGlobalResults());
            OperatorStatistics.sumStatistics(mutationOperatorStatistics, perFuncResults.get(i).randomizeMutation.getGlobalResults());
        }

        System.out.println("Global operator statistics:");
        int numEvaluations = perFuncResults.stream()
                .mapToInt(result -> result.numEvaluations)
                .sum();
        printStats(perFuncResults.get(0).randomizeCrossover, crossoverOperatorStatistics,
                perFuncResults.get(0).randomizeMutation, mutationOperatorStatistics, numEvaluations);

        List<Integer> elapsedTimes = perFuncResults.stream()
                .mapToInt(result -> result.elapsedTime)
                .boxed()
                .collect(Collectors.toList());

        List<Integer> numberOfCLBs = perFuncResults.stream()
                .mapToInt(result -> result.result.getBlockConfiguration().getNumCLB())
                .boxed()
                .collect(Collectors.toList());

        System.out.println(String.format("Results for functions (index, number of CLBs, elapsed time), average time is %10.3f seconds):",
                (double)(elapsedTimes.stream().mapToInt(i -> i).sum() / elapsedTimes.size()) / 1000.0));

        for (int i = 0; i < elapsedTimes.size(); i++) {
            System.out.println(String.format("%4d   %4d   %10.3f", i, numberOfCLBs.get(i), elapsedTimes.get(i) / 1000.0));
        }

        return perFuncResults;
    }

    private BoolVectorSolution solverIsDone(BoolVectorSolution solution) {
        if (solutionConsumer != null) {
//            System.out.println("Notifying solution consumer.");
            solutionConsumer.accept(solution);
        }

//        System.out.println("Boolean solver is done.");
        return solution;
    }

    private RunResults doARun(BoolVecProblem problem, boolean canDecreaseNumCLB, boolean canIncreaseNumCLB) {
        CLBController controller = problem.getClbController();
        RandomizeCrossover<int[]> randomCrossovers = generateRandomizeCrossover(controller);
        RandomizeMutation<int[]> randomMutations = generateRandomizeMutation(controller);

        List<BoolVecEvaluator> evaluators = new ArrayList<>();

        Supplier<Evaluator<int[]>> evaluatorSupplier = () -> {
            BoolVecEvaluator evaluator = new BoolVecEvaluator(problem);
            evaluators.add(evaluator);
            evaluator.addFitnessListener(randomCrossovers);
            evaluator.addFitnessListener(randomMutations);
            return evaluator;
        };

        BoolVecEvaluator evaluator = new BoolVecEvaluator(problem);

        GAThreadPool<int[]> threadPool = new AnnealedThreadPool<>(randomCrossovers, randomMutations, evaluatorSupplier);
        algorithm = new ParallelGA<>(problem, evaluator, threadPool);
        algorithm.addFitnessListener(randomCrossovers);
        algorithm.addTerminationListener(randomCrossovers);
        algorithm.addFitnessListener(randomMutations);
        algorithm.addTerminationListener(randomMutations);

        if (!canIncreaseNumCLB) {
            algorithm.setDoFullRuns(true);
        }

        Solution<int[]> bestSolution = null;
        Solution<int[]> solution;
        Solution<int[]> bestButNotSolvedSolution = null;

        controller.addCLBChangeListener(new CLBChangeListener() {
            @Override
            public void numCLBInputsChanged(int prevNumCLBInputs, int newNumCLBInputs) {
                // do nothing
            }

            @Override
            public void numCLBChanged(int prevNumCLB, int newNumCLB) {
                bestSolutionWithCurrentNumCLB = null;
            }
        });

        int numCLBOfBest = -1;
        int numFailed = 0;
        int numConsecutiveBestFitnessBelowThreshold = 0;
        Timer timer = new Timer();

        while (true) {
            System.out.println(String.format("Running GA with %6d CLB (attempt %d/%d)",
                    controller.getNumCLB(), numFailed + 1, maxNumFails));

            if (bestSolutionWithCurrentNumCLB != null) {
                problem.setNextToSupply(bestSolutionWithCurrentNumCLB);
            }

            if (controller.getNumCLB() == 1) {
                algorithm.setMaxNonImprovingGenerationsRatio(algorithm.getMaxNonImprovingGenerationsRatio() / 10.0);
            }

            solution = algorithm.run();

            if (shouldStop) {
                break;
            }

            if (bestSolution == null) {
                bestButNotSolvedSolution = solution;
            }

            if (bestSolutionWithCurrentNumCLB == null
                    || solution.getFitness() > bestSolutionWithCurrentNumCLB.getFitness()) {
                bestSolutionWithCurrentNumCLB = solution;
            }

            if (solution.getFitness() != Constants.FITNESS_SCALE) {
                numFailed++;

                if (numFailed >= maxNumFails) {
                    System.out.print("Failed to find a solution after maximum number of tries, ");

                    if (checkShouldBreak(bestSolution, bestButNotSolvedSolution.getFitness(),
                            controller, canIncreaseNumCLB)) {
                        break;
                    } else {
                        numFailed = 0;
                        numConsecutiveBestFitnessBelowThreshold = 0;
                        continue;
                    }
                }

                double stopTryingThreshold = noBestThresholdToStopTrying;
                if (bestSolution != null) {
                    stopTryingThreshold = bestExistsThresholdToStopTrying;
                }

                if (solution.getFitness() < stopTryingThreshold) {
                    numConsecutiveBestFitnessBelowThreshold++;

                    if (numConsecutiveBestFitnessBelowThreshold >= maxNumBelowThresholdAttempts) {
                        System.out.print(String.format("Exceeded maximum number of below threshold attempts. " +
                                "Fitness of the best solution was below %.4f for %d consecutive attempts, ",
                                stopTryingThreshold, maxNumBelowThresholdAttempts));

                        if (checkShouldBreak(bestSolution, bestButNotSolvedSolution.getFitness(),
                                controller, canIncreaseNumCLB)) {
                            break;
                        } else {
                            numFailed = 0;
                            numConsecutiveBestFitnessBelowThreshold = 0;
                            continue;
                        }
                    }
                } else {
                    numConsecutiveBestFitnessBelowThreshold = 0;
                }

                System.out.println("No solution found, trying again.");
                continue;
            }

            bestSolution = solution;
            numCLBOfBest = controller.getNumCLB();
//            algorithm.setDoFullRuns(true);
            System.out.println(problem.getSolutionTestResults(bestSolution, evaluator));
            int numUnusedBlocks = evaluator.getUnusedBlocks().cardinality();

            if (!canDecreaseNumCLB) {
                System.out.println("Not allowed to decrease num of CLB, stopping.");
                break;
            }

            int newNumCLB = controller.getNumCLB() - numUnusedBlocks - 1;
            if (newNumCLB < 1) {
                System.out.println("A solution with a single CLB was found, stopping");
                break;
            }

            controller.setNumCLB(controller.getNumCLB() - numUnusedBlocks - 1);
            numFailed = 0;
            numConsecutiveBestFitnessBelowThreshold = 0;
        }

        if (bestSolution == null) {
            return null;
        }

        return handleResults(bestSolution, numCLBOfBest, problem,
                randomCrossovers, randomMutations, evaluators, evaluator, timer);
    }

    private static RunResults handleResults(Solution<int[]> bestSolution, int numCLBOfBest, BoolVecProblem problem,
                                      RandomizeCrossover<int[]> randomCrossovers,
                                      RandomizeMutation<int[]> randomMutations,
                                      List<BoolVecEvaluator> evaluators, BoolVecEvaluator evaluator, Timer timer) {

        int numEvaluations = evaluators.stream()
                .mapToInt(AbstractEvaluator::getNumEvaluations)
                .sum();
        printStats(randomCrossovers, randomCrossovers.getCumulativeResults(),
                randomMutations, randomMutations.getCumulativeResults(), numEvaluations);
        int elapsedTime = timer.getElapsedTime();

        if (bestSolution == null) {
            return new RunResults(null, randomCrossovers, randomMutations, numEvaluations, elapsedTime);
        }

        problem.getClbController().setNumCLB(numCLBOfBest);
        evaluator.setLogging(true);
        evaluator.evaluateSolution(bestSolution, false);
        int numUnusedBlocksInBest = evaluator.getUnusedBlocks().cardinality();

        if (numUnusedBlocksInBest > 0) {
            bestSolution = problem.trimmedBoolSolution(bestSolution, evaluator.getUnusedBlocks());
            problem.getClbController().setNumCLB(numCLBOfBest -  numUnusedBlocksInBest);
            evaluator.resetLog();
            evaluator.evaluateSolution(bestSolution, false);
        }

        evaluator.setLogging(false);
        System.out.println(evaluator.getLog());
        System.out.println(problem.solutionToString(bestSolution, evaluator.getBlockUsage()));
        System.out.println(bestSolution.getFitness());

        return new RunResults(new BoolVectorSolution(problem.getBoolVector(), problem.generateBlockConfiguration(bestSolution)),
                randomCrossovers, randomMutations, numEvaluations, elapsedTime);
    }

    private boolean checkShouldBreak(Solution<int[]> bestSolution, double bestFitness,
                                            CLBController controller, boolean canIncreaseNumCLB) {

        if (bestSolution == null) {
            if (canIncreaseNumCLB) {
                int numCLBIncreaseAmount = 1;

                if (bestFitness < skipIncreaseNumCLBFitnessThreshold) {
                    System.out.print("doing a skip, ");
                    numCLBIncreaseAmount = Math.max(1,
                            (int)(controller.getNumCLB() * skipIncreaseNumCLBAmount));
                }

                System.out.println(String.format("increasing number of CLBs by %d.", numCLBIncreaseAmount));
                controller.setNumCLB(controller.getNumCLB() + numCLBIncreaseAmount);
                return false;
            } else {
                System.out.println("not allowed to increase the number of CLBs, ");
            }
        }

        System.out.println("aborting.");
        return true;
    }

    private static void printStats(RandomizeCrossover<int[]> randomizeCrossover, List<OperatorStatistics> crossoverStats,
                            RandomizeMutation<int[]> randomizeMutation, List<OperatorStatistics> mutationStats,
                            int numEvaluations) {

        System.out.println(randomizeCrossover.resultsToString(crossoverStats));
        System.out.println(randomizeMutation.resultsToString(mutationStats));
        System.out.println("Number of evaluations: " + numEvaluations + "\n");
    }

    private static RandomizeCrossover<int[]> generateRandomizeCrossover(CLBController controller) {
        List<AbstractBoolCrossover> crossoverList = new ArrayList<>();
        crossoverList.add(new SingleBlockCrossover(controller));
        crossoverList.add(new IntervalBlockCrossover(controller));
        crossoverList.add(new SingleBlockCrossover(controller, false));
        crossoverList.add(new IntervalBlockCrossover(controller, false));
        return new RandomizeCrossover<>(crossoverList);
    }

    private static RandomizeMutation<int[]> generateRandomizeMutation(CLBController controller) {
        List<AbstractBoolMutation> mutationList = new ArrayList<>();
        mutationList.add(new InputFullMutation(controller));
        mutationList.add(new InputSingleMutation(controller));
        mutationList.add(new TableCopyMutation(controller));
        mutationList.add(new TableFullMutation(controller));
        mutationList.add(new TableSingleMutation(controller));
        return new RandomizeMutation<>(mutationList);
    }

    @Override
    public void reset() {
        bestSolutionWithCurrentNumCLB = null;
    }
}
