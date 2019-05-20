package hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.listener;

public interface TerminationListener {

    void terminate();
    void setIgnoreTermination(boolean ignoreTermination);
}
