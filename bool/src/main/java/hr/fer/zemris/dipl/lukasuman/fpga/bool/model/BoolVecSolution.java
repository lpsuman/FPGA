package hr.fer.zemris.dipl.lukasuman.fpga.bool.model;

import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.AbstractSolution;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.Solution;

public class BoolVecSolution extends AbstractSolution<int[]> {

    public BoolVecSolution(int[] data) {
        super(data);
    }

    private BoolVecSolution(int[] data, double fitness) {
        super(data, fitness);
    }

    @Override
    public Solution<int[]> duplicate() {
        return new BoolVecSolution(this.data.clone(), this.fitness);
    }
}
