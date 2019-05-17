package hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.operators.mutation;

import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.operator.Operator;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.Solution;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.operator.AbstractRandomizeOperator;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.util.List;

public class RandomizeMutation<T extends Solution> extends AbstractRandomizeOperator implements Mutation<T> {

    private List<Mutation<T>> mutations;

    @SuppressWarnings("unchecked")
    public RandomizeMutation(List<Mutation<T>> mutations) {
        super((List<Operator>) (List<?>) Utility.checkNull(mutations, "list of mutations"));
        this.mutations = mutations;
        this.mutations.forEach(m -> operatorNames.add(m.getClass().getSimpleName()));
    }

    @Override
    public T mutate(T candidate) {
        int indexOperator = calcRandomOperatorIndex();
        addWaitingSolution(candidate, candidate.getFitness(), indexOperator);
        return mutations.get(indexOperator).mutate(candidate);
    }

    @Override
    protected String getResultMessage() {
        return "Mutation operator statistics.";
    }
}
