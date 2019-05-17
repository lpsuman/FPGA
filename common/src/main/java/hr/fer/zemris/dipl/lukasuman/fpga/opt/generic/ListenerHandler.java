package hr.fer.zemris.dipl.lukasuman.fpga.opt.generic;

import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.listener.FitnessListener;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.listener.TerminationListener;

public interface ListenerHandler {

    void addTerminationListener(TerminationListener terminationListener);
    void removeTerminationListener(TerminationListener terminationListener);

    void addFitnessListener(FitnessListener fitnessListener);
    void removeFitnessListener(FitnessListener fitnessListener);
}
