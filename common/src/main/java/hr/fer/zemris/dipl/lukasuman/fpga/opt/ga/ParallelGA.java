package hr.fer.zemris.dipl.lukasuman.fpga.opt.ga;

import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.algorithm.AbstractAlgorithm;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.AbstractSolution;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.evaluator.Evaluator;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.Solution;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Timer;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ParallelGA<T> extends AbstractAlgorithm<T> {

    private final int populationSize;
    private final int maxGenerations;
    private final int elitismSize;
    private final double threshold;
    private long timeToStop;

    private final Supplier<Solution<T>> candidateSupplier;
    private final Evaluator<T> evaluator;
    private final GAThreadPool<T> threadPool;

    private double maxNonImprovingGenerationsRatio;
    private double minImprovingGenerationsRatio;
    private boolean shouldDoFullRuns;
    private boolean enablePrinting;

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
        
        setDoFullRuns(false);
        enablePrinting = true;
        setMaxNonImprovingGenerationsRatio(Constants.DEFAULT_NON_IMPROVING_GENERATION_STOP_RATIO);
        setMinImprovingGenerationsRatio(Constants.DEFAULT_IMPROVING_GENERATION_CONTINUE_RATIO);
    }

    public ParallelGA(Supplier<Solution<T>> candidateSupplier, Evaluator<T> evaluator, GAThreadPool<T> threadPool,
                      int populationSize, int maxGenerations, int elitismSize, double threshold) {

        this(candidateSupplier, evaluator, threadPool, populationSize, maxGenerations, elitismSize,
                threshold, Constants.DEFAULT_TIME_LIMIT);
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

    public ParallelGA(Supplier<Solution<T>> candidateSupplier, Evaluator<T> evaluator, GAThreadPool<T> threadPool,
                      ParallelGAConfig config) {
        this(candidateSupplier, evaluator, threadPool, config.getPopulationSize(), config.getMaxGenerations(),
                config.getElitismSize(), config.getFitnessThreshold(), config.getTimeToStop());
    }

    @Override
    public Solution<T> run() {
        threadPool.runThreads();

        int lastImprovingGeneration = 0;
        int maxNonImprovingGenerations = (int)(maxGenerations * maxNonImprovingGenerationsRatio);
        int minImprovingGenerations = (int)(maxGenerations * minImprovingGenerationsRatio);
        boolean continuedAfterMaxGenerations = false;

        List<Solution<T>> population = generatePopulation();
        List<Solution<T>> newPopulation = new ArrayList<>(population.size());
        for (Solution<T> solution : population) {
            newPopulation.add(solution.duplicate());
        }
        List<Solution<T>> temp;
        Solution<T> best = population.get(0);

        Timer timer = null;
        if (timeToStop > 0) {
            timer = new Timer(timeToStop);
            timer.start();
        }

        try {
            int i = 0;
            while (true) {
                i++;
                notifyFitnessListeners(best, true);
                notifyGenerationListeners((double) i / maxGenerations);
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
                    if (enablePrinting) System.out.println(String.format("Fitness threshold of %.4f reached with fitness %.4f.",
                            best.getFitness(), threshold));
                    break;
                }

                if (best.getFitness() > prevBestFitness) {
                    lastImprovingGeneration = i;
                }

                Solution<T> worst = population.get(population.size() - 1);

                if ((Constants.ENABLE_PRINT_GEN_STEP && (i % Constants.GENERATION_PRINT_STEP == 0))
                        || (Constants.ENABLE_PRINT_GEN_IF_BEST_IMPROVED && (best.getFitness() > prevBestFitness))
                        || (i == 1) || (i == maxGenerations)) {

                    if (enablePrinting) System.out.printf(Constants.PER_GENERATION_OUTPUT_MSG, i, population.size(),
                            best.getFitness(), worst.getFitness());
                }

                if (!shouldDoFullRuns && timer != null && timer.isTimeLimitReached()) {
                    if (enablePrinting) System.out.println(String.format("Execution time limit of %.3f seconds reached.", timeToStop / 1000.0));
                    break;
                }

                if (!threadPool.isRunning()) {
                    if (best.getFitness() != Constants.FITNESS_SCALE) {
                        System.err.println("Warning best solution is not a solution.");
                    }
                    if (enablePrinting) System.out.println("Threadpool stopping. Max number of generations not reached.");
                    break;
                }

                int numGenerationsSinceLastImprovement = i - lastImprovingGeneration;

                if (!shouldDoFullRuns && numGenerationsSinceLastImprovement >= maxNonImprovingGenerations) {
                    if (enablePrinting) System.out.println(String.format("Past %d generations failed to improve the best solution, stopping.",
                            numGenerationsSinceLastImprovement));
                    break;
                }

                if (i >= maxGenerations) {
                    if (numGenerationsSinceLastImprovement <= minImprovingGenerations) {
                        if (!continuedAfterMaxGenerations) {
                            if (enablePrinting) System.out.println(String.format("Maximum number of generations reached, but the best " +
                                    "solution was improved %d generations ago (below the threshold of %d), continuing.",
                                    numGenerationsSinceLastImprovement, minImprovingGenerations));
                            continuedAfterMaxGenerations = true;
                        }
                    } else {
                        if (continuedAfterMaxGenerations) {
                            if (enablePrinting) System.out.println("Failed to find a solution after continuing.");
                        } else {
                            if (enablePrinting) System.out.println("Maxiumum number of generations reached.");
                        }
                        break;
                    }
                }
            }
        } finally {
            if (threadPool.isRunning()) {
//                if (enablePrinting) System.out.println("Threadpool forced to shutdown.");
                threadPool.shutdown();
            }
            notifyTerminationListeners();
        }
        if (enablePrinting) System.out.println("GA is finished with best fitness: " + best.getFitness());

        return best;
    }

    @Override
    public void stop() {
        threadPool.shutdown();
    }

    private List<Solution<T>> generatePopulation() {
        List<Solution<T>> population = new ArrayList<>(populationSize);

        for (int i = 0; i < populationSize; i++) {
            Solution<T> solution = candidateSupplier.get();
            evaluator.evaluateSolution(solution, false);
            population.add(solution);
        }

        population.sort(AbstractSolution.COMPARATOR_BY_FITNESS.reversed());

        return population;
    }

    public void setTimeToStop(long timeToStop) {
        this.timeToStop = timeToStop;
    }

    public void setDoFullRuns(boolean shouldDoFullRuns) {
        this.shouldDoFullRuns = shouldDoFullRuns;
    }

    public void setEnablePrinting(boolean enablePrinting) {
        this.enablePrinting = enablePrinting;
    }

    public double getMaxNonImprovingGenerationsRatio() {
        return maxNonImprovingGenerationsRatio;
    }

    public void setMaxNonImprovingGenerationsRatio(double maxNonImprovingGenerationsRatio) {
        Utility.checkLimit(Constants.DOUBLE_RATIO_LIMIT, maxNonImprovingGenerationsRatio);
        this.maxNonImprovingGenerationsRatio = maxNonImprovingGenerationsRatio;
    }

    public double getMinImprovingGenerationsRatio() {
        return minImprovingGenerationsRatio;
    }

    public void setMinImprovingGenerationsRatio(double minImprovingGenerationsRatio) {
        Utility.checkLimit(Constants.DOUBLE_RATIO_LIMIT, minImprovingGenerationsRatio);
        this.minImprovingGenerationsRatio = minImprovingGenerationsRatio;
    }
}
