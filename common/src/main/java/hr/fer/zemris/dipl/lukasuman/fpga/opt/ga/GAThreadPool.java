package hr.fer.zemris.dipl.lukasuman.fpga.opt.ga;

import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.Solution;

import java.util.List;

public interface GAThreadPool<T> {

    void runThreads();
    void setNewPopulation(List<Solution<T>> newPopulation, int currentSize);
    boolean submitPopulation(List<Solution<T>> population);
    void waitForCalculation();
    void shutdown();
    boolean isRunning();
}
