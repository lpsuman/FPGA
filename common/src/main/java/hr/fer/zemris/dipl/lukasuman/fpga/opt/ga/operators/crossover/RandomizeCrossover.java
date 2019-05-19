package hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.operators.crossover;

import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.operator.Operator;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.Solution;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.operator.AbstractRandomizeOperator;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.util.List;

public class RandomizeCrossover<T> extends AbstractRandomizeOperator implements Crossover<T> {

    private List<Crossover<T>> crossovers;

    @SuppressWarnings("unchecked")
    public RandomizeCrossover(List<Crossover<T>> crossovers) {
        super((List<Operator>) (List<?>) Utility.checkNull(crossovers, "list of crossovers"));
        this.crossovers = crossovers;
        this.crossovers.forEach(c -> operatorNames.add(c.getClass().getSimpleName()));
    }

    @Override
    public void crossover(Solution<T> firstParent, Solution<T> secondParent) {
        int indexOperator = calcRandomOperatorIndex();
        crossovers.get(indexOperator).crossover(firstParent, secondParent);
        double betterFitness = Math.max(firstParent.getFitness(), secondParent.getFitness());

        addWaitingSolution(firstParent, betterFitness, indexOperator);
        addWaitingSolution(secondParent, betterFitness, indexOperator);
    }

    @Override
    protected String getResultMessage() {
        return "Crossover operator statistics.";
    }
}
