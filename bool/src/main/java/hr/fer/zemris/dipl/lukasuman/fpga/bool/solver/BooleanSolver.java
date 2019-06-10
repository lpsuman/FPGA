package hr.fer.zemris.dipl.lukasuman.fpga.bool.solver;

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
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.*;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.operators.crossover.RandomizeCrossover;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.operators.mutation.RandomizeMutation;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.evaluator.AbstractEvaluator;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.evaluator.Evaluator;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.operator.OperatorStatistics;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.Solution;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Resetable;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Timer;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.io.Serializable;
import java.util.*;
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

    private final SolverMode solverMode;
    private final Consumer<BoolVectorSolution> solutionConsumer;
    private boolean printOnlyBestSolution;
    private boolean useStatistics;
    private boolean printOnlyGlobalStatistics;
    private int maxNumFails;
    private double noBestThresholdToStopTrying;
    private double bestExistsThresholdToStopTrying;
    private int maxNumBelowThresholdAttempts;
    private double skipIncreaseNumCLBFitnessThreshold;
    private double skipIncreaseNumCLBAmount;

    private ParallelGA<int[]> algorithm;
    private ParallelGAConfig algorithmConfig;
    private AnnealedThreadPoolConfig threadPoolConfig;
    private double mutationChance;
    private boolean shouldStop;
    private Solution<int[]> bestSolution;
    private int numCLBOfBest;
    private Solution<int[]> bestSolutionWithCurrentNumCLB;
    private int highestNumCLBFailed;

    public BooleanSolver(SolverMode solverMode, Consumer<BoolVectorSolution> solutionConsumer,
                         boolean printOnlyBestSolution, boolean useStatistics, boolean printOnlyGlobalStatistics,
                         int maxNumFails, double noBestThresholdToStopTrying, double bestExistsThresholdToStopTrying,
                         int maxNumBelowThresholdAttempts, double skipIncreaseNumCLBFitnessThreshold,
                         double skipIncreaseNumCLBAmount) {

        this.solverMode = Utility.checkNull(solverMode, "solver mode");
        this.solutionConsumer = solutionConsumer;
        this.printOnlyBestSolution = printOnlyBestSolution;
        this.useStatistics = useStatistics;
        this.printOnlyGlobalStatistics = printOnlyGlobalStatistics;
        this.maxNumFails = maxNumFails;
        this.noBestThresholdToStopTrying = noBestThresholdToStopTrying;
        this.bestExistsThresholdToStopTrying = bestExistsThresholdToStopTrying;
        this.maxNumBelowThresholdAttempts = maxNumBelowThresholdAttempts;
        this.skipIncreaseNumCLBFitnessThreshold = skipIncreaseNumCLBFitnessThreshold;
        this.skipIncreaseNumCLBAmount = skipIncreaseNumCLBAmount;

        this.mutationChance = Constants.OPERATOR_CHANCE_MULTIPLIER;
    }

    public BooleanSolver(SolverMode solverMode, Consumer<BoolVectorSolution> solutionConsumer,
                         boolean printOnlyBestSolution, boolean useStatistics, boolean printOnlyGlobalStatistics) {

        this(solverMode, solutionConsumer, printOnlyBestSolution, useStatistics, printOnlyGlobalStatistics,
                Constants.DEFAULT_MAX_NUM_FAILS, Constants.DEFAULT_NO_BEST_THRESHOLD_TO_STOP_TRYING,
                Constants.DEFAULT_BEST_EXISTS_THRESHOLD_TO_STOP_TRYING, Constants.DEFAULT_MAX_NUM_BELOW_THRESHOLD_ATTEMPTS,
                Constants.DEFAULT_SKIP_INCREASE_NUM_CLB_FITNESS_THRESHOLD, Constants.DEFAULT_SKIP_INCREASE_NUM_CLB_AMOUNT);
    }

    public BooleanSolver(SolverMode solverMode, Consumer<BoolVectorSolution> solutionConsumer) {
        this(solverMode, solutionConsumer, true, true, true);
    }

    public BooleanSolver(SolverMode solverMode, Consumer<BoolVectorSolution> solutionConsumer, BooleanSolverConfig config) {
        this(solverMode, solutionConsumer, config.isPrintOnlyBestSolution(), config.isUseStatistics(), config.isPrintOnlyGlobalStatistics());
    }

    public void setAlgorithmConfig(ParallelGAConfig algorithmConfig) {
        this.algorithmConfig = algorithmConfig;
    }

    public void setThreadPoolConfig(AnnealedThreadPoolConfig threadPoolConfig) {
        this.threadPoolConfig = threadPoolConfig;
    }

    public void setMutationChance(double mutationChance) {
        this.mutationChance = mutationChance;
    }

    public BoolVectorSolution solve(BoolVecProblem problem) {
        Timer timer = new Timer();
        timer.start();
        shouldStop = false;
        List<BoolVectorSolution> bruteSolutions = doBruteSolve(problem);
        int numFunctions = problem.getBoolVector().getNumFunctions();

        if (shouldStop) {
            if (bruteSolutions.size() == numFunctions) {
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
            if (bruteSolutions.size() == 1) {
                return solverIsDone(bruteSolutions.get(0), problem);
            } else {
                return solverIsDone(BoolVectorSolution.mergeSolutions(bruteSolutions), problem);
            }
        }

        List<RunResults> perFuncResults = solveIndividually(problem, bruteSolutions);
        printPerFuncResults(perFuncResults);

        int avgNumCLBForSolvedFuncs = -1;
        OptionalDouble optionalAvg = perFuncResults.stream()
                .filter(r -> r.result != null)
                .mapToInt(r -> r.result.getBlockConfiguration().getNumCLB()).average();
        if (optionalAvg.isPresent()) {
            avgNumCLBForSolvedFuncs = (int) Math.ceil(optionalAvg.getAsDouble());
        }

        for (int i = 0; i < perFuncResults.size(); i++) {
            if (perFuncResults.get(i).result == null) {
                System.out.println(String.format("No solution found for function %s, substituting with the brute solution.",
                        problem.getBoolVector().getBoolFunctions().get(i).getName()));
                perFuncResults.get(i).result = bruteSolutions.get(i);
            }
        }

        if (shouldStop) {
            if (perFuncResults.size() == numFunctions) {
                System.out.println("Warning, solver was forcibly stopped so the result might not be the best.");
            } else {
                System.out.println("Solver was forcibly stopped with no solution found.");
                return null;
            }
        } else {
            if (perFuncResults.isEmpty()) {
                System.out.println("No solutions found for individual functions.");
                return null;
            } else {
                System.out.println("Found solutions for individual functions.");
            }
        }

        BoolVectorSolution solution = perFuncResults.get(0).result;

        if (numFunctions == 1) {
            System.out.println("A vector with a single function was given, it's solution is the result.");
            return solverIsDone(solution, problem);
        }

        List<BoolVectorSolution> perFuncSolutions = perFuncResults.stream()
                .map(result -> result.result)
                .collect(Collectors.toList());
        solution = BoolVectorSolution.mergeSolutions(perFuncSolutions);

        if (Constants.STOP_AFTER_MERGING || shouldStop) {
            System.out.println("Returning the merge of individual functions' solutions.");
            return solverIsDone(solution, problem);
        }

        int numCLBInMerged = solution.getBlockConfiguration().getNumCLB();
        timer.lap();
        RunResults mergedRunResult = doARun(problem,
                numCLBInMerged - 1, avgNumCLBForSolvedFuncs * numFunctions,
                solverMode.getMaxRunningTimeMilliseconds() * numFunctions);
        timer.lap();

        printPerFuncResults(perFuncResults);
        System.out.println(String.format("Running timer for the merged solution is %.3f seconds.", timer.getLastLapDuration() / 1000.0));
        System.out.println(String.format("Total running time is %.3f seconds.", timer.getElapsedTime() / 1000.0));

        if (mergedRunResult.result == null) {
            System.out.println("Couldn't find a better solution than the merge of individual function's solutions.");
            return solverIsDone(solution, problem);
        } else {
            System.out.println("Found a solution better than the merge of individual function's solutions");
            return solverIsDone(mergedRunResult.result, problem);
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
            perFunctionBestSolutions.add(BoolVecProblem.bruteSolve(function, numCLBInputs));

            if (shouldStop) {
                break;
            }
        }

        return perFunctionBestSolutions;
    }

    private List<RunResults> solveIndividually(BoolVecProblem problem, List<BoolVectorSolution> bruteSolutions) {
        int numCLBInputs = problem.getClbController().getNumCLBInputs();
        List<BooleanFunction> functions = problem.getBoolVector().getBoolFunctions();
        List<RunResults> perFuncResults = new ArrayList<>();

        for (int i = 0; i < functions.size(); i++) {
            BooleanFunction func = functions.get(i);
            BoolVecProblem singleFuncProblem = new BoolVecProblem(new BooleanVector(func), numCLBInputs);
            int numCLBBrute = bruteSolutions.get(i).getBlockConfiguration().getNumCLB();
            RunResults runResults = doARun(
                    singleFuncProblem,
                    numCLBBrute - 1, (int) Math.ceil((double) func.getNumInputs() / numCLBInputs),
                    solverMode.getMaxRunningTimeMilliseconds());

            perFuncResults.add(runResults);
            if (shouldStop) {
                break;
            }
        }

//        if (perFuncResults.size() != functions.size()) {
//            return null;
//        }

        if (useStatistics) {
            List<OperatorStatistics> crossoverOperatorStatistics = perFuncResults.get(0).randomizeCrossover.getGlobalResults();
            List<OperatorStatistics> mutationOperatorStatistics = perFuncResults.get(0).randomizeMutation.getGlobalResults();
            for (int i = 1; i < perFuncResults.size(); i++) {
                OperatorStatistics.sumStatistics(crossoverOperatorStatistics, perFuncResults.get(i).randomizeCrossover.getGlobalResults());
                OperatorStatistics.sumStatistics(mutationOperatorStatistics, perFuncResults.get(i).randomizeMutation.getGlobalResults());
            }

            System.out.println("Global operator statistics:");
            printStats(perFuncResults.get(0).randomizeCrossover, crossoverOperatorStatistics,
                    perFuncResults.get(0).randomizeMutation, mutationOperatorStatistics);
        }

        return perFuncResults;
    }

    private void printPerFuncResults(List<RunResults> perFuncResults) {
        int numEvaluations = perFuncResults.stream()
                .mapToInt(result -> result.numEvaluations)
                .sum();
        printNumEvaluations(numEvaluations);

        List<Integer> elapsedTimes = perFuncResults.stream()
                .mapToInt(result -> result.elapsedTime)
                .boxed()
                .collect(Collectors.toList());

        List<Integer> numberOfCLBs = perFuncResults.stream()
                .mapToInt(result -> result.result == null ? -1 : result.result.getBlockConfiguration().getNumCLB())
                .boxed()
                .collect(Collectors.toList());

        System.out.println(String.format("Results for functions (index, number of CLBs, elapsed time), average time is %10.3f seconds):",
                (double)(elapsedTimes.stream().mapToInt(i -> i).sum() / elapsedTimes.size()) / 1000.0));

        for (int i = 0; i < elapsedTimes.size(); i++) {
            System.out.println(String.format("%4d   %4d   %10.3f", i, numberOfCLBs.get(i), elapsedTimes.get(i) / 1000.0));
        }
    }

    private BoolVectorSolution solverIsDone(BoolVectorSolution solution, BoolVecProblem problem) {
        solution.setName(problem.getBoolVector().getName());
        if (solutionConsumer != null) {
//            System.out.println("Notifying solution consumer.");
            solutionConsumer.accept(solution);
        }

//        System.out.println("Boolean solver is done.");
        return solution;
    }

    private RunResults doARun(BoolVecProblem problem, int maxNumCLBs, int estimatedNumCLBs, long timeLimit) {
        CLBController controller = problem.getClbController();
        controller.setNumCLB(Math.min(Math.max(1, maxNumCLBs), Math.max(1, estimatedNumCLBs)));
        RandomizeCrossover<int[]> randomCrossovers = generateRandomizeCrossover(controller, useStatistics);
        RandomizeMutation<int[]> randomMutations = generateRandomizeMutation(controller, mutationChance, useStatistics);

        List<BoolVecEvaluator> evaluators = new ArrayList<>();

        Supplier<Evaluator<int[]>> evaluatorSupplier = () -> {
            BoolVecEvaluator evaluator = new BoolVecEvaluator(problem);
            evaluators.add(evaluator);

            if (useStatistics) {
                evaluator.addFitnessListener(randomCrossovers);
                evaluator.addFitnessListener(randomMutations);
            }

            return evaluator;
        };

        BoolVecEvaluator evaluator = new BoolVecEvaluator(problem);

        GAThreadPool<int[]> threadPool;
        if (threadPoolConfig != null) {
            threadPool = new AnnealedThreadPool<>(randomCrossovers, randomMutations, evaluatorSupplier, threadPoolConfig);
        } else {
            threadPool = new AnnealedThreadPool<>(randomCrossovers, randomMutations, evaluatorSupplier);
        }

        if (algorithmConfig != null) {
            algorithm = new ParallelGA<>(problem, evaluator, threadPool, algorithmConfig);
        } else {
            algorithm = new ParallelGA<>(problem, evaluator, threadPool);
        }

        if (useStatistics) {
            algorithm.addFitnessListener(randomCrossovers);
            algorithm.addTerminationListener(randomCrossovers);
            algorithm.addFitnessListener(randomMutations);
            algorithm.addTerminationListener(randomMutations);
        }

        boolean useTime = timeLimit > 0;
        Timer timer = new Timer(timeLimit);
        timer.start();

        bestSolution = null;
        Solution<int[]> solution;
        Solution<int[]> bestButNotSolvedSolution = null;
        highestNumCLBFailed = 0;

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

        numCLBOfBest = -1;
        int numFailed = 0;
        int numConsecutiveBestFitnessBelowThreshold = 0;

        while (true) {
            System.out.println(String.format("Running GA with %6d CLB (attempt %d/%d)",
                    controller.getNumCLB(), numFailed + 1, maxNumFails));

            if (bestSolutionWithCurrentNumCLB != null) {
                problem.setNextToSupply(bestSolutionWithCurrentNumCLB);
            }
            double currMaxNonImprovingGenerationsRatio = algorithm.getMaxNonImprovingGenerationsRatio();
            if (controller.getNumCLB() == 1) {
                algorithm.setMaxNonImprovingGenerationsRatio(currMaxNonImprovingGenerationsRatio
                        * Constants.SINGLE_CLB_MAX_NON_IMPROVING_MODIFIER);
            }
            if (useTime) {
                long remainingTime = timer.getRemainingTime();
                algorithm.setTimeToStop(remainingTime);
            }

            solution = algorithm.run();

            if (controller.getNumCLB() == 1) {
                algorithm.setMaxNonImprovingGenerationsRatio(currMaxNonImprovingGenerationsRatio);
            }
            if (useTime) {
                if (timer.isTimeLimitReached()) {
                    break;
                }
//                System.out.println("Remaining time: " + timer.getRemainingTime());
            }
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

                    if (checkShouldBreak(bestSolution, maxNumCLBs, controller)) {
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

                        if (checkShouldBreak(bestSolution, maxNumCLBs, controller)) {
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

            String testResults = problem.getSolutionTestResults(solution, evaluator, true);
            if (!printOnlyBestSolution) {
                System.out.println(testResults);
            }
            bestSolution = problem.trimmedBoolSolution(solution, evaluator.getUnusedCLBBlocks());
            int numUnusedBlocks = evaluator.getUnusedCLBBlocks().cardinality();
            numCLBOfBest = controller.getNumCLB() - numUnusedBlocks;
            if (numCLBOfBest == 1) {
                System.out.println("A solution with a single CLB was found, stopping");
                break;
            } else {
                System.out.println(String.format("CLBs in solution: %d (%d were unused)", numCLBOfBest, numUnusedBlocks));
            }

            if (numCLBOfBest <= highestNumCLBFailed + 1) {
                break;
            }

            controller.setNumCLB(highestNumCLBFailed + (numCLBOfBest - highestNumCLBFailed) / 2);
            numFailed = 0;
            numConsecutiveBestFitnessBelowThreshold = 0;
        }

        reset();

        return handleResults(bestSolution, numCLBOfBest, problem,
                randomCrossovers, randomMutations, evaluators, evaluator, timer);
    }

    private RunResults handleResults(Solution<int[]> bestSolution, int numCLBOfBest, BoolVecProblem problem,
                                      RandomizeCrossover<int[]> randomCrossovers,
                                      RandomizeMutation<int[]> randomMutations,
                                      List<BoolVecEvaluator> evaluators, BoolVecEvaluator evaluator, Timer timer) {

        if (useStatistics && !printOnlyGlobalStatistics) {
            printStats(randomCrossovers, randomCrossovers.getCumulativeResults(),
                    randomMutations, randomMutations.getCumulativeResults());
        }

        int numEvaluations = evaluators.stream()
                .mapToInt(AbstractEvaluator::getNumEvaluations)
                .sum();
        printNumEvaluations(numEvaluations);

        int elapsedTime = timer.getElapsedTime();

        if (bestSolution == null) {
            return new RunResults(null, randomCrossovers, randomMutations, numEvaluations, elapsedTime);
        }

        problem.getClbController().setNumCLB(numCLBOfBest);
        evaluator.setLogging(true);
        evaluator.evaluateSolution(bestSolution, false);
        int numUnusedBlocksInBest = evaluator.getUnusedCLBBlocks().cardinality();

        if (numUnusedBlocksInBest > 0) {
            bestSolution = problem.trimmedBoolSolution(bestSolution, evaluator.getUnusedCLBBlocks());
            problem.getClbController().setNumCLB(numCLBOfBest -  numUnusedBlocksInBest);
            evaluator.resetLog();
            evaluator.evaluateSolution(bestSolution, false);
        }

        evaluator.setLogging(false);
        System.out.println(evaluator.getLog());
        System.out.println(problem.solutionToString(bestSolution, evaluator.getBlockUsage()));
//        System.out.println(bestSolution.getFitness());

        return new RunResults(new BoolVectorSolution(problem.getBoolVector(), problem.generateBlockConfiguration(bestSolution)),
                randomCrossovers, randomMutations, numEvaluations, elapsedTime);
    }

    private boolean checkShouldBreak(Solution<int[]> bestSolution, int maxNumCLBs, CLBController controller) {
        int currentNumCLB = controller.getNumCLB();
        highestNumCLBFailed = currentNumCLB;
        if (bestSolution == null) {
            if (currentNumCLB == maxNumCLBs) {
                System.out.println("aborting.");
                return true;
            } else {
                controller.setNumCLB(Math.min(currentNumCLB * 2, maxNumCLBs));
                System.out.println("doubling CLBs.");
                return false;
            }
        } else {
            if (currentNumCLB == numCLBOfBest - 1) {
                System.out.println("aborting.");
                return true;
            } else {
                controller.setNumCLB(currentNumCLB + (numCLBOfBest - currentNumCLB) / 2);
                System.out.println("increasing CLBs.");
                return false;
            }
        }
    }

    private static void printStats(RandomizeCrossover<int[]> randomizeCrossover, List<OperatorStatistics> crossoverStats,
                            RandomizeMutation<int[]> randomizeMutation, List<OperatorStatistics> mutationStats) {

        System.out.println(randomizeCrossover.resultsToString(crossoverStats));
        System.out.println(randomizeMutation.resultsToString(mutationStats));
    }

    private static void printNumEvaluations(int numEvaluations) {
        System.out.println("Number of evaluations: " + numEvaluations + "\n");
    }

    private static RandomizeCrossover<int[]> generateRandomizeCrossover(CLBController controller, boolean useStatistics) {
        List<AbstractBoolCrossover> crossoverList = new ArrayList<>();
        crossoverList.add(new SingleBlockCrossover(controller));
        crossoverList.add(new IntervalBlockCrossover(controller));
        crossoverList.add(new SingleBlockCrossover(controller, false));
        crossoverList.add(new IntervalBlockCrossover(controller, false));
        return new RandomizeCrossover<>(crossoverList, useStatistics);
    }

    private static RandomizeMutation<int[]> generateRandomizeMutation(CLBController controller,
                                                                      double mutationChance, boolean useStatistics) {

        List<AbstractBoolMutation> mutationList = new ArrayList<>();
        mutationList.add(new InputFullMutation(controller, mutationChance));
        mutationList.add(new InputSingleMutation(controller, mutationChance));
        mutationList.add(new TableCopyMutation(controller, mutationChance));
        mutationList.add(new TableFullMutation(controller, mutationChance));
        mutationList.add(new TableSingleMutation(controller, mutationChance));
        return new RandomizeMutation<>(mutationList, useStatistics);
    }

    @Override
    public void reset() {
        bestSolutionWithCurrentNumCLB = null;
    }
}
