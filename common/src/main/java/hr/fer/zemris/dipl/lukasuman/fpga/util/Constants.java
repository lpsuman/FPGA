package hr.fer.zemris.dipl.lukasuman.fpga.util;

public class Constants {

    private Constants() {
    }

    public static final double FITNESS_SCALE = 100.0;
    public static final double STRUCTURE_FITNESS_SCALE = 0.9999;
    public static final boolean USE_STRUCTURE_FITNESS = false;

    public static final int DEFAULT_MAX_NUM_GENERATIONS = 500;
    public static final int DEFAULT_POPULATION_SIZE = 2000;
    public static final double OPERATOR_CHANCE_MULTIPLIER = 0.0;

    public static final double NUM_CLB_ESTIMATION_MULTIPLIER = 2.0;
    public static final double DEFAULT_ANNEALING_THRESHOLD = 0.8;
    public static final double DEFAULT_IMPROVING_GENERATION_CONTINUE_RATIO = 0.25;
    public static final double DEFAULT_NON_IMPROVING_GENERATION_STOP_RATIO = 0.5;
    public static final int DEFAULT_MAX_NUM_FAILS = 5;
    public static final double DEFAULT_BEST_EXISTS_THRESHOLD_TO_STOP_TRYING = 0.93;
    public static final double DEFAULT_NO_BEST_THRESHOLD_TO_STOP_TRYING = 0.97;
    public static final double DEFAULT_MAX_NUM_BELOW_THRESHOLD_ATTEMPTS = 0.4;
    public static final double SINGLE_CLB_MAX_NON_IMPROVING_MODIFIER = 0.05;

    public static final boolean STOP_AFTER_MERGING = false;

    public static final ArgumentLimit<Integer> NUM_FUNCTION_INPUTS_LIMIT =
            new ArgumentIntervalLimit<>("number of inputs for boolean function", 2, 14);

    public static final ArgumentLimit<Integer> NUM_FUNCTIONS_LIMIT =
            new ArgumentIntervalLimit<>("number of functions", 1, 100);

    public static final ArgumentLimit<Integer> NUM_CLB_INPUTS_LIMIT =
            new ArgumentIntervalLimit<>("number of CLB inputs", 2, NUM_FUNCTION_INPUTS_LIMIT.getUpperLimit());

    public static final ArgumentLimit<Integer> NUM_CLB_LIMIT =
            new ArgumentIntervalLimit<>("number of CLBs", 1, 10000);

    public static final ArgumentLimit<Integer> TOURNAMENT_SIZE_LIMIT =
            new ArgumentIntervalLimit<>("tournament size", 2, 1000);

    public static final ArgumentLimit<Integer> NUM_OPERATORS_LIMIT =
            new ArgumentIntervalLimit<>("number of operators", 1, 10);

    public static final ArgumentLimit<Integer> BITSET_SIZE_LIMIT =
            new ArgumentIntervalLimit<>("size of bitset", 0,
                    (int) Math.pow(2, NUM_FUNCTION_INPUTS_LIMIT.getUpperLimit()));

    public static final ArgumentLimit<Integer> INTEGER_LENGTH_LIMIT =
            new ArgumentIntervalLimit<>("length of integer", 0, 32);

    public static final ArgumentLimit<Double> ANNEALING_THRESHOLD_LIMIT =
            new ArgumentIntervalLimit<>("annealing threshold", 0.0, 1.0);

    public static final ArgumentLimit<Double> DOUBLE_RATIO_LIMIT =
            new ArgumentIntervalLimit<>("ratio limit", 0.0, 1.0);

    public static final ArgumentLimit<Integer> POPULATION_SIZE_LIMIT =
            new ArgumentIntervalLimit<>("population size", 2, 10000);

    public static final ArgumentLimit<Integer> MAX_GENERATIONS_LIMIT =
            new ArgumentIntervalLimit<>("population size", 1, 100000);

    public static final ArgumentLimit<Integer> ELITISM_SIZE_LIMIT =
            new ArgumentIntervalLimit<>("elitism size", 1, POPULATION_SIZE_LIMIT.getUpperLimit());

    public static final ArgumentLimit<Integer> NUM_THREADS_LIMIT =
            new ArgumentIntervalLimit<>("elitism size", 1, 64);

    public static final ArgumentLimit<Integer> NUM_FAILS_LIMIT =
            new ArgumentIntervalLimit<>("number of fails", 1, 100);

    public static final boolean DEFAULT_PRINT_ONLY_BEST_SOLUTION = true;
    public static final boolean DEFAULT_USE_STATISTICS = true;
    public static final boolean DEFAULT_PRINT_ONLY_GLOBAL_STATISTICS = true;
    public static final boolean DEFAULT_SOLVE_INDIVIDUALLY = false;
    public static final boolean DEFAULT_USE_CLB_OUTPUT_CACHING = false;

    public static final int MAXIMUM_INPUT_ID_LENGTH = 10;
    public static final int MAXIMUM_NAME_LENGTH = 30;

    public static final String SOLUTION_PRINT_FORMAT = "%4d   ";

    public static final String PER_GENERATION_OUTPUT_MSG =
            "%6d. generation(%6d) => best: %10.4f, worst: %10.4f%n";
    public static final int GENERATION_PRINT_STEP = 100;
    public static final boolean ENABLE_PRINT_GEN_STEP = false;
    public static final boolean ENABLE_PRINT_GEN_IF_BEST_IMPROVED = true;

    public static final int BOOL_VECTOR_PRINT_CELL_SIZE = 4;
    public static final String BOOL_VECTOR_PRINT_SEPARATOR = "#";

    public static final int DEFAULT_NUM_WORKERS = 3;
    public static final int DEFAULT_MIN_ELITISM_SIZE = 2;
    public static final double DEFAULT_ELITISM_RATIO = 0.002;
    public static final int DEFAULT_TOURNAMENT_SIZE = 3;

    public static final double DEFAULT_FITNESS_THRESHOLD = Double.MAX_VALUE;
    public static final long DEFAULT_TIME_LIMIT = 0;
}
