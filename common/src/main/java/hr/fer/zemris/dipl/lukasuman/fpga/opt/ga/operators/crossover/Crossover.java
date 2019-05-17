package hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.operators.crossover;

import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.operator.Operator;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.Solution;

import java.util.List;

public interface Crossover<T extends Solution> extends Operator {

    List<T> crossover(T firstParent, T secondParent);
}
