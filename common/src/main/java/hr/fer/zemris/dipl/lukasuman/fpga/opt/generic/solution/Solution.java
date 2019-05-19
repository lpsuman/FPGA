package hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution;

public interface Solution<T> extends Comparable<Solution<T>> {

    T getData();
    double getFitness();
    void setFitness(double fitness);
    Solution<T> duplicate();
    void copyOver(Solution<T> other);
}
