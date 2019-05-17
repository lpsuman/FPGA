package hr.fer.zemris.dipl.lukasuman.fpga.opt.generic;

import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.listener.FitnessListener;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.listener.TerminationListener;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.Solution;

import java.util.HashSet;
import java.util.Set;

public class AbstractListenerHandler<T extends Solution> implements ListenerHandler{

    private Set<TerminationListener> terminationListeners;
    private Set<FitnessListener> fitnessListeners;

    public AbstractListenerHandler() {
    }

    @Override
    public void addTerminationListener(TerminationListener terminationListener) {
        if (terminationListeners == null) {
            terminationListeners = new HashSet<>();
        }
        terminationListeners.add(terminationListener);
    }

    @Override
    public void removeTerminationListener(TerminationListener terminationListener) {
        if (terminationListeners == null || terminationListeners.isEmpty()) {
            return;
        }
        terminationListeners.remove(terminationListener);
    }

    protected void notifyTerminationListeners() {
        if (terminationListeners == null) {
            return;
        }
        terminationListeners.forEach(TerminationListener::terminate);
    }

    @Override
    public void addFitnessListener(FitnessListener fitnessListener) {
        if (fitnessListeners == null) {
            fitnessListeners = new HashSet<>();
        }
        fitnessListeners.add(fitnessListener);
    }

    @Override
    public void removeFitnessListener(FitnessListener fitnessListener) {
        if (fitnessListeners == null || fitnessListeners.isEmpty()) {
            return;
        }
        fitnessListeners.remove(fitnessListener);
    }

    protected void notifyFitnessListeners(T solution, boolean isBest) {
        if (fitnessListeners == null || fitnessListeners.isEmpty()) {
            return;
        }

        if (isBest) {
            fitnessListeners.forEach(l -> l.updateBestFitness(solution));
        } else {
            fitnessListeners.forEach(l -> l.fitnessChanged(solution));
        }
    }
}
