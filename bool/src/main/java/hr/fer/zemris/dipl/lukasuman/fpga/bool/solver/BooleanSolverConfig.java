package hr.fer.zemris.dipl.lukasuman.fpga.bool.solver;

import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;

public class BooleanSolverConfig {

    private boolean printOnlyBestSolution;
    private boolean useStatistics;
    private boolean printOnlyGlobalStatistics;
    private boolean solveIndividually;

    public BooleanSolverConfig() {
        printOnlyBestSolution = Constants.DEFAULT_PRINT_ONLY_BEST_SOLUTION;
        useStatistics = Constants.DEFAULT_USE_STATISTICS;
        printOnlyGlobalStatistics = Constants.DEFAULT_PRINT_ONLY_GLOBAL_STATISTICS;
        solveIndividually = Constants.DEFAULT_SOLVE_INDIVIDUALLY;
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
}
