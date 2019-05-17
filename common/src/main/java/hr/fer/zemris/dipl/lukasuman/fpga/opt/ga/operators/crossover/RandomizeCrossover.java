package hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.operators.crossover;

import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.operator.Operator;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.Solution;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.operator.AbstractRandomizeOperator;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.util.List;

public class RandomizeCrossover<T extends Solution> extends AbstractRandomizeOperator implements Crossover<T> {

    private List<Crossover<T>> crossovers;

    @SuppressWarnings("unchecked")
    public RandomizeCrossover(List<Crossover<T>> crossovers) {
        super((List<Operator>) (List<?>) Utility.checkNull(crossovers, "list of crossovers"));
        this.crossovers = crossovers;
        this.crossovers.forEach(c -> operatorNames.add(c.getClass().getSimpleName()));
    }

    @Override
    public List<T> crossover(T firstParent, T secondParent) {
        int indexOperator = calcRandomOperatorIndex();
        List<T> children = crossovers.get(indexOperator).crossover(firstParent, secondParent);
        double betterFitness = Math.max(firstParent.getFitness(), secondParent.getFitness());
        addWaitingSolution(children.get(0), betterFitness, indexOperator);
        addWaitingSolution(children.get(1), betterFitness, indexOperator);
        return children;
    }

    @Override
    protected String getResultMessage() {
        return "Crossover operator statistics.";
    }
}
