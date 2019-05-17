package hr.fer.zemris.dipl.lukasuman.fpga.util;

public class Constants {

    private Constants() {
    }

    public static final double DEFAULT_INPUT_FULL_MUTATION_CHANCE = 0.10;
    public static final double DEFAULT_INPUT_SINGLE_MUTATION_CHANCE = 0.10;

    public static final double DEFAULT_TABLE_FULL_MUTATION_CHANCE = 0.10;
    public static final double DEFAULT_TABLE_SINGLE_MUTATION_CHANCE = 0.10;
    public static final double DEFAULT_TABLE_COPY_MUTATION_CHANCE = 0.10;

    private static final int MAX_NUM_FUNTION_INPUTS = 16;

    public static final ArgumentLimit<Integer> NUM_FUNCTION_INPUTS_LIMIT =
            new ArgumentIntervalLimit<>("number of inputs for boolean function", 1,MAX_NUM_FUNTION_INPUTS);

    public static final ArgumentLimit<Integer> NUM_FUNCTIONS_LIMIT =
            new ArgumentIntervalLimit<>("number of functions", 1, 100);

    public static final ArgumentLimit<Integer> NUM_CLB_INPUTS_LIMIT =
            new ArgumentIntervalLimit<>("number of CLB inputs", 2, 10);

    public static final ArgumentLimit<Integer> NUM_CLB_LIMIT =
            new ArgumentIntervalLimit<>("number of CLBs", 1, 1000);

    public static final ArgumentLimit<Integer> TOURNAMENT_SIZE_LIMIT =
            new ArgumentIntervalLimit<>("tournament size", 2, 10);

    public static final ArgumentLimit<Integer> NUM_OPERATORS_LIMIT =
            new ArgumentIntervalLimit<>("number of operators", 1, 10);

    public static final ArgumentLimit<Integer> BITSET_SIZE_LIMIT =
            new ArgumentIntervalLimit<>("size of bitset", 0, (int) Math.pow(2, MAX_NUM_FUNTION_INPUTS));

    public static final ArgumentLimit<Integer> INTEGER_LENGTH_LIMIT =
            new ArgumentIntervalLimit<>("length of integer", 0, 32);

    public static final String PER_GENERATION_OUTPUT_MSG =
            "%6d. generation(%6d) => best: %10.4f, worst: %10.4f%n";
    public static final int GENERATION_PRINT_STEP = 100;
    public static final boolean ENABLE_PRINT_GEN_STEP = true;
    public static final boolean ENABLE_PRINT_GEN_IF_BEST_IMPROVED = true;

    public static final int BOOL_VECTOR_PRINT_CELL_SIZE = 4;
    public static final String BOOL_VECTOR_PRINT_SEPARATOR = "#";

    public static final double FITNESS_SCALE = 100.0;
    public static final double STRUCTURE_FITNESS_SCALE = 0.9999;

    public static final int DEFAULT_NUM_WORKERS = 4;
    public static final int DEFAULT_MIN_ELITISM_SIZE = 2;
    public static final double DEFAULT_ELITISM_RATIO = 0.002;
    public static final int DEFAULT_TOURNAMENT_SIZE = 3;
    public static final int DEFAULT_MAX_NUM_GENERATIONS = 2000;
    public static final int DEFAULT_POPULATION_SIZE = 2000;

    public static final int DEFAULT_MAX_NUM_FAILS = 10;
    public static final double DEFAULT_FITNESS_THRESHOLD = Double.MAX_VALUE;
    public static final boolean DEFAULT_USE_TIME = true;
    public static final long DEFAULT_TIME = Long.MAX_VALUE;
}
