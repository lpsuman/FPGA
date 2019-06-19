package hr.fer.zemris.dipl.lukasuman.fpga.bool.solver;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanFunction;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanVector;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.ga.operators.crossover.AbstractBoolCrossover;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.ga.operators.crossover.IntervalBlockCrossover;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.ga.operators.crossover.SingleBlockCrossover;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.ga.operators.crossover.SubBlockCrossover;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.ga.operators.mutation.*;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.BoolVecEvaluator;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.BoolVecProblem;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.CLBChangeListener;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.CLBController;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.*;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.operators.crossover.RandomizeCrossover;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.operators.mutation.RandomizeMutation;
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

    public static class RunResults {
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
    private boolean solveIndividually;
    private int maxNumFails;
    private double noBestThresholdToStopTrying;
    private double bestExistsThresholdToStopTrying;
    private int maxNumBelowThresholdAttempts;

    private ParallelGA<int[]> algorithm;
    private ParallelGAConfig algorithmConfig;
    private AnnealedThreadPoolConfig threadPoolConfig;
    private double mutationChance;
    private boolean shouldStop;
    private Solution<int[]> bestSolution;
    private int numCLBOfBest;
    private Solution<int[]> bestSolutionWithCurrentNumCLB;
    private int numCLBCurrentSolution;
    private int highestNumCLBFailed;

    private boolean enablePrinting;
    private List<RunResults> runResults;
    private List<OperatorStatistics> crossoverOperatorStatistics;
    private List<OperatorStatistics> mutationOperatorStatistics;

    public BooleanSolver(SolverMode solverMode, Consumer<BoolVectorSolution> solutionConsumer,
                         boolean printOnlyBestSolution, boolean useStatistics, boolean printOnlyGlobalStatistics, boolean solveIndividually,
                         int maxNumFails, double noBestThresholdToStopTrying, double bestExistsThresholdToStopTrying,
                         double maxNumBelowThresholdAttempts) {

        this.solverMode = Utility.checkNull(solverMode, "solver mode");
        this.solutionConsumer = solutionConsumer;
        this.printOnlyBestSolution = printOnlyBestSolution;
        this.useStatistics = useStatistics;
        this.printOnlyGlobalStatistics = printOnlyGlobalStatistics;
        this.maxNumFails = maxNumFails;
        this.noBestThresholdToStopTrying = noBestThresholdToStopTrying * Constants.FITNESS_SCALE;
        this.bestExistsThresholdToStopTrying = bestExistsThresholdToStopTrying * Constants.FITNESS_SCALE;
        this.maxNumBelowThresholdAttempts = (int)(maxNumFails * maxNumBelowThresholdAttempts);
        enablePrinting = true;
        this.solveIndividually = solveIndividually;
        this.mutationChance = Constants.OPERATOR_CHANCE_MULTIPLIER;
    }

    public BooleanSolver(SolverMode solverMode, Consumer<BoolVectorSolution> solutionConsumer,
                         boolean printOnlyBestSolution, boolean useStatistics, boolean printOnlyGlobalStatistics, boolean solveIndividually) {

        this(solverMode, solutionConsumer, printOnlyBestSolution, useStatistics, printOnlyGlobalStatistics, solveIndividually,
                Constants.DEFAULT_MAX_NUM_FAILS, Constants.DEFAULT_NO_BEST_THRESHOLD_TO_STOP_TRYING,
                Constants.DEFAULT_BEST_EXISTS_THRESHOLD_TO_STOP_TRYING, Constants.DEFAULT_MAX_NUM_BELOW_THRESHOLD_ATTEMPTS);
    }

    public BooleanSolver(SolverMode solverMode, Consumer<BoolVectorSolution> solutionConsumer) {
        this(solverMode, solutionConsumer, true, true, true, true);
    }

    public BooleanSolver(SolverMode solverMode, Consumer<BoolVectorSolution> solutionConsumer, BooleanSolverConfig config) {
        this(solverMode, solutionConsumer, config.isPrintOnlyBestSolution(), config.isUseStatistics(),
                config.isPrintOnlyGlobalStatistics(), config.isSolveIndividually(), config.getMaxNumFails(),
                config.getNoBestThresholdToStopTrying(), config.getBestExistsThresholdToStopTrying(),
                config.getMaxNumBelowThresholdAttempts());
    }

    public BoolVectorSolution solve(BoolVecProblem problem) {
        Timer timer = new Timer();
        timer.start();

        shouldStop = false;
        List<BoolVectorSolution> bruteSolutions = doBruteSolve(problem);
        int numFunctions = problem.getBoolVector().getNumFunctions();

        if (shouldStop) {
            if (bruteSolutions.size() == numFunctions) {
                if (enablePrinting)
                    System.out.println("Warning, solver was forcibly stopped so the result might not be the best.");
            } else {
                if (enablePrinting) System.out.println("Solver was forcibly stopped with no solution found.");
                return null;
            }
        } else {
            if (enablePrinting) System.out.println("Found brute solutions.");
        }

        if (solverMode == SolverMode.BRUTE || shouldStop) {
            if (enablePrinting) System.out.println("Returning brute solution as the result.");
            if (bruteSolutions.size() == 1) {
//                return solverIsDone(bruteSolutions.get(0), problem);
                return solverIsDone(BoolVectorSolution.removeRedundantCLBs(bruteSolutions.get(0)), problem);
            } else {
                if (numFunctions == 1) {
                    return solverIsDone(BoolVectorSolution.removeRedundantCLBs(bruteSolutions.get(0)), problem);
                } else {
                    return solverIsDone(BoolVectorSolution.mergeSolutions(bruteSolutions), problem);
                }
            }
        }

        List<RunResults> perFuncResults = null;
        BoolVectorSolution solution;
        int numCLBEstimation;
        runResults = new ArrayList<>();
        int totalNumEvaluations = 0;

        if (solveIndividually) {
            perFuncResults = solveIndividually(problem, bruteSolutions);
            if (enablePrinting) printPerFuncResults(perFuncResults);

            int avgNumCLBForSolvedFuncs = -1;
            OptionalDouble optionalAvg = perFuncResults.stream()
                    .filter(r -> r.result != null)
                    .mapToInt(r -> r.result.getBlockConfiguration().getNumCLB()).average();
            if (optionalAvg.isPresent()) {
                avgNumCLBForSolvedFuncs = (int) Math.ceil(optionalAvg.getAsDouble());
            }

            for (int i = 0; i < perFuncResults.size(); i++) {
                if (perFuncResults.get(i).result == null) {
                    if (enablePrinting)
                        System.out.println(String.format("No solution found for function %s, substituting with the brute solution.",
                                problem.getBoolVector().getBoolFunctions().get(i).getName()));
                    perFuncResults.get(i).result = bruteSolutions.get(i);
                }
            }

            if (shouldStop) {
                if (perFuncResults.size() == numFunctions) {
                    if (enablePrinting)
                        System.out.println("Warning, solver was forcibly stopped so the result might not be the best.");
                } else {
                    if (enablePrinting) System.out.println("Solver was forcibly stopped with no solution found.");
                    return null;
                }
            } else {
                if (perFuncResults.isEmpty()) {
                    if (enablePrinting) System.out.println("No solutions found for individual functions.");
                    return null;
                } else {
                    if (enablePrinting) System.out.println("Found solutions for individual functions.");
                }
            }

            solution = perFuncResults.get(0).result;

            if (numFunctions == 1) {
                if (enablePrinting)
                    System.out.println("A vector with a single function was given, it's solution is the result.");
                return solverIsDone(BoolVectorSolution.removeRedundantCLBs(solution), problem);
            }

            List<BoolVectorSolution> perFuncSolutions = perFuncResults.stream()
                    .map(result -> result.result)
                    .collect(Collectors.toList());
            solution = BoolVectorSolution.mergeSolutions(perFuncSolutions);

            if (Constants.STOP_AFTER_MERGING || shouldStop) {
                if (enablePrinting) System.out.println("Returning the merge of individual functions' solutions.");
                return solverIsDone(solution, problem);
            }

            runResults.addAll(perFuncResults);
            totalNumEvaluations = perFuncResults.stream()
                    .mapToInt(result -> result.numEvaluations)
                    .sum();
            numCLBEstimation = avgNumCLBForSolvedFuncs * numFunctions - 1;
        } else {
            if (numFunctions == 1) {
                solution = BoolVectorSolution.removeRedundantCLBs(bruteSolutions.get(0));
            } else {
                solution = BoolVectorSolution.mergeSolutions(bruteSolutions);
            }
            numCLBEstimation = 2 * numFunctions;
        }

        RunResults mergedRunResult = doARun(problem, solution.getBlockConfiguration().getNumCLB() - 1, numCLBEstimation,
                solverMode.getMaxRunningTimeMilliseconds() * numFunctions);

        totalNumEvaluations += mergedRunResult.numEvaluations;
        runResults.add(mergedRunResult);
        runResults.add(new RunResults(mergedRunResult.result == null ? solution : mergedRunResult.result,
                mergedRunResult.randomizeCrossover, mergedRunResult.randomizeMutation,
                totalNumEvaluations, timer.getElapsedTime()));

        if (useStatistics) {
            if (solveIndividually) {
                OperatorStatistics.sumStatistics(crossoverOperatorStatistics, mergedRunResult.randomizeCrossover.getGlobalResults());
                OperatorStatistics.sumStatistics(mutationOperatorStatistics, mergedRunResult.randomizeMutation.getGlobalResults());
            } else {
                crossoverOperatorStatistics = mergedRunResult.randomizeCrossover.getGlobalResults();
                mutationOperatorStatistics = mergedRunResult.randomizeMutation.getGlobalResults();
            }
            if (enablePrinting) {
                printGlobalOperatorStatistics(mergedRunResult.randomizeCrossover, mergedRunResult.randomizeMutation);
            }
        }

        if (enablePrinting) {
            if (solveIndividually) {
                printPerFuncResults(perFuncResults);
            }
            System.out.println(String.format("Running timer for the merged solution is %.3f seconds.", timer.getLastLapDuration() / 1000.0));
            System.out.println(String.format("Total running time is %.3f seconds.", timer.getElapsedTime() / 1000.0));
        }

        if (mergedRunResult.result == null) {
            if (enablePrinting) System.out.println("Couldn't find a better solution than the merge of individual function's solutions.");
            return solverIsDone(solution, problem);
        } else {
            if (enablePrinting) System.out.println("Found a solution better than the merge of individual function's solutions");
            return solverIsDone(BoolVectorSolution.removeRedundantCLBs(mergedRunResult.result), problem);
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
            crossoverOperatorStatistics = perFuncResults.get(0).randomizeCrossover.getGlobalResults();
            mutationOperatorStatistics = perFuncResults.get(0).randomizeMutation.getGlobalResults();
            for (int i = 1; i < perFuncResults.size(); i++) {
                OperatorStatistics.sumStatistics(crossoverOperatorStatistics, perFuncResults.get(i).randomizeCrossover.getGlobalResults());
                OperatorStatistics.sumStatistics(mutationOperatorStatistics, perFuncResults.get(i).randomizeMutation.getGlobalResults());
            }

            if (enablePrinting) printGlobalOperatorStatistics(perFuncResults.get(0).randomizeCrossover, perFuncResults.get(0).randomizeMutation);
        }

        return perFuncResults;
    }

    public static void printPerFuncResults(List<RunResults> perFuncResults) {
        printPerFuncResultsMulti(Collections.singletonList(perFuncResults));
    }

    public static void printPerFuncResultsMulti(List<List<RunResults>> perFuncResults) {
        int numTests = perFuncResults.size();
        int numResults = perFuncResults.get(0).size();

        int numEvaluations = perFuncResults.stream()
                .flatMap(List::stream)
                .mapToInt(result -> result.numEvaluations)
                .sum() / numTests;
        printNumEvaluations(numEvaluations);

        List<Double> elapsedTimes = new ArrayList<>();
        List<Double> numberOfCLBs = new ArrayList<>();

        for (int i = 0; i < numResults; i++) {
            int elapsedTimesSum = 0;
            int numCLBsSum = 0;
            for (int j = 0; j < numTests; j++) {
                elapsedTimesSum += perFuncResults.get(j).get(i).elapsedTime;
                if (perFuncResults.get(j).get(i).result != null) {
                    numCLBsSum += perFuncResults.get(j).get(i).result.getBlockConfiguration().getNumCLB();
                }
            }
            elapsedTimes.add((double) elapsedTimesSum / numTests);
            numberOfCLBs.add((double) numCLBsSum / numTests);
        }

        System.out.println(String.format("Results for functions (index, number of CLBs, elapsed time), average time is %10.3f seconds):",
                (elapsedTimes.stream().mapToDouble(i -> i).sum() / elapsedTimes.size()) / 1000.0));

        for (int i = 0; i < elapsedTimes.size(); i++) {
            System.out.println(String.format("%4d   %4.2f   %10.3f", i, numberOfCLBs.get(i), elapsedTimes.get(i) / 1000.0));
        }
    }

    private BoolVectorSolution solverIsDone(BoolVectorSolution solution, BoolVecProblem problem) {
        solution.setName(problem.getBoolVector().getName());
        if (solutionConsumer != null) {
//            if (enablePrinting) System.out.println("Notifying solution consumer.");
            solutionConsumer.accept(solution);
        }

        if (enablePrinting) System.out.println("Boolean solver is done.");
        return solution;
    }

    private RunResults doARun(BoolVecProblem problem1, int maxNumCLBs, int estimatedNumCLBs, long timeLimit) {
        final BoolVecProblem problem = new BoolVecProblem(problem1.getBoolVector(), problem1.getClbController().getNumCLBInputs());
        CLBController controller = problem.getClbController();
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
        bestSolutionWithCurrentNumCLB = null;
        controller.setNumCLB(Math.min(Math.max(1, maxNumCLBs), Math.max(1, estimatedNumCLBs)));
        RandomizeCrossover<int[]> randomCrossovers = generateRandomizeCrossover(controller, useStatistics);
        RandomizeMutation<int[]> randomMutations = generateRandomizeMutation(controller, mutationChance, useStatistics);

        int totalNumEvaluations = 0;
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
        threadPool.setEnablePrinting(enablePrinting);

        if (algorithmConfig != null) {
            algorithm = new ParallelGA<>(problem, evaluator, threadPool, algorithmConfig);
        } else {
            algorithm = new ParallelGA<>(problem, evaluator, threadPool);
        }
        algorithm.setEnablePrinting(enablePrinting);

        if (useStatistics) {
            algorithm.addFitnessListener(randomCrossovers);
            algorithm.addTerminationListener(randomCrossovers);
            algorithm.addFitnessListener(randomMutations);
            algorithm.addTerminationListener(randomMutations);
        }

        boolean useTime = timeLimit > 0;
        Timer timer = new Timer(timeLimit);
        timer.start();

        if (maxNumCLBs == 0) {
            return handleResults(null, -1, problem,
                    randomCrossovers, randomMutations, totalNumEvaluations, evaluator, timer);
        }

        bestSolution = null;
        Solution<int[]> solution;
        Solution<int[]> bestButNotSolvedSolution = null;
        highestNumCLBFailed = 0;

        numCLBOfBest = -1;
        int numFailed = 0;
        int numConsecutiveBestFitnessBelowThreshold = 0;

        while (true) {
            if (enablePrinting) System.out.println(String.format("Running GA with %6d CLB (attempt %d/%d)",
                    controller.getNumCLB(), numFailed + 1, maxNumFails));

            if (bestSolutionWithCurrentNumCLB != null) {
                if (numCLBCurrentSolution == controller.getNumCLB()) {
                    problem.setNextToSupply(bestSolutionWithCurrentNumCLB);
                } else {
                    bestSolutionWithCurrentNumCLB = null;
                }
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

            for (BoolVecEvaluator eval : evaluators) {
                totalNumEvaluations += eval.getNumEvaluations();
            }
            evaluators.clear();

            if (controller.getNumCLB() == 1) {
                algorithm.setMaxNonImprovingGenerationsRatio(currMaxNonImprovingGenerationsRatio);
            }
            if (useTime) {
                if (timer.isTimeLimitReached()) {
                    break;
                }
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
                numCLBCurrentSolution = controller.getNumCLB();
            }

            if (solution.getFitness() != Constants.FITNESS_SCALE) {
                numFailed++;

                if (numFailed >= maxNumFails) {
                    if (enablePrinting) System.out.print("Failed to find a solution after maximum number of tries, ");

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
                        if (enablePrinting) System.out.print(String.format("Exceeded maximum number of below threshold attempts. " +
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

                if (enablePrinting) System.out.println("No solution found, trying again.");
                continue;
            }

            int prevNumCLB = controller.getNumCLB();
            solution = problem.removeRedundant(solution);
            int numRedundantCLB = prevNumCLB - controller.getNumCLB();
            String testResults = problem.getSolutionTestResults(solution, evaluator, true);
            if (enablePrinting && !printOnlyBestSolution) {
                System.out.println(testResults);
            }
            bestSolution = problem.trimmedBoolSolution(solution, evaluator.getUnusedCLBBlocks());
            int numUnusedBlocks = evaluator.getUnusedCLBBlocks().cardinality();
            numCLBOfBest = controller.getNumCLB() - numUnusedBlocks;
            if (numCLBOfBest == 1) {
                if (enablePrinting) System.out.println("A solution with a single CLB was found, stopping");
                break;
            } else if (enablePrinting) {
                System.out.println(String.format("CLBs in solution: %d (%d were unused, %d were redundant)", numCLBOfBest, numUnusedBlocks, numRedundantCLB));
            }

            if (numCLBOfBest <= highestNumCLBFailed + 1) {
                break;
            }

            controller.setNumCLB(numCLBOfBest);
            int nextNumCLB = highestNumCLBFailed + (numCLBOfBest - highestNumCLBFailed) / 2;
            List<Solution<int[]>> trimmedRandomSolutions = problem.trimRandom(bestSolution, nextNumCLB,
                    Math.min(10 * algorithmConfig.getElitismSize(), (int)(0.1 * algorithmConfig.getPopulationSize())));

            bestSolutionWithCurrentNumCLB = null;
            controller.setNumCLB(nextNumCLB);
            problem.setNextToSupplyList(trimmedRandomSolutions, 0);
            numFailed = 0;
            numConsecutiveBestFitnessBelowThreshold = 0;
        }

        reset();

        return handleResults(bestSolution, numCLBOfBest, problem,
                randomCrossovers, randomMutations, totalNumEvaluations, evaluator, timer);
    }

    private RunResults handleResults(Solution<int[]> bestSolution, int numCLBOfBest, BoolVecProblem problem,
                                      RandomizeCrossover<int[]> randomCrossovers,
                                      RandomizeMutation<int[]> randomMutations,
                                     int numEvaluations, BoolVecEvaluator evaluator, Timer timer) {

        if (enablePrinting && useStatistics && !printOnlyGlobalStatistics) {
            printStats(randomCrossovers, randomCrossovers.getCumulativeResults(),
                    randomMutations, randomMutations.getCumulativeResults());
        }

        if (enablePrinting) printNumEvaluations(numEvaluations);

        int elapsedTime = timer.getElapsedTime();

        if (bestSolution == null) {
            return new RunResults(null, randomCrossovers, randomMutations, numEvaluations, elapsedTime);
        }

        bestSolutionWithCurrentNumCLB = null;
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
        if (enablePrinting) {
            System.out.println(evaluator.getLog());
            System.out.println(problem.solutionToString(bestSolution, evaluator.getBlockUsage()));
        }

        return new RunResults(new BoolVectorSolution(problem.getBoolVector(), problem.generateBlockConfiguration(bestSolution)),
                randomCrossovers, randomMutations, numEvaluations, elapsedTime);
    }

    private boolean checkShouldBreak(Solution<int[]> bestSolution, int maxNumCLBs, CLBController controller) {
        int currentNumCLB = controller.getNumCLB();
        highestNumCLBFailed = currentNumCLB;
        if (bestSolution == null) {
            if (currentNumCLB == maxNumCLBs) {
                if (enablePrinting) System.out.println("aborting.");
                return true;
            } else {
                bestSolutionWithCurrentNumCLB = null;
                controller.setNumCLB(Math.min(currentNumCLB * 2, maxNumCLBs));
                if (enablePrinting) System.out.println("doubling CLBs.");
                return false;
            }
        } else {
            if (currentNumCLB == numCLBOfBest - 1) {
                if (enablePrinting) System.out.println("aborting.");
                return true;
            } else {
                bestSolutionWithCurrentNumCLB = null;
                controller.setNumCLB(currentNumCLB + (numCLBOfBest - currentNumCLB) / 2);
                if (enablePrinting) System.out.println("increasing CLBs.");
                return false;
            }
        }
    }

    public static void printStats(RandomizeCrossover<int[]> randomizeCrossover, List<OperatorStatistics> crossoverStats,
                            RandomizeMutation<int[]> randomizeMutation, List<OperatorStatistics> mutationStats) {

        System.out.println(randomizeCrossover.resultsToString(crossoverStats));
        System.out.println(randomizeMutation.resultsToString(mutationStats));
    }

    private static void printNumEvaluations(int numEvaluations) {
        System.out.println("Number of evaluations: " + numEvaluations);
    }

    private void printGlobalOperatorStatistics(RandomizeCrossover<int[]> randomizeCrossover, RandomizeMutation<int[]> randomizeMutation) {
        System.out.println("Global operator statistics:");
        printStats(randomizeCrossover, crossoverOperatorStatistics, randomizeMutation, mutationOperatorStatistics);
    }

    public static RandomizeCrossover<int[]> generateRandomizeCrossover(CLBController controller, boolean useStatistics) {
        List<AbstractBoolCrossover> crossoverList = new ArrayList<>();
        crossoverList.add(new SingleBlockCrossover(controller));
        crossoverList.add(new IntervalBlockCrossover(controller));
        crossoverList.add(new SingleBlockCrossover(controller, false));
        crossoverList.add(new IntervalBlockCrossover(controller, false));
        crossoverList.add(new SubBlockCrossover(controller, true));
        return new RandomizeCrossover<>(crossoverList, useStatistics);
    }

    public static RandomizeMutation<int[]> generateRandomizeMutation(CLBController controller,
                                                                      double mutationChance, boolean useStatistics) {

        List<AbstractBoolMutation> mutationList = new ArrayList<>();
        mutationList.add(new InputFullMutation(controller, mutationChance));
        mutationList.add(new InputSingleMutation(controller, mutationChance));
        mutationList.add(new TableCopyMutation(controller, mutationChance));
        mutationList.add(new TableFullMutation(controller, mutationChance));
        mutationList.add(new TableSingleMutation(controller, mutationChance));
        return new RandomizeMutation<>(mutationList, useStatistics);
    }

    public void setMaxNumFails(int maxNumFails) {
        this.maxNumFails = maxNumFails;
    }

    public void setAlgorithmConfig(ParallelGAConfig algorithmConfig) {
        this.algorithmConfig = algorithmConfig;
    }

    public void setThreadPoolConfig(AnnealedThreadPoolConfig threadPoolConfig) {
        this.threadPoolConfig = threadPoolConfig;
        this.threadPoolConfig.evaluateAfterCrossover(useStatistics);
    }

    public void setMutationChance(double mutationChance) {
        this.mutationChance = mutationChance;
    }

    public List<RunResults> getRunResults() {
        return runResults;
    }

    public List<OperatorStatistics> getCrossoverOperatorStatistics() {
        return crossoverOperatorStatistics;
    }

    public List<OperatorStatistics> getMutationOperatorStatistics() {
        return mutationOperatorStatistics;
    }

    public void setEnablePrinting(boolean enablePrinting) {
        this.enablePrinting = enablePrinting;
    }

    @Override
    public void reset() {
        bestSolutionWithCurrentNumCLB = null;
    }
}
