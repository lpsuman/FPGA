package hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.operators.selection;

import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.Solution;

import java.util.List;

public interface Selection<T extends Solution> {

    T selectFromPopulation(List<T> population);
}
