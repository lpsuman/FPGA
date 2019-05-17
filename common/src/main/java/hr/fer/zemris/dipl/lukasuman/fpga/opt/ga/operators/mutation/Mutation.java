package hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.operators.mutation;

import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.operator.Operator;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.Solution;

public interface Mutation<T extends Solution> extends Operator {

    T  mutate(T candidate);
}
