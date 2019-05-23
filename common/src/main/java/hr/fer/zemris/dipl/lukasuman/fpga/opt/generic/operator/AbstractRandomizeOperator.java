package hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.operator;

import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.listener.FitnessListener;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.listener.TerminationListener;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.Solution;
import hr.fer.zemris.dipl.lukasuman.fpga.rng.IRNG;
import hr.fer.zemris.dipl.lukasuman.fpga.rng.RNG;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.util.*;

public abstract class AbstractRandomizeOperator implements OperatorRandomizer, FitnessListener, TerminationListener, Operator {

    private static class WaitingSolutionData {
        private double prevFitness;
        private int indexOperator;

        public WaitingSolutionData(double prevFitness, int indexOperator) {
            this.prevFitness = prevFitness;
            this.indexOperator = indexOperator;
        }
    }

    private List<OperatorStatistics> statsList;
    private List<OperatorStatistics> cumulativeStatsList;
    private List<OperatorStatistics> globalStatsList;

    private Map<Solution, WaitingSolutionData> waitingSolutions;
    private double bestFitness;
    private boolean shouldIgnoreFitnessChanges;

    protected List<String> operatorNames;
    protected List<Double> operatorChances;

    //TODO modification of operator chances
    //TODO operator chances in constructor

    public AbstractRandomizeOperator(int numOperators) {
        Utility.checkLimit(Constants.NUM_OPERATORS_LIMIT, numOperators);

        statsList = new ArrayList<>(numOperators);
        cumulativeStatsList = new ArrayList<>(numOperators);
        globalStatsList = new ArrayList<>(numOperators);
        for (int i = 0; i < numOperators; i++) {
            statsList.add(new AtomicOperatorStatistics());
            cumulativeStatsList.add(new AtomicOperatorStatistics());
            globalStatsList.add(new AtomicOperatorStatistics());
        }

        waitingSolutions = new HashMap<>();
        bestFitness = 0.0;
        operatorNames = new ArrayList<>(numOperators);
        operatorChances = null;
    }

    public AbstractRandomizeOperator(List<? extends Operator> operators) {
        this(Utility.checkNull(operators, "operators").size());

        if (operators.isEmpty()) {
            throw new IllegalArgumentException("List of operators must not be empty.");
        }

        calcOperatorChances(operators);
    }

    private void calcOperatorChances(List<? extends Operator> operators) {
        int numOperators = operators.size();
        operatorChances = new ArrayList<>(numOperators);
        double chanceSum = operators.stream().mapToDouble(Operator::getChance).sum();

        for (Operator operator : operators) {
            this.operatorChances.add(operator.getChance() / chanceSum);
        }
    }

    protected int calcRandomOperatorIndex() {
        IRNG random = RNG.getRNG();

        if (operatorChances == null) {
            return random.nextInt(0, statsList.size());
        } else {
            double chance = random.nextDouble(0.0, 1.0);

            for (int i = 0, n = operatorChances.size(); i < n; i++) {
                chance -= operatorChances.get(i);
                if (chance <= 0.0) {
                    return i;
                }
            }

            throw new IllegalStateException("Operator chances are invalid.");
        }
    }

    protected void addWaitingSolution(Solution solution, double prevFitness, int indexOperator) {
        waitingSolutions.put(solution, new WaitingSolutionData(prevFitness, indexOperator));
    }

    @Override
    public List<OperatorStatistics> getLatestResults() {
        return statsList;
    }

    @Override
    public List<OperatorStatistics> getCumulativeResults() {
        return cumulativeStatsList;
    }

    @Override
    public List<OperatorStatistics> getGlobalResults() {
        return globalStatsList;
    }

    @Override
    public String resultsToString(List<OperatorStatistics> operatorStatistics) {
        StringBuilder sb = new StringBuilder();
        sb.append('\n');
        sb.append(getResultMessage());
        sb.append('\n');
        sb.append("Used   DecreasedFitness   IncreasedFitness   IncreasedBest");
        sb.append('\n');

        for (int i = 0, n = operatorStatistics.size(); i < n; i++) {
            sb.append(operatorNames.get(i));
            sb.append('\n');
            sb.append(operatorStatistics.get(i));
            sb.append('\n');
        }

        return sb.toString();
    }

    protected abstract String getResultMessage();

    @Override
    public void reset() {
        OperatorStatistics.resetStatistics(statsList);
        OperatorStatistics.resetStatistics(cumulativeStatsList);
    }

    @Override
    public void fitnessChanged(Solution solution) {
        WaitingSolutionData data = waitingSolutions.get(solution);
        if (data == null) {
            return;
        }
        waitingSolutions.remove(solution);

        if (!shouldIgnoreFitnessChanges) {
            statsList.get(data.indexOperator).incrementNumUsed(data.prevFitness, solution.getFitness(), bestFitness);
        }
    }

    @Override
    public void updateBestFitness(Solution solution) {
        bestFitness = solution.getFitness();
    }

    @Override
    public void terminate() {
        OperatorStatistics.sumStatistics(cumulativeStatsList, statsList);
        OperatorStatistics.sumStatistics(globalStatsList, statsList);
        OperatorStatistics.resetStatistics(statsList);
    }

    @Override
    public void setIgnoreFitnessChanges(boolean ignoreFitnessChanges) {
        shouldIgnoreFitnessChanges = ignoreFitnessChanges;
    }

    @Override
    public void setIgnoreTermination(boolean ignoreTermination) {
        // do nothing
    }

    private static List<Double> getOperatorChances(List<Operator> operators) {
        List<Double> result = new ArrayList<>(operators.size());
        operators.forEach(m -> result.add(m.getChance()));
        return result;
    }

    @Override
    public double getChance() {
        return 1.0;
    }

    @Override
    public void setChance(double chance) {
        // do  nothing
    }
}
