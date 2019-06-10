package hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.operators.crossover;

import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.Solution;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.operator.AbstractRandomizeOperator;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.util.List;

public class RandomizeCrossover<T> extends AbstractRandomizeOperator implements Crossover<T> {

    private List<? extends Crossover<T>> crossovers;

    public RandomizeCrossover(List<? extends Crossover<T>> crossovers, boolean useStatistics) {
        super(Utility.checkNull(crossovers, "list of crossovers"), useStatistics);
        this.crossovers = crossovers;
        this.crossovers.forEach(c -> operatorNames.add(c.toString()));
    }

    @Override
    public void crossover(Solution<T> firstParent, Solution<T> secondParent) {
        int indexOperator = calcRandomOperatorIndex();
        double betterFitness = Math.max(firstParent.getFitness(), secondParent.getFitness());

        if (useStatistics) {
            addWaitingSolution(firstParent, betterFitness, indexOperator);
            addWaitingSolution(secondParent, betterFitness, indexOperator);
        }

        crossovers.get(indexOperator).crossover(firstParent, secondParent);
    }

    @Override
    protected String getResultMessage() {
        return "Crossover operator statistics.";
    }
}
