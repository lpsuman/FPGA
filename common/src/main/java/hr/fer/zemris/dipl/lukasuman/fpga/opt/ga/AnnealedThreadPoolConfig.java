package hr.fer.zemris.dipl.lukasuman.fpga.opt.ga;

import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

public class AnnealedThreadPoolConfig {

    private int numThreads;
    private double annealingThreshold;
    private boolean evaluateAfterCrossover;

    public AnnealedThreadPoolConfig() {
        numThreads = Constants.DEFAULT_NUM_WORKERS;
        annealingThreshold = Constants.DEFAULT_ANNEALING_THRESHOLD;
        evaluateAfterCrossover = Constants.DEFAULT_USE_STATISTICS;
    }

    public AnnealedThreadPoolConfig numThreads(int numThreads) {
        Utility.checkLimit(Constants.NUM_THREADS_LIMIT, numThreads);
        this.numThreads = numThreads;
        return this;
    }

    public AnnealedThreadPoolConfig annealingThreshold(double annealingThreshold) {
        Utility.checkLimit(Constants.ANNEALING_THRESHOLD_LIMIT, annealingThreshold);
        this.annealingThreshold = annealingThreshold;
        return this;
    }

    public AnnealedThreadPoolConfig evaluateAfterCrossover(boolean evaluateAfterCrossover) {
        this.evaluateAfterCrossover = evaluateAfterCrossover;
        return this;
    }

    public int getNumThreads() {
        return numThreads;
    }

    public double getAnnealingThreshold() {
        return annealingThreshold;
    }

    public boolean isEvaluateAfterCrossover() {
        return evaluateAfterCrossover;
    }
}
