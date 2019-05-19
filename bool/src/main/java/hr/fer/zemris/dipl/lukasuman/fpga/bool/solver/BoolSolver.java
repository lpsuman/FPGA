package hr.fer.zemris.dipl.lukasuman.fpga.bool.solver;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BlockConfiguration;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BoolFunc;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BoolVector;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.ga.operators.crossover.IntervalBlockCrossover;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.ga.operators.crossover.SingleBlockCrossover;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.ga.operators.mutation.InputSingleMutation;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.ga.operators.mutation.TableCopyMutation;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.ga.operators.mutation.TableSingleMutation;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.BoolVecEvaluator;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.BoolVecProblem;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.IntArraySolution;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.CLBController;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.EVOIterationThreadPool;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.GAThreadPool;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.ParallelGA;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.operators.crossover.Crossover;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.operators.crossover.RandomizeCrossover;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.operators.mutation.Mutation;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.operators.mutation.RandomizeMutation;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.evaluator.Evaluator;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class BoolSolver {

    private BoolSolver() {
    }

    public static BoolVectorSolution solve(BoolVecProblem problem) {
        int numCLBInputs = problem.getClbController().getNumCLBInputs();
        List<BoolFunc> functions = problem.getBoolVector().getBoolFunctions();
        List<Integer> perFuncEstimates = new ArrayList<>(Arrays.asList(3, 3, 6, 6, 6));

        List<BlockConfiguration> perFuncSolutions = new ArrayList<>();

        for (int i = 0; i < functions.size(); ++i) {
            BoolVecProblem singleFuncProblem = new BoolVecProblem(
                    new BoolVector(Collections.singletonList(functions.get(i))), numCLBInputs);
            singleFuncProblem.getClbController().setNumCLB(perFuncEstimates.get(i));
            perFuncSolutions.add(doARun(singleFuncProblem, true));
        }

        int numCLBEstimation = perFuncSolutions.stream()
                .mapToInt(BlockConfiguration::getNumCLB)
                .sum() - 1;

        problem.getClbController().setNumCLB(numCLBEstimation);
        BlockConfiguration solution = doARun(problem, true);

        return new BoolVectorSolution(problem.getBoolVector(), solution);
    }

    private static BlockConfiguration doARun(BoolVecProblem problem, boolean shouldChangeNumCLB) {
        CLBController controller = problem.getClbController();

        List<Crossover<IntArraySolution>> crossoverList = new ArrayList<>();
        crossoverList.add(new SingleBlockCrossover(controller));
        crossoverList.add(new IntervalBlockCrossover(controller));
//        crossoverList.add(new SingleBlockCrossover(controller, false));
//        crossoverList.add(new IntervalBlockCrossover(controller, false));
        RandomizeCrossover<IntArraySolution> randomCrossovers = new RandomizeCrossover<>(crossoverList);

        List<Mutation<IntArraySolution>> mutationList = new ArrayList<>();
        mutationList.add(new InputSingleMutation(controller));
//        mutationList.add(new InputFullMutation(controller));
        mutationList.add(new TableCopyMutation(controller));
        mutationList.add(new TableSingleMutation(controller));
//        mutationList.add(new TableFullMutation(controller));
        RandomizeMutation<IntArraySolution> randomMutations = new RandomizeMutation<>(mutationList);

        List<BoolVecEvaluator> evaluators = new ArrayList<>();
        Supplier<Evaluator<IntArraySolution>> evaluatorSupplier = () -> {
            BoolVecEvaluator evaluator = new BoolVecEvaluator(problem);
            evaluators.add(evaluator);
            evaluator.addFitnessListener(randomCrossovers);
            evaluator.addFitnessListener(randomMutations);
            return evaluator;
        };

        BoolVecEvaluator evaluator = new BoolVecEvaluator(problem);
        ParallelGA<IntArraySolution> algorithm;
        GAThreadPool<IntArraySolution> threadPool = new EVOIterationThreadPool<>(randomCrossovers, randomMutations, evaluatorSupplier);

        if (Constants.DEFAULT_USE_TIME) {
            algorithm = new ParallelGA<>(problem, evaluator, threadPool);
        } else {
            algorithm = new ParallelGA<>(problem, evaluator, threadPool);
        }

        algorithm.addFitnessListener(randomCrossovers);
        algorithm.addTerminationListener(randomCrossovers);
        algorithm.addFitnessListener(randomMutations);
        algorithm.addTerminationListener(randomMutations);

        IntArraySolution bestSolution = null;
        IntArraySolution solution = null;
        int numCLBOfBest = -1;
        int numFailed = 0;

        while (true) {
            System.out.println(String.format("Running GA with %6d CLB (attempt %d/%d)",
                    controller.getNumCLB(), numFailed + 1, Constants.DEFAULT_MAX_NUM_FAILS));

            solution = algorithm.run();

            if (solution == null) {
                return null;
            }

            if (bestSolution == null || solution.getFitness() >= bestSolution.getFitness()) {
                bestSolution = solution;
                numCLBOfBest = controller.getNumCLB();
            }

            if (solution.getFitness() != Constants.FITNESS_SCALE) {
                numFailed++;
                if (numFailed >= Constants.DEFAULT_MAX_NUM_FAILS) {
                    System.out.println("Failed to find a solution, aborting");
                    break;
                }
                System.out.println("No solution found, trying again.");
                continue;
            }

            evaluator.setLogging(true);
            evaluator.evaluateSolution(solution, false);
            System.out.println(evaluator.getLog());
            evaluator.resetLog();
            evaluator.setLogging(false);
            System.out.println(problem.solutionToString(bestSolution, evaluator.getBlockUsage()));

            int numUnusedBlocks = evaluator.getUnusedBlocks().cardinality();
            System.out.println(String.format("There were %d unused blocks.", numUnusedBlocks));

            if (!shouldChangeNumCLB) {
                System.out.println("Not allowed to change num of CLB, stopping.");
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

        System.out.println(randomCrossovers.resultsToString(randomCrossovers.getCumulativeResults()));
        System.out.println(randomMutations.resultsToString(randomMutations.getCumulativeResults()));
        System.out.println();

        controller.setNumCLB(numCLBOfBest);
        evaluator.evaluateSolution(bestSolution, false);
        System.out.println(problem.solutionToString(bestSolution, evaluator.getBlockUsage()));
        System.out.println(bestSolution.getFitness());

        return problem.generateBlockConfiguration(bestSolution);
    }
}
