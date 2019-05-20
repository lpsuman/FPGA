package hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.algorithm;

import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.listener.AbstractListenerHandler;

public abstract class AbstractAlgorithm<T> extends AbstractListenerHandler<T> implements Algorithm<T> {

    protected AbstractAlgorithm() {
    }
}
