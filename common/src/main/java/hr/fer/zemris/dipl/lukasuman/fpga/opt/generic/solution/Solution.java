package hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution;

public interface Solution<T> {

    T getData();
    double getFitness();
    void setFitness(double fitness);
    int compareTo(Solution<T> o);
    Solution<T> duplicate();
}
