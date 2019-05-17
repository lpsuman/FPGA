package hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.evaluator;

import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.listener.FitnessListener;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.listener.TerminationListener;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.Solution;

public interface Evaluator<T extends Solution> {

    double evaluateSolution(T solution, boolean allowTermination);
    int getNumEvaluations();
    void resetNumEvaluations();

    void addTerminationListener(TerminationListener terminationListener);
    void removeTerminationListener(TerminationListener terminationListener);

    void addFitnessListener(FitnessListener fitnessListener);
    void removeFitnessListener(FitnessListener fitnessListener);
}
