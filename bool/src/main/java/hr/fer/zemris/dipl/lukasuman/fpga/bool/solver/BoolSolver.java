package hr.fer.zemris.dipl.lukasuman.fpga.bool.solver;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BoolFunc;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BoolVector;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.ga.operators.crossover.AbstractBoolCrossover;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.ga.operators.crossover.IntervalBlockCrossover;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.ga.operators.crossover.SingleBlockCrossover;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.ga.operators.mutation.*;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.BoolVecEvaluator;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.BoolVecProblem;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.CLBController;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.EVOIterationThreadPool;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.GAThreadPool;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.ParallelGA;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.operators.crossover.RandomizeCrossover;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.operators.mutation.RandomizeMutation;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.evaluator.AbstractEvaluator;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.evaluator.Evaluator;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.operator.OperatorStatistics;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.Solution;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Timer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BoolSolver {

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

    private BoolSolver() {
    }

    public static BoolVectorSolution solve(BoolVecProblem problem) {
        CLBController controller = problem.getClbController();
        int numCLBInputs = controller.getNumCLBInputs();
        List<BoolFunc> functions = problem.getBoolVector().getBoolFunctions();
//        List<Integer> perFuncEstimates = new ArrayList<>(Arrays.asList(3, 3, 6, 6, 6));

        List<RunResults> perFuncResults = new ArrayList<>();

        for (int i = 0; i < functions.size(); ++i) {
            BoolVecProblem singleFuncProblem = new BoolVecProblem(
                    new BoolVector(Collections.singletonList(functions.get(i))), numCLBInputs);
//            singleFuncProblem.getClbController().setNumCLB(perFuncEstimates.get(i));
            perFuncResults.add(doARun(singleFuncProblem, true, true));
        }

        List<OperatorStatistics> crossoverOperatorStatistics = perFuncResults.get(0).randomizeCrossover.getGlobalResults();
        List<OperatorStatistics> mutationOperatorStatistics = perFuncResults.get(0).randomizeMutation.getGlobalResults();
        for (int i = 1; i < functions.size(); ++i) {
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

        System.out.println("Elapsed times for functions.");
        elapsedTimes.forEach(System.out::println);

        if (functions.size() == 1) {
            return perFuncResults.get(0).result;
        }

        int numCLBEstimation = perFuncResults.stream()
                .mapToInt(result -> result.result.getBlockConfiguration().getNumCLB())
                .sum() - 1;

        problem.getClbController().setNumCLB(numCLBEstimation);
        RunResults runResults = doARun(problem, true, false);

        if (runResults.result == null) {
            System.out.println("Couldn't find a better solution than the merge of individual function's solutions.");
            return null;
        }

        return runResults.result;
    }

    private static RunResults doARun(BoolVecProblem problem, boolean canDecreaseNumCLB, boolean canIncreaseNumCLB) {
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

        GAThreadPool<int[]> threadPool = new EVOIterationThreadPool<>(randomCrossovers, randomMutations, evaluatorSupplier);
        ParallelGA<int[]> algorithm = new ParallelGA<>(problem, evaluator, threadPool);
        algorithm.addFitnessListener(randomCrossovers);
        algorithm.addTerminationListener(randomCrossovers);
        algorithm.addFitnessListener(randomMutations);
        algorithm.addTerminationListener(randomMutations);

        Solution<int[]> bestSolution = null;
        Solution<int[]> solution;
        double bestFitness = 0.0;
        int numCLBOfBest = -1;
        int numFailed = 0;
        int numConsecutiveBestFitnessBelowThreshold = 0;
        Timer timer = new Timer();

        while (true) {
            System.out.println(String.format("Running GA with %6d CLB (attempt %d/%d)",
                    controller.getNumCLB(), numFailed + 1, Constants.DEFAULT_MAX_NUM_FAILS));

            solution = algorithm.run();

            if (bestSolution == null) {
                bestFitness = solution.getFitness();
            }

            if (solution.getFitness() != Constants.FITNESS_SCALE) {
                numFailed++;

                if (numFailed >= Constants.DEFAULT_MAX_NUM_FAILS) {
                    System.out.print("Failed to find a solution after maximum number of tries, ");

                    if (checkShouldBreak(bestSolution, bestFitness, controller, canIncreaseNumCLB)) {
                        break;
                    } else {
                        numFailed = 0;
                        numConsecutiveBestFitnessBelowThreshold = 0;
                        continue;
                    }
                }

                if (solution.getFitness() < Constants.DEFAULT_BEST_FITNESS_THRESHOLD_TO_STOP_TRYING) {
                    numConsecutiveBestFitnessBelowThreshold++;

                    if (numConsecutiveBestFitnessBelowThreshold >= Constants.DEFAULT_MAX_NUM_BELOW_THRESHOLD_ATTEMPTS) {
                        System.out.print(String.format("Exceeded maximum number of below threshold attempts. " +
                                "Fitness of the best solution was below %.4f for %d consecutive attempts, ",
                                Constants.DEFAULT_BEST_FITNESS_THRESHOLD_TO_STOP_TRYING,
                                Constants.DEFAULT_MAX_NUM_BELOW_THRESHOLD_ATTEMPTS));

                        if (checkShouldBreak(bestSolution, bestFitness, controller, canIncreaseNumCLB)) {
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

            evaluator.setLogging(true);
            evaluator.evaluateSolution(solution, false);
            System.out.println(evaluator.getLog());
            evaluator.resetLog();
            evaluator.setLogging(false);
            System.out.println(problem.solutionToString(bestSolution, evaluator.getBlockUsage()));

            int numUnusedBlocks = evaluator.getUnusedBlocks().cardinality();
            System.out.println(String.format("There were %d unused blocks.\n", numUnusedBlocks));

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
        }

        int numEvaluations = evaluators.stream()
                .mapToInt(AbstractEvaluator::getNumEvaluations)
                .sum();
        printStats(randomCrossovers, randomCrossovers.getCumulativeResults(),
                randomMutations, randomMutations.getCumulativeResults(), numEvaluations);
        int elapsedTime = timer.getElapsedTime();

        if (bestSolution == null) {
            return new RunResults(null, randomCrossovers, randomMutations, numEvaluations, elapsedTime);
        }

        controller.setNumCLB(numCLBOfBest);
        evaluator.setLogging(true);
        evaluator.evaluateSolution(bestSolution, false);
        int numUnusedBlocksInBest = evaluator.getUnusedBlocks().cardinality();

        if (numUnusedBlocksInBest > 0) {
            bestSolution = problem.trimmedBoolSolution(bestSolution, evaluator.getUnusedBlocks());
            controller.setNumCLB(numCLBOfBest -  numUnusedBlocksInBest);
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

    private static boolean checkShouldBreak(Solution<int[]> bestSolution, double bestFitness,
                                            CLBController controller, boolean canIncreaseNumCLB) {

        if (bestSolution == null) {
            if (canIncreaseNumCLB) {
                int numCLBIncreaseAmount = 1;

                if (bestFitness < Constants.DEFAULT_SKIP_INCREASE_NUM_CLB_FITNESS_THRESHOLD) {
                    numCLBIncreaseAmount = Math.max(1,
                            (int)(controller.getNumCLB() * Constants.DEFAULT_SKIP_INCREASE_NUM_CLB_AMOUNT));
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
}
