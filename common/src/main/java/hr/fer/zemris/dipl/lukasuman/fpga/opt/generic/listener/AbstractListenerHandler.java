package hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.listener;

import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.Solution;

public class AbstractListenerHandler<T> {

    private SetListener<TerminationListener> terminationListeners;
    private SetListener<FitnessListener> fitnessListeners;
    private SetListener<GenerationListener> generationListeners;

    public AbstractListenerHandler() {
        terminationListeners = new SetListener<>();
        fitnessListeners = new SetListener<>();
        generationListeners = new SetListener<>();
    }

    public void addTerminationListener(TerminationListener terminationListener) {
        terminationListeners.addListener(terminationListener);
    }

    public void removeTerminationListener(TerminationListener terminationListener) {
        terminationListeners.removeListener(terminationListener);
    }

    protected void notifyTerminationListeners() {
        if (terminationListeners.hasListeners()) {
            terminationListeners.getListeners().forEach(TerminationListener::terminate);
        }
    }

    public void addFitnessListener(FitnessListener fitnessListener) {
        fitnessListeners.addListener(fitnessListener);
    }

    public void removeFitnessListener(FitnessListener fitnessListener) {
        fitnessListeners.removeListener(fitnessListener);
    }

    protected void notifyFitnessListeners(Solution<T> solution, boolean isBest) {
        if (fitnessListeners.hasListeners()) {
            if (isBest) {
                fitnessListeners.getListeners().forEach(l -> l.updateBestFitness(solution));
            } else {
                fitnessListeners.getListeners().forEach(l -> l.fitnessChanged(solution));
            }
        }
    }

    public void addGenerationListener(GenerationListener generationListener) {
        generationListeners.addListener(generationListener);
    }

    public void removeGenerationListener(GenerationListener generationListener) {
        generationListeners.removeListener(generationListener);
    }

    protected void notifyGenerationListeners(double generationProgress) {
        if (generationListeners.hasListeners()) {
            generationListeners.getListeners().forEach(l -> l.generationProgress(generationProgress));
        }
    }
}
