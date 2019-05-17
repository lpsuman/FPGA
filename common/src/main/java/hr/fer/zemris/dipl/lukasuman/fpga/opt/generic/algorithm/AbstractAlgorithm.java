package hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.algorithm;

import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.AbstractListenerHandler;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.Solution;

public abstract class AbstractAlgorithm<T extends Solution> extends AbstractListenerHandler<T> implements Algorithm<T> {

    protected AbstractAlgorithm() {
    }
}
