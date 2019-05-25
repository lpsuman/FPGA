package hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution;

import java.io.Serializable;

public interface Solution<T> extends Comparable<Solution<T>>, Serializable {

    T getData();
    double getFitness();
    void setFitness(double fitness);
    Solution<T> duplicate();
    void copyOver(Solution<T> other);
}
