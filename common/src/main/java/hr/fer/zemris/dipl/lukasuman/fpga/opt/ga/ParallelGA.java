package hr.fer.zemris.dipl.lukasuman.fpga.opt.ga;

import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.algorithm.AbstractAlgorithm;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.AbstractSolution;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.evaluator.Evaluator;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.IntArraySolution;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.Solution;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ParallelGA<T> extends AbstractAlgorithm<T> {

    private final int populationSize;
    private final int maxGenerations;
    private final int elitismSize;
    private final double threshold;
    private final long timeToStop;

    private Supplier<Solution<T>> candidateSupplier;
    private Evaluator<T> evaluator;
    private GAThreadPool<T> threadPool;

    public ParallelGA(Supplier<Solution<T>> candidateSupplier, Evaluator<T> evaluator, GAThreadPool<T> threadPool,
                      int populationSize, int maxGenerations, int elitismSize, double threshold, long timeToStop) {

        this.candidateSupplier = candidateSupplier;
        this.evaluator = evaluator;
        this.threadPool = threadPool;
        this.populationSize = populationSize;
        this.maxGenerations = maxGenerations;
        this.elitismSize = elitismSize;
        this.threshold = threshold;
        this.timeToStop = timeToStop;
    }

    public ParallelGA(Supplier<Solution<T>> candidateSupplier, Evaluator<T> evaluator, GAThreadPool<T> threadPool,
                      int populationSize, int maxGenerations, int elitismSize, double threshold) {

        this(candidateSupplier, evaluator, threadPool, populationSize, maxGenerations, elitismSize,
                threshold, Constants.DEFAULT_TIME);
    }

    public ParallelGA(Supplier<Solution<T>> candidateSupplier, Evaluator<T> evaluator, GAThreadPool<T> threadPool,
                      int populationSize, int maxGenerations) {

        this(candidateSupplier, evaluator, threadPool, populationSize, maxGenerations,
                (int) Math.max(Constants.DEFAULT_MIN_ELITISM_SIZE, Constants.DEFAULT_ELITISM_RATIO * populationSize),
                Constants.DEFAULT_FITNESS_THRESHOLD);
    }

    public ParallelGA(Supplier<Solution<T>> candidateSupplier, Evaluator<T> evaluator, GAThreadPool<T> threadPool) {
        this(candidateSupplier, evaluator, threadPool,
                Constants.DEFAULT_POPULATION_SIZE, Constants.DEFAULT_MAX_NUM_GENERATIONS);
    }

    @Override
    public Solution<T> run() {
        threadPool.runThreads();

        List<Solution<T>> population = generatePopulation();
        List<Solution<T>> newPopulation = new ArrayList<>(population.size());
        for (Solution<T> solution : population) {
            newPopulation.add(solution.duplicate());
        }
        List<Solution<T>> temp;
        Solution<T> best = population.get(0);

        try {
            for (int i = 1; i <= maxGenerations; ++i) {
                notifyFitnessListeners(best, true);
                int copyOverIndex = 0;

                for (int j = 0; j < elitismSize; ++j) {
                    population.get(j).copyOver(newPopulation.get(copyOverIndex));
                    copyOverIndex++;
                }

                threadPool.setNewPopulation(newPopulation, copyOverIndex);
                int remaining = populationSize - copyOverIndex;

                while (remaining > 0) {
                    if (!threadPool.submitPopulation(population)) {
                        break;
                    }

                    remaining -= 2;
                }

                threadPool.waitForCalculation();

                newPopulation.sort(null);
                temp = population;
                population = newPopulation;
                newPopulation = temp;
                double prevBestFitness = best.getFitness();
                best = population.get(0);

                if (best.getFitness() > threshold) {
                    System.out.println(String.format("Fitness threshold of %.4f reached with fitness %.4f.",
                            best.getFitness(), threshold));
                    break;
                }

                Solution<T> worst = population.get(population.size() - 1);

                if ((Constants.ENABLE_PRINT_GEN_STEP && (i % Constants.GENERATION_PRINT_STEP == 0))
                        || (Constants.ENABLE_PRINT_GEN_IF_BEST_IMPROVED && (best.getFitness() > prevBestFitness))
                        || (i == 1) || (i == maxGenerations)) {

                    System.out.printf(Constants.PER_GENERATION_OUTPUT_MSG, i, population.size(), best.getFitness(), worst.getFitness());
                }

                if (System.currentTimeMillis() >= timeToStop) {
                    System.out.println("Execution time limit reached. Max number of generations not reached");
                    break;
                }

                if (!threadPool.isRunning()) {
                    if (best.getFitness() != Constants.FITNESS_SCALE) {
                        System.err.println("Warning best solution is not a solution.");
                    }
                    System.out.println("Threadpool stopping. Max number of generations not reached.");
                    break;
                }
            }
        } finally {
            if (threadPool.isRunning()) {
                System.out.println("Threadpool forced to shutdown.");
                threadPool.shutdown();
            }
            notifyTerminationListeners();
            System.out.println("GA is finished.");
        }

        System.out.println("Best fitness: " + best.getFitness());
        return best;
    }

    private List<Solution<T>> generatePopulation() {
        List<Solution<T>> population = new ArrayList<>(populationSize);

        for (int i = 0; i < populationSize; ++i) {
            Solution<T> solution = candidateSupplier.get();
            evaluator.evaluateSolution(solution, false);
            population.add(solution);
        }

        population.sort(AbstractSolution.COMPARATOR_BY_FITNESS.reversed());

        return population;
    }
}
