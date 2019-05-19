package hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.evaluator;

import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.AbstractListenerHandler;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.Solution;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Logger;

public abstract class AbstractEvaluator<T> extends AbstractListenerHandler<T> implements Evaluator<T>, Logger {

    protected int numEvaluations;

    protected AbstractEvaluator() {
        numEvaluations = 0;
    }

    @Override
    public int getNumEvaluations() {
        return numEvaluations;
    }

    @Override
    public void resetNumEvaluations() {
        numEvaluations = 0;
    }
}
