package hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.algorithm;

import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.ListenerHandler;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.Solution;

public interface Algorithm<T> extends ListenerHandler {

    Solution<T> run();
}
