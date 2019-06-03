package hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.algorithm;

import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.Solution;

public interface Algorithm<T> {

    Solution<T> run();
    void stop();
}
