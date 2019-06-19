package hr.fer.zemris.dipl.lukasuman.fpga.opt.ga;

import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

public class ParallelGAConfig {

    private int populationSize;
    private int maxGenerations;
    private int elitismSize;
    private double fitnessThreshold;
    private long timeToStop;
    private double maxNonImprovingGenerationsRatio;
    private double minImprovingGenerationsRatio;

    public ParallelGAConfig() {
        populationSize = Constants.DEFAULT_POPULATION_SIZE;
        maxGenerations = Constants.DEFAULT_MAX_NUM_GENERATIONS;
        elitismSize = Constants.DEFAULT_MIN_ELITISM_SIZE;
        fitnessThreshold = Constants.DEFAULT_FITNESS_THRESHOLD;
        timeToStop = Constants.DEFAULT_TIME_LIMIT;
        maxNonImprovingGenerationsRatio = Constants.DEFAULT_NON_IMPROVING_GENERATION_STOP_RATIO;
        minImprovingGenerationsRatio = Constants.DEFAULT_IMPROVING_GENERATION_CONTINUE_RATIO;
    }

    public ParallelGAConfig populationSize(int populationSize) {
        Utility.checkLimit(Constants.POPULATION_SIZE_LIMIT, populationSize);
        this.populationSize = populationSize;
        return this;
    }

    public ParallelGAConfig maxGenerations(int maxGenerations) {
        Utility.checkLimit(Constants.MAX_GENERATIONS_LIMIT, maxGenerations);
        this.maxGenerations = maxGenerations;
        return this;
    }

    public ParallelGAConfig elitismSize(int elitismSize) {
        Utility.checkLimit(Constants.ELITISM_SIZE_LIMIT, elitismSize);
        this.elitismSize = elitismSize;
        return this;
    }

    public ParallelGAConfig maxNonImprovingGenerationsRatio(double maxNonImprovingGenerationsRatio) {
        this.maxNonImprovingGenerationsRatio = maxNonImprovingGenerationsRatio;
        return this;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public int getMaxGenerations() {
        return maxGenerations;
    }

    public int getElitismSize() {
        return elitismSize;
    }

    public double getFitnessThreshold() {
        return fitnessThreshold;
    }

    public long getTimeToStop() {
        return timeToStop;
    }

    public double getMaxNonImprovingGenerationsRatio() {
        return maxNonImprovingGenerationsRatio;
    }

    public double getMinImprovingGenerationsRatio() {
        return minImprovingGenerationsRatio;
    }
}