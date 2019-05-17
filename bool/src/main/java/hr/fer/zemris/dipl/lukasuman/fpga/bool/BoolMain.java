package hr.fer.zemris.dipl.lukasuman.fpga.bool;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BoolFunc;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BoolFuncController;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.ga.operators.crossover.IntervalBlockCrossover;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.ga.operators.crossover.SingleBlockCrossover;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.ga.operators.mutation.*;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.*;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.EVOIterationThreadPool;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.GAThreadPool;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.operators.crossover.RandomizeCrossover;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.operators.mutation.RandomizeMutation;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.ParallelGA;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.operators.crossover.Crossover;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.operators.mutation.Mutation;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.evaluator.Evaluator;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class BoolMain {

    public static void main(String[] args) {
//        BoolVecProblem problem = BoolVecProblem.generateRandomProblem(1, 3, 2);

        List<BoolFunc> functions = new ArrayList<>();
        functions.add(BoolFuncController.generateFromMask(0b11101110111100011111111100011111, 5));
        functions.add(BoolFuncController.generateFromMask(0b00000000111100000001000100011110, 5));

        BoolVecProblem problem = new BoolVecProblem(functions, 3);

//        problem.getClbController().setNumCLB(3);
//        doTest(problem, false);

        doTest(problem, true);
    }

    private static void doTest(BoolVecProblem problem, boolean shouldChangeNumCLB) {

        CLBController controller = problem.getClbController();

        List<Crossover<BoolVecSolution>> crossoverList = new ArrayList<>();
        crossoverList.add(new SingleBlockCrossover(controller));
        crossoverList.add(new IntervalBlockCrossover(controller));
//        crossoverList.add(new SingleBlockCrossover(controller, false));
//        crossoverList.add(new IntervalBlockCrossover(controller, false));
        RandomizeCrossover<BoolVecSolution> randomCrossovers = new RandomizeCrossover<>(crossoverList);

        List<Mutation<BoolVecSolution>> mutationList = new ArrayList<>();
        mutationList.add(new InputSingleMutation(controller));
//        mutationList.add(new InputFullMutation(controller));
        mutationList.add(new TableCopyMutation(controller));
        mutationList.add(new TableSingleMutation(controller));
//        mutationList.add(new TableFullMutation(controller));
        RandomizeMutation<BoolVecSolution> randomMutations = new RandomizeMutation<>(mutationList);

        List<BoolVecEvaluator> evaluators = new ArrayList<>();
        Supplier<Evaluator<BoolVecSolution>> evaluatorSupplier = () -> {
            BoolVecEvaluator evaluator = new BoolVecEvaluator(problem);
            evaluators.add(evaluator);
            evaluator.addFitnessListener(randomCrossovers);
            evaluator.addFitnessListener(randomMutations);
            return evaluator;
        };

        BoolVecEvaluator evaluator = new BoolVecEvaluator(problem);
        ParallelGA<BoolVecSolution> algorithm;
        GAThreadPool<BoolVecSolution> threadPool = new EVOIterationThreadPool<>(randomCrossovers, randomMutations, evaluatorSupplier);

        if (Constants.DEFAULT_USE_TIME) {
            algorithm = new ParallelGA<>(problem, evaluator, threadPool);
        } else {
            algorithm = new ParallelGA<>(problem, evaluator, threadPool);
        }

        algorithm.addFitnessListener(randomCrossovers);
        algorithm.addTerminationListener(randomCrossovers);
        algorithm.addFitnessListener(randomMutations);
        algorithm.addTerminationListener(randomMutations);

        BoolVecSolution bestSolution = null;
        BoolVecSolution solution = null;
        int numFailed = 0;

        while (true) {
            System.out.println(String.format("Running GA with %6d CLB (attempt %d/%d)",
                    controller.getNumCLB(), numFailed + 1, Constants.DEFAULT_MAX_NUM_FAILS));

            solution = algorithm.run();

            if (solution == null) {
                return;
            }

            if (bestSolution == null || solution.getFitness() >= bestSolution.getFitness()) {
                bestSolution = solution;
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
            System.out.println(problem.solutionToString(bestSolution));

            int numUnusedBlocks = evaluators.get(0).getUnusedBlocks().cardinality();
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
        System.out.println(problem.solutionToString(bestSolution));
        System.out.println(bestSolution.getFitness());
    }
}
