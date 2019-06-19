package hr.fer.zemris.dipl.lukasuman.fpga.bool.solver;

import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;

public class BooleanSolverConfig {

    private boolean printOnlyBestSolution;
    private boolean useStatistics;
    private boolean printOnlyGlobalStatistics;
    private boolean solveIndividually;
    private int maxNumFails;
    private double noBestThresholdToStopTrying;
    private double bestExistsThresholdToStopTrying;
    private double maxNumBelowThresholdAttempts;

    public BooleanSolverConfig() {
        printOnlyBestSolution = Constants.DEFAULT_PRINT_ONLY_BEST_SOLUTION;
        useStatistics = Constants.DEFAULT_USE_STATISTICS;
        printOnlyGlobalStatistics = Constants.DEFAULT_PRINT_ONLY_GLOBAL_STATISTICS;
        solveIndividually = Constants.DEFAULT_SOLVE_INDIVIDUALLY;
        maxNumFails = Constants.DEFAULT_MAX_NUM_FAILS;
        noBestThresholdToStopTrying = Constants.DEFAULT_NO_BEST_THRESHOLD_TO_STOP_TRYING;
        bestExistsThresholdToStopTrying = Constants.DEFAULT_BEST_EXISTS_THRESHOLD_TO_STOP_TRYING;
        maxNumBelowThresholdAttempts = Constants.DEFAULT_MAX_NUM_BELOW_THRESHOLD_ATTEMPTS;
    }

    public BooleanSolverConfig printOnlyBestSolution(boolean printOnlyBestSolution) {
        this.printOnlyBestSolution = printOnlyBestSolution;
        return this;
    }

    public BooleanSolverConfig useStatistics(boolean useStatistics) {
        this.useStatistics = useStatistics;
        return this;
    }

    public BooleanSolverConfig printOnlyGlobalStatistics(boolean printOnlyGlobalStatistics) {
        this.printOnlyGlobalStatistics = printOnlyGlobalStatistics;
        return this;
    }

    public BooleanSolverConfig solveIndividually(boolean solveIndividually) {
        this.solveIndividually = solveIndividually;
        return this;
    }

    public BooleanSolverConfig maxNumFails(int maxNumFails) {
        this.maxNumFails = maxNumFails;
        return this;
    }

    public BooleanSolverConfig noBestThresholdToStopTrying(double noBestThresholdToStopTrying) {
        this.noBestThresholdToStopTrying = noBestThresholdToStopTrying;
        return this;
    }

    public BooleanSolverConfig bestExistsThresholdToStopTrying(double bestExistsThresholdToStopTrying) {
        this.bestExistsThresholdToStopTrying = bestExistsThresholdToStopTrying;
        return this;
    }

    public BooleanSolverConfig maxNumBelowThresholdAttempts(double maxNumBelowThresholdAttempts) {
        this.maxNumBelowThresholdAttempts = maxNumBelowThresholdAttempts;
        return this;
    }

    public boolean isPrintOnlyBestSolution() {
        return printOnlyBestSolution;
    }

    public boolean isUseStatistics() {
        return useStatistics;
    }

    public boolean isPrintOnlyGlobalStatistics() {
        return printOnlyGlobalStatistics;
    }

    public boolean isSolveIndividually() {
        return solveIndividually;
    }

    public int getMaxNumFails() {
        return maxNumFails;
    }

    public double getNoBestThresholdToStopTrying() {
        return noBestThresholdToStopTrying;
    }

    public double getBestExistsThresholdToStopTrying() {
        return bestExistsThresholdToStopTrying;
    }

    public double getMaxNumBelowThresholdAttempts() {
        return maxNumBelowThresholdAttempts;
    }
}
