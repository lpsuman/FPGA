package hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.operators.mutation;

import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.Solution;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.operator.AbstractRandomizeOperator;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.util.List;

public class RandomizeMutation<T> extends AbstractRandomizeOperator implements Mutation<T> {

    private List<? extends Mutation<T>> mutations;

    public RandomizeMutation(List<? extends Mutation<T>> mutations) {
        super(Utility.checkNull(mutations, "list of mutations"));
        this.mutations = mutations;
        this.mutations.forEach(m -> operatorNames.add(m.getClass().getSimpleName()));
    }

    @Override
    public void mutate(Solution<T> candidate) {
        int indexOperator = calcRandomOperatorIndex();
        addWaitingSolution(candidate, candidate.getFitness(), indexOperator);
        mutations.get(indexOperator).mutate(candidate);
    }

    @Override
    protected String getResultMessage() {
        return "Mutation operator statistics.";
    }
}
