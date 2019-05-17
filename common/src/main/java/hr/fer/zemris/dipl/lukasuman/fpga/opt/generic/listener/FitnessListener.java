package hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.listener;

import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.Solution;

public interface FitnessListener {

    void fitnessChanged(Solution solution);
    void updateBestFitness(Solution solution);
}
