package hr.fer.zemris.dipl.lukasuman.fpga.opt.ga;

import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.Solution;

import java.util.List;

public interface GAThreadPool<T extends Solution> {

    void runThreads();
    boolean submitPopulation(List<T> population);
    public List<T> takeChildren();
    void shutdown();
    boolean isRunning();
}
