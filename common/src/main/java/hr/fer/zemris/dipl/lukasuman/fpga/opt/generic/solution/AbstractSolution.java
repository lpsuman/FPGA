package hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution;

import java.util.Comparator;

public abstract class AbstractSolution<T> implements Solution<T> {

    public static final Comparator<Solution> COMPARATOR_BY_FITNESS =
            Comparator.comparingDouble(Solution::getFitness);

    private static final double DEFAULT_FITNESS_VALUE = 0.0;

    protected T data;
    protected double fitness;

    public AbstractSolution(T data, double fitness) {
        this.data = data;
        setFitness(fitness);
    }

    public AbstractSolution(T data) {
        this(data, DEFAULT_FITNESS_VALUE);
    }

    @Override
    public T getData() {
        return data;
    }

    @Override
    public double getFitness() {
        return fitness;
    }

    @Override
    public void setFitness(double fitness) {
//        if (Double.isNaN(fitness)) {
//            throw new IllegalArgumentException("Fitness must not be NaN.");
//        }
//
        this.fitness = fitness;
    }

    @Override
    public int compareTo(Solution<T> o) {
        return -Double.compare(fitness, o.getFitness());
    }
}
