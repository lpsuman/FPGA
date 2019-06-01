package hr.fer.zemris.dipl.lukasuman.fpga.bool.solver;

/**
 * Defines the available methods for solving the problem of implementing a set of boolean functions
 * in CLBs (configurable logic blocks). Methods differ in the trade-off between running time and
 * solution quality.
 */
public enum SolverMode {

    /**
     * Fastest way to implement any boolean function (regardless of its truth table) is building a
     * multiplexer tree. Number of CLBs depends on the number of inputs of both the function and the CLBs.
     * This method also provides the upper bound on the number of required CLBs.
     */
    BRUTE(0),

    /**
     * Faster method which uses Karnaugh maps and relies on the simplicity of the boolean function's truth table.
     * Rarely effective on random functions, but might quickly find a good solution on functions given by expressions.
     */
    FAST(10),

    /**
     * Normal solving mode uses heuristics to find better solutions. There is no guarantee that a better solution
     * exists. Even if a better solution might exist, there is no guarantee how much time it will take to find it
     * (if at all). Thus the algorithm stops after when it "feels" it's not worth trying anymore.
     */
    NORMAL(60),

    /**
     * Complete solving mode does the same thing as normal mode, but it tries much harder in exploring the
     * problem space. Running time is greatly extended and fewer shortcuts are taken.
     */
    FULL(-1);

    private int maxRunningTimeSeconds;

    SolverMode(int maxRunningTimeSeconds) {
        this.maxRunningTimeSeconds = maxRunningTimeSeconds;
    }

    public int getMaxRunningTimeSeconds() {
        return maxRunningTimeSeconds;
    }
}
